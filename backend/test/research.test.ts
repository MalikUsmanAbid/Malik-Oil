import {test} from 'node:test';
import assert from 'node:assert/strict';
import {research} from '../src/research';
import {parseFeed,readFeed,allowedSourceUrl,searchQueries} from '../src/web-sources';
const env={ENABLE_RESEARCH:'true',GEMINI_API_KEY:'fixture-secret'};
const title='Pakistan oilseed production target increased';
const feed=(date=new Date().toUTCString())=>`<rss><channel><item><title>${title}</title><link>https://www.dawn.com/news/12345</link><pubDate>${date}</pubDate><description>Pakistan plans higher oilseed production. Production target 100 units. Exact local mandi prices are unavailable.</description></item></channel></rss>`;
function mock(answer:unknown,status=200){return (async(url:any,init:any)=>{
 if(new URL(url).hostname!=='generativelanguage.googleapis.com'){assert.equal(init.headers['x-goog-api-key'],undefined);return new Response(feed())}
 assert.equal(init.headers['x-goog-api-key'],'fixture-secret');assert.match(String(url),/gemini-3\.5-flash-lite:generateContent$/);const body=JSON.parse(init.body);assert.equal(body.tools,undefined);assert.match(body.systemInstruction.parts[0].text,/untrusted data/);assert.equal(body.generationConfig.responseJsonSchema.required[0],'findings');return Response.json(answer,{status});
}) as typeof fetch}
const generated=(findings:unknown[])=>({candidates:[{content:{parts:[{text:JSON.stringify({findings})}]}}]});
test('research rejects missing configuration and private extra fields before network',async()=>{
 await assert.rejects(()=>research({},{}),(e:any)=>e.status===503);await assert.rejects(()=>research({text:'private chat'},env));await assert.rejects(()=>research({question:'password=private'},env));await assert.rejects(()=>research({question:'owner@example.com'},env));
});
test('feed parser rejects stale/future/irrelevant records, decodes CDATA, preserves publication date',()=>{
 assert.equal(parseFeed(feed('Fri, 03 Mar 2017 11:35:07 GMT'),'sarson').length,0);
 assert.equal(parseFeed(feed(new Date(Date.now()+86400000).toUTCString()),'sarson').length,0);
 assert.equal(parseFeed(feed().replaceAll('oilseed','weather'),'sarson').length,0);
 const result=parseFeed(feed().replace(title,'<![CDATA[Pakistan oilseed &amp; canola]]>'),'sarson');assert.equal(result[0].title,'Pakistan oilseed & canola');assert.equal(result[0].kind,'feed-excerpt');assert.ok(result[0].publishedAt);
 assert.throws(()=>parseFeed('<!DOCTYPE rss>'+feed(),'sarson'));
});
test('network allowlist rejects localhost, userinfo and redirect to private addresses',async()=>{
 for(const url of ['http://www.dawn.com/','https://127.0.0.1/','https://user:pass@www.dawn.com/','https://www.dawn.com:444/'])assert.equal(allowedSourceUrl(url),null);
 await assert.rejects(()=>readFeed('https://www.dawn.com/feeds/business',(async()=>new Response('',{status:302,headers:{location:'https://127.0.0.1/secret'}})) as typeof fetch));
 await assert.rejects(()=>readFeed('https://www.dawn.com/feeds/business',(async()=>new Response('x'.repeat(1500001))) as typeof fetch));
});
test('free research uses fetched evidence and no paid tools; credentials never go to feeds',async()=>{
 const result=await research({},env,mock(generated([{sourceId:1,summary:'Oilseed production ka context hai.',evidenceQuote:'Pakistan oilseed production target increased'}])));
 assert.equal(result.mode,'free-web-research');assert.equal(result.sources.length,1);assert.match(result.text,/\[1\]/);assert.match(result.text,/current rate verify nahi/);assert.equal(result.supports.length,1);assert.equal(result.reviewRequired,true);
});
test('fabricated citations, quotes and numeric claims are withheld',async()=>{
 for(const finding of [{sourceId:99,summary:'Claim',evidenceQuote:title},{sourceId:1,summary:'Claim',evidenceQuote:'not present in source text'},{sourceId:1,summary:'Rate is 99999',evidenceQuote:title},{sourceId:1,summary:'نرخ ۹۹۹۹۹',evidenceQuote:title},{sourceId:1,summary:'Production is 1',evidenceQuote:title}]){
  const result=await research({},env,mock(generated([finding])));assert.equal(result.mode,'sources-only');assert.doesNotMatch(result.text,/99999/);
 }
});
test('provider quota failure returns real sources with explicit non-AI fallback',async()=>{
 const result=await research({},env,mock({error:{message:'private diagnostic'}},429));assert.equal(result.mode,'sources-only');assert.match(result.text,/free quota/);assert.doesNotMatch(result.text,/private diagnostic/);assert.equal(result.sources.length,1);
});
test('no matching sources means no generation call and no invented research',async()=>{
 let aiCalls=0;const send=(async(url:any)=>{if(String(url).includes('generativelanguage'))aiCalls++;return new Response('<rss><channel/></rss>')}) as typeof fetch;
 const result=await research({},env,send);assert.equal(result.mode,'sources-only');assert.equal(result.sources.length,0);assert.equal(aiCalls,0);
});

test('question changes discovery queries without allowing search operators',()=>{
 const base={commodity:'sarson',market:'Samundri'};
 assert.equal(searchQueries(base).length,2);
 const queries=searchQueries({...base,question:'export ban site:evil.example?'});
 assert.equal(queries.length,3);
 assert.match(queries[0],/export ban/);
 assert.doesNotMatch(queries[0],/site:|https?:/);
 assert.notDeepEqual(queries,searchQueries({...base,question:'rain harvest damage'}));
});
test('research sends the public question to discovery and reports limited coverage',async()=>{
 const urls:string[]=[];
 const send=(async(url:any,init:any)=>{urls.push(String(url));return mock(generated([{sourceId:1,summary:'Production context.',evidenceQuote:title}]))(url,init)}) as typeof fetch;
 const result=await research({question:'harvest rain damage'},env,send);
 assert.ok(urls.some(url=>new URL(url).searchParams.get('q')?.includes('harvest rain damage')));
 assert.equal(result.searchCoverage,'news-rss-only');
 assert.equal(result.searchQueries.length,3);
});

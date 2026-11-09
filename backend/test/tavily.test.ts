import {test} from 'node:test';
import assert from 'node:assert/strict';
import {searchWeb,publicResultUrl,researchQueries} from '../src/tavily';
import {research} from '../src/research';
const env={TAVILY_API_KEY:'fixture-search-secret',TAVILY_FREE_TIER_CONFIRMED:'true',GEMINI_API_KEY:'fixture-ai-secret',ENABLE_RESEARCH:'true'};
const q={question:'harvest rain damage',commodity:'sarson',market:'Samundri',context:['What is affecting mustard supply?']};
const result={results:[{title:'Mustard supply',url:'https://example.com/agriculture',content:'Mustard harvest faces rain damage in Punjab. Production estimate 100 units.'}]};
test('search requires explicit free-tier confirmation before any call',async()=>{
 let calls=0;await assert.rejects(()=>searchWeb(q,{...env,TAVILY_FREE_TIER_CONFIRMED:'false'},(async()=>{calls++;return Response.json(result)}) as typeof fetch));assert.equal(calls,0);
});
test('one bounded basic search uses public context, no advanced or automatic upsell',async()=>{
 let calls=0;const data=await searchWeb(q,env,(async(url:any,init:any)=>{calls++;assert.equal(url,'https://api.tavily.com/search');assert.equal(init.redirect,'error');assert.equal(init.headers.Authorization,'Bearer fixture-search-secret');assert.equal(init.headers['x-goog-api-key'],undefined);const b=JSON.parse(init.body);assert.equal(b.search_depth,'basic');assert.equal(b.auto_parameters,false);assert.equal(b.include_answer,false);assert.equal(b.include_raw_content,false);assert.match(b.query,/harvest rain damage/);assert.match(b.query,/affecting mustard supply/);return Response.json(result)}) as typeof fetch);
 assert.equal(calls,1);assert.equal(data.sources.length,1);assert.equal(data.sources[0].publishedAt,'');assert.equal(data.sources[0].kind,'search-excerpt');
});
test('quota, malformed and oversized responses fail safely without retries or secret diagnostics',async()=>{
 for(const response of [new Response('secret provider diagnostic',{status:429}),Response.json({results:'bad'}),new Response('x'.repeat(1048577))]){
 let calls=0;const data=await searchWeb(q,env,(async()=>{calls++;return response}) as typeof fetch);assert.equal(calls,1);assert.equal(data.sources.length,0);assert.equal(data.failures.length,1);assert.doesNotMatch(JSON.stringify(data),/secret provider diagnostic|fixture-search-secret/);
 }
});
test('unsafe links discarded; provider URLs never fetched and duplicates removed',async()=>{
 for(const url of ['javascript:alert(1)','https://127.0.0.1/a','https://[::1]/','https://user:pass@example.com/','https://thing.local/','https://example.com:8443/'])assert.equal(publicResultUrl(url),null);
 let calls=0;const data=await searchWeb(q,env,(async()=>{calls++;return Response.json({results:[...result.results,...result.results,{...result.results[0],url:'https://127.0.0.1/'}]})}) as typeof fetch);assert.equal(calls,1);assert.equal(data.sources.length,1);
});
test('Tavily evidence feeds Gemini with separate keys and validated citations',async()=>{
 const urls:string[]=[];const data=await research(q,env,(async(url:any,init:any)=>{urls.push(String(url));if(String(url).includes('api.tavily.com'))return Response.json(result);assert.equal(init.headers['x-goog-api-key'],'fixture-ai-secret');assert.equal(init.headers.Authorization,undefined);return Response.json({candidates:[{content:{parts:[{text:JSON.stringify({findings:[{sourceId:1,summary:'Rain damage affects mustard harvest.',evidenceQuote:'Mustard harvest faces rain damage in Punjab.'}]})}]}}]})}) as typeof fetch);
 assert.equal(urls.length,2);assert.equal(data.searchCoverage,'web-search-excerpts');assert.equal(data.supports.length,1);assert.equal(data.mode,'free-web-research');
});
test('follow-up accepts at most two public questions and rejects secret-like input',async()=>{
 let calls=0;const send=(async()=>{calls++;return Response.json(result)}) as typeof fetch;
 for(const context of [['one','two','three'],['password=secret'],['tvly-abcdefghijklmnopqrstuvwxyz']])await assert.rejects(()=>research({...q,context},env,send));assert.equal(calls,0);
});

test('deeper rate research queries local market, nearby Faisalabad and dated official context',async()=>{
 const queries=researchQueries({...q,question:'sarsoo ka aj ka rate',depth:'deep'});assert.equal(queries.length,3);assert.match(queries[1],/Faisalabad.*AMIS/);assert.match(queries[2],/40 kg/);
 let calls=0;const data=await searchWeb({...q,depth:'deep'},env,(async(url:any,init:any)=>{calls++;const body=JSON.parse(init.body);assert.equal(body.include_raw_content,'text');assert.equal(body.include_published_date,true);assert.equal(body.search_depth,'basic');return Response.json({results:[{...result.results[0],raw_content:'Provider extracted mustard harvest page text with exact evidence and publication context.'}]})}) as typeof fetch);
 assert.equal(calls,3);assert.equal(data.sources.length,1);assert.equal(data.sources[0].id,1);assert.equal(data.sources[0].kind,'page-extract');assert.match(data.sources[0].text,/Provider extracted/);
});
test('deeper search tolerates individual failures without inventing sources',async()=>{
 let calls=0;const data=await searchWeb({...q,depth:'deep'},env,(async()=>{calls++;if(calls===1)return new Response('quota',{status:429});return Response.json(result)}) as typeof fetch);assert.equal(calls,3);assert.equal(data.failures.length,1);assert.equal(data.sources.length,1);
});

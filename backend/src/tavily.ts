import {boundedText} from './limits';
import {plain,type WebSource} from './web-sources';
export type SearchConfig={TAVILY_API_KEY?:string;TAVILY_FREE_TIER_CONFIRMED?:string};
// Result links are display-only: never fetch arbitrary provider-supplied URLs.
export function publicResultUrl(value:unknown):string|null{
 if(typeof value!=='string'||value.length>2048)return null;
 try{const u=new URL(value);const h=u.hostname.toLowerCase();
 if(u.protocol!=='https:'||u.username||u.password||u.port||!h.includes('.')||/[\[\]:]/.test(h)||/^\d+(?:\.\d+){3}$/.test(h)||h==='localhost'||/\.(local|localhost|internal|test|invalid)$/.test(h))return null;
 return u.href;}catch{return null}
}
export function researchQueries(q:{question:string;market:string;commodity:string;context?:string[];depth?:string}){
 const clean=(s:string)=>s.normalize('NFKC').replace(/[^\p{L}\p{N}\s.-]/gu,' ').replace(/\s+/g,' ').trim();
 const words:Record<string,string>={sarson:'mustard seed rapeseed',canola:'canola',til:'sesame',taramira:'taramira',alsi:'linseed flaxseed'};
 const subject=words[q.commodity];if(!subject)throw Error('Unsupported commodity');
 const context=(q.context??[]).slice(-2).map(clean).join(' ').slice(0,120);
 const question=clean(q.question)||'latest market supply production news';
 const today=new Date().toISOString().slice(0,10);
 const rateIntent=/rate|price|qeemat|keemat|nرخ|نرخ|قیمت|bha[ou]v/i.test(question);
 const primary=`${question} ${context} ${subject} ${clean(q.market)} Punjab Pakistan`.slice(0,600);
 const regional=q.market.toLowerCase()==='samundri'?'Faisalabad division Samundri':clean(q.market);
 const extra=rateIntent?[`${subject} mandi wholesale rate ${regional} AMIS Punjab ${today}`,`${subject} ${regional} mandi price dated report 40 kg`]:[`${question.slice(0,240)} ${subject} Punjab Pakistan official report`,`${subject} Pakistan supply production imports latest ${today}`];
 return [...new Set(q.depth==='deep'?[primary,...extra]:[primary])];
}
export async function searchWeb(q:{question:string;market:string;commodity:string;context?:string[];depth?:string},env:SearchConfig,send:typeof fetch=fetch){
 if(!env.TAVILY_API_KEY||env.TAVILY_FREE_TIER_CONFIRMED!=='true')throw Error('Search is not configured');
 const queries=researchQueries(q),checkedAt=new Date().toISOString(),sources:WebSource[]=[],failures:string[]=[];
 // Parallel, bounded basic searches only. No automatic retries or paid upgrade.
 const results=await Promise.allSettled(queries.map(async query=>{
  const response=await send('https://api.tavily.com/search',{method:'POST',headers:{Authorization:'Bearer '+env.TAVILY_API_KEY,'Content-Type':'application/json'},redirect:'error',signal:AbortSignal.timeout(15000),body:JSON.stringify({query,search_depth:'basic',topic:'general',max_results:6,include_answer:false,include_raw_content:q.depth==='deep'?'text':false,include_published_date:true,include_images:false,auto_parameters:false,include_usage:true,country:'pakistan'})});
  if(!response.ok){await response.body?.cancel();throw Error('Search unavailable');}
  const data=JSON.parse(await boundedText(response,1048576));if(!Array.isArray(data.results))throw Error('Invalid search response');return data.results.slice(0,6);
 }));
 for(const result of results){
  if(result.status==='rejected'){failures.push('Tavily query unavailable or quota exhausted; no paid fallback.');continue}
  for(const row of result.value){
   const url=publicResultUrl(row?.url);if(!url||typeof row.title!=='string'||typeof row.content!=='string')continue;
   const extracted=typeof row.raw_content==='string'&&row.raw_content.trim().length>=30;
   const text=plain(extracted?row.raw_content:row.content).slice(0,extracted?8000:3000);if(text.length<30)continue;
   const parsed=typeof row.published_date==='string'?Date.parse(row.published_date):NaN;
   const source:WebSource={id:0,title:plain(row.title).slice(0,300),url,publisher:new URL(url).hostname,publishedAt:Number.isFinite(parsed)&&parsed<=Date.parse(checkedAt)+900000?new Date(parsed).toISOString():'',checkedAt,kind:extracted?'page-extract':'search-excerpt',text};
   const existing=sources.findIndex(s=>s.url===url);if(existing>=0){if(source.kind==='page-extract'&&sources[existing].kind!=='page-extract')sources[existing]=source}else if(sources.length<10)sources.push(source);
  }
 }
 return {sources:sources.map((s,i)=>({...s,id:i+1})),failures,checkedAt,queries};
}

import {boundedText} from './limits';
export type WebSource={id:number;title:string;url:string;publisher:string;publishedAt:string;checkedAt:string;kind:'headline-only'|'feed-excerpt'|'search-excerpt'|'page-extract';text:string};
const hosts=new Set(['news.google.com','www.dawn.com','www.brecorder.com']);
export function allowedSourceUrl(value:string){
 try{const u=new URL(value);return u.protocol==='https:'&&hosts.has(u.hostname)&&!u.username&&!u.password&&(!u.port||u.port==='443')?u:null}catch{return null}
}
export function plain(value:string){
 return value.replace(/<!\[CDATA\[([\s\S]*?)\]\]>/g,'$1').replace(/<(script|style)\b[^>]*>[\s\S]*?<\/\1>/gi,' ').replace(/<[^>]*>/g,' ').replace(/&#(x[0-9a-f]+|\d+);/gi,(_,n)=>{const v=n[0].toLowerCase()==='x'?parseInt(n.slice(1),16):Number(n);return v>0&&v<=0x10ffff?String.fromCodePoint(v):' '}).replace(/&(amp|lt|gt|quot|apos|nbsp);/g,(_,n)=>(({amp:'&',lt:'<',gt:'>',quot:'"',apos:"'",nbsp:' '} as Record<string,string>)[n])).replace(/\s+/g,' ').trim();
}
const terms:Record<string,string[]>= {sarson:['sarson','mustard','rapeseed','oilseed'],canola:['canola','rapeseed','oilseed'],til:['sesame','til','oilseed'],taramira:['taramira','rocket seed','oilseed'],alsi:['linseed','flaxseed','oilseed']};
export function parseFeed(xml:string,commodity:string,checkedAt=new Date().toISOString()):WebSource[]{
 if(/<!DOCTYPE|<!ENTITY/i.test(xml))throw Error('Feed declarations not supported');
 const google=xml.includes('news.google.com');const now=Date.parse(checkedAt);const result:WebSource[]=[];
 for(const match of xml.matchAll(/<item\b[^>]*>([\s\S]*?)<\/item>/gi)){
  const item=match[1];const field=(tag:string)=>item.match(new RegExp('<'+tag+'(?:\\s[^>]*)?>([\\s\\S]*?)<\\/'+tag+'>','i'))?.[1]??'';
  const title=plain(field('title')).slice(0,300),url=allowedSourceUrl(plain(field('link'))),date=Date.parse(plain(field('pubDate')));
  const excerpt=google?'':plain(field('description')).slice(0,1400);
  const text=google?title:(title+' '+excerpt).trim();
  const agricultural=/\b(oilseeds?|crops?|agricultur\w*|harvest|mandi|wholesale|farm\w*|rapeseed|canola|sesame|linseed|flaxseed)\b/i.test(text);
  if(!agricultural)continue;
  if(!url||!Number.isFinite(date)||date<now-30*86400000||date>now+900000||!terms[commodity]?.some(t=>new RegExp('\\b'+t+'\\b','i').test(text)))continue;
  result.push({id:0,title,url:url.href,publisher:google?plain(field('source'))||'Google News indexed publisher':url.hostname,publishedAt:new Date(date).toISOString(),checkedAt,kind:google?'headline-only':'feed-excerpt',text:text.slice(0,1600)});
 }
 return result;
}
export async function readFeed(url:string,send:typeof fetch=fetch):Promise<string>{
 let target=allowedSourceUrl(url);if(!target)throw Error('Unapproved feed');
 const signal=AbortSignal.timeout(10000);
 for(let hop=0;hop<3;hop++){
  const r:Response=await send(target.href,{headers:{'User-Agent':'MalikOilResearch/0.1 (public RSS reader)','Accept':'application/rss+xml,application/xml,text/xml'},redirect:'manual',signal});
  if(r.status>=300&&r.status<400){const location:string|null=r.headers.get('location');await r.body?.cancel();target=location?allowedSourceUrl(new URL(location,target).href):null;if(!target)throw Error('Unapproved redirect');continue}
  if(!r.ok){await r.body?.cancel();throw Error('Feed HTTP '+r.status);}
  return boundedText(r,1500000);
 }
 throw Error('Too many feed redirects');
}
export function searchQueries(q:{commodity:string;market:string;question?:string}){
 const word=terms[q.commodity]?.[0]==='sarson'?'mustard':terms[q.commodity]?.[0];
 if(!word)throw Error('Unsupported commodity');
 // Treat the question as literal search terms, never as search operators or URLs.
 const question=(q.question??'').normalize('NFKC').replace(/[^\p{L}\p{N}\s]/gu,' ').replace(/\s+/g,' ').trim().slice(0,300);
 return [...new Set([...(question?[`${question} ${word} Pakistan when:30d`]:[]),`${q.market} Punjab Pakistan ${word} when:30d`,`Pakistan (${word} OR oilseed) when:30d`])];
}
export async function collectSources(q:{commodity:string;market:string;question?:string},send:typeof fetch=fetch){
 const query=(search:string)=>'https://news.google.com/rss/search?'+new URLSearchParams({q:search,hl:'en-PK',gl:'PK',ceid:'PK:en'});
 const queries=searchQueries(q);
 const feeds=[...queries.map(query),'https://www.dawn.com/feeds/business','https://www.brecorder.com/feeds/latest-news'];
 const checkedAt=new Date().toISOString();const results=await Promise.allSettled(feeds.map(async url=>parseFeed(await readFeed(url,send),q.commodity,checkedAt)));
 const failures=results.flatMap((r,i)=>r.status==='rejected'?[new URL(feeds[i]).hostname+': unavailable']:[]);
 const unique=new Map<string,WebSource>();
 for(const r of results)if(r.status==='fulfilled')for(const source of r.value)if(!unique.has(source.url))unique.set(source.url,source);
 const sources=[...unique.values()].sort((a,b)=>(a.kind==='feed-excerpt'?0:1)-(b.kind==='feed-excerpt'?0:1)||Date.parse(b.publishedAt)-Date.parse(a.publishedAt)).slice(0,8).map((s,i)=>({...s,id:i+1}));
 return {sources,failures,checkedAt,queries};
}

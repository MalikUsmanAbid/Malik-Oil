import { z } from 'zod';
const sourceUrl=z.union([z.literal(''),z.url().max(2048).startsWith('https://').refine(value=>{const u=new URL(value);return !u.username&&!u.password;},'Source URL must not contain credentials')]);
const label = z.string().trim().min(1).max(80).regex(/^[\p{L}\p{N} _.-]+$/u);
export const Quote = z.object({commodity:z.string().regex(/^[a-z0-9-]{1,40}$/),market:label,grade:label,
 source:label,sourceUrl,
 amount:z.number().positive().max(100000000),unit:z.enum(['kg','40kg','100kg']),
 observedAt:z.number().int().positive(),sourceEventId:z.string().trim().min(1).max(160)}).strict();
export type QuoteInput = z.infer<typeof Quote>;
export function normalize(q:QuoteInput) {
 const price=Math.round(q.amount*100/({'kg':1,'40kg':40,'100kg':100}[q.unit]));
 if(price<1) throw new Error('Price rounds below one paisa/kg');
 const stream=JSON.stringify([q.commodity,q.market.toLowerCase(),q.grade.toLowerCase(),q.source.toLowerCase(),'PKR/kg']);
 return {price,stream};
}
export async function hash(text:string){return Array.from(new Uint8Array(await crypto.subtle.digest('SHA-256',new TextEncoder().encode(text)))).map(x=>x.toString(16).padStart(2,'0')).join('');}
export async function fingerprint(q:QuoteInput){
 const {price,stream}=normalize(q);
 return hash(JSON.stringify([stream,q.observedAt,price,q.sourceEventId]));
}
// Only this explicit format is parsed; free text is left for manual review.
// SARSON | Lahore | standard | 8000 | 40kg | 2026-10-18T10:00:00+05:00
export function parseExport(text:string,source:string){
 const quotes:QuoteInput[]=[]; let ignored=0;
 for(const line of text.split(/\r?\n/)){
  const p=line.split('|').map(s=>s.trim());
  if(p.length!==6){if(line.trim()) ignored++;continue;}
  const result=Quote.safeParse({commodity:p[0].toLowerCase(),market:p[1],grade:p[2],amount:Number(p[3].replace(/,/g,'')),unit:p[4],observedAt:Date.parse(p[5])/1000,source,sourceUrl:'',sourceEventId:line.trim()});
  if(result.success) quotes.push(result.data);else ignored++;
 }
 return {quotes,ignored};
}

export const MarginInput=z.object({quantityKg:z.number().positive().max(1e9),buyPkrKg:z.number().nonnegative().max(1e9),sellPkrKg:z.number().nonnegative().max(1e9),transportPkr:z.number().nonnegative().max(1e12),otherCostsPkr:z.number().nonnegative().max(1e12)}).strict();
export function margin(input:z.infer<typeof MarginInput>){
 const purchase=input.quantityKg*input.buyPkrKg, revenue=input.quantityKg*input.sellPkrKg;
 const landed=purchase+input.transportPkr+input.otherCostsPkr;
 return {purchasePkr:purchase,revenuePkr:revenue,landedCostPkr:landed,grossMarginPkr:revenue-landed,breakEvenPkrKg:landed/input.quantityKg,notice:'Scenario calculation from supplied costs. No ledger, tax, credit loss or extraction yield is assumed.'};
}

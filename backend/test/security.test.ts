import {test} from 'node:test';
import assert from 'node:assert/strict';
import {boundedText} from '../src/limits';
import {Quote} from '../src/domain';
import worker from '../src/index';
test('upstream body limits reject declared and streamed oversize responses',async()=>{
 assert.equal(await boundedText(new Response('safe'),4),'safe');
 await assert.rejects(()=>boundedText(new Response('safe',{headers:{'Content-Length':'999'}}),4));
 await assert.rejects(()=>boundedText(new Response('x'.repeat(1025)),1024));
});
test('all business routes fail closed without owner authorization, before database or provider access',async()=>{
 const paths=[['GET','/v1/rates'],['GET','/v1/history?stream=s'],['GET','/v1/audit'],['GET','/v1/alerts'],['GET','/v1/candidates'],['POST','/v1/research'],['POST','/v1/candidates'],['POST','/v1/import/whatsapp'],['POST','/v1/devices'],['POST','/v1/ai/summary'],['DELETE','/v1/devices/id']];
 for(const [method,path] of paths){
  const response=await worker.fetch(new Request('https://api.invalid'+path,{method}),{FIREBASE_PROJECT_ID:'project',OWNER_UID:'owner',AUTH_NOT_BEFORE:'0'} as any);
  assert.equal(response.status,401,path);
 }
 const huge=await worker.fetch(new Request('https://api.invalid/v1/rates',{headers:{Authorization:'Bearer '+'x'.repeat(8193)}}),{FIREBASE_PROJECT_ID:'project',OWNER_UID:'owner',AUTH_NOT_BEFORE:'0'} as any);assert.equal(huge.status,401);
});
test('imported provenance URLs reject embedded credentials and oversized values',()=>{
 const base={commodity:'sarson',market:'Samundri',grade:'standard',source:'manual',sourceUrl:'',amount:200,unit:'kg',observedAt:100,sourceEventId:'test'};
 assert.equal(Quote.safeParse({...base,sourceUrl:'https://user:secret@example.com/'}).success,false);
 assert.equal(Quote.safeParse({...base,sourceUrl:'https://example.com/'+'x'.repeat(2048)}).success,false);
 assert.equal(Quote.safeParse({...base,sourceUrl:'https://example.com/quote'}).success,true);
});

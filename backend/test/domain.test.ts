import {test} from 'node:test';
import assert from 'node:assert/strict';
import {Quote,normalize,parseExport,fingerprint} from '../src/domain';
import worker from '../src/index';
const q={commodity:'sarson',market:'Lahore',grade:'standard',source:'manual',sourceUrl:'',amount:8000,unit:'40kg' as const,observedAt:1792300000,sourceEventId:'q1'};
test('unit conversion and source isolation',()=>{assert.equal(normalize(q).price,20000);assert.equal(normalize({...q,amount:200,unit:'kg'}).price,20000);assert.notEqual(normalize(q).stream,normalize({...q,source:'AMIS'}).stream);});
test('fingerprint stable, content change distinct',async()=>{assert.equal(await fingerprint(q),await fingerprint({...q}));assert.notEqual(await fingerprint(q),await fingerprint({...q,amount:8240}));});
test('reject ambiguous or unsafe amounts',()=>{for(const amount of [0,-1,NaN,Infinity])assert.equal(Quote.safeParse({...q,amount}).success,false);assert.equal(Quote.safeParse({...q,unit:'maund'}).success,false);});
test('export handles explicit rows, refuses to guess ordinary chat',()=>{const p=parseExport('sarson | Lahore | standard | 8,000 | 40kg | 2026-10-18T10:00:00+05:00\nAli: kal rate acha hoga','WhatsApp');assert.equal(p.quotes.length,1);assert.equal(p.ignored,1);});
test('private endpoints fail closed before touching database',async()=>{const env={FIREBASE_PROJECT_ID:'',OWNER_UID:'',AUTH_NOT_BEFORE:'0'} as any;const r=await worker.fetch(new Request('https://example.com/v1/rates'),env);assert.equal(r.status,503);const r2=await worker.fetch(new Request('https://example.com/v1/rates'),{...env,FIREBASE_PROJECT_ID:'test-project',OWNER_UID:'owner'});assert.equal(r2.status,401);});

test('business scenario includes all supplied costs',async()=>{const {margin}=await import('../src/domain');const result=margin({quantityKg:1000,buyPkrKg:200,sellPkrKg:210,transportPkr:2000,otherCostsPkr:500});assert.equal(result.grossMarginPkr,7500);assert.equal(result.breakEvenPkrKg,202.5);});

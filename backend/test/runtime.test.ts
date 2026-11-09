import {test} from 'node:test';
import assert from 'node:assert/strict';
import {readFile} from 'node:fs/promises';
import {Miniflare,convertV4MiniflareOptions} from 'miniflare';
import {generateKeyPair,exportPKCS8} from 'jose';
import {dispatch} from '../src/push';
test('real local D1 triggers, notification leases, retry and invalid-token handling',async()=>{
 const mf=new Miniflare(convertV4MiniflareOptions({modules:true,script:'export default {fetch(){return new Response("ok")}}',d1Databases:['DB'],compatibilityDate:'2026-10-18'}));
 const originalFetch=globalThis.fetch;
 try {
 const db=await mf.getD1Database('DB');
 // D1 exec is line based. Use prepare per statement, preserving trigger body.
 const sql=(await readFile(new URL('../migrations/0001_init.sql',import.meta.url),'utf8')).replace(/^--.*$/gm,'');
 const start=sql.indexOf('CREATE TRIGGER');
 for(const statement of sql.slice(0,start).split(';').map(s=>s.trim()).filter(Boolean))await db.prepare(statement).run();
 await db.prepare(sql.slice(start)).run();
 await db.prepare("INSERT INTO devices VALUES('phone','private-token',1,1000000)").run();
 const add=async(id:string,price:number,time:number)=>db.prepare('INSERT INTO rates VALUES(?,?,?,?,?,?,?,?,?,?,?)').bind(id,'s','sarson','Lahore','standard','manual','',price,time,time,id).run();
 await add('base',10000,1000000);await add('rise',10300,1000001);
 assert.equal((await db.prepare('SELECT COUNT(*) AS n FROM alerts').first())?.n,1);
 const {privateKey}=await generateKeyPair('RS256',{extractable:true});const key=await exportPKCS8(privateKey);
 let fcmCode=200,calls=0;
 globalThis.fetch=(async(input:RequestInfo|URL)=>{
 if(String(input).includes('oauth2.googleapis.com'))return Response.json({access_token:'fake-test-token',expires_in:3600});
 calls++;return fcmCode===200?Response.json({name:'test-message'}):Response.json({error:{details:[{errorCode:fcmCode===404?'UNREGISTERED':'QUOTA_EXCEEDED'}]}},{status:fcmCode});
 }) as typeof fetch;
 const env={DB:db as unknown as D1Database,FIREBASE_PROJECT_ID:'test',FCM_CLIENT_EMAIL:'test@example.invalid',FCM_PRIVATE_KEY:key};
 await dispatch(env);await dispatch(env);assert.equal(calls,1);assert.equal((await db.prepare("SELECT state FROM deliveries WHERE alert_id='rise'").first())?.state,'sent');
 await add('fall',9991,1000002);fcmCode=429;await dispatch(env);
 const retry=await db.prepare("SELECT state,attempts,next_at FROM deliveries WHERE alert_id='fall'").first();assert.equal(retry?.state,'pending');assert.equal(retry?.attempts,1);assert.ok(Number(retry?.next_at)>Date.now()/1000);
 await db.prepare("UPDATE deliveries SET next_at=0 WHERE alert_id='fall'").run();fcmCode=404;await dispatch(env);
 assert.equal((await db.prepare("SELECT state FROM deliveries WHERE alert_id='fall'").first())?.state,'failed');assert.equal((await db.prepare("SELECT active FROM devices WHERE id='phone'").first())?.active,0);
 } finally {globalThis.fetch=originalFetch;await mf.dispose();}
});




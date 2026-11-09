import {test} from 'node:test';
import assert from 'node:assert/strict';
import {generateKeyPair,SignJWT} from 'jose';
import {verifyOwnerToken} from '../src/auth';
const env={FIREBASE_PROJECT_ID:'malik-test',OWNER_UID:'owner',AUTH_NOT_BEFORE:'0'};
test('signed owner accepted; wrong UID, project, expired, future and revoked sessions denied',async()=>{
 const {publicKey,privateKey}=await generateKeyPair('RS256');const now=Math.floor(Date.now()/1000);const keys=async()=>publicKey;
 const make=async(changes:Record<string,unknown>={})=>new SignJWT({sub:'owner',aud:'malik-test',iss:'https://securetoken.google.com/malik-test',exp:now+3600,iat:now,auth_time:now-10,...changes}).setProtectedHeader({alg:'RS256',kid:'test'}).sign(privateKey);
 assert.equal(await verifyOwnerToken(await make(),env,keys),'owner');
 for(const change of [{sub:'intruder'},{aud:'other'},{exp:now-5},{iat:now+500},{auth_time:now+500}])await assert.rejects(verifyOwnerToken(await make(change),env,keys));
 await assert.rejects(verifyOwnerToken(await make(),{...env,AUTH_NOT_BEFORE:String(now)},keys));
});

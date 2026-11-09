import {importPKCS8,SignJWT} from 'jose';
export interface PushEnv {DB:D1Database;FIREBASE_PROJECT_ID:string;FCM_CLIENT_EMAIL?:string;FCM_PRIVATE_KEY?:string}
let cached:{value:string;until:number}|undefined;
async function accessToken(env:PushEnv){
 if(cached&&cached.until>Date.now()) return cached.value;
 const key=await importPKCS8(env.FCM_PRIVATE_KEY!.replace(/\\n/g,'\n'),'RS256');
 const assertion=await new SignJWT({scope:'https://www.googleapis.com/auth/firebase.messaging'})
 .setProtectedHeader({alg:'RS256'}).setIssuer(env.FCM_CLIENT_EMAIL!).setAudience('https://oauth2.googleapis.com/token').setIssuedAt().setExpirationTime('55m').sign(key);
 const r=await fetch('https://oauth2.googleapis.com/token',{method:'POST',body:new URLSearchParams({grant_type:'urn:ietf:params:oauth:grant-type:jwt-bearer',assertion}),signal:AbortSignal.timeout(10000)});
 if(!r.ok) throw new Error('FCM OAuth failed');
 const j=await r.json() as {access_token:string;expires_in:number};cached={value:j.access_token,until:Date.now()+Math.max(0,j.expires_in-120)*1000};return j.access_token;
}
export async function dispatch(env:PushEnv){
 if(!env.FCM_CLIENT_EMAIL||!env.FCM_PRIVATE_KEY||!env.FIREBASE_PROJECT_ID) return {state:'unconfigured'};
 const token=await accessToken(env);const now=Math.floor(Date.now()/1000),lease=crypto.randomUUID();
 const rows=await env.DB.prepare(`UPDATE deliveries SET lease_until=?,lease_id=?,attempts=attempts+1
 WHERE rowid IN (SELECT d.rowid FROM deliveries d JOIN devices v ON v.id=d.device_id WHERE v.active=1 AND d.state='pending' AND d.next_at<=? AND d.lease_until<? AND d.attempts<10 LIMIT 5)
 RETURNING alert_id,device_id,attempts`).bind(now+120,lease,now,now).all<{alert_id:string;device_id:string;attempts:number}>();
 for(const row of rows.results){
 const device=await env.DB.prepare('SELECT token FROM devices WHERE id=? AND active=1').bind(row.device_id).first<{token:string}>();
 if(!device)continue;
 let code=0,retryAfter=0,unregistered=false;
 try{
 const response=await fetch(`https://fcm.googleapis.com/v1/projects/${env.FIREBASE_PROJECT_ID}/messages:send`,{method:'POST',headers:{Authorization:`Bearer ${token}`,'Content-Type':'application/json'},body:JSON.stringify({message:{token:device.token,data:{alert_id:row.alert_id},android:{priority:'high',ttl:'86400s'}}}),signal:AbortSignal.timeout(10000)});
 code=response.status;retryAfter=Number(response.headers.get('Retry-After')||0);
 if(!response.ok){const body=await response.json() as {error?:{details?:{errorCode?:string}[]}};unregistered=body.error?.details?.some(d=>d.errorCode==='UNREGISTERED')||false;}
 }catch{/* Network failure retries after lease-safe backoff. Never log token or payload. */}
 const terminal=unregistered||row.attempts>=10||(code>=400&&code<500&&code!==429&&code!==401&&code!==403);
 const state=code>=200&&code<300?'sent':terminal?'failed':'pending';
 await env.DB.batch([
 env.DB.prepare('UPDATE deliveries SET state=?,next_at=?,lease_until=0,last_code=? WHERE alert_id=? AND device_id=? AND lease_id=?').bind(state,now+Math.max(retryAfter,Math.min(21600,60*2**row.attempts)+Math.floor(Math.random()*30)),code,row.alert_id,row.device_id,lease),
 env.DB.prepare('INSERT INTO audit(actor,action,object_id,created_at) VALUES(?,?,?,?)').bind('system',`push.${state}`,row.alert_id,now),
 ...(unregistered?[env.DB.prepare('UPDATE devices SET active=0 WHERE id=?').bind(row.device_id)]:[])
 ]);
 }
 return {state:'processed',count:rows.results.length};
}

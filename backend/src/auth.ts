import {createRemoteJWKSet,jwtVerify,type JWTVerifyGetKey} from 'jose';
const keys=createRemoteJWKSet(new URL('https://www.googleapis.com/service_accounts/v1/jwk/securetoken@system.gserviceaccount.com'));
export interface AuthEnv {FIREBASE_PROJECT_ID:string;OWNER_UID:string;AUTH_NOT_BEFORE:string}
export async function owner(request:Request,env:AuthEnv){
 if(!env.FIREBASE_PROJECT_ID||!env.OWNER_UID) throw new Response('Authentication is not configured',{status:503});
 const token=request.headers.get('Authorization')?.match(/^Bearer (\S+)$/)?.[1];
 if(token&&token.length>8192)throw new Response('Session token too large',{status:401});
 if(!token) throw new Response('Sign in required',{status:401});
 try{
 return await verifyOwnerToken(token,env);
 }catch{throw new Response('Invalid session or access denied',{status:403});}
}
export async function verifyOwnerToken(token:string,env:AuthEnv,keySet:JWTVerifyGetKey=keys){
 const {payload}=await jwtVerify(token,keySet,{algorithms:['RS256'],issuer:`https://securetoken.google.com/${env.FIREBASE_PROJECT_ID}`,audience:env.FIREBASE_PROJECT_ID,requiredClaims:['exp','iat','sub','auth_time']});
 const now=Math.floor(Date.now()/1000);
 const cutoff=Number(env.AUTH_NOT_BEFORE||0);
 if(!Number.isFinite(cutoff)||cutoff<0)throw new Error('Invalid revocation configuration');
 if(payload.sub!==env.OWNER_UID||typeof payload.iat!=='number'||payload.iat>now||typeof payload.auth_time!=='number'||payload.auth_time>now||payload.auth_time<cutoff) throw new Error('Access denied');
 return payload.sub;
}

"""Import non-secret Firebase Android configuration, without deployment.
python tools/configure_firebase.py PATH_TO_GOOGLE_SERVICES_JSON [--owner-uid UID]
"""
import argparse,json
from pathlib import Path
p=argparse.ArgumentParser();p.add_argument('config');p.add_argument('--owner-uid');a=p.parse_args()
r=Path(__file__).resolve().parents[1]
data=json.loads(Path(a.config).read_text(encoding='utf-8-sig'))
client=next((c for c in data.get('client',[]) if c.get('client_info',{}).get('android_client_info',{}).get('package_name')=='com.malikoil.intelligence'),None)
if not client:raise SystemExit('Select google-services.json registered for com.malikoil.intelligence')
keys=client.get('api_key',[])
if not keys:raise SystemExit('Firebase Android API key missing')
info=data['project_info']
config={'MALIK_FIREBASE_API_KEY':keys[0]['current_key'],'MALIK_FIREBASE_APP_ID':client['client_info']['mobilesdk_app_id'],'MALIK_FIREBASE_PROJECT_ID':info['project_id'],'MALIK_FIREBASE_SENDER_ID':info['project_number']}
# Validate the complete input before replacing any existing local settings.
if any(not isinstance(v,str) or not v.strip() or '\n' in v or '\r' in v for v in config.values()):
 raise SystemExit('All four Firebase Android identifiers must be nonempty single-line strings')
if a.owner_uid is not None and (not a.owner_uid.strip() or len(a.owner_uid)>128):
 raise SystemExit('Invalid owner UID')
backend=r/'backend/wrangler.jsonc'
if a.owner_uid is not None:
 wrangler=json.loads(backend.read_text());wrangler['vars']['FIREBASE_PROJECT_ID']=info['project_id'];wrangler['vars']['OWNER_UID']=a.owner_uid
def esc(v):return str(v).replace('\\','\\\\').replace('\n','\\n').replace('\r','\\r')
local=r/'android/firebase.local.properties'
# Preserve API URL if previously configured.
previous=local.read_text() if local.exists() else ''
api=next((line for line in previous.splitlines() if line.startswith('MALIK_API_URL=')),'MALIK_API_URL=')
local.write_text('# Local Firebase app configuration; excluded from Git and source archive.\n'+api+'\n'+'\n'.join(k+'='+esc(v) for k,v in config.items())+'\n')
if a.owner_uid is not None:
 backend.write_text(json.dumps(wrangler,indent=2)+'\n')
print('Local app configuration saved. No resources created and nothing deployed.')

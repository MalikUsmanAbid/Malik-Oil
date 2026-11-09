"""Isolated self-checks; synthetic fixture values never touch the actual project."""
import json,shutil,subprocess,sys,tempfile,unittest
from pathlib import Path
class ConfigImport(unittest.TestCase):
 def setUp(self):
  self.temp=tempfile.TemporaryDirectory();self.addCleanup(self.temp.cleanup);self.root=Path(self.temp.name)
  (self.root/'tools').mkdir();(self.root/'android').mkdir();(self.root/'backend').mkdir()
  self.script=self.root/'tools/configure_firebase.py';shutil.copyfile(Path(__file__).with_name('configure_firebase.py'),self.script)
  self.local=self.root/'android/firebase.local.properties';self.local.write_text('MALIK_API_URL=https://example.invalid\n')
  self.backend=self.root/'backend/wrangler.jsonc';self.backend.write_text(json.dumps({'vars':{'OWNER_UID':'before','FIREBASE_PROJECT_ID':'before'}}))
  self.data={'project_info':{'project_id':'fixture-project','project_number':'123'},'client':[{'client_info':{'android_client_info':{'package_name':'com.malikoil.intelligence'},'mobilesdk_app_id':'1:123:android:fixture'},'api_key':[{'current_key':'fixture-only-key'}]}]}
 def run_import(self,uid='fixture-owner'):
  config=self.root/'fixture.json';config.write_text(json.dumps(self.data))
  return subprocess.run([sys.executable,str(self.script),str(config),'--owner-uid',uid],capture_output=True,text=True)
 def unchanged(self,before):self.assertEqual((self.local.read_bytes(),self.backend.read_bytes()),before)
 def test_success_preserves_api_url(self):
  result=self.run_import();self.assertEqual(result.returncode,0,result.stderr);self.assertIn('MALIK_API_URL=https://example.invalid',self.local.read_text());self.assertEqual(json.loads(self.backend.read_text())['vars']['OWNER_UID'],'fixture-owner')
 def test_wrong_app_leaves_files_unchanged(self):
  self.data['client'][0]['client_info']['android_client_info']['package_name']='other.package';before=(self.local.read_bytes(),self.backend.read_bytes());self.assertNotEqual(self.run_import().returncode,0);self.unchanged(before)
 def test_invalid_uid_leaves_files_unchanged(self):
  before=(self.local.read_bytes(),self.backend.read_bytes());self.assertNotEqual(self.run_import('x'*129).returncode,0);self.unchanged(before)
 def test_missing_identifier_leaves_files_unchanged(self):
  self.data['project_info']['project_number']='';before=(self.local.read_bytes(),self.backend.read_bytes());self.assertNotEqual(self.run_import().returncode,0);self.unchanged(before)
if __name__=='__main__':unittest.main()

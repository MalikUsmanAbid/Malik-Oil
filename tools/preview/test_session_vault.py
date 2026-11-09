import json, os, tempfile, unittest, uuid
from pathlib import Path
from session_vault import SessionVault, Conflict, validate
class VaultTests(unittest.TestCase):
 def session(self):return {'id':str(uuid.uuid4()),'title':'Private owner question','scope':'Samundri|sarson','turns':[{'question':'Public mustard question','text':'Cited answer','sources':[]}]}
 def test_bounds(self):
  with self.assertRaises(ValueError):validate([self.session() for _ in range(9)])
  item=self.session();item['turns']*=13
  with self.assertRaises(ValueError):validate([item])
  item=self.session()
  with self.assertRaises(ValueError):validate([item,item])
 @unittest.skipUnless(os.name=='nt','Windows DPAPI only')
 def test_encrypted_restart_conflict_and_tamper(self):
  with tempfile.TemporaryDirectory() as temp:
   path=Path(temp)/'sessions.bin';vault=SessionVault(path);item=self.session()
   saved=vault.save([item],'empty');self.assertNotIn(b'Private owner question',path.read_bytes());self.assertNotIn(b'Public mustard question',path.read_bytes())
   self.assertEqual(SessionVault(path).read()['sessions'],[item])
   before=path.read_bytes()
   with self.assertRaises(Conflict):vault.save([],'empty')
   self.assertEqual(path.read_bytes(),before)
   changed=bytearray(before);changed[-8]^=1;path.write_bytes(changed)
   with self.assertRaises(ValueError):vault.read()
   with self.assertRaises(ValueError):vault.save([],saved['revision'])
   self.assertEqual(path.read_bytes(),changed)
 @unittest.skipUnless(os.name=='nt','Windows DPAPI only')
 def test_delete_persists(self):
  with tempfile.TemporaryDirectory() as temp:
   vault=SessionVault(Path(temp)/'sessions.bin');revision=vault.save([self.session()],'empty')['revision'];vault.save([],revision);self.assertEqual(vault.read()['sessions'],[])
if __name__=='__main__':unittest.main()

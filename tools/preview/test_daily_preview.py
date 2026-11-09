import io,json,tempfile,unittest,zipfile
from pathlib import Path
from datetime import datetime,timedelta
from xml.etree import ElementTree as E
from daily_preview import DailyPreview,make_report,workbook,today,PK
from session_vault import Conflict
class DailyTests(unittest.TestCase):
 def setUp(self):
  self.temp=tempfile.TemporaryDirectory();self.addCleanup(self.temp.cleanup);self.v=DailyPreview(Path(self.temp.name)/'report.bin')
 def act(self,action,**kwargs):return self.v.action(dict(action=action,revision=self.v.read()['revision'],**kwargs))
 def rows(self,source='TEST SYNTHETIC'):
  now=datetime.now(PK)-timedelta(minutes=1)
  text='\n'.join('sarson | Samundri | standard | '+str(value)+' | 40kg | '+(now-timedelta(days=days)).isoformat() for days,value in [(1,8000),(0,8240)])
  return self.act('approve',reviewed=True,source=source,text=text)
 def test_encryption_and_stale_revision(self):
  self.rows();self.assertNotIn(b'Samundri',self.v.path.read_bytes());before=self.v.path.read_bytes()
  with self.assertRaises(Conflict):self.v.action(dict(action='generate',revision='empty'))
  self.assertEqual(before,self.v.path.read_bytes())
 def test_group_exact_title_disabled_and_duplicate(self):
  with self.assertRaises(ValueError):self.act('capture',group='TEST',text='sarson')
  self.act('configure',group='TEST',enabled=True,schedule=True,priority='Samundri, Faisalabad')
  with self.assertRaises(ValueError):self.act('capture',group='OTHER',text='sarson')
  self.act('capture',group='TEST',text='sarson Samundri TEST');s=self.act('capture',group='TEST',text='sarson Samundri TEST');self.assertEqual(1,len(s['messages']))
 def test_invalid_rows_are_atomic(self):
  s=self.rows();before=self.v.path.read_bytes()
  with self.assertRaises(ValueError):self.act('approve',reviewed=True,source='TEST',text='sarson | X | standard | NaN | 40kg | 2026-11-07T00:00:00Z')
  self.assertEqual(before,self.v.path.read_bytes())
 def test_matching_comparison_and_missing_day(self):
  s=self.rows();r=make_report(s,'roman','sarson');self.assertEqual('3.00',r['rates'][0]['change']);self.assertIn('8240.00',r['summary'])
  s['rates']=s['rates'][:1];self.assertEqual([],make_report(s,'en','sarson')['rates'])
 def test_workbook_safe_strings_chart_and_values(self):
  s=self.rows('=HYPERLINK("https://invalid.test")');r=make_report(s,'en','sarson');b=workbook(r)
  with zipfile.ZipFile(io.BytesIO(b)) as z:
   for name in z.namelist():E.fromstring(z.read(name))
   self.assertIn('xl/charts/chart1.xml',z.namelist());self.assertNotIn('<f>=HYPERLINK',z.read('xl/worksheets/sheet2.xml').decode());self.assertIn(b'8240.00',z.read('xl/worksheets/sheet2.xml'));self.assertFalse(any('externalLink' in n or 'vba' in n for n in z.namelist()))
 def test_empty_report_no_invented_chart(self):
  r=make_report(self.v.read(),'ur','sarson')
  with zipfile.ZipFile(io.BytesIO(workbook(r))) as z:self.assertNotIn('xl/charts/chart1.xml',z.namelist())
 def test_regeneration_replaces_snapshot(self):
  self.rows();self.act('generate',language='en',filter='sarson');s=self.act('generate',language='roman',filter='all');self.assertEqual(1,len(s['reports']));self.assertEqual('roman',s['reports'][0]['language'])
if __name__=='__main__':unittest.main()

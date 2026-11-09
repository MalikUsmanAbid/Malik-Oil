import sqlite3, unittest
from pathlib import Path
class Alerts(unittest.TestCase):
 def setUp(self):
  self.db=sqlite3.connect(':memory:'); self.db.executescript((Path(__file__).parents[1]/'migrations/0001_init.sql').read_text()); self.n=0
  self.db.execute("INSERT INTO devices VALUES('phone','token',1,1000000)")
 def rate(self,p,t=1000000,stream='s',received=None,event=None):
  self.n+=1; key=event or str(self.n)
  self.db.execute('INSERT INTO rates VALUES(?,?,?,?,?,?,?,?,?,?,?) ON CONFLICT(fingerprint) DO NOTHING',(key,stream,'sarson','Lahore','standard','manual','',p,t,received or t,key))
 def count(self):return self.db.execute('SELECT COUNT(*) FROM alerts').fetchone()[0]
 def test_threshold_both_directions_and_anchor(self):
  self.rate(10000);self.rate(10299,1000001);self.assertEqual(self.count(),0)
  self.rate(10300,1000002);self.assertEqual(self.count(),1)
  self.rate(10300,1000003);self.assertEqual(self.count(),1)
  self.rate(9991,1000004);self.assertEqual(self.count(),2)
  self.assertEqual(self.db.execute('SELECT COUNT(*) FROM deliveries').fetchone()[0],2)
 def test_duplicate_old_and_source_isolation(self):
  self.rate(10000,event='a');self.rate(20000,event='a');self.rate(20000,999999);self.rate(20000,1000001,stream='other');self.assertEqual(self.count(),0)
 def test_stale_and_gap(self):
  self.rate(10000);self.rate(15000,1000001,received=1200000);self.assertEqual(self.count(),0)
  self.rate(16000,1200000);self.assertEqual(self.count(),0)
  self.rate(16480,1200001);self.assertEqual(self.count(),1)
 def test_stale_first_never_seeds_baseline(self):
  self.rate(10000,1000000,received=1086401);self.rate(20000,1000002,received=1086401);self.assertEqual(self.count(),0)
 def test_equal_time_ignored(self):
  self.rate(10000);self.rate(20000);self.assertEqual(self.count(),0)
 def test_atomic_rollback(self):
  self.rate(10000);self.db.commit();self.rate(10300,1000001);self.db.rollback();self.assertEqual(self.count(),0)
 def test_lease_prevents_second_claim(self):
  self.rate(10000);self.rate(10300,1000001)
  sql="UPDATE deliveries SET lease_until=2000000,lease_id=? WHERE state='pending' AND lease_until<1000002 RETURNING alert_id"
  self.assertEqual(len(self.db.execute(sql,('one',)).fetchall()),1);self.assertEqual(len(self.db.execute(sql,('two',)).fetchall()),0)
if __name__=='__main__':unittest.main()

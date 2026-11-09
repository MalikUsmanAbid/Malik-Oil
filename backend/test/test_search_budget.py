import sqlite3, unittest
from pathlib import Path
class SearchBudget(unittest.TestCase):
 def test_monthly_cap_and_rollover(self):
  db=sqlite3.connect(':memory:');db.executescript((Path(__file__).parents[1]/'migrations/0003_search_budget.sql').read_text())
  sql='INSERT INTO search_usage VALUES(?,?) ON CONFLICT(month) DO UPDATE SET credits=credits+excluded.credits WHERE credits+excluded.credits<=900 RETURNING credits'
  for i in range(300): self.assertEqual(db.execute(sql,('2026-11',3)).fetchone()[0],(i+1)*3)
  self.assertIsNone(db.execute(sql,('2026-11',1)).fetchone())
  self.assertIsNone(db.execute(sql,('2026-11',3)).fetchone())
  self.assertEqual(db.execute(sql,('2026-12',1)).fetchone()[0],1)
 def test_schema_rejects_invalid_credit_total(self):
  db=sqlite3.connect(':memory:');db.executescript((Path(__file__).parents[1]/'migrations/0003_search_budget.sql').read_text())
  for value in [-1,901]:
   with self.assertRaises(sqlite3.IntegrityError):db.execute('INSERT INTO search_usage VALUES(?,?)',('bad',value))
if __name__=='__main__':unittest.main()

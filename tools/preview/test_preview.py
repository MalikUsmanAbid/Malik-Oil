import unittest
from server import connect, candidate, approve, margin, state


class Preview(unittest.TestCase):
    def setUp(self):
        self.db = connect(':memory:')
        self.addCleanup(self.db.close)

    def quote(self, amount, market='Lahore'):
        return candidate(self.db, dict(commodity='sarson', market=market, unit='kg', amount=amount))['id']

    def test_research_usage_and_remaining(self):
        import time
        today=time.strftime('%Y-%m-%d',time.gmtime())
        self.db.execute('INSERT INTO research_usage VALUES(?,?)',(today,7))
        usage=state(self.db)['researchUsage']
        self.assertEqual(usage['used'],7)
        self.assertEqual(usage['remaining'],13)
        self.assertGreater(usage['resetAt'],int(time.time())*1000)

    def test_three_percent_both_directions_and_duplicate(self):
        first = self.quote(206)
        approve(self.db, first)
        self.assertEqual(len(state(self.db)['alerts']), 1)
        approve(self.db, first)
        self.assertEqual(len(state(self.db)['alerts']), 1)
        approve(self.db, self.quote(199.82))
        self.assertEqual(len(state(self.db)['alerts']), 2)

    def test_below_threshold_then_cumulative_crossing(self):
        approve(self.db, self.quote(205.98))
        self.assertEqual(len(state(self.db)['alerts']), 0)
        approve(self.db, self.quote(206))
        self.assertEqual(len(state(self.db)['alerts']), 1)

    def test_market_case_uses_same_stream(self):
        approve(self.db, self.quote(206, market='lahore'))
        self.assertEqual(len(state(self.db)['alerts']), 1)

    def test_invalid_amounts_rejected(self):
        for value in [0, -1, 'NaN', 'Infinity']:
            with self.assertRaises(ValueError):
                self.quote(value)

    def test_research_daily_budget_persists_and_denies_21st(self):
        from pathlib import Path
        migration = Path(__file__).resolve().parents[2] / 'backend/migrations/0002_research_budget.sql'
        self.db.executescript(migration.read_text(encoding='utf-8'))
        sql = "INSERT INTO research_usage VALUES(?,1) ON CONFLICT(day) DO UPDATE SET calls=calls+1 WHERE calls<20 RETURNING calls"
        for count in range(1,21):
            self.assertEqual(self.db.execute(sql, ('2026-11-06',)).fetchone()[0], count)
        self.assertIsNone(self.db.execute(sql, ('2026-11-06',)).fetchone())
        self.assertEqual(self.db.execute(sql, ('2026-11-07',)).fetchone()[0], 1)

    def test_landed_cost_and_margin(self):
        result = margin(dict(quantityKg=1000, buyPkrKg=200, sellPkrKg=210,
                             transportPkr=2000, otherCostsPkr=500))
        self.assertEqual(result['grossMarginPkr'], 7500)
        self.assertEqual(result['breakEvenPkrKg'], 202.5)


if __name__ == '__main__':
    unittest.main()

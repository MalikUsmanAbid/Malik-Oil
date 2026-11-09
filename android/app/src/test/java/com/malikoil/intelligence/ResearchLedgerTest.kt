package com.malikoil.intelligence
import org.junit.Assert.*
import org.junit.Test
class ResearchLedgerTest{
 @Test fun historyIsBoundedAndSearchable(){val ledger=ResearchLedger();val one=ledger.create();ledger.rename(one.id,"Samundri mustard");ledger.append(one.id,"Historical evidence");assertEquals(one,ledger.search("MUSTARD").single());assertEquals(one,ledger.search("evidence").single());repeat(7){ledger.create()};assertThrows(IllegalArgumentException::class.java){ledger.create()};ledger.delete(one.id);assertEquals(7,ledger.sessions.size)}
 @Test fun answerLimitsPreserveExistingHistory(){val ledger=ResearchLedger();val one=ledger.create();repeat(12){ledger.append(one.id,"answer")};assertThrows(IllegalArgumentException::class.java){ledger.append(one.id,"extra")};assertEquals(12,one.turns.size);assertThrows(IllegalArgumentException::class.java){ledger.rename(one.id," ")}}
 @Test fun rehydrationPreservesConversation(){val original=ResearchThread(title="Saved",turns=mutableListOf("{answer}"));val loaded=ResearchLedger(mutableListOf(original.copy(turns=original.turns.toMutableList())));assertEquals(original.id,loaded.sessions.single().id);assertEquals("{answer}",loaded.sessions.single().turns.single())}
}

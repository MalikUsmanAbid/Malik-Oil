package com.malikoil.intelligence
import org.junit.Assert.*
import org.junit.Test
class MandiMessageIndexTest{
 @Test fun sarsonTestingDoesNotConfuseOilOrVegetables(){assertTrue(MandiMessageIndex.sarsonMention("FSD sarson 8000"));assertTrue(MandiMessageIndex.sarsonMention("سرسوں 8000"));assertFalse(MandiMessageIndex.sarsonMention("mustard oil 400"));assertFalse(MandiMessageIndex.sarsonMention("ساگ سرسوں"));assertFalse(MandiMessageIndex.sarsonMention("wheat 4000"));assertTrue(MandiMessageIndex.sarsonMention("sarson 8000\nMustard oil 400"))}
 @Test fun cityAliasesAndBoundaries(){assertTrue("Faisalabad" in MandiMessageIndex.cities("FSD سرسوں"));assertTrue("Samundri" in MandiMessageIndex.cities("سمندری نرخ"));assertFalse("Lahore" in MandiMessageIndex.cities("Lahoreville"))}
 @Test fun customPriorityDoesNotDropUnknownTowns(){val priorities=MandiMessageIndex.priorities("FSD, New Mandi");assertEquals(listOf("Faisalabad","New Mandi"),priorities);assertEquals(0,MandiMessageIndex.rank(MandiMessageIndex.cities("FSD rates"),priorities));assertTrue("New Mandi" in MandiMessageIndex.cities("New Mandi sarson",priorities));assertEquals(Int.MAX_VALUE,MandiMessageIndex.rank(listOf("Lahore"),priorities))}
 @Test fun discoveryExpiresAndRejectsPrivateOrOtherApps(){assertEquals("Ramzan Oil",CapturePolicy.discoverTitle("com.whatsapp.w4b","Ramzan Oil",true,false,2000,1000));assertNull(CapturePolicy.discoverTitle("com.whatsapp.w4b","Ramzan Oil",false,false,2000,1000));assertNull(CapturePolicy.discoverTitle("com.whatsapp.w4b","Ramzan Oil",true,false,1000,1000));assertNull(CapturePolicy.discoverTitle("com.other","Ramzan Oil",true,false,2000,1000))}
}

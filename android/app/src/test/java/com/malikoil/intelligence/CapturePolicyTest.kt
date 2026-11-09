package com.malikoil.intelligence
import org.junit.Assert.*
import org.junit.Test
class CapturePolicyTest{
 private fun item(pkg:String="com.whatsapp.w4b",target:String="Samundri Oil Rates",title:String=target,group:Boolean=true,summary:Boolean=false,text:String="sarson 8000 per 40kg",at:Long=2000,started:Long=1000,now:Long=3000)=CapturePolicy.accept(pkg,target,title,group,summary,text,at,started,now)
 @Test fun exactSelectedBusinessGroupOnly(){assertNotNull(item());assertNull(item(pkg="com.whatsapp"));assertNull(item(pkg="com.other.app"));assertNull(item(title="Private contact"));assertNull(item(title="samundri oil rates"))}
 @Test fun privateMessagesAndSummariesNeverCollected(){assertNull(item(group=false));assertNull(item(summary=true));assertNull(item(target=""));assertNull(item(target="x".repeat(101)))}
 @Test fun invalidOrOversizeMessagesRejected(){assertNull(item(text=" "));assertNull(item(text="x".repeat(5001)));assertNotNull(item(text="x".repeat(5000)))}
 @Test fun timestampsPreventHistoryBackfillAndFutureData(){assertNull(item(at=0));assertNull(item(at=999));assertNull(item(at=303001));assertNotNull(item(at=1000))}
 @Test fun repeatedBundledNotificationsDeduplicated(){val first=item()!!;val repeated=item(now=4000)!!;assertEquals(first.id,repeated.id);val rows=listOf(first);assertSame(rows,CapturePolicy.add(rows,repeated))}
 @Test fun messageEditsAndNewTimesKeepSeparateProvenance(){assertNotEquals(item()!!.id,item(text="sarson 8100 per 40kg")!!.id);assertNotEquals(item()!!.id,item(at=2001)!!.id)}
 @Test fun retentionBoundedToLatestTwoHundred(){var rows=emptyList<CapturedGroupMessage>();repeat(230){i->rows=CapturePolicy.add(rows,item(text="rate "+i,at=2000+i.toLong(),now=3000+i.toLong())!!)};assertEquals(200,rows.size);assertEquals("rate 30",rows.first().text);assertEquals("rate 229",rows.last().text)}
 @Test fun unicodeNamesMatchCanonicalFormWithoutBroadeningCase(){assertNotNull(item(target="Cafe\u0301",title="Caf\u00e9",text="سرسوں نرخ"));assertNotEquals(item(target="گروپ",text="نرخ")!!.id,item(target="گروپ",text="دوسرا نرخ")!!.id)}
}

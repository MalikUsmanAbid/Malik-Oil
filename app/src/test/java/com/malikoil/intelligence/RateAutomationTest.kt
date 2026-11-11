package com.malikoil.intelligence
import com.malikoil.intelligence.data.MarketRepository
import org.junit.Assert.*
import org.junit.Test
class RateAutomationTest {
 @Test fun productionStartsWithoutFabricatedRates(){
  val repo=MarketRepository()
  assertTrue(repo.rates.value.isEmpty())
  assertTrue(repo.getCuratedPunjabNews().isEmpty())
  assertEquals(listOf("sarson"),repo.commodities.value.map{it.id})
  repo.addCustomCommodity("wheat","Wheat","گندم")
  assertTrue(repo.rates.value.isEmpty())
 }
 @Test fun explicitNotificationRowsRequireRealObservationTime(){
  val repo=MarketRepository()
  assertTrue(repo.checkAndParseRows("sarson | Samundri | standard | 8000 | 40kg").isEmpty())
  val rows=repo.checkAndParseRows("sarson | Samundri | standard | 8000 | 40kg | 2026-11-07T10:00:00Z")
  assertEquals(1,rows.size)
  assertEquals(20000L,rows[0].pricePaisaKg)
 }
 @Test fun notificationReplayDoesNotCreateDuplicateCandidates(){
  val repo=MarketRepository();val text="sarson | Samundri | standard | 8000 | 40kg | 2026-11-07T10:00:00Z"
  repo.receiveRateNotification("TEST group",text,1794050000000)
  repo.receiveRateNotification("TEST group",text,1794050000000)
  assertEquals(1,repo.notifications.value.size);assertEquals(1,repo.candidates.value.size)
 }
 @Test fun alertComparisonRequiresSameSource(){
  val repo=MarketRepository()
  repo.addCandidate("sarson","Samundri",8000.0,"40kg",source="Source A")
  repo.approveCandidate(repo.candidates.value.first().id)
  repo.addCandidate("sarson","Samundri",9000.0,"40kg",source="Source B")
  repo.approveCandidate(repo.candidates.value.first().id)
  assertTrue(repo.alerts.value.isEmpty())
 }
 @Test fun missingDateAndGradeStayReviewDrafts(){
  val draft=MarketRateParser.drafts("sarson Samundri 8000 per 40kg")
  assertTrue(draft.contains("OBSERVATION_DATE_REQUIRED"))
  assertTrue(draft.contains("GRADE_REQUIRED"))
  assertTrue(MarketRateParser.drafts("سرسوں سمندری ۸,۰۰۰ فی ۴۰ کلو").contains("8000 | 40kg"))
  assertTrue(MarketRateParser.drafts("sarson Samundri 7000-8000 per 40kg").isEmpty())
 }
 @Test fun publicFeedRejectsEntitiesAndPreservesDates(){
  try{PublicRateResearch.parse("<!DOCTYPE rss [<!ENTITY x SYSTEM 'file:///secret'>]><rss/>");fail("DTD accepted")}catch(_:IllegalArgumentException){}
  val date=java.time.ZonedDateTime.now(java.time.ZoneOffset.UTC).format(java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME)
  val xml="<rss><channel><item><title>TEST sarson headline</title><link>https://news.google.com/articles/test</link><pubDate>$date</pubDate><source>TEST publisher</source></item></channel></rss>"
  val sources=PublicRateResearch.parse(xml)
  assertEquals(1,sources.size);assertEquals("headline-only",sources.first().kind)
 }
 @Test fun csvExportEscapesOwnerLabels(){
  val repo=MarketRepository()
  repo.addCandidate("sarson","=SUM(1,1)",8000.0,"40kg")
  repo.approveCandidate(repo.candidates.value.first().id)
  assertTrue(repo.generateTodayReport().rawCsv.contains("'=SUM(1,1)"))
 }
}

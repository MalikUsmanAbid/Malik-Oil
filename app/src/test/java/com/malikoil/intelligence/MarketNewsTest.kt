package com.malikoil.intelligence

import com.malikoil.intelligence.data.MarketRepository
import org.junit.Assert.*
import org.junit.Test

class MarketNewsTest {

    @Test
    fun testCuratedPunjabNewsPopulated() {
        val repo = MarketRepository()
        val news = repo.getCuratedPunjabNews()

        assertTrue("Expected at least 5 curated news articles", news.size >= 5)
        val hasPamra = news.any { it.sourceName.contains("PAMRA", ignoreCase = true) }
        assertTrue("Expected PAMRA regulatory news", hasPamra)

        val hasExpeller = news.any { it.category == "crushing" }
        assertTrue("Expected crushing category news", hasExpeller)

        news.forEach { article ->
            assertTrue("Article title must not be blank", article.title.isNotBlank())
            assertTrue("Article summary must not be blank", article.summary.isNotBlank())
            assertTrue("Article URL must start with http", article.url.startsWith("http"))
            assertTrue("Key takeaways should be provided", article.keyTakeaways.isNotEmpty())
        }
    }

    @Test
    fun testMarketSentimentInitialized() {
        val repo = MarketRepository()
        val sentiment = repo.marketSentiment.value

        assertNotNull(sentiment)
        assertEquals("Bullish", sentiment.overallSentiment)
        assertTrue(sentiment.sentimentDeltaPercent > 0.0)
        assertTrue(sentiment.dominantFactor.contains("Expeller", ignoreCase = true))
    }

    @Test
    fun testPriceNormalization() {
        val repo = MarketRepository()
        // 8640 PKR per 40kg -> 216 PKR/kg -> 21600 paisa/kg
        val paisa = repo.normalizePricePaisaKg(8640.0, "40kg")
        assertEquals(21600L, paisa)

        // 200 PKR per kg -> 20000 paisa/kg
        val paisaKg = repo.normalizePricePaisaKg(200.0, "kg")
        assertEquals(20000L, paisaKg)
    }

    @Test
    fun testSarsonCanola7DayTrends() {
        val repo = MarketRepository()
        val rates = repo.rates.value

        val sarsonRates = rates.filter { it.commodity == "sarson" && !it.isSample }
        val canolaRates = rates.filter { it.commodity == "canola" }

        assertTrue("Expected at least 7 Sarson observations for 7-day trend", sarsonRates.size >= 7)
        assertTrue("Expected at least 7 Canola observations for 7-day trend", canolaRates.size >= 7)

        val sarsonLatest = sarsonRates.maxByOrNull { it.observedAt }!!
        val canolaLatest = canolaRates.maxByOrNull { it.observedAt }!!

        assertTrue("Canola rate should be positive", canolaLatest.pricePkrKg > 0)
        assertTrue("Sarson rate should be positive", sarsonLatest.pricePkrKg > 0)

        // Canola typically commands a premium over Sarson in Punjab
        val spread = canolaLatest.pricePkrKg - sarsonLatest.pricePkrKg
        assertTrue("Expected positive Canola price spread over Sarson", spread >= 0)
    }

    @Test
    fun test7DayHighLowExtremesCalculation() {
        val repo = MarketRepository()
        val rates = repo.rates.value

        val sarsonRates = rates.filter { it.commodity == "sarson" && !it.isSample }
        val canolaRates = rates.filter { it.commodity == "canola" }

        val sarsonMax = sarsonRates.maxByOrNull { it.pricePkrKg }
        val sarsonMin = sarsonRates.minByOrNull { it.pricePkrKg }
        assertNotNull(sarsonMax)
        assertNotNull(sarsonMin)
        assertTrue("Sarson peak should be greater than or equal to dip", sarsonMax!!.pricePkrKg >= sarsonMin!!.pricePkrKg)

        val canolaMax = canolaRates.maxByOrNull { it.pricePkrKg }
        val canolaMin = canolaRates.minByOrNull { it.pricePkrKg }
        assertNotNull(canolaMax)
        assertNotNull(canolaMin)
        assertTrue("Canola peak should be greater than or equal to dip", canolaMax!!.pricePkrKg >= canolaMin!!.pricePkrKg)

        // Global extremes
        val globalPeak = maxOf(sarsonMax.pricePkrKg, canolaMax.pricePkrKg)
        val globalDip = minOf(sarsonMin.pricePkrKg, canolaMin.pricePkrKg)
        assertTrue("Global peak must exceed global dip", globalPeak > globalDip)
    }

    @Test
    fun testTargetPriceAlertCreationAndUnits() {
        val repo = MarketRepository()

        // Alert with 40kg maund unit: 8000 PKR / 40kg -> 200 PKR/kg
        val alertMaund = repo.addTargetPriceAlert(
            commodity = "sarson",
            targetPrice = 8000.0,
            unit = "40kg",
            condition = com.malikoil.intelligence.model.PriceCondition.AT_OR_BELOW,
            market = "Samundri",
            notes = "Target bid price"
        )
        assertEquals(200.0, alertMaund.targetPricePkrKg, 0.01)
        assertEquals(8000.0, alertMaund.targetPricePkrMaund, 0.01)
        assertEquals("sarson", alertMaund.commodity)
        assertEquals("Samundri", alertMaund.market)
        assertTrue(alertMaund.isActive)
        assertNull(alertMaund.triggeredAt)

        // Alert with kg unit: 220 PKR/kg
        val alertKg = repo.addTargetPriceAlert(
            commodity = "canola",
            targetPrice = 220.0,
            unit = "kg",
            condition = com.malikoil.intelligence.model.PriceCondition.AT_OR_ABOVE,
            market = "All Mandis"
        )
        assertEquals(220.0, alertKg.targetPricePkrKg, 0.01)
        assertEquals(8800.0, alertKg.targetPricePkrMaund, 0.01)
    }

    @Test
    fun testTargetPriceAlertTriggerConditions() {
        val repo = MarketRepository()

        // Create alert for Sarson AT_OR_BELOW 200 PKR/kg (8000/maund)
        val alert = repo.addTargetPriceAlert(
            commodity = "sarson",
            targetPrice = 200.0,
            unit = "kg",
            condition = com.malikoil.intelligence.model.PriceCondition.AT_OR_BELOW,
            market = "Samundri"
        )

        // Observation above threshold (205 PKR/kg) -> Should NOT trigger
        val highObs = com.malikoil.intelligence.model.RateObservation(
            id = "obs_high",
            stream = "sarson_samundri",
            commodity = "sarson",
            market = "Samundri",
            grade = "standard",
            source = "Mandi Board",
            observedAt = java.time.Instant.now().epochSecond,
            pricePaisaKg = 20500L
        )
        val triggeredHigh = repo.checkTargetPriceAlerts(null, highObs)
        assertTrue("Higher price should not trigger AT_OR_BELOW alert", triggeredHigh.none { it.id == alert.id })

        // Observation at or below threshold (198 PKR/kg) -> Should trigger!
        val lowObs = com.malikoil.intelligence.model.RateObservation(
            id = "obs_low",
            stream = "sarson_samundri",
            commodity = "sarson",
            market = "Samundri",
            grade = "standard",
            source = "Mandi Board",
            observedAt = java.time.Instant.now().epochSecond,
            pricePaisaKg = 19800L
        )
        val triggeredLow = repo.checkTargetPriceAlerts(null, lowObs)
        assertTrue("Lower price should trigger AT_OR_BELOW alert", triggeredLow.any { it.id == alert.id })

        val updatedAlert = repo.targetAlerts.value.find { it.id == alert.id }
        assertNotNull(updatedAlert)
        assertNotNull(updatedAlert!!.triggeredAt)
        assertEquals(198.0, updatedAlert.triggeredPricePkrKg!!, 0.01)
    }

    @Test
    fun testTargetPriceAlertSimulationAndToggle() {
        val repo = MarketRepository()
        val alert = repo.addTargetPriceAlert(
            commodity = "canola",
            targetPrice = 230.0,
            unit = "kg",
            condition = com.malikoil.intelligence.model.PriceCondition.AT_OR_BELOW,
            market = "Gojra"
        )

        // Toggle active status
        repo.toggleTargetPriceAlert(alert.id)
        val inactiveAlert = repo.targetAlerts.value.find { it.id == alert.id }!!
        assertFalse(inactiveAlert.isActive)

        repo.toggleTargetPriceAlert(alert.id)
        val activeAgain = repo.targetAlerts.value.find { it.id == alert.id }!!
        assertTrue(activeAgain.isActive)

        // Test simulation trigger
        val simSuccess = repo.simulateTargetAlertTrigger(null, alert.id)
        assertTrue("Simulation must succeed", simSuccess)

        val triggeredSim = repo.targetAlerts.value.find { it.id == alert.id }!!
        assertNotNull("Alert must be triggered after simulation", triggeredSim.triggeredAt)
        assertTrue(triggeredSim.triggeredPricePkrKg!! <= 230.0)

        // Remove alert
        repo.removeTargetPriceAlert(alert.id)
        assertNull(repo.targetAlerts.value.find { it.id == alert.id })
    }

    @Test
    fun testSearchGroundingToolNewsFeed() {
        val repo = MarketRepository()
        val news = repo.getCuratedPunjabNews()

        assertTrue("Expected search grounded articles", news.isNotEmpty())
        news.forEach { article ->
            assertTrue("Article should have search grounded flag", article.isSearchGrounded)
            assertTrue("Article must declare search grounding query", article.searchGroundingQuery.isNotBlank())
            assertTrue("Article must have verified grounded sources", article.groundedSources.isNotEmpty())
            assertTrue("Grounding score should be positive", article.groundingScore > 0.0)
        }

        val groundingStatus = repo.searchGroundingStatus.value
        assertNotNull(groundingStatus)
        assertTrue(groundingStatus.isGrounded)
        assertTrue(groundingStatus.activeQuery.contains("oilseed", ignoreCase = true))
        assertTrue(groundingStatus.verifiedSourcesCount > 0)
    }
}


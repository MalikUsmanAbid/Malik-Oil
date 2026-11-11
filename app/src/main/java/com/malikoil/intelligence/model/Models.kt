package com.malikoil.intelligence.model

data class Commodity(
    val id: String,
    val nameEn: String,
    val nameUr: String
) {
    fun displayName(lang: String): String = if (lang == "ur") nameUr else nameEn
}

data class RateObservation(
    val id: String,
    val stream: String,
    val commodity: String,
    val market: String,
    val grade: String,
    val source: String,
    val observedAt: Long, // Epoch seconds
    val pricePaisaKg: Long,
    val isSample: Boolean = false
) {
    val pricePkrKg: Double get() = pricePaisaKg / 100.0
    val pricePkrMaund: Double get() = pricePkrKg * 40.0
}

data class RateCandidate(
    val id: String,
    val commodity: String,
    val market: String,
    val grade: String,
    val source: String,
    val amount: Double,
    val unit: String,
    val pricePaisaKg: Long,
    val observedAt: Long
)

data class MovementAlert(
    val id: String,
    val commodity: String,
    val market: String,
    val stream: String,
    val oldPricePaisaKg: Long,
    val newPricePaisaKg: Long,
    val deltaPercent: Double,
    val createdAt: Long
) {
    val isRise: Boolean get() = deltaPercent >= 0
    val oldPricePkr: Double get() = oldPricePaisaKg / 100.0
    val newPricePkr: Double get() = newPricePaisaKg / 100.0
}

data class ResearchFinding(
    val summary: String,
    val sourceId: Int
)

data class ResearchSource(
    val id: Int,
    val title: String,
    val publisher: String,
    val publishedAt: String,
    val url: String,
    val kind: String = "search-excerpt"
)

data class EvidenceSupport(
    val sourceId: Int,
    val quote: String
)

data class ResearchTurn(
    val question: String,
    val text: String,
    val commodity: String,
    val market: String,
    val depth: String,
    val findings: List<ResearchFinding> = emptyList(),
    val sources: List<ResearchSource> = emptyList(),
    val supports: List<EvidenceSupport> = emptyList(),
    val durationMs: Long = 1400L,
    val checkedAt: Long = System.currentTimeMillis()
)

data class ResearchSession(
    val id: String,
    var title: String = "",
    var scope: String = "",
    val turns: MutableList<ResearchTurn> = mutableListOf()
)

data class LandedCostInput(
    val quantityKg: Double = 1000.0,
    val buyPkrKg: Double = 200.0,
    val sellPkrKg: Double = 210.0,
    val transportPkr: Double = 2000.0,
    val otherCostsPkr: Double = 500.0
)

data class LandedCostResult(
    val landedCostPkr: Double,
    val revenuePkr: Double,
    val grossMarginPkr: Double,
    val breakEvenPkrKg: Double,
    val marginPercent: Double,
    val notice: String
)

data class DailyReportSnapshot(
    val date: String,
    val summary: String,
    val totalObservations: Int,
    val generatedAt: Long,
    val rawCsv: String
)

data class NotificationCaptureItem(
    val id: String,
    val group: String,
    val text: String,
    val capturedAt: Long,
    val cities: List<String>,
    val isSarson: Boolean
)

// NEW: Oil & Khali Recovery Calculator Models
data class OilYieldInput(
    val commodity: String = "sarson",
    val seedQuantityMaund: Double = 25.0, // 25 maunds = 1,000 kg
    val seedPricePerMaund: Double = 8600.0, // PKR per 40kg
    val crushingCostPerMaund: Double = 180.0, // PKR per 40kg expeller charge
    val oilRecoveryPercent: Double = 34.0, // Standard Sarson: ~34% oil
    val khaliRecoveryPercent: Double = 63.0, // Standard Khali: ~63%
    val oilSellingPriceKg: Double = 560.0, // PKR per kg
    val khaliSellingPriceMaund: Double = 3400.0 // PKR per 40kg bag
)

data class OilYieldResult(
    val totalSeedWeightKg: Double,
    val totalSeedCostPkr: Double,
    val totalCrushingCostPkr: Double,
    val totalExpensesPkr: Double,
    val oilProducedKg: Double,
    val oilRevenuePkr: Double,
    val khaliProducedKg: Double,
    val khaliBags40kg: Double,
    val khaliRevenuePkr: Double,
    val totalRevenuePkr: Double,
    val netProfitPkr: Double,
    val profitPerMaundPkr: Double,
    val breakEvenSeedPriceMaund: Double
)

// NEW: Moisture & Quality Tare Deduction Models
data class MoistureDeductionInput(
    val grossWeightMaund: Double = 100.0,
    val quotedPriceMaund: Double = 8600.0,
    val actualMoisturePercent: Double = 11.0,
    val standardMoisturePercent: Double = 8.0,
    val foreignMatterPercent: Double = 1.0,
    val bagTareKgPerBag: Double = 1.0,
    val bagsCount: Int = 100
)

data class MoistureDeductionResult(
    val grossWeightKg: Double,
    val moistureCutKg: Double,
    val foreignMatterCutKg: Double,
    val bagTareCutKg: Double,
    val totalDeductionKg: Double,
    val netBillableWeightKg: Double,
    val netBillableMaund: Double,
    val grossPayablePkr: Double,
    val netPayablePkr: Double,
    val effectivePricePerMaundPkr: Double,
    val totalDeductionSavingsPkr: Double
)

// NEW: Mandi Freight Distance & Cost Model
data class MandiFreightItem(
    val mandi: String,
    val distanceKm: Int,
    val defaultFreightPerMaund: Double,
    val typicalTransitHours: String
)

// NEW: Market Insights & News Aggregator Models
data class MarketNewsArticle(
    val id: String,
    val title: String,
    val summary: String,
    val sourceName: String,
    val category: String, // "sarson", "canola", "crushing", "policy", "general"
    val publishedAt: String,
    val url: String,
    val impactOnPrices: String = "Neutral", // "Bullish (+)", "Bearish (-)", "Neutral"
    val mandiTag: String = "Punjab",
    val keyTakeaways: List<String> = emptyList(),
    val isLiveApi: Boolean = false,
    val isSearchGrounded: Boolean = true,
    val searchGroundingQuery: String = "Punjab oilseed mustard canola mandi prices",
    val groundedSources: List<String> = listOf("Google News RSS", "AgriWatch Pakistan", "Directorate of Agri Punjab"),
    val groundingScore: Double = 0.95
)

data class SearchGroundingStatus(
    val isGrounded: Boolean = true,
    val activeQuery: String = "Punjab oilseed mustard canola mandi market news",
    val engine: String = "Google News Search Grounding",
    val verifiedSourcesCount: Int = 4,
    val lastGroundedTime: String = "Today · Verified Feed"
)

data class MarketSentimentSummary(
    val overallSentiment: String = "Bullish", // "Bullish", "Neutral", "Bearish"
    val sentimentDeltaPercent: Double = 2.4, // e.g. +2.4%
    val dominantFactor: String = "Expeller mills in Faisalabad division actively purchasing dry mustard arrivals",
    val sarsonTrend: String = "Strong Buying (PKR 8,550 - 8,700/maund)",
    val canolaTrend: String = "Steady Arrivals (PKR 9,100 - 9,250/maund)",
    val crushingDemand: String = "High Capacity Utilization (84%)",
    val updatedAt: String = "Today · Live Aggregator"
)

// NEW: Target Purchase Price Alert Models
enum class PriceCondition {
    AT_OR_BELOW, // Target purchase (buy when price drops to or below target)
    AT_OR_ABOVE  // Target sale (sell when price rises to or above target)
}

data class TargetPriceAlert(
    val id: String = java.util.UUID.randomUUID().toString(),
    val commodity: String, // "sarson" or "canola"
    val targetPricePkrKg: Double,
    val condition: PriceCondition = PriceCondition.AT_OR_BELOW,
    val market: String = "All Mandis",
    val isActive: Boolean = true,
    val createdAt: Long = java.time.Instant.now().epochSecond,
    val triggeredAt: Long? = null,
    val triggeredPricePkrKg: Double? = null,
    val notes: String = ""
) {
    val targetPricePkrMaund: Double get() = targetPricePkrKg * 40.0
    val isTriggered: Boolean get() = triggeredAt != null
}


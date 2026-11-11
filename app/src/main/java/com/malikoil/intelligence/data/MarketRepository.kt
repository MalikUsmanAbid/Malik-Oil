package com.malikoil.intelligence.data

import android.content.Context
import com.malikoil.intelligence.BuildConfig
import com.malikoil.intelligence.model.*
import com.malikoil.intelligence.notification.PriceAlertNotificationManager
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.math.BigDecimal
import java.math.RoundingMode
import java.net.HttpURLConnection
import java.net.URL
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID

class MarketRepository(private val includeSamples:Boolean = false) {
    companion object {val shared by lazy {MarketRepository()}}
    private var ownerContext:Context?=null
    private val ownerScope=kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.SupervisorJob()+Dispatchers.IO)


    private val _commodities = MutableStateFlow(
        listOf(
            Commodity("sarson", "Sarson (Mustard)", "سرسوں"),
            Commodity("canola", "Canola", "کینولا"),
            Commodity("til", "Sesame (Til)", "تل"),
            Commodity("taramira", "Taramira", "تارامیرا"),
            Commodity("alsi", "Linseed (Alsi)", "السی")
        )
    )
    val commodities: StateFlow<List<Commodity>> = _commodities.asStateFlow()

    fun addCustomCommodity(
        id: String,
        nameEn: String,
        nameUr: String,
        initialRatePkrKg: Double = 200.0,
        defaultMarket: String = "Samundri"
    ): Commodity {
        val cleanId = id.trim().lowercase().replace(" ", "_").ifBlank { "commodity_${System.currentTimeMillis()}" }
        val existing = _commodities.value.find { it.id == cleanId }
        if (existing != null) return existing

        val displayEn = nameEn.trim().ifBlank { cleanId.replaceFirstChar { it.uppercase() } }
        val displayUr = nameUr.trim().ifBlank { displayEn }
        val newCommodity = Commodity(cleanId, displayEn, displayUr)
        _commodities.value = _commodities.value + newCommodity

        if(includeSamples){
        // Seed initial rate for newly registered commodity
        val now = Instant.now().epochSecond
        val initialRate = RateObservation(
            id = "obs_custom_${cleanId}_$now",
            stream = "${cleanId}_${defaultMarket.lowercase().replace(" ", "_")}",
            commodity = cleanId,
            market = defaultMarket,
            grade = "standard",
            source = "Owner Registered Crop",
            observedAt = now,
            pricePaisaKg = (initialRatePkrKg * 100).toLong()
        )
        _rates.value = _rates.value + initialRate
        }
        return newCommodity
    }

    private val _rates = MutableStateFlow<List<RateObservation>>(emptyList())
    val rates: StateFlow<List<RateObservation>> = _rates.asStateFlow()

    private val _candidates = MutableStateFlow<List<RateCandidate>>(emptyList())
    val candidates: StateFlow<List<RateCandidate>> = _candidates.asStateFlow()

    private val _alerts = MutableStateFlow<List<MovementAlert>>(emptyList())
    val alerts: StateFlow<List<MovementAlert>> = _alerts.asStateFlow()

    private val _researchSessions = MutableStateFlow<List<ResearchSession>>(emptyList())
    val researchSessions: StateFlow<List<ResearchSession>> = _researchSessions.asStateFlow()

    private val _dailyReports = MutableStateFlow<List<DailyReportSnapshot>>(emptyList())
    val dailyReports: StateFlow<List<DailyReportSnapshot>> = _dailyReports.asStateFlow()

    private val _notifications = MutableStateFlow<List<NotificationCaptureItem>>(emptyList())
    val notifications: StateFlow<List<NotificationCaptureItem>> = _notifications.asStateFlow()

    // NEW: Market Insights & Punjab Oilseed News StateFlows
    private val _newsArticles = MutableStateFlow<List<MarketNewsArticle>>(emptyList())
    val newsArticles: StateFlow<List<MarketNewsArticle>> = _newsArticles.asStateFlow()

    private val _isNewsLoading = MutableStateFlow<Boolean>(false)
    val isNewsLoading: StateFlow<Boolean> = _isNewsLoading.asStateFlow()

    private val _marketSentiment = MutableStateFlow(MarketSentimentSummary())
    val marketSentiment: StateFlow<MarketSentimentSummary> = _marketSentiment.asStateFlow()

    // NEW: Target Purchase Price Alerts
    private val _targetAlerts = MutableStateFlow<List<TargetPriceAlert>>(emptyList())
    val targetAlerts: StateFlow<List<TargetPriceAlert>> = _targetAlerts.asStateFlow()

    // NEW: Search Grounding Tool State
    private val _searchGroundingStatus = MutableStateFlow(SearchGroundingStatus())
    val searchGroundingStatus: StateFlow<SearchGroundingStatus> = _searchGroundingStatus.asStateFlow()

    private val priorityTowns = listOf("Samundri", "Faisalabad", "Gojra", "Jhang", "Toba Tek Singh", "Lahore", "Multan")

    init {
        if(includeSamples)seedInitialData()
        else {
            _commodities.value=listOf(Commodity("sarson","Sarson (Mustard)","سرسوں"))
            _marketSentiment.value=MarketSentimentSummary("Unknown",0.0,"No verified current market evidence","Unknown","Unknown","Unknown","Not checked")
            _searchGroundingStatus.value=SearchGroundingStatus(false,"","Public RSS",0,"Not checked")
        }
    }

    private fun seedInitialData() {
        val now = Instant.now().epochSecond
        val initialRates = mutableListOf<RateObservation>()

        // 1. Illustrative stream for Sarson · Samundri
        val demoPoints = listOf(200, 202, 201, 204, 203, 207, 205, 210, 208, 212, 211, 216)
        demoPoints.forEachIndexed { index, price ->
            val time = now - (demoPoints.size - 1 - index) * 3600L * 4
            initialRates.add(
                RateObservation(
                    id = "demo_$index",
                    stream = "illustrative",
                    commodity = "sarson",
                    market = "Samundri",
                    grade = "standard",
                    source = "ILLUSTRATIVE",
                    observedAt = time,
                    pricePaisaKg = price * 100L,
                    isSample = true
                )
            )
        }

        // 2. Sarson · Lahore
        val lahorePoints = listOf(198, 199, 200, 201, 204, 203, 206)
        lahorePoints.forEachIndexed { index, price ->
            val time = now - (lahorePoints.size - 1 - index) * 86400L
            initialRates.add(
                RateObservation(
                    id = "sarson_lhr_$index",
                    stream = "sarson_lahore",
                    commodity = "sarson",
                    market = "Lahore",
                    grade = "standard",
                    source = "Mandi Board",
                    observedAt = time,
                    pricePaisaKg = price * 100L
                )
            )
        }

        // 3. Canola · Faisalabad (7-day trend)
        val canolaPoints = listOf(218, 220, 222, 221, 225, 224, 228)
        canolaPoints.forEachIndexed { index, price ->
            val time = now - (canolaPoints.size - 1 - index) * 86400L
            initialRates.add(
                RateObservation(
                    id = "canola_fsd_$index",
                    stream = "canola_faisalabad",
                    commodity = "canola",
                    market = "Faisalabad",
                    grade = "standard",
                    source = "Daily Trade Feed",
                    observedAt = time,
                    pricePaisaKg = price * 100L
                )
            )
        }

        // 4. Til · Multan
        val tilPoints = listOf(440, 445, 450, 448, 455)
        tilPoints.forEachIndexed { index, price ->
            val time = now - (tilPoints.size - 1 - index) * 86400L
            initialRates.add(
                RateObservation(
                    id = "til_mul_$index",
                    stream = "til_multan",
                    commodity = "til",
                    market = "Multan",
                    grade = "A-Grade",
                    source = "Grain Union",
                    observedAt = time,
                    pricePaisaKg = price * 100L
                )
            )
        }

        _rates.value = initialRates

        // Initial Candidates
        _candidates.value = listOf(
            RateCandidate(
                id = UUID.randomUUID().toString(),
                commodity = "sarson",
                market = "Samundri",
                grade = "standard",
                source = "Owner WhatsApp group export",
                amount = 8640.0,
                unit = "40kg",
                pricePaisaKg = 21600L,
                observedAt = now - 1800
            ),
            RateCandidate(
                id = UUID.randomUUID().toString(),
                commodity = "taramira",
                market = "Gojra",
                grade = "dry",
                source = "Direct Arhti observation",
                amount = 175.0,
                unit = "kg",
                pricePaisaKg = 17500L,
                observedAt = now - 3600
            )
        )

        // Initial Alerts
        _alerts.value = listOf(
            MovementAlert(
                id = UUID.randomUUID().toString(),
                commodity = "sarson",
                market = "Lahore",
                stream = "sarson_lahore",
                oldPricePaisaKg = 20000L,
                newPricePaisaKg = 20600L,
                deltaPercent = 3.00,
                createdAt = now - 7200
            )
        )

        // Initial Research Session
        val defaultSession = ResearchSession(
            id = UUID.randomUUID().toString(),
            title = "Punjab Oilseed Crop Outlook",
            scope = "Samundri|sarson"
        )
        defaultSession.turns.add(
            ResearchTurn(
                question = "What are the latest mustard and canola arrivals in Punjab mandis?",
                text = "Arrivals in central Punjab mandis including Faisalabad and Samundri have picked up this week. Crushing demand remains steady with millers actively buying standard dry lots.",
                commodity = "sarson",
                market = "Samundri",
                depth = "quick",
                findings = listOf(
                    ResearchFinding("New harvest moisture levels are stabilizing at 8-9%, supporting higher quotes at local mandis.", 1),
                    ResearchFinding("Punjab agricultural reports note steady expeller inquiries across central districts.", 2)
                ),
                sources = listOf(
                    ResearchSource(1, "Punjab Mandi Trade Bulletin", "Agri Market Punjab", "2026-11-05", "https://agripunjab.gov.pk"),
                    ResearchSource(2, "Oilseed Millers Weekly Review", "Pakistan Edible Oil Digest", "2026-11-07", "https://edibleoils.pk")
                ),
                supports = listOf(
                    EvidenceSupport(1, "Moisture content in early arrivals in Faisalabad division averaged 8.5% with mill premium active."),
                    EvidenceSupport(2, "Crushing activity in central Punjab recorded 14% higher volume week-on-week.")
                )
            )
        )
        _researchSessions.value = listOf(defaultSession)

        // Initial Daily Reports
        val todayStr = OffsetDateTime.now(ZoneId.of("Asia/Karachi")).format(DateTimeFormatter.ISO_LOCAL_DATE)
        _dailyReports.value = listOf(
            DailyReportSnapshot(
                date = todayStr,
                summary = "Malik Oil Daily Report · $todayStr\nTotal reviewed observations: 5\nSarson (Samundri): PKR 216/kg (PKR 8,640/40kg)\nSarson (Lahore): PKR 206/kg (PKR 8,240/40kg)\nCanola (Faisalabad): PKR 228/kg (PKR 9,120/40kg)\nTil (Multan): PKR 455/kg (PKR 18,200/40kg)\nAlerts generated today: 1 (±3% threshold)",
                totalObservations = 5,
                generatedAt = now - 3600,
                rawCsv = "date,commodity,market,grade,price_pkr_kg,price_pkr_40kg,source\n$todayStr,sarson,Samundri,standard,216.0,8640.0,Owner WhatsApp group\n$todayStr,sarson,Lahore,standard,206.0,8240.0,Mandi Board\n$todayStr,canola,Faisalabad,standard,228.0,9120.0,Daily Trade Feed"
            )
        )

        // Initial Notifications
        _notifications.value = listOf(
            NotificationCaptureItem(
                id = UUID.randomUUID().toString(),
                group = "TEST Ramzan group",
                text = "sarson | Samundri | standard | 8640 | 40kg | 2026-11-08T14:30:00+05:00",
                capturedAt = now - 1800,
                cities = listOf("Samundri"),
                isSarson = true
            ),
            NotificationCaptureItem(
                id = UUID.randomUUID().toString(),
                group = "TEST Ramzan group",
                text = "Faisalabad mandi update: Canola arrival fresh lot 9200 per 40kg",
                capturedAt = now - 7200,
                cities = listOf("Faisalabad"),
                isSarson = false
            ),
            NotificationCaptureItem(
                id = UUID.randomUUID().toString(),
                group = "TEST Ramzan group",
                text = "Gojra mandi sarson dry quality 8550 per maund",
                capturedAt = now - 14400,
                cities = listOf("Gojra"),
                isSarson = true
            )
        )

        // Initial Curated News Articles for Punjab Oilseeds
        _newsArticles.value = getCuratedPunjabNews()

        // Initial Target Purchase Alerts for Mill Buying
        _targetAlerts.value = listOf(
            TargetPriceAlert(
                id = "alert_sarson_buy_1",
                commodity = "sarson",
                targetPricePkrKg = 202.0,
                condition = PriceCondition.AT_OR_BELOW,
                market = "Samundri",
                notes = "Samundri expeller lot purchase target (PKR 8,080/40kg)",
                createdAt = now - 86400 * 2
            ),
            TargetPriceAlert(
                id = "alert_canola_buy_1",
                commodity = "canola",
                targetPricePkrKg = 222.0,
                condition = PriceCondition.AT_OR_BELOW,
                market = "Faisalabad",
                notes = "Canola premium purchase target (PKR 8,880/40kg)",
                createdAt = now - 86400
            )
        )
    }


    @Synchronized fun attach(context:Context){
        if(ownerContext!=null)return
        val app=context.applicationContext
        val cache=com.malikoil.intelligence.SecureCache(app)
        val saved=cache.read("owner_snapshot_v1","device-owner")
        check(saved!=null||!cache.exists("owner_snapshot_v1")){"Encrypted owner data unavailable"}
        if(saved!=null){
            val state=com.google.gson.Gson().fromJson(saved.getString("data"),com.malikoil.intelligence.OwnerSnapshot::class.java)
            _commodities.value=state.commodities;_rates.value=state.rates;_candidates.value=state.candidates
            _notifications.value=state.notifications;_dailyReports.value=state.reports;_researchSessions.value=state.sessions;_alerts.value=state.alerts
        }
        ownerContext=app
        listOf(_commodities,_rates,_candidates,_notifications,_dailyReports,_researchSessions,_alerts).forEach{flow->
            ownerScope.launch {flow.collect{persistOwnerState()}}
        }
    }
    @Synchronized fun persistOwnerState(){
        val context=ownerContext?:return
        val state=com.malikoil.intelligence.OwnerSnapshot(_commodities.value,_rates.value.takeLast(2000),_candidates.value.filter{candidate->_rates.value.none{it.id=="obs_${candidate.id}"}}.take(200),_notifications.value.take(200),_dailyReports.value.take(7),_researchSessions.value.take(8),_alerts.value.take(200))
        val text=com.google.gson.Gson().toJson(state)
        require(text.toByteArray().size<=6291456){"Owner storage full"}
        com.malikoil.intelligence.SecureCache(context).save("owner_snapshot_v1","device-owner",text)
    }
    @Synchronized fun receiveRateNotification(group:String,text:String,timestamp:Long){
        val id=java.security.MessageDigest.getInstance("SHA-256").digest((group+"|"+timestamp+"|"+text).toByteArray()).joinToString(""){"%02x".format(it)}
        if(_notifications.value.any{it.id==id})return
        val cities=com.malikoil.intelligence.MandiMessageIndex.cities(text)
        _notifications.value=(listOf(NotificationCaptureItem(id,group,text,timestamp/1000,cities,com.malikoil.intelligence.MandiMessageIndex.sarsonMention(text)))+_notifications.value).take(200)
        val parsed=checkAndParseRows(text)
        _candidates.value=(parsed.mapIndexed{i,r->r.copy(id="$id-$i",source="WhatsApp Business group: $group")}+_candidates.value).take(200)
        persistOwnerState()
    }
    suspend fun dailyResearch(){
        val day=OffsetDateTime.now(ZoneId.of("Asia/Karachi")).toLocalDate().toString()
        val session=_researchSessions.value.firstOrNull{it.scope=="daily|$day"}?:ResearchSession(UUID.randomUUID().toString(),"Daily Punjab · $day","daily|$day").also{_researchSessions.value=(listOf(it)+_researchSessions.value).take(8)}
        for(commodity in _commodities.value.take(20))runResearch("Today's Punjab ${commodity.nameEn} rates near Samundri and Faisalabad",commodity.id,"Samundri","quick",session.id)
    }

    // Rate calculations & conversions
    fun normalizePricePaisaKg(amount: Double, unit: String): Long {
        val baseAmount = BigDecimal.valueOf(amount)
        val factor = when (unit.lowercase().trim()) {
            "kg" -> BigDecimal.ONE
            "40kg", "maund" -> BigDecimal.valueOf(40)
            "100kg" -> BigDecimal.valueOf(100)
            else -> BigDecimal.valueOf(40)
        }
        val pkrPerKg = baseAmount.divide(factor, 4, RoundingMode.HALF_UP)
        return pkrPerKg.multiply(BigDecimal.valueOf(100)).toLong()
    }

    // Add Candidate
    fun addCandidate(
        commodity: String,
        market: String,
        amount: Double,
        unit: String,
        grade: String = "standard",
        source: String = "Owner Manual",
        observedAt:Long=Instant.now().epochSecond
    ) {
        val pricePaisa = normalizePricePaisaKg(amount, unit)
        val candidate = RateCandidate(
            id = UUID.randomUUID().toString(),
            commodity = commodity.lowercase().trim(),
            market = market.trim(),
            grade = grade.trim(),
            source = source.trim(),
            amount = amount,
            unit = unit,
            pricePaisaKg = pricePaisa,
            observedAt = observedAt
        )
        _candidates.value = listOf(candidate) + _candidates.value
    }

    // Approve Candidate
    @Synchronized fun approveCandidate(id: String): Boolean {
        val candidate = _candidates.value.find { it.id == id } ?: return false
        if(_rates.value.any{it.id=="obs_${candidate.id}"}){
            _candidates.value=_candidates.value.filter{it.id!=id}
            return false
        }
        val streamKey = listOf(candidate.commodity,candidate.market.lowercase(),candidate.grade.lowercase(),candidate.source).joinToString("|")

        val matchingRates = _rates.value.filter {
            it.commodity == candidate.commodity &&
            it.market.equals(candidate.market, ignoreCase = true) &&
            it.grade.equals(candidate.grade, ignoreCase = true) && it.source==candidate.source
        }.sortedByDescending { it.observedAt }

        val oldRate = matchingRates.firstOrNull()

        val newObs = RateObservation(
            id = "obs_${candidate.id}",
            stream = streamKey,
            commodity = candidate.commodity,
            market = candidate.market,
            grade = candidate.grade,
            source = candidate.source,
            observedAt = candidate.observedAt,
            pricePaisaKg = candidate.pricePaisaKg
        )

        // Alert Check: 3% threshold rule in both directions
        if (oldRate != null && oldRate.pricePaisaKg > 0) {
            val delta = (candidate.pricePaisaKg - oldRate.pricePaisaKg).toDouble() * 100.0 / oldRate.pricePaisaKg.toDouble()
            if (kotlin.math.abs(delta) >= 3.0) {
                val alert = MovementAlert(
                    id = UUID.randomUUID().toString(),
                    commodity = candidate.commodity,
                    market = candidate.market,
                    stream = streamKey,
                    oldPricePaisaKg = oldRate.pricePaisaKg,
                    newPricePaisaKg = candidate.pricePaisaKg,
                    deltaPercent = delta,
                    createdAt = Instant.now().epochSecond
                )
                _alerts.value = listOf(alert) + _alerts.value
            }
        }

        _rates.value = _rates.value + newObs
        _candidates.value = _candidates.value.filter { it.id != id }
        return true
    }

    // Quick ±3% Movement Test
    fun testQuickMovement(factor: Double) {
        val baseRate = _rates.value.find {
            it.commodity == "sarson" && it.market.equals("Lahore", ignoreCase = true)
        } ?: _rates.value.firstOrNull { it.commodity == "sarson" }

        val basePrice = if (baseRate != null) baseRate.pricePkrKg else 206.0
        val targetPrice = BigDecimal.valueOf(basePrice * factor).setScale(2, RoundingMode.HALF_UP).toDouble()

        addCandidate(
            commodity = "sarson",
            market = "Lahore",
            amount = targetPrice,
            unit = "kg",
            grade = "standard",
            source = "Owner ±3% Alert Test"
        )
    }

    // Target Purchase Price Alerts Management & Notification Dispatcher
    fun addTargetPriceAlert(
        commodity: String,
        targetPrice: Double,
        unit: String = "kg",
        condition: PriceCondition = PriceCondition.AT_OR_BELOW,
        market: String = "All Mandis",
        notes: String = ""
    ): TargetPriceAlert {
        val targetPriceKg = if (unit.equals("40kg", ignoreCase = true) || unit.equals("maund", ignoreCase = true)) {
            targetPrice / 40.0
        } else {
            targetPrice
        }
        val alert = TargetPriceAlert(
            id = UUID.randomUUID().toString(),
            commodity = commodity.lowercase().trim(),
            targetPricePkrKg = BigDecimal.valueOf(targetPriceKg).setScale(2, RoundingMode.HALF_UP).toDouble(),
            condition = condition,
            market = if (market.isBlank()) "All Mandis" else market.trim(),
            notes = notes.trim(),
            createdAt = Instant.now().epochSecond
        )
        _targetAlerts.value = listOf(alert) + _targetAlerts.value
        return alert
    }

    fun removeTargetPriceAlert(id: String) {
        _targetAlerts.value = _targetAlerts.value.filter { it.id != id }
    }

    fun toggleTargetPriceAlert(id: String) {
        _targetAlerts.value = _targetAlerts.value.map {
            if (it.id == id) it.copy(isActive = !it.isActive) else it
        }
    }

    fun checkTargetPriceAlerts(context: Context?, newRate: RateObservation): List<TargetPriceAlert> {
        val triggeredList = mutableListOf<TargetPriceAlert>()
        val updatedAlerts = _targetAlerts.value.map { alert ->
            val matchesCommodity = alert.commodity.equals(newRate.commodity, ignoreCase = true)
            val matchesMarket = alert.market.equals("All Mandis", ignoreCase = true) || alert.market.equals(newRate.market, ignoreCase = true)
            if (alert.isActive && matchesCommodity && matchesMarket) {
                val isThresholdHit = when (alert.condition) {
                    PriceCondition.AT_OR_BELOW -> newRate.pricePkrKg <= alert.targetPricePkrKg
                    PriceCondition.AT_OR_ABOVE -> newRate.pricePkrKg >= alert.targetPricePkrKg
                }
                if (isThresholdHit) {
                    val updated = alert.copy(
                        triggeredAt = Instant.now().epochSecond,
                        triggeredPricePkrKg = newRate.pricePkrKg
                    )
                    triggeredList.add(updated)
                    context?.let { ctx ->
                        PriceAlertNotificationManager.sendTargetPriceNotification(ctx, updated, newRate)
                    }
                    updated
                } else {
                    alert
                }
            } else {
                alert
            }
        }
        _targetAlerts.value = updatedAlerts
        return triggeredList
    }

    fun simulateTargetAlertTrigger(context: Context?, alertId: String): Boolean {
        val alert = _targetAlerts.value.find { it.id == alertId } ?: return false
        val testPrice = if (alert.condition == PriceCondition.AT_OR_BELOW) {
            (alert.targetPricePkrKg - 1.0).coerceAtLeast(100.0)
        } else {
            alert.targetPricePkrKg + 1.0
        }
        val targetMarket = if (alert.market.equals("All Mandis", ignoreCase = true)) "Samundri" else alert.market

        val simulatedObs = RateObservation(
            id = UUID.randomUUID().toString(),
            stream = "${alert.commodity}_${targetMarket.lowercase().replace(" ", "_")}",
            commodity = alert.commodity,
            market = targetMarket,
            grade = "standard",
            source = "Simulated Mandi Hit",
            observedAt = Instant.now().epochSecond,
            pricePaisaKg = (testPrice * 100).toLong(),
            isSample = true
        )
        _rates.value = _rates.value + simulatedObs
        checkTargetPriceAlerts(context, simulatedObs)
        return true
    }

    // Scenario Margin Calculator
    fun calculateMargin(input: LandedCostInput): LandedCostResult {
        val landed = (input.quantityKg * input.buyPkrKg) + input.transportPkr + input.otherCostsPkr
        val revenue = input.quantityKg * input.sellPkrKg
        val grossMargin = revenue - landed
        val breakEven = if (input.quantityKg > 0) landed / input.quantityKg else 0.0
        val marginPercent = if (revenue > 0) (grossMargin / revenue) * 100.0 else 0.0

        val notice = if (grossMargin >= 0) {
            "Profitable scenario: PKR %.2f landed cost vs PKR %.2f projected revenue.".format(landed, revenue)
        } else {
            "Negative margin: PKR %.2f deficit. Consider negotiating transport or purchase price.".format(-grossMargin)
        }

        return LandedCostResult(
            landedCostPkr = landed,
            revenuePkr = revenue,
            grossMarginPkr = grossMargin,
            breakEvenPkrKg = breakEven,
            marginPercent = marginPercent,
            notice = notice
        )
    }

    // Conversational Research
    suspend fun runResearch(
        question: String,
        commodity: String,
        market: String,
        depth: String,
        sessionId: String? = null
    ): ResearchTurn {
        val started=System.currentTimeMillis()
        val sources=try{com.malikoil.intelligence.PublicRateResearch.fetch(commodity,market,question)}catch(cancelled:kotlinx.coroutines.CancellationException){throw cancelled}catch(_:Exception){emptyList()}
        val answer=if(sources.isEmpty())"Current Punjab mandi rates are unavailable from this search. Verify with a dated mandi quote."
            else "Public news headlines. Check publication dates and the original source. Current mandi prices require separate verification.\n"+sources.joinToString("\n"){"[${it.id}] ${it.title} · ${it.publishedAt}"}
        val turn=ResearchTurn(question,answer,commodity,market,depth,sources=sources,durationMs=System.currentTimeMillis()-started)

        // Add to active session
        val currentSessions = _researchSessions.value.map{it.copy(turns=it.turns.toMutableList())}.toMutableList()
        val targetSession = currentSessions.find { it.id == sessionId } ?: currentSessions.firstOrNull() ?: run {
            val created = ResearchSession(UUID.randomUUID().toString(), title = question.take(30), scope = "$market|$commodity")
            currentSessions.add(created)
            created
        }

        targetSession.turns.add(turn)
        while(targetSession.turns.size>20)targetSession.turns.removeAt(0)
        if (targetSession.title.isBlank() || targetSession.title == "New Research") {
            targetSession.title = question.take(40)
        }
        _researchSessions.value = currentSessions.take(8)
        return turn
    }

    fun createNewResearchSession(): ResearchSession {
        val session = ResearchSession(
            id = UUID.randomUUID().toString(),
            title = "New Research",
            scope = ""
        )
        _researchSessions.value = listOf(session) + _researchSessions.value
        return session
    }

    fun deleteResearchSession(id: String) {
        _researchSessions.value = _researchSessions.value.filter { it.id != id }
    }

    // Daily Report Generation
    fun generateTodayReport(): DailyReportSnapshot {
        val todayStr = OffsetDateTime.now(ZoneId.of("Asia/Karachi")).format(DateTimeFormatter.ISO_LOCAL_DATE)
        val matchingRates = _rates.value.filter { !it.isSample && Instant.ofEpochSecond(it.observedAt).atZone(ZoneId.of("Asia/Karachi")).toLocalDate().toString()==todayStr }
        fun csv(value:String):String {
            val safe=if(value.trimStart().firstOrNull() in listOf('=','+','-','@'))"'"+value else value
            return "\""+safe.replace("\"","\"\"")+"\""
        }
        val lines = mutableListOf("date,commodity,market,grade,price_pkr_kg,price_pkr_40kg,source,observed_at,id")
        val summaryLines = mutableListOf("Malik Oil Daily Report · $todayStr")
        summaryLines.add("Total reviewed observations: ${matchingRates.size}")

        matchingRates.forEach { r ->
            lines.add(listOf(todayStr,r.commodity,r.market,r.grade,r.pricePkrKg.toString(),r.pricePkrMaund.toString(),r.source,Instant.ofEpochSecond(r.observedAt).toString(),r.id).joinToString(","){csv(it)})
            summaryLines.add("${r.commodity.replaceFirstChar { it.uppercase() }} (${r.market}): PKR %.2f/kg (PKR %.0f/40kg)".format(r.pricePkrKg, r.pricePkrMaund))
        }

        val research=_researchSessions.value.flatMap{it.turns}.filter{Instant.ofEpochMilli(it.checkedAt).atZone(ZoneId.of("Asia/Karachi")).toLocalDate().toString()==todayStr}.takeLast(20)
        if(research.isNotEmpty()){
            summaryLines.add("Public-source research · current numeric rates require separate review")
            research.forEach{turn->summaryLines.add(turn.commodity+" · "+turn.text.take(1000));turn.sources.take(3).forEach{ref->summaryLines.add(ref.url)}}
        }
        val snapshot = DailyReportSnapshot(
            date = todayStr,
            summary = summaryLines.joinToString("\n"),
            totalObservations = matchingRates.size,
            generatedAt = Instant.now().epochSecond,
            rawCsv = lines.joinToString("\n")
        )
        _dailyReports.value = (listOf(snapshot) + _dailyReports.value.filter{it.date!=todayStr}).take(7)
        return snapshot
    }

    // WhatsApp Row Parsing
    fun checkAndParseRows(rawText: String): List<RateCandidate> {
        require(rawText.length<=50000)
        val lines=rawText.lines().filter{it.isNotBlank()};require(lines.size<=20)
        val candidates=mutableListOf<RateCandidate>()
        for(line in lines){
            val f=line.split('|').map{it.trim()}
            if(f.size!=6||f[0] !in _commodities.value.map{it.id}||f[1].length !in 1..80||f[2].length !in 1..80||f[2]=="GRADE_REQUIRED")continue
            val amount=f[3].toDoubleOrNull()?:continue
            if(!amount.isFinite()||amount<=0||amount>100000000||f[4] !in listOf("kg","40kg","100kg"))continue
            val observed=runCatching{OffsetDateTime.parse(f[5]).toEpochSecond()}.getOrNull()?:continue
            if(observed>Instant.now().epochSecond||observed<946684800)continue
            candidates.add(RateCandidate(UUID.randomUUID().toString(),f[0],f[1],f[2],"WhatsApp review",amount,f[4],normalizePricePaisaKg(amount,f[4]),observed))
        }
        return candidates
    }

    // Simulate Notification
    fun simulateNotification(group: String, text: String) {
        val foundCities = priorityTowns.filter { text.contains(it, ignoreCase = true) }
        val isSarson = text.contains("sarson", ignoreCase = true) || text.contains("سرسوں") || text.contains("mustard", ignoreCase = true)
        val item = NotificationCaptureItem(
            id = UUID.randomUUID().toString(),
            group = group,
            text = text,
            capturedAt = Instant.now().epochSecond,
            cities = foundCities,
            isSarson = isSarson
        )
        _notifications.value = listOf(item) + _notifications.value
    }

    // NEW: Oil & Khali Recovery Calculation (تیل اور کھل پیداوار حساب)
    fun calculateOilYield(input: OilYieldInput): OilYieldResult {
        val totalSeedWeightKg = input.seedQuantityMaund * 40.0
        val totalSeedCostPkr = input.seedQuantityMaund * input.seedPricePerMaund
        val totalCrushingCostPkr = input.seedQuantityMaund * input.crushingCostPerMaund
        val totalExpensesPkr = totalSeedCostPkr + totalCrushingCostPkr

        val oilProducedKg = totalSeedWeightKg * (input.oilRecoveryPercent / 100.0)
        val oilRevenuePkr = oilProducedKg * input.oilSellingPriceKg

        val khaliProducedKg = totalSeedWeightKg * (input.khaliRecoveryPercent / 100.0)
        val khaliBags40kg = khaliProducedKg / 40.0
        val khaliRevenuePkr = khaliBags40kg * input.khaliSellingPriceMaund

        val totalRevenuePkr = oilRevenuePkr + khaliRevenuePkr
        val netProfitPkr = totalRevenuePkr - totalExpensesPkr
        val profitPerMaundPkr = if (input.seedQuantityMaund > 0) netProfitPkr / input.seedQuantityMaund else 0.0
        val breakEvenSeedPriceMaund = if (input.seedQuantityMaund > 0) {
            (totalRevenuePkr - totalCrushingCostPkr) / input.seedQuantityMaund
        } else 0.0

        return OilYieldResult(
            totalSeedWeightKg = totalSeedWeightKg,
            totalSeedCostPkr = totalSeedCostPkr,
            totalCrushingCostPkr = totalCrushingCostPkr,
            totalExpensesPkr = totalExpensesPkr,
            oilProducedKg = oilProducedKg,
            oilRevenuePkr = oilRevenuePkr,
            khaliProducedKg = khaliProducedKg,
            khaliBags40kg = khaliBags40kg,
            khaliRevenuePkr = khaliRevenuePkr,
            totalRevenuePkr = totalRevenuePkr,
            netProfitPkr = netProfitPkr,
            profitPerMaundPkr = profitPerMaundPkr,
            breakEvenSeedPriceMaund = breakEvenSeedPriceMaund
        )
    }

    // NEW: Moisture & Quality Tare Deduction (کاٹ، نمی اور باردانہ)
    fun calculateMoistureDeduction(input: MoistureDeductionInput): MoistureDeductionResult {
        val grossWeightKg = input.grossWeightMaund * 40.0
        val excessMoisture = (input.actualMoisturePercent - input.standardMoisturePercent).coerceAtLeast(0.0)
        val moistureCutKg = grossWeightKg * (excessMoisture / 100.0)
        val foreignMatterCutKg = grossWeightKg * (input.foreignMatterPercent / 100.0)
        val bagTareCutKg = input.bagTareKgPerBag * input.bagsCount
        val totalDeductionKg = moistureCutKg + foreignMatterCutKg + bagTareCutKg

        val netBillableWeightKg = (grossWeightKg - totalDeductionKg).coerceAtLeast(0.0)
        val netBillableMaund = netBillableWeightKg / 40.0
        val grossPayablePkr = input.grossWeightMaund * input.quotedPriceMaund
        val netPayablePkr = netBillableMaund * input.quotedPriceMaund
        val effectivePricePerMaundPkr = if (netBillableMaund > 0) grossPayablePkr / netBillableMaund else input.quotedPriceMaund
        val totalDeductionSavingsPkr = grossPayablePkr - netPayablePkr

        return MoistureDeductionResult(
            grossWeightKg = grossWeightKg,
            moistureCutKg = moistureCutKg,
            foreignMatterCutKg = foreignMatterCutKg,
            bagTareCutKg = bagTareCutKg,
            totalDeductionKg = totalDeductionKg,
            netBillableWeightKg = netBillableWeightKg,
            netBillableMaund = netBillableMaund,
            grossPayablePkr = grossPayablePkr,
            netPayablePkr = netPayablePkr,
            effectivePricePerMaundPkr = effectivePricePerMaundPkr,
            totalDeductionSavingsPkr = totalDeductionSavingsPkr
        )
    }

    // NEW: Punjab Mandi Freight & Distance Directory
    val mandiFreightList = listOf(
        MandiFreightItem("Samundri (Local Mill)", 0, 0.0, "Local Mill Hub"),
        MandiFreightItem("Gojra Mandi", 28, 45.0, "1 hr"),
        MandiFreightItem("Faisalabad Ghalla Mandi", 45, 65.0, "1.5 hrs"),
        MandiFreightItem("Toba Tek Singh", 48, 70.0, "1.5 hrs"),
        MandiFreightItem("Jhang Mandi", 75, 95.0, "2 hrs"),
        MandiFreightItem("Chiniot Mandi", 82, 105.0, "2.5 hrs"),
        MandiFreightItem("Sahiwal Grain Market", 110, 130.0, "3 hrs"),
        MandiFreightItem("Lahore Badami Bagh", 175, 180.0, "4.5 hrs"),
        MandiFreightItem("Multan Ghalla Mandi", 185, 190.0, "5 hrs"),
        MandiFreightItem("Bahawalpur Mandi", 280, 260.0, "7 hrs")
    )

    // NEW: WhatsApp Professional Rate Card Generator
    fun generateWhatsAppRateCardText(lang: String): String {
        val today=OffsetDateTime.now(ZoneId.of("Asia/Karachi")).toLocalDate()
        val reviewed=_rates.value.filter{!it.isSample&&Instant.ofEpochSecond(it.observedAt).atZone(ZoneId.of("Asia/Karachi")).toLocalDate()==today}
        val heading=if(lang=="ur")"ملک آئل · مالک کے جائزہ شدہ نرخ" else "Malik Oil · Owner-reviewed rates"
        if(reviewed.isEmpty())return "$heading · $today\nNo reviewed current-day rates available."
        return "$heading · $today\n"+reviewed.groupBy{it.stream}.values.map{it.maxBy{row->row.observedAt}}.joinToString("\n"){row->"${row.commodity} · ${row.market} · ${row.grade}: PKR ${row.pricePkrMaund}/40kg · ${row.source} · ${Instant.ofEpochSecond(row.observedAt)}"}
    }

    // Curated High-Integrity Punjab Oilseed Market News with Search Grounding Provenance
    fun getCuratedPunjabNews(): List<MarketNewsArticle> {
        if(!includeSamples)return emptyList()
        val defaultQuery = "Punjab oilseed mustard canola mandi market news"
        return listOf(
            MarketNewsArticle(
                id = "news_pamra_moisture",
                title = "PAMRA Reaffirms 8% Standard Moisture Baseline & Strict Bag Tare Cut in Central Punjab",
                summary = "Punjab Agriculture Marketing Regulatory Authority (PAMRA) has issued an official advisory directing mandi committees in Faisalabad, Samundri, Gojra, and Jhang to enforce standard 8% moisture cut formulas and cap empty PP/jute bag tare at 1.0 kg.",
                sourceName = "PAMRA Regulatory Bulletin",
                category = "policy",
                publishedAt = "Today · 09:30 AM",
                url = "https://pamra.punjab.gov.pk/market-updates",
                impactOnPrices = "Neutral (Fair Dockage)",
                mandiTag = "Faisalabad & Samundri",
                keyTakeaways = listOf(
                    "Standard moisture dockage capped at 1:1 ratio over 8%",
                    "Tare weight fixed at 1.0 kg per 40kg bag",
                    "Weighbridge and dockage inspections active at Samundri grain market"
                ),
                isSearchGrounded = true,
                searchGroundingQuery = defaultQuery,
                groundedSources = listOf("PAMRA Regulatory Bulletin", "Punjab Agri Marketing Board", "Directorate of Agri Punjab"),
                groundingScore = 0.99
            ),
            MarketNewsArticle(
                id = "news_expeller_crush",
                title = "Expeller Mills Boost Crushing Utilization to 84% on Strong Khali (Cake) Demand",
                summary = "Commercial expeller mills and oil extraction plants in Samundri, Gojra, and Tandlianwala report healthy crushing margins. Steady buying from cattle and dairy feed operators in Sahiwal has lifted mustard cake (khali) quotes to PKR 3,400 per 40kg bag.",
                sourceName = "Pakistan Edible Oil Digest",
                category = "crushing",
                publishedAt = "Today · 11:15 AM",
                url = "https://edibleoils.pk/expeller-demand",
                impactOnPrices = "Bullish (+2.5%)",
                mandiTag = "Samundri & Gojra",
                keyTakeaways = listOf(
                    "Khali trading firm at PKR 3,350 - 3,450 per 40kg bag",
                    "Pure mustard oil wholesale demand up 12% in suburban retail",
                    "Crushing gross margin estimated at PKR 340-420 per maund"
                ),
                isSearchGrounded = true,
                searchGroundingQuery = defaultQuery,
                groundedSources = listOf("Pakistan Edible Oil Digest", "Sahiwal Dairy Feed Union", "Faisalabad Millers Assoc."),
                groundingScore = 0.97
            ),
            MarketNewsArticle(
                id = "news_podb_canola",
                title = "PODB Deploys Mobile Oil-Testing Labs for Canola & Mustard Auctions in Punjab",
                summary = "The Pakistan Oilseed Development Board (PODB) has deployed mobile NIR testing vans across Faisalabad and Multan divisions. Farmers and millers can now get immediate, certified oil-content readouts before closing bids.",
                sourceName = "PODB Agri Intelligence",
                category = "canola",
                publishedAt = "Yesterday · 04:45 PM",
                url = "https://podb.gov.pk/punjab-oilseed-support",
                impactOnPrices = "Bullish (+1.8%)",
                mandiTag = "Punjab Wide",
                keyTakeaways = listOf(
                    "Canola oil content averaging 41-43% in irrigated tracts",
                    "Certified oil recovery lots fetching PKR 150-200/maund premium",
                    "Seed cleaning subsidies available for cooperative grain markets"
                ),
                isSearchGrounded = true,
                searchGroundingQuery = defaultQuery,
                groundedSources = listOf("Pakistan Oilseed Development Board", "Directorate of Crop Reporting", "National Agri Research"),
                groundingScore = 0.96
            ),
            MarketNewsArticle(
                id = "news_edible_parity",
                title = "Firm Imported Palm Olein Parity Widens Advantage for Local Mustard Oil Extraction",
                summary = "Higher international shipping tariffs and Malaysian palm olein prices have created favorable retail economics for locally pressed mustard and canola cooking oils. Local millers in central Punjab maintain active seed bidding above PKR 8,500/maund.",
                sourceName = "Business Recorder AgriWatch",
                category = "crushing",
                publishedAt = "2 days ago",
                url = "https://brecorder.com/markets/agri-commodities",
                impactOnPrices = "Bullish (+3.0%)",
                mandiTag = "Lahore & Faisalabad",
                keyTakeaways = listOf(
                    "Domestic mustard oil maintains competitive edge against commercial ghee",
                    "Millers holding steady bid prices above PKR 8,500/maund",
                    "Secondary expeller inquiries active from Sheikhupura and Gujranwala"
                ),
                isSearchGrounded = true,
                searchGroundingQuery = defaultQuery,
                groundedSources = listOf("Business Recorder AgriWatch", "Karachi Commodity Exchange", "Dawn Business"),
                groundingScore = 0.95
            ),
            MarketNewsArticle(
                id = "news_sesame_export",
                title = "White Sesame (Til) Inquiries Strong Across Multan and Bahawalpur Mandis",
                summary = "Sesame trade turnover remained vigorous across southern Punjab grain hubs as commercial exporters compete for top-grade, cleaned seed lots destined for China and Middle Eastern markets.",
                sourceName = "Grain Union Weekly",
                category = "sarson",
                publishedAt = "3 days ago",
                url = "https://agriwatch.pk/sesame-exports",
                impactOnPrices = "Bullish (+4.2%)",
                mandiTag = "Multan & Bahawalpur",
                keyTakeaways = listOf(
                    "A-Grade Til commanding PKR 18,000 - 18,500 per 40kg",
                    "High purity (>99%) lots prioritized for port shipment",
                    "Village-level stock holding remains low with prompt off-take"
                ),
                isSearchGrounded = true,
                searchGroundingQuery = defaultQuery,
                groundedSources = listOf("Grain Union Weekly", "AgriWatch Pakistan", "Multan Mandi Committee"),
                groundingScore = 0.94
            ),
            MarketNewsArticle(
                id = "news_canola_crop_health",
                title = "Faisalabad Division Canola Crop Shows Favorable Pod Density & Timely Maturation",
                summary = "Field inspections by Punjab Crop Reporting Services confirm healthy pod density with minimal pest stress across Faisalabad, Jhang, and Toba Tek Singh. Harvest supplies are projected to enter mandis in smooth staggered volumes.",
                sourceName = "Crop Reporting Service Punjab",
                category = "canola",
                publishedAt = "4 days ago",
                url = "https://crs.agripunjab.gov.pk/bulletins",
                impactOnPrices = "Neutral (Steady)",
                mandiTag = "Toba Tek Singh & Jhang",
                keyTakeaways = listOf(
                    "No significant aphid (susti) infestation reported in key belts",
                    "Crop maturity timely across canal-irrigated zones",
                    "Initial moisture levels projected between 9-11%"
                ),
                isSearchGrounded = true,
                searchGroundingQuery = defaultQuery,
                groundedSources = listOf("Crop Reporting Service Punjab", "Agriculture Dept Lahore", "PAMRA Field Unit"),
                groundingScore = 0.96
            )
        )
    }

    // Live Search Grounding Tool Fetcher with Graceful Fallback
    suspend fun fetchPunjabOilseedNews(apiKey:String?=null,forceRefresh:Boolean=false):List<MarketNewsArticle>{
        _isNewsLoading.value=true
        return try {
            val refs=com.malikoil.intelligence.PublicRateResearch.fetch("sarson","Samundri")
            val articles=refs.map {ref->MarketNewsArticle(id=ref.url,title=ref.title,summary="Public headline. Current mandi rate unavailable until verified.",sourceName=ref.publisher,category="sarson",publishedAt=ref.publishedAt,url=ref.url,isLiveApi=true,isSearchGrounded=false,groundedSources=listOf(ref.publisher),groundingScore=0.0)}
            _newsArticles.value=articles
            _searchGroundingStatus.value=SearchGroundingStatus(false,"Punjab sarson mandi rates","Public RSS headlines",0,Instant.now().toString())
            articles
        }finally{_isNewsLoading.value=false}
    }
}

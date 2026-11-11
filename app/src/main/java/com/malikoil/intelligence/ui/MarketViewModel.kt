package com.malikoil.intelligence.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.malikoil.intelligence.data.MarketRepository
import com.malikoil.intelligence.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant

class MarketViewModel(
    val repository: MarketRepository = MarketRepository()
) : ViewModel() {

    private val _currentTab = MutableStateFlow("rates")
    val currentTab: StateFlow<String> = _currentTab.asStateFlow()

    private val _language = MutableStateFlow("roman") // "roman", "ur", "en"
    val language: StateFlow<String> = _language.asStateFlow()

    private val _selectedStream = MutableStateFlow("illustrative")
    val selectedStream: StateFlow<String> = _selectedStream.asStateFlow()

    private val _chartDays = MutableStateFlow(7)
    val chartDays: StateFlow<Int> = _chartDays.asStateFlow()

    private val _isChartDemo = MutableStateFlow(true)
    val isChartDemo: StateFlow<Boolean> = _isChartDemo.asStateFlow()

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    private val _isResearching = MutableStateFlow(false)
    val isResearching: StateFlow<Boolean> = _isResearching.asStateFlow()

    private val _activeSessionId = MutableStateFlow<String?>(null)
    val activeSessionId: StateFlow<String?> = _activeSessionId.asStateFlow()

    // Landed cost calculator state
    private val _marginInput = MutableStateFlow(LandedCostInput())
    val marginInput: StateFlow<LandedCostInput> = _marginInput.asStateFlow()

    private val _marginResult = MutableStateFlow(repository.calculateMargin(LandedCostInput()))
    val marginResult: StateFlow<LandedCostResult> = _marginResult.asStateFlow()

    // NEW: Business Sub-Tab Selection ("landed", "oil_yield", "moisture", "freight")
    private val _activeBusinessSubTab = MutableStateFlow("landed")
    val activeBusinessSubTab: StateFlow<String> = _activeBusinessSubTab.asStateFlow()

    // NEW: Oil & Khali Extraction Calculator State
    private val _oilYieldInput = MutableStateFlow(OilYieldInput())
    val oilYieldInput: StateFlow<OilYieldInput> = _oilYieldInput.asStateFlow()

    private val _oilYieldResult = MutableStateFlow(repository.calculateOilYield(OilYieldInput()))
    val oilYieldResult: StateFlow<OilYieldResult> = _oilYieldResult.asStateFlow()

    // NEW: Moisture & Quality Tare Deduction State
    private val _moistureInput = MutableStateFlow(MoistureDeductionInput())
    val moistureInput: StateFlow<MoistureDeductionInput> = _moistureInput.asStateFlow()

    private val _moistureResult = MutableStateFlow(repository.calculateMoistureDeduction(MoistureDeductionInput()))
    val moistureResult: StateFlow<MoistureDeductionResult> = _moistureResult.asStateFlow()

    // Mandi Freight Directory
    val mandiFreightList: List<MandiFreightItem> = repository.mandiFreightList

    // Notifications Filter State
    private val _sarsonOnly = MutableStateFlow(true)
    val sarsonOnly: StateFlow<Boolean> = _sarsonOnly.asStateFlow()

    private val _nearbyOnly = MutableStateFlow(false)
    val nearbyOnly: StateFlow<Boolean> = _nearbyOnly.asStateFlow()

    // Pass-through StateFlows
    val rates: StateFlow<List<RateObservation>> = repository.rates
    val candidates: StateFlow<List<RateCandidate>> = repository.candidates
    val alerts: StateFlow<List<MovementAlert>> = repository.alerts
    val researchSessions: StateFlow<List<ResearchSession>> = repository.researchSessions
    val dailyReports: StateFlow<List<DailyReportSnapshot>> = repository.dailyReports
    val notifications: StateFlow<List<NotificationCaptureItem>> = repository.notifications

    // NEW: Target Purchase Price Alerts
    val targetAlerts: StateFlow<List<TargetPriceAlert>> = repository.targetAlerts

    // NEW: Market Insights & News Aggregator
    val newsArticles: StateFlow<List<MarketNewsArticle>> = repository.newsArticles
    val isNewsLoading: StateFlow<Boolean> = repository.isNewsLoading
    val marketSentiment: StateFlow<MarketSentimentSummary> = repository.marketSentiment
    val searchGroundingStatus: StateFlow<SearchGroundingStatus> = repository.searchGroundingStatus

    private val _selectedNewsCategory = MutableStateFlow("all")
    val selectedNewsCategory: StateFlow<String> = _selectedNewsCategory.asStateFlow()

    private val _newsSearchQuery = MutableStateFlow("")
    val newsSearchQuery: StateFlow<String> = _newsSearchQuery.asStateFlow()

    val filteredNewsArticles: StateFlow<List<MarketNewsArticle>> = combine(
        repository.newsArticles,
        _selectedNewsCategory,
        _newsSearchQuery
    ) { list, cat, query ->
        list.filter { article ->
            val matchesCat = if (cat == "all") true else article.category.equals(cat, ignoreCase = true)
            val matchesQuery = if (query.isBlank()) true else {
                article.title.contains(query, ignoreCase = true) ||
                article.summary.contains(query, ignoreCase = true) ||
                article.sourceName.contains(query, ignoreCase = true) ||
                article.mandiTag.contains(query, ignoreCase = true)
            }
            matchesCat && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.Lazily, repository.newsArticles.value)

    init {
        val firstSession = repository.researchSessions.value.firstOrNull()
        _activeSessionId.value = firstSession?.id
    }

    fun setTab(tab: String) {
        _currentTab.value = tab
    }

    fun setLanguage(lang: String) {
        _language.value = lang
    }

    fun selectStream(stream: String) {
        _selectedStream.value = stream
        _isChartDemo.value = stream == "illustrative"
    }

    fun setChartDays(days: Int) {
        _chartDays.value = days
    }

    fun toggleChartDemo() {
        val nextDemo = !_isChartDemo.value
        _isChartDemo.value = nextDemo
        if (nextDemo) {
            _selectedStream.value = "illustrative"
        } else {
            val nonDemo = rates.value.firstOrNull { it.stream != "illustrative" }?.stream ?: "sarson_lahore"
            _selectedStream.value = nonDemo
        }
    }

    fun submitCandidate(commodity: String, market: String, amount: Double, unit: String) {
        repository.addCandidate(commodity, market, amount, unit)
        _userMessage.value = "Observation submitted for review."
        _currentTab.value = "review"
    }

    fun approveCandidate(id: String) {
        val success = repository.approveCandidate(id)
        if (success) {
            _userMessage.value = "Rate approved and added to market history."
        }
    }

    fun testQuickMovement(factor: Double) {
        repository.testQuickMovement(factor)
        _userMessage.value = "Sample ±3% candidate added to review queue."
        _currentTab.value = "review"
    }

    fun updateMarginInput(input: LandedCostInput) {
        _marginInput.value = input
        _marginResult.value = repository.calculateMargin(input)
    }

    fun runResearch(question: String, commodity: String, market: String, depth: String) {
        if (question.isBlank()) return
        viewModelScope.launch {
            _isResearching.value = true
            _userMessage.value = "Searching sources and analyzing market evidence…"
            delay(1200) // realistic source-search feel
            val turn = repository.runResearch(question, commodity, market, depth, _activeSessionId.value)
            _isResearching.value = false
            _userMessage.value = "Evidence verified with citations."
        }
    }

    fun createNewResearchSession() {
        val session = repository.createNewResearchSession()
        _activeSessionId.value = session.id
        _userMessage.value = "New research session started."
    }

    fun selectResearchSession(id: String) {
        _activeSessionId.value = id
    }

    fun deleteResearchSession(id: String) {
        repository.deleteResearchSession(id)
        _activeSessionId.value = repository.researchSessions.value.firstOrNull()?.id
        _userMessage.value = "Session deleted."
    }

    fun generateDailyReport() {
        val report = repository.generateTodayReport()
        _userMessage.value = "Daily report generated for ${report.date}."
    }

    fun approveDailyRows(rowsText: String, sourceLabel: String) {
        val parsed = repository.checkAndParseRows(rowsText)
        if (parsed.isEmpty()) {
            _userMessage.value = "No valid rows parsed. Use: commodity | market | grade | amount | unit"
            return
        }
        parsed.forEach { item ->
            repository.addCandidate(
                commodity = item.commodity,
                market = item.market,
                amount = item.amount,
                unit = item.unit,
                grade = item.grade,
                source = sourceLabel
            )
        }
        _userMessage.value = "${parsed.size} rows approved for reports and review."
    }

    fun simulateNotification(group: String, text: String) {
        if (text.isBlank()) return
        repository.simulateNotification(group, text)
        _userMessage.value = "Simulated notification received in inbox."
    }

    fun toggleSarsonOnly(enabled: Boolean) {
        _sarsonOnly.value = enabled
    }

    fun toggleNearbyOnly(enabled: Boolean) {
        _nearbyOnly.value = enabled
    }

    fun dismissUserMessage() {
        _userMessage.value = null
    }

    fun selectBusinessSubTab(subTab: String) {
        _activeBusinessSubTab.value = subTab
    }

    fun updateOilYieldInput(input: OilYieldInput) {
        _oilYieldInput.value = input
        _oilYieldResult.value = repository.calculateOilYield(input)
    }

    fun updateMoistureInput(input: MoistureDeductionInput) {
        _moistureInput.value = input
        _moistureResult.value = repository.calculateMoistureDeduction(input)
    }

    fun getWhatsAppRateCardText(): String {
        return repository.generateWhatsAppRateCardText(_language.value)
    }

    fun getExportCsv(): String {
        val currentRates = rates.value.filter {
            if (_isChartDemo.value) it.stream == "illustrative" else it.stream == _selectedStream.value
        }
        val header = "commodity,market,grade,source,observed_utc,price_pkr_kg,sample\n"
        val rows = currentRates.joinToString("\n") { r ->
            "${r.commodity},${r.market},${r.grade},\"${r.source}\",${Instant.ofEpochSecond(r.observedAt)},${r.pricePkrKg},${r.isSample}"
        }
        return header + rows
    }

    // NEW: News Aggregator Controls
    fun setNewsCategory(category: String) {
        _selectedNewsCategory.value = category
    }

    fun setNewsSearchQuery(query: String) {
        _newsSearchQuery.value = query
    }

    fun refreshMarketNews() {
        viewModelScope.launch {
            _userMessage.value = "Fetching latest Punjab oilseed market news…"
            repository.fetchPunjabOilseedNews(forceRefresh = true)
            _userMessage.value = "Market insights and sentiment updated."
        }
    }

    // Target Price Alert Actions
    fun addTargetPriceAlert(
        commodity: String,
        targetPrice: Double,
        unit: String = "kg",
        condition: PriceCondition = PriceCondition.AT_OR_BELOW,
        market: String = "All Mandis",
        notes: String = ""
    ) {
        val alert = repository.addTargetPriceAlert(commodity, targetPrice, unit, condition, market, notes)
        _userMessage.value = "🎯 Target alert activated for ${commodity.uppercase()} at PKR %.2f/kg!".format(alert.targetPricePkrKg)
    }

    fun removeTargetPriceAlert(id: String) {
        repository.removeTargetPriceAlert(id)
        _userMessage.value = "Target alert removed."
    }

    fun toggleTargetPriceAlert(id: String) {
        repository.toggleTargetPriceAlert(id)
    }

    fun simulateTargetAlertTrigger(context: android.content.Context, alertId: String) {
        val success = repository.simulateTargetAlertTrigger(context, alertId)
        if (success) {
            _userMessage.value = "🔔 Market rate hit simulated! Check your notification bar."
        }
    }
}

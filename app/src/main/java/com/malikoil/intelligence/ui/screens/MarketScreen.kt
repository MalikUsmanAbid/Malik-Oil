package com.malikoil.intelligence.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.malikoil.intelligence.model.RateObservation
import com.malikoil.intelligence.ui.MarketViewModel
import com.malikoil.intelligence.ui.components.MarketNewsFeedSection
import com.malikoil.intelligence.ui.components.PriceChartCard
import com.malikoil.intelligence.ui.components.SarsonCanolaTrendChartCard
import com.malikoil.intelligence.ui.components.Translations
import com.malikoil.intelligence.ui.theme.*
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketScreen(
    viewModel: MarketViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val language by viewModel.language.collectAsState()
    val rates by viewModel.rates.collectAsState()
    val selectedStream by viewModel.selectedStream.collectAsState()
    val chartDays by viewModel.chartDays.collectAsState()
    val isChartDemo by viewModel.isChartDemo.collectAsState()
    val alerts by viewModel.alerts.collectAsState()
    val candidates by viewModel.candidates.collectAsState()

    // Form inputs
    var addCommodity by remember { mutableStateOf("sarson") }
    var addMarket by remember { mutableStateOf("Samundri") }
    var addAmount by remember { mutableStateOf("8640") }
    var addUnit by remember { mutableStateOf("40kg") }
    var showObservationsTable by remember { mutableStateOf(false) }

    val filteredRates = remember(rates, selectedStream, isChartDemo, chartDays) {
        val streamRates = if (isChartDemo) {
            rates.filter { it.stream == "illustrative" }
        } else {
            rates.filter { it.stream == selectedStream }
        }
        val cutoff = Instant.now().epochSecond - chartDays * 86400L
        streamRates.filter { it.observedAt >= cutoff }.sortedBy { it.observedAt }
    }

    // 7-Day Sarson and Canola Trend Rates
    val sarsonTrendRates = remember(rates, chartDays, isChartDemo) {
        val cutoff = Instant.now().epochSecond - chartDays * 86400L
        if (isChartDemo) {
            rates.filter { it.stream == "illustrative" && it.observedAt >= cutoff }
                .ifEmpty { rates.filter { it.stream == "illustrative" } }
                .sortedBy { it.observedAt }
        } else {
            rates.filter { it.commodity == "sarson" && it.observedAt >= cutoff && !it.isSample }
                .ifEmpty { rates.filter { it.commodity == "sarson" && !it.isSample } }
                .sortedBy { it.observedAt }
        }
    }

    val canolaTrendRates = remember(rates, chartDays) {
        val cutoff = Instant.now().epochSecond - chartDays * 86400L
        rates.filter { it.commodity == "canola" && it.observedAt >= cutoff }
            .ifEmpty { rates.filter { it.commodity == "canola" } }
            .sortedBy { it.observedAt }
    }

    val prices = remember(filteredRates) { filteredRates.map { it.pricePkrKg } }
    val lastPrice = prices.lastOrNull()
    val rangeDelta = remember(prices) {
        if (prices.size >= 2) {
            val first = prices.first()
            if (first > 0) ((prices.last() - first) * 100.0 / first) else 0.0
        } else 0.0
    }
    val highPrice = prices.maxOrNull()
    val lowPrice = prices.minOrNull()

    // Unique streams for selector
    val availableStreams = remember(rates) {
        rates.map { it.stream }.distinct().filter { it != "illustrative" }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SandBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Executive Notice & Live Mandi Pulse Card
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Surface(
                color = PanelBackground,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, Color(0xFFE4DFD3)),
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth().testTag("market_notice_banner")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                color = GoldAccent,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = Translations.t("demo_badge", language),
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                )
                            }
                            Text(
                                text = "SAMUNDRI MILL WORKSPACE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = ForestGreenDark,
                                letterSpacing = 0.5.sp
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = PriceUpLight,
                            border = BorderStroke(1.dp, ForestGreen.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Surface(shape = androidx.compose.foundation.shape.CircleShape, color = ForestGreen, modifier = Modifier.size(6.dp)) {}
                                Text("±3% Policy Active", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ForestGreenDark)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = Translations.t("preview_notice", language),
                        color = TextInk,
                        fontSize = 12.sp,
                        lineHeight = 17.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            }
        }

        // 2. Watchlist Header & Live Rates Carousel Card
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = Translations.t("watchlist_title", language),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ForestGreen,
                            letterSpacing = 0.8.sp
                        )
                        Text(
                            text = Translations.t("overview_heading", language),
                            fontSize = 19.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextInk
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = RaisedBackground,
                        border = BorderStroke(1.dp, LineBorder)
                    ) {
                        Text(
                            text = "LIVE TICKER",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MutedInk,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                // Watchlist Cards (Horizontal Scroll with High-Fidelity Ticker Design)
                val watchlistStreams = listOf("illustrative") + availableStreams.take(3)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("watchlist_row")
                ) {
                    items(watchlistStreams) { stream ->
                        val streamObs = rates.filter { it.stream == stream }.sortedBy { it.observedAt }
                        val currentObs = streamObs.lastOrNull()
                        val firstObs = streamObs.firstOrNull()
                        val delta = if (currentObs != null && firstObs != null && firstObs.pricePkrKg > 0) {
                            (currentObs.pricePkrKg - firstObs.pricePkrKg) * 100.0 / firstObs.pricePkrKg
                        } else 0.0

                        val isSelected = (isChartDemo && stream == "illustrative") || (!isChartDemo && selectedStream == stream)

                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) Color(0xFFF2F8F4) else PanelBackground,
                            border = BorderStroke(
                                if (isSelected) 2.dp else 1.dp,
                                if (isSelected) ForestGreen else Color(0xFFE2DDD1)
                            ),
                            shadowElevation = if (isSelected) 3.dp else 1.5.dp,
                            modifier = Modifier
                                .width(205.dp)
                                .clickable {
                                    if (stream == "illustrative") {
                                        viewModel.selectStream("illustrative")
                                    } else {
                                        viewModel.selectStream(stream)
                                    }
                                }
                                .testTag("watch_card_$stream")
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val commName = currentObs?.commodity?.replaceFirstChar { c -> c.uppercase() } ?: "Sarson"
                                    Text(
                                        text = commName,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextInk
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (isSelected) ForestGreen else RaisedBackground
                                    ) {
                                        Text(
                                            text = currentObs?.market ?: "Samundri",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (isSelected) Color.White else MutedInk,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Large Primary Rate (PKR/kg)
                                Row(verticalAlignment = Alignment.Bottom) {
                                    Text(
                                        text = currentObs?.let { "PKR %.2f".format(it.pricePkrKg) } ?: "—",
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Black,
                                        color = TextInk,
                                        letterSpacing = (-0.5).sp
                                    )
                                    Text(
                                        text = "/kg",
                                        fontSize = 12.sp,
                                        color = MutedInk,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(start = 3.dp, bottom = 3.dp)
                                    )
                                }

                                // 40kg Maund Equivalent (Prominent for Mill Owners)
                                Text(
                                    text = currentObs?.let { "PKR %.0f / 40kg (من)".format(it.pricePkrMaund) } ?: "—",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ForestGreen
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                // Delta Pill
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (delta >= 0) PriceUpLight else PriceDownLight
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = if (delta >= 0) "▲" else "▼",
                                            fontSize = 9.sp,
                                            color = if (delta >= 0) PriceUp else PriceDown
                                        )
                                        Text(
                                            text = "%+.2f%% · %s".format(delta, if (stream == "illustrative") "Demo" else currentObs?.source ?: "Mandi"),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (delta >= 0) PriceUp else PriceDown
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Quick Owner Action Tiles (Alerts & Pending Review)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Alerts Action Tile
                Surface(
                    color = PriceUpLight,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFFC7DAB3)),
                    shadowElevation = 2.dp,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.setTab("alerts") }
                        .testTag("quick_alerts_tile")
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                Icon(Icons.Default.Notifications, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(17.dp))
                                Text(
                                    text = Translations.t("alerts", language),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ForestGreenDark
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = Translations.t("alerts_quick", language),
                                fontSize = 11.sp,
                                color = MutedInk
                            )
                        }
                        Surface(
                            shape = androidx.compose.foundation.shape.CircleShape,
                            color = ForestGreen,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = alerts.size.toString(),
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                // Review Action Tile
                Surface(
                    color = PriceDownLight,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFFE5D5B3)),
                    shadowElevation = 2.dp,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { viewModel.setTab("review") }
                        .testTag("quick_review_tile")
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                Icon(Icons.Default.Rule, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(17.dp))
                                Text(
                                    text = Translations.t("pending_review", language),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF725019)
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = Translations.t("owner_approval", language),
                                fontSize = 11.sp,
                                color = MutedInk
                            )
                        }
                        Surface(
                            shape = androidx.compose.foundation.shape.CircleShape,
                            color = GoldAccent,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = candidates.size.toString(),
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4. WhatsApp One-Click Rate Card Broadcast Hero CTA Card
        item {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color(0xFF1E7E34),
                shadowElevation = 4.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val rateCardText = viewModel.getWhatsAppRateCardText()
                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, rateCardText)
                            type = "text/plain"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, "Share Malik Oil Rate Card")
                        context.startActivity(shareIntent)
                    }
                    .testTag("btn_share_rate_card")
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            androidx.compose.ui.graphics.Brush.horizontalGradient(
                                listOf(Color(0xFF20BF6B), Color(0xFF0F6E3B))
                            )
                        )
                        .padding(horizontal = 18.dp, vertical = 15.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Surface(
                                shape = androidx.compose.foundation.shape.CircleShape,
                                color = Color.White,
                                modifier = Modifier.size(44.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = "Share",
                                        tint = Color(0xFF0F6E3B),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            Column {
                                Text(
                                    text = Translations.t("rate_card_share", language),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                                Text(
                                    text = Translations.t("one_click_broadcast", language),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFFE2F9EB)
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }

        // 5. Quick Expeller & Mandi Tools Section
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = Translations.t("quick_tools_title", language),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MutedInk,
                    letterSpacing = 0.8.sp
                )

                // Quick Tool Shortcut Buttons (4-Item Horizontal Row)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth().testTag("quick_tools_shortcut_row")
                ) {
                    // 1. Market Insights (News)
                    item {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = PriceUpLight,
                            border = BorderStroke(1.dp, ForestGreen.copy(alpha = 0.5f)),
                            shadowElevation = 1.dp,
                            modifier = Modifier
                                .clickable { viewModel.setTab("insights") }
                                .testTag("btn_quick_market_insights")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(shape = androidx.compose.foundation.shape.CircleShape, color = ForestGreen, modifier = Modifier.size(28.dp)) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.Newspaper, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                                    }
                                }
                                Text(
                                    text = Translations.t("insights", language),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ForestGreenDark
                                )
                            }
                        }
                    }

                    // 2. Oil & Khali Yield Calculator
                    item {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = PanelBackground,
                            border = BorderStroke(1.dp, Color(0xFFE2DDD1)),
                            shadowElevation = 1.dp,
                            modifier = Modifier
                                .clickable {
                                    viewModel.selectBusinessSubTab("oil_yield")
                                    viewModel.setTab("business")
                                }
                                .testTag("btn_quick_oil_yield")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(shape = androidx.compose.foundation.shape.CircleShape, color = GoldAccent, modifier = Modifier.size(28.dp)) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.Calculate, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                                    }
                                }
                                Text(
                                    text = Translations.t("oil_khali_calculator", language),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextInk
                                )
                            }
                        }
                    }

                    // 3. Moisture & Tare Cut Calculator
                    item {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = PanelBackground,
                            border = BorderStroke(1.dp, Color(0xFFE2DDD1)),
                            shadowElevation = 1.dp,
                            modifier = Modifier
                                .clickable {
                                    viewModel.selectBusinessSubTab("moisture")
                                    viewModel.setTab("business")
                                }
                                .testTag("btn_quick_moisture")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(shape = androidx.compose.foundation.shape.CircleShape, color = Color(0xFF1E88E5), modifier = Modifier.size(28.dp)) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.WaterDrop, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                                    }
                                }
                                Text(
                                    text = Translations.t("moisture_calculator", language),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextInk
                                )
                            }
                        }
                    }

                    // 4. Mandi Freight & Distance Matrix
                    item {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = PanelBackground,
                            border = BorderStroke(1.dp, Color(0xFFE2DDD1)),
                            shadowElevation = 1.dp,
                            modifier = Modifier
                                .clickable {
                                    viewModel.selectBusinessSubTab("freight")
                                    viewModel.setTab("business")
                                }
                                .testTag("btn_quick_freight")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(shape = androidx.compose.foundation.shape.CircleShape, color = Color(0xFF6D4C41), modifier = Modifier.size(28.dp)) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.LocalShipping, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                                    }
                                }
                                Text(
                                    text = Translations.t("freight_matrix", language),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextInk
                                )
                            }
                        }
                    }
                }
            }
        }

        // 6. Market News Feed Section with Search Grounding Tool
        item {
            MarketNewsFeedSection(
                viewModel = viewModel,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // 7. 7-Day Price Trend Line Chart for Sarson & Canola Seeds (Compose Canvas)
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Compose Canvas Line Chart
                SarsonCanolaTrendChartCard(
                    sarsonObservations = sarsonTrendRates,
                    canolaObservations = canolaTrendRates,
                    language = language,
                    chartDays = chartDays,
                    onPeriodSelected = { viewModel.setChartDays(it) },
                    modifier = Modifier.fillMaxWidth()
                )

                // Chart Action Buttons (Export CSV & Toggle Demo)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            val csv = viewModel.getExportCsv()
                            Toast.makeText(context, "Observations exported (${filteredRates.size} rows).", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PanelBackground, contentColor = TextInk),
                        border = BorderStroke(1.dp, Color(0xFFE2DDD1)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("export_csv_button")
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp), tint = ForestGreen)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = Translations.t("export_csv", language), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = { viewModel.toggleChartDemo() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isChartDemo) PriceDownLight else PanelBackground,
                            contentColor = TextInk
                        ),
                        border = BorderStroke(1.dp, if (isChartDemo) PriceDown else Color(0xFFE2DDD1)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("toggle_demo_button")
                    ) {
                        Text(
                            text = Translations.t("toggle_demo", language),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Observations Data Table Toggle Card
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = PanelBackground,
                    border = BorderStroke(1.dp, Color(0xFFE2DDD1)),
                    shadowElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showObservationsTable = !showObservationsTable }
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(
                                    imageVector = if (showObservationsTable) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = null,
                                    tint = ForestGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = Translations.t("exact_observations", language),
                                    color = TextInk,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "${(sarsonTrendRates + canolaTrendRates).size} rows",
                                color = MutedInk,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        if (showObservationsTable) {
                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(color = LineBorder.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(8.dp))

                            (sarsonTrendRates + canolaTrendRates)
                                .sortedByDescending { it.observedAt }
                                .take(14)
                                .forEach { r ->
                                    val dateFmt = Instant.ofEpochSecond(r.observedAt)
                                        .atZone(ZoneId.systemDefault())
                                        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 6.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "${r.commodity.uppercase()} · ${r.market}",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = TextInk
                                            )
                                            Text(text = dateFmt, fontSize = 10.sp, color = MutedInk)
                                        }
                                        Text(
                                            text = "PKR %.2f/kg".format(r.pricePkrKg),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ForestGreenDark
                                        )
                                    }
                                    HorizontalDivider(color = LineBorder.copy(alpha = 0.3f))
                                }
                        }
                    }
                }
            }
        }

        // 8. Add Observation & 3% Alert Test Card
        item {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = PanelBackground,
                border = BorderStroke(1.dp, Color(0xFFE2DDD1)),
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth().testTag("add_sample_drawer")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Icon(Icons.Default.AddCircle, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(18.dp))
                        Text(
                            text = Translations.t("add_sample_rate", language),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextInk
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                    // Commodity dropdown + Mandi input
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = addCommodity,
                            onValueChange = { addCommodity = it },
                            label = { Text(Translations.t("commodity", language)) },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).testTag("input_add_commodity")
                        )
                        OutlinedTextField(
                            value = addMarket,
                            onValueChange = { addMarket = it },
                            label = { Text(Translations.t("mandi", language)) },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).testTag("input_add_market")
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Amount + Unit
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = addAmount,
                            onValueChange = { addAmount = it },
                            label = { Text(Translations.t("amount_pkr", language)) },
                            shape = RoundedCornerShape(10.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f).testTag("input_add_amount")
                        )
                        OutlinedTextField(
                            value = addUnit,
                            onValueChange = { addUnit = it },
                            label = { Text(Translations.t("quoted_unit", language)) },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).testTag("input_add_unit")
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            val amt = addAmount.toDoubleOrNull() ?: 8000.0
                            viewModel.submitCandidate(addCommodity, addMarket, amt, addUnit)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("btn_submit_review")
                    ) {
                        Text(Translations.t("submit_review", language), color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = LineBorder.copy(alpha = 0.6f))
                    Spacer(modifier = Modifier.height(16.dp))

                    // Quick ±3% Movement Test
                    Text(
                        text = Translations.t("quick_test", language),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextInk
                    )
                    Text(
                        text = Translations.t("quick_test_sub", language),
                        fontSize = 12.sp,
                        color = MutedInk
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { viewModel.testQuickMovement(1.03) },
                            colors = ButtonDefaults.buttonColors(containerColor = PriceUpLight, contentColor = PriceUp),
                            border = BorderStroke(1.dp, PriceUp),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).height(46.dp).testTag("btn_quick_test_rise")
                        ) {
                            Text("+3% Rise", fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { viewModel.testQuickMovement(0.97) },
                            colors = ButtonDefaults.buttonColors(containerColor = PriceDownLight, contentColor = PriceDown),
                            border = BorderStroke(1.dp, PriceDown),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f).height(46.dp).testTag("btn_quick_test_fall")
                        ) {
                            Text("−3% Fall", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

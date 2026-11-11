package com.malikoil.intelligence.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.malikoil.intelligence.model.RateObservation
import com.malikoil.intelligence.ui.theme.*
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.abs

@Composable
fun PriceChartCard(
    observations: List<RateObservation>,
    modifier: Modifier = Modifier,
    isSample: Boolean = false
) {
    if (observations.isEmpty()) {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .height(260.dp),
            color = ChartDarkBg,
            shape = RoundedCornerShape(14.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, ChartDarkBorder)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "Insufficient observations for chart.",
                    color = ChartMuted,
                    fontSize = 14.sp
                )
            }
        }
        return
    }

    var selectedIndex by remember(observations) { mutableStateOf<Int?>(null) }
    val prices = remember(observations) { observations.map { it.pricePkrKg } }
    val minPrice = remember(prices) { prices.minOrNull() ?: 0.0 }
    val maxPrice = remember(prices) { prices.maxOrNull() ?: 1.0 }
    val pad = remember(minPrice, maxPrice) { ((maxPrice - minPrice) * 0.16).coerceAtLeast(1.0) }
    val chartMin = remember(minPrice, pad) { minPrice - pad }
    val chartMax = remember(maxPrice, pad) { maxPrice + pad }

    val selectedObs = selectedIndex?.let { observations.getOrNull(it) }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("price_history_chart_card"),
        color = ChartDarkBg,
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, ChartDarkBorder)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            // Selected tooltip header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (selectedObs != null) {
                    val dateFormatted = remember(selectedObs.observedAt) {
                        Instant.ofEpochSecond(selectedObs.observedAt)
                            .atZone(ZoneId.systemDefault())
                            .format(DateTimeFormatter.ofPattern("MMM dd, HH:mm"))
                    }
                    Text(
                        text = "$dateFormatted · PKR %.2f/kg%s".format(
                            selectedObs.pricePkrKg,
                            if (isSample) " (Sample)" else ""
                        ),
                        color = ChartGold,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                } else {
                    Text(
                        text = "Touch chart points to inspect rates",
                        color = ChartMuted,
                        fontSize = 12.sp
                    )
                }

                Text(
                    text = if (isSample) "ILLUSTRATIVE" else "VERIFIED",
                    color = if (isSample) ChartGold else ChartMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Canvas Plot
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .pointerInput(observations) {
                        detectTapGestures { tapOffset ->
                            if (observations.isEmpty()) return@detectTapGestures
                            val canvasWidth = size.width
                            val paddingLeft = 40.dp.toPx()
                            val paddingRight = 16.dp.toPx()
                            val plotWidth = (canvasWidth - paddingLeft - paddingRight).coerceAtLeast(1f)

                            val step = if (observations.size > 1) plotWidth / (observations.size - 1) else plotWidth
                            val tappedIndex = ((tapOffset.x - paddingLeft) / step)
                                .toInt()
                                .coerceIn(0, observations.size - 1)
                            selectedIndex = tappedIndex
                        }
                    }
            ) {
                val canvasWidth = size.width
                val canvasHeight = size.height

                val paddingLeft = 40.dp.toPx()
                val paddingRight = 16.dp.toPx()
                val paddingTop = 16.dp.toPx()
                val paddingBottom = 24.dp.toPx()

                val plotWidth = canvasWidth - paddingLeft - paddingRight
                val plotHeight = canvasHeight - paddingTop - paddingBottom

                // Horizontal grid lines
                val gridLines = 4
                for (i in 0..gridLines) {
                    val y = paddingTop + (i.toFloat() / gridLines) * plotHeight
                    drawLine(
                        color = ChartDarkBorder,
                        start = Offset(paddingLeft, y),
                        end = Offset(canvasWidth - paddingRight, y),
                        strokeWidth = 1f
                    )
                }

                if (observations.size < 2) return@Canvas

                val priceRange = (chartMax - chartMin).coerceAtLeast(0.01)
                val points = observations.mapIndexed { index, obs ->
                    val x = paddingLeft + (index.toFloat() / (observations.size - 1)) * plotWidth
                    val normalizedY = ((chartMax - obs.pricePkrKg) / priceRange).toFloat()
                    val y = paddingTop + normalizedY * plotHeight
                    Offset(x, y)
                }

                // Gradient fill under path
                val areaPath = Path().apply {
                    moveTo(points.first().x, paddingTop + plotHeight)
                    points.forEach { lineTo(it.x, it.y) }
                    lineTo(points.last().x, paddingTop + plotHeight)
                    close()
                }

                drawPath(
                    path = areaPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            ChartGold.copy(alpha = 0.25f),
                            ChartGold.copy(alpha = 0.02f)
                        ),
                        startY = paddingTop,
                        endY = paddingTop + plotHeight
                    )
                )

                // Polyline stroke
                val strokePath = Path().apply {
                    moveTo(points.first().x, points.first().y)
                    for (i in 1 until points.size) {
                        lineTo(points[i].x, points[i].y)
                    }
                }

                drawPath(
                    path = strokePath,
                    color = ChartGold,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                )

                // Draw circles for data points
                points.forEachIndexed { index, point ->
                    val isSelected = selectedIndex == index
                    drawCircle(
                        color = if (isSelected) Color.White else ChartGold,
                        radius = if (isSelected) 6.dp.toPx() else 4.dp.toPx(),
                        center = point
                    )
                    drawCircle(
                        color = ChartDarkBg,
                        radius = if (isSelected) 3.dp.toPx() else 2.dp.toPx(),
                        center = point
                    )
                }

                // Selected vertical guide line
                selectedIndex?.let { idx ->
                    if (idx in points.indices) {
                        val pt = points[idx]
                        drawLine(
                            color = ChartMuted.copy(alpha = 0.6f),
                            start = Offset(pt.x, paddingTop),
                            end = Offset(pt.x, paddingTop + plotHeight),
                            strokeWidth = 1.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
                        )
                    }
                }
            }

            // High/Low and timestamps row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val startFormatted = remember(observations) {
                    Instant.ofEpochSecond(observations.first().observedAt)
                        .atZone(ZoneId.systemDefault())
                        .format(DateTimeFormatter.ofPattern("MMM dd"))
                }
                val endFormatted = remember(observations) {
                    Instant.ofEpochSecond(observations.last().observedAt)
                        .atZone(ZoneId.systemDefault())
                        .format(DateTimeFormatter.ofPattern("MMM dd"))
                }

                Text(text = startFormatted, color = ChartMuted, fontSize = 11.sp)
                Text(
                    text = "High: PKR %.1f | Low: PKR %.1f".format(maxPrice, minPrice),
                    color = ChartMuted,
                    fontSize = 11.sp
                )
                Text(text = endFormatted, color = ChartMuted, fontSize = 11.sp)
            }
        }
    }
}

/**
 * Compose Canvas 7-Day Price Trend Line Chart for Sarson and Canola Seeds.
 * Features dual-line trend curves, interactive crosshair touch inspection,
 * price spread metrics, and series toggles (Both, Sarson only, Canola only).
 */
@Composable
fun SarsonCanolaTrendChartCard(
    sarsonObservations: List<RateObservation>,
    canolaObservations: List<RateObservation>,
    modifier: Modifier = Modifier,
    language: String = "en",
    chartDays: Int = 7,
    onPeriodSelected: (Int) -> Unit = {}
) {
    // Mode toggle: "both", "sarson", "canola"
    var chartMode by remember { mutableStateOf("both") }
    var selectedIndex by remember(sarsonObservations, canolaObservations) { mutableStateOf<Int?>(null) }

    val sarsonPrices = remember(sarsonObservations) { sarsonObservations.map { it.pricePkrKg } }
    val canolaPrices = remember(canolaObservations) { canolaObservations.map { it.pricePkrKg } }

    val sarsonCurrent = sarsonPrices.lastOrNull() ?: 206.0
    val sarsonFirst = sarsonPrices.firstOrNull() ?: 198.0
    val sarsonDelta = if (sarsonFirst > 0) ((sarsonCurrent - sarsonFirst) * 100.0 / sarsonFirst) else 0.0

    val canolaCurrent = canolaPrices.lastOrNull() ?: 228.0
    val canolaFirst = canolaPrices.firstOrNull() ?: 218.0
    val canolaDelta = if (canolaFirst > 0) ((canolaCurrent - canolaFirst) * 100.0 / canolaFirst) else 0.0

    val currentSpread = canolaCurrent - sarsonCurrent
    val currentSpreadMaund = currentSpread * 40.0

    // Compute unified vertical price range across both series
    val activePrices = remember(chartMode, sarsonPrices, canolaPrices) {
        when (chartMode) {
            "sarson" -> sarsonPrices.ifEmpty { listOf(200.0) }
            "canola" -> canolaPrices.ifEmpty { listOf(220.0) }
            else -> (sarsonPrices + canolaPrices).ifEmpty { listOf(200.0, 230.0) }
        }
    }

    val minPrice = remember(activePrices) { activePrices.minOrNull() ?: 190.0 }
    val maxPrice = remember(activePrices) { activePrices.maxOrNull() ?: 230.0 }
    val pad = remember(minPrice, maxPrice) { ((maxPrice - minPrice) * 0.15).coerceAtLeast(2.0) }
    val chartMin = remember(minPrice, pad) { minPrice - pad }
    val chartMax = remember(maxPrice, pad) { maxPrice + pad }

    val dataCount = remember(sarsonObservations, canolaObservations) {
        maxOf(sarsonObservations.size, canolaObservations.size).coerceAtLeast(1)
    }

    // Selected point inspection
    val inspectedSarson = selectedIndex?.let { sarsonObservations.getOrNull(it) }
    val inspectedCanola = selectedIndex?.let { canolaObservations.getOrNull(it) }
    val inspectedTime = inspectedSarson?.observedAt ?: inspectedCanola?.observedAt

    val sarsonMaxObs = remember(sarsonObservations) { sarsonObservations.maxByOrNull { it.pricePkrKg } }
    val sarsonMaxIdx = remember(sarsonObservations, sarsonMaxObs) { sarsonObservations.indexOf(sarsonMaxObs) }
    val sarsonMinObs = remember(sarsonObservations) { sarsonObservations.minByOrNull { it.pricePkrKg } }
    val sarsonMinIdx = remember(sarsonObservations, sarsonMinObs) { sarsonObservations.indexOf(sarsonMinObs) }

    val canolaMaxObs = remember(canolaObservations) { canolaObservations.maxByOrNull { it.pricePkrKg } }
    val canolaMaxIdx = remember(canolaObservations, canolaMaxObs) { canolaObservations.indexOf(canolaMaxObs) }
    val canolaMinObs = remember(canolaObservations) { canolaObservations.minByOrNull { it.pricePkrKg } }
    val canolaMinIdx = remember(canolaObservations, canolaMinObs) { canolaObservations.indexOf(canolaMinObs) }

    // Active peak and dip depending on selected chart mode
    val activePeakPrice = remember(chartMode, sarsonMaxObs, canolaMaxObs) {
        when (chartMode) {
            "sarson" -> sarsonMaxObs?.pricePkrKg ?: 216.0
            "canola" -> canolaMaxObs?.pricePkrKg ?: 228.0
            else -> maxOf(sarsonMaxObs?.pricePkrKg ?: 216.0, canolaMaxObs?.pricePkrKg ?: 228.0)
        }
    }
    val activeDipPrice = remember(chartMode, sarsonMinObs, canolaMinObs) {
        when (chartMode) {
            "sarson" -> sarsonMinObs?.pricePkrKg ?: 198.0
            "canola" -> canolaMinObs?.pricePkrKg ?: 218.0
            else -> minOf(sarsonMinObs?.pricePkrKg ?: 198.0, canolaMinObs?.pricePkrKg ?: 218.0)
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("sarson_canola_trend_chart"),
        color = ChartDarkBg,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, ChartDarkBorder)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            // Header Row: Title, Subtitle, and Period Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            color = ForestGreen,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "7-DAY TREND",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Text(
                            text = Translations.t("sarson_canola_compare", language),
                            color = ChartMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = Translations.t("trend_7d_title", language),
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = Translations.t("trend_7d_sub", language),
                        color = ChartMuted,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }

                // Period Buttons
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf(1 to "1D", 7 to "7D", 30 to "1M", 3650 to "All").forEach { (days, label) ->
                        val selected = chartDays == days
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (selected) ForestGreen else Color(0xFF1B3D2B),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (selected) ChartCanolaGreen else ChartDarkBorder
                            ),
                            modifier = Modifier
                                .clickable { onPeriodSelected(days) }
                                .testTag("period_btn_$label")
                        ) {
                            Text(
                                text = label,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (selected) Color.White else ChartMuted,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Metrics Summary Grid (Sarson, Canola, Spread)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Sarson Metric Tile
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF143324),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (chartMode == "sarson" || chartMode == "both") ChartGold.copy(alpha = 0.6f) else ChartDarkBorder
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { chartMode = if (chartMode == "sarson") "both" else "sarson" }
                        .testTag("metric_tile_sarson")
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Surface(shape = androidx.compose.foundation.shape.CircleShape, color = ChartGold, modifier = Modifier.size(8.dp)) {}
                            Text(text = "Sarson (سرسوں)", color = ChartGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "PKR %.2f".format(sarsonCurrent),
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "%+.1f%% 7D".format(sarsonDelta),
                            color = if (sarsonDelta >= 0) ChartCanolaGreen else PriceDown,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Canola Metric Tile
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF143324),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (chartMode == "canola" || chartMode == "both") ChartCanolaGreen.copy(alpha = 0.6f) else ChartDarkBorder
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { chartMode = if (chartMode == "canola") "both" else "canola" }
                        .testTag("metric_tile_canola")
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Surface(shape = androidx.compose.foundation.shape.CircleShape, color = ChartCanolaGreen, modifier = Modifier.size(8.dp)) {}
                            Text(text = "Canola (کینولا)", color = ChartCanolaGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "PKR %.2f".format(canolaCurrent),
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "%+.1f%% 7D".format(canolaDelta),
                            color = if (canolaDelta >= 0) ChartCanolaGreen else PriceDown,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Spread Metric Tile
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF143324),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ChartDarkBorder),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(text = "Spread (فرق)", color = ChartMuted, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = "+PKR %.1f".format(currentSpread),
                            color = ChartText,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "+%.0f/40kg".format(currentSpreadMaund),
                            color = ChartGold,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Chart Series Filter Chips (Both, Sarson, Canola)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf(
                    "both" to Translations.t("trend_both", language),
                    "sarson" to Translations.t("sarson_trend", language),
                    "canola" to Translations.t("canola_trend", language)
                ).forEach { (mode, label) ->
                    val selected = chartMode == mode
                    val activeColor = when (mode) {
                        "sarson" -> ChartGold
                        "canola" -> ChartCanolaGreen
                        else -> ForestGreen
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (selected) activeColor else Color(0xFF163827),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (selected) activeColor else ChartDarkBorder
                        ),
                        modifier = Modifier
                            .clickable { chartMode = mode }
                            .testTag("chart_mode_$mode")
                    ) {
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            color = if (selected) Color.White else ChartMuted,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick Interactive 7D High & 7D Low Jump Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // High Jump Chip
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF1A3D28),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ChartGold.copy(alpha = 0.7f)),
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            val targetIdx = if (chartMode == "canola") canolaMaxIdx else sarsonMaxIdx
                            if (targetIdx >= 0) selectedIndex = targetIdx
                        }
                        .testTag("chip_7d_high")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("▲", color = ChartGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(Translations.t("peak_label", language), color = ChartText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Text("PKR %.1f".format(activePeakPrice), color = ChartGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Low Jump Chip
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF33201F),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PriceDown.copy(alpha = 0.7f)),
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            val targetIdx = if (chartMode == "canola") canolaMinIdx else sarsonMinIdx
                            if (targetIdx >= 0) selectedIndex = targetIdx
                        }
                        .testTag("chip_7d_low")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("▼", color = Color(0xFFFF8A80), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(Translations.t("dip_label", language), color = ChartText, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Text("PKR %.1f".format(activeDipPrice), color = Color(0xFFFF8A80), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Active Inspection Tooltip Bar with High/Low Extreme Badges
            val isSelectedPeak = selectedIndex != null && (
                (chartMode == "sarson" && selectedIndex == sarsonMaxIdx) ||
                (chartMode == "canola" && selectedIndex == canolaMaxIdx) ||
                (chartMode == "both" && (selectedIndex == sarsonMaxIdx || selectedIndex == canolaMaxIdx))
            )
            val isSelectedDip = selectedIndex != null && (
                (chartMode == "sarson" && selectedIndex == sarsonMinIdx) ||
                (chartMode == "canola" && selectedIndex == canolaMinIdx) ||
                (chartMode == "both" && (selectedIndex == sarsonMinIdx || selectedIndex == canolaMinIdx))
            )

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = when {
                    isSelectedPeak -> Color(0xFF1B3D27)
                    isSelectedDip -> Color(0xFF321F1F)
                    else -> Color(0xFF132B1F)
                },
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    when {
                        isSelectedPeak -> ChartGold
                        isSelectedDip -> PriceDown
                        else -> ChartDarkBorder
                    }
                ),
                modifier = Modifier.fillMaxWidth().testTag("chart_active_tooltip")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (selectedIndex != null && inspectedTime != null) {
                        val dateFormatted = Instant.ofEpochSecond(inspectedTime)
                            .atZone(ZoneId.systemDefault())
                            .format(DateTimeFormatter.ofPattern("MMM dd"))

                        val sarsonVal = inspectedSarson?.pricePkrKg
                        val canolaVal = inspectedCanola?.pricePkrKg

                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "$dateFormatted (Day ${(selectedIndex ?: 0) + 1}/$dataCount)",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )

                                if (isSelectedPeak) {
                                    Surface(
                                        color = ChartGold,
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "★ 7D PEAK",
                                            color = Color(0xFF143324),
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                } else if (isSelectedDip) {
                                    Surface(
                                        color = PriceDown,
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "★ 7D DIP",
                                            color = Color.White,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (sarsonVal != null && (chartMode == "both" || chartMode == "sarson")) {
                                    Text(
                                        text = "Sarson: PKR %.1f/kg".format(sarsonVal),
                                        color = ChartGold,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                if (canolaVal != null && (chartMode == "both" || chartMode == "canola")) {
                                    Text(
                                        text = "Canola: PKR %.1f/kg".format(canolaVal),
                                        color = ChartCanolaGreen,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    } else {
                        Text(
                            text = Translations.t("touch_to_inspect", language),
                            color = ChartMuted,
                            fontSize = 11.sp
                        )
                    }

                    Text(
                        text = "CANVAS CHART",
                        color = ChartMuted,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // COMPOSE CANVAS LINE CHART
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp)
                    .pointerInput(dataCount) {
                        detectTapGestures { tapOffset ->
                            if (dataCount <= 0) return@detectTapGestures
                            val canvasWidth = size.width
                            val paddingLeft = 40.dp.toPx()
                            val paddingRight = 16.dp.toPx()
                            val plotWidth = (canvasWidth - paddingLeft - paddingRight).coerceAtLeast(1f)
                            val step = if (dataCount > 1) plotWidth / (dataCount - 1) else plotWidth
                            val tappedIndex = ((tapOffset.x - paddingLeft + step / 2) / step)
                                .toInt()
                                .coerceIn(0, dataCount - 1)
                            selectedIndex = tappedIndex
                        }
                    }
                    .pointerInput(dataCount) {
                        detectDragGestures { change, _ ->
                            if (dataCount <= 0) return@detectDragGestures
                            val canvasWidth = size.width
                            val paddingLeft = 40.dp.toPx()
                            val paddingRight = 16.dp.toPx()
                            val plotWidth = (canvasWidth - paddingLeft - paddingRight).coerceAtLeast(1f)
                            val step = if (dataCount > 1) plotWidth / (dataCount - 1) else plotWidth
                            val tappedIndex = ((change.position.x - paddingLeft + step / 2) / step)
                                .toInt()
                                .coerceIn(0, dataCount - 1)
                            selectedIndex = tappedIndex
                        }
                    }
            ) {
                val canvasWidth = size.width
                val canvasHeight = size.height

                val paddingLeft = 42.dp.toPx()
                val paddingRight = 16.dp.toPx()
                val paddingTop = 26.dp.toPx()
                val paddingBottom = 26.dp.toPx()

                val plotWidth = canvasWidth - paddingLeft - paddingRight
                val plotHeight = canvasHeight - paddingTop - paddingBottom

                val priceRange = (chartMax - chartMin).coerceAtLeast(1.0)

                // Draw Horizontal Gridlines & Y-Axis Reference Ticks
                val gridLines = 4
                for (i in 0..gridLines) {
                    val y = paddingTop + (i.toFloat() / gridLines) * plotHeight
                    drawLine(
                        color = ChartDarkBorder.copy(alpha = 0.8f),
                        start = Offset(paddingLeft, y),
                        end = Offset(canvasWidth - paddingRight, y),
                        strokeWidth = 1f
                    )
                }

                // Draw 7D High & 7D Low Horizontal Benchmark Guidelines
                val highY = paddingTop + (((chartMax - activePeakPrice) / priceRange).toFloat().coerceIn(0f, 1f) * plotHeight)
                drawLine(
                    color = ChartGold.copy(alpha = 0.45f),
                    start = Offset(paddingLeft, highY),
                    end = Offset(canvasWidth - paddingRight, highY),
                    strokeWidth = 1.2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                )

                val lowY = paddingTop + (((chartMax - activeDipPrice) / priceRange).toFloat().coerceIn(0f, 1f) * plotHeight)
                drawLine(
                    color = PriceDown.copy(alpha = 0.45f),
                    start = Offset(paddingLeft, lowY),
                    end = Offset(canvasWidth - paddingRight, lowY),
                    strokeWidth = 1.2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                )

                // Function to compute point coordinates
                fun getPoints(series: List<RateObservation>): List<Offset> {
                    if (series.isEmpty()) return emptyList()
                    return series.mapIndexed { index, obs ->
                        val x = paddingLeft + (index.toFloat() / (series.size - 1).coerceAtLeast(1)) * plotWidth
                        val normalizedY = ((chartMax - obs.pricePkrKg) / priceRange).toFloat()
                        val y = paddingTop + normalizedY.coerceIn(0f, 1f) * plotHeight
                        Offset(x, y)
                    }
                }

                val sarsonPoints = getPoints(sarsonObservations)
                val canolaPoints = getPoints(canolaObservations)

                // 1. Draw Sarson Curve if visible
                if ((chartMode == "both" || chartMode == "sarson") && sarsonPoints.size >= 2) {
                    val sarsonArea = Path().apply {
                        moveTo(sarsonPoints.first().x, paddingTop + plotHeight)
                        sarsonPoints.forEach { lineTo(it.x, it.y) }
                        lineTo(sarsonPoints.last().x, paddingTop + plotHeight)
                        close()
                    }
                    drawPath(
                        path = sarsonArea,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                ChartGold.copy(alpha = 0.22f),
                                ChartGold.copy(alpha = 0.01f)
                            ),
                            startY = paddingTop,
                            endY = paddingTop + plotHeight
                        )
                    )

                    val sarsonStroke = Path().apply {
                        moveTo(sarsonPoints.first().x, sarsonPoints.first().y)
                        for (i in 1 until sarsonPoints.size) {
                            lineTo(sarsonPoints[i].x, sarsonPoints[i].y)
                        }
                    }
                    drawPath(
                        path = sarsonStroke,
                        color = ChartGold,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )

                    // Draw Points with Peak & Dip Highlights
                    sarsonPoints.forEachIndexed { idx, pt ->
                        val isSel = selectedIndex == idx
                        val isPeak = idx == sarsonMaxIdx
                        val isDip = idx == sarsonMinIdx

                        if (isPeak) {
                            // Glowing halo for peak
                            drawCircle(
                                color = ChartGold.copy(alpha = 0.35f),
                                radius = 9.dp.toPx(),
                                center = pt
                            )
                            // Peak Callout Flag
                            val flagWidth = 42.dp.toPx()
                            val flagHeight = 16.dp.toPx()
                            val flagLeft = (pt.x - flagWidth / 2).coerceIn(paddingLeft, canvasWidth - paddingRight - flagWidth)
                            val flagTop = (pt.y - flagHeight - 7.dp.toPx()).coerceAtLeast(2.dp.toPx())
                            drawRoundRect(
                                color = ChartGold,
                                topLeft = Offset(flagLeft, flagTop),
                                size = androidx.compose.ui.geometry.Size(flagWidth, flagHeight),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx())
                            )
                            // Downward pointer
                            val pointerPath = Path().apply {
                                moveTo(pt.x - 3.dp.toPx(), flagTop + flagHeight)
                                lineTo(pt.x + 3.dp.toPx(), flagTop + flagHeight)
                                lineTo(pt.x, pt.y - 2.dp.toPx())
                                close()
                            }
                            drawPath(pointerPath, color = ChartGold)
                        }

                        if (isDip) {
                            // Halo for dip
                            drawCircle(
                                color = PriceDown.copy(alpha = 0.35f),
                                radius = 9.dp.toPx(),
                                center = pt
                            )
                            // Dip Callout Flag
                            val flagWidth = 38.dp.toPx()
                            val flagHeight = 16.dp.toPx()
                            val flagLeft = (pt.x - flagWidth / 2).coerceIn(paddingLeft, canvasWidth - paddingRight - flagWidth)
                            val flagTop = (pt.y + 7.dp.toPx()).coerceAtMost(canvasHeight - paddingBottom - flagHeight)
                            drawRoundRect(
                                color = PriceDown,
                                topLeft = Offset(flagLeft, flagTop),
                                size = androidx.compose.ui.geometry.Size(flagWidth, flagHeight),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx())
                            )
                            // Upward pointer
                            val pointerPath = Path().apply {
                                moveTo(pt.x - 3.dp.toPx(), flagTop)
                                lineTo(pt.x + 3.dp.toPx(), flagTop)
                                lineTo(pt.x, pt.y + 2.dp.toPx())
                                close()
                            }
                            drawPath(pointerPath, color = PriceDown)
                        }

                        drawCircle(
                            color = if (isSel) Color.White else ChartGold,
                            radius = if (isSel) 6.dp.toPx() else (if (isPeak || isDip) 5.dp.toPx() else 3.5.dp.toPx()),
                            center = pt
                        )
                        drawCircle(
                            color = ChartDarkBg,
                            radius = if (isSel) 3.dp.toPx() else 1.8.dp.toPx(),
                            center = pt
                        )
                    }
                }

                // 2. Draw Canola Curve if visible
                if ((chartMode == "both" || chartMode == "canola") && canolaPoints.size >= 2) {
                    val canolaArea = Path().apply {
                        moveTo(canolaPoints.first().x, paddingTop + plotHeight)
                        canolaPoints.forEach { lineTo(it.x, it.y) }
                        lineTo(canolaPoints.last().x, paddingTop + plotHeight)
                        close()
                    }
                    drawPath(
                        path = canolaArea,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                ChartCanolaGreen.copy(alpha = 0.18f),
                                ChartCanolaGreen.copy(alpha = 0.01f)
                            ),
                            startY = paddingTop,
                            endY = paddingTop + plotHeight
                        )
                    )

                    val canolaStroke = Path().apply {
                        moveTo(canolaPoints.first().x, canolaPoints.first().y)
                        for (i in 1 until canolaPoints.size) {
                            lineTo(canolaPoints[i].x, canolaPoints[i].y)
                        }
                    }
                    drawPath(
                        path = canolaStroke,
                        color = ChartCanolaGreen,
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )

                    canolaPoints.forEachIndexed { idx, pt ->
                        val isSel = selectedIndex == idx
                        val isPeak = idx == canolaMaxIdx
                        val isDip = idx == canolaMinIdx

                        if (isPeak) {
                            drawCircle(
                                color = ChartCanolaGreen.copy(alpha = 0.35f),
                                radius = 9.dp.toPx(),
                                center = pt
                            )
                            val flagWidth = 42.dp.toPx()
                            val flagHeight = 16.dp.toPx()
                            val flagLeft = (pt.x - flagWidth / 2).coerceIn(paddingLeft, canvasWidth - paddingRight - flagWidth)
                            val flagTop = (pt.y - flagHeight - 7.dp.toPx()).coerceAtLeast(2.dp.toPx())
                            drawRoundRect(
                                color = ChartCanolaGreen,
                                topLeft = Offset(flagLeft, flagTop),
                                size = androidx.compose.ui.geometry.Size(flagWidth, flagHeight),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx())
                            )
                            val pointerPath = Path().apply {
                                moveTo(pt.x - 3.dp.toPx(), flagTop + flagHeight)
                                lineTo(pt.x + 3.dp.toPx(), flagTop + flagHeight)
                                lineTo(pt.x, pt.y - 2.dp.toPx())
                                close()
                            }
                            drawPath(pointerPath, color = ChartCanolaGreen)
                        }

                        if (isDip) {
                            drawCircle(
                                color = PriceDown.copy(alpha = 0.35f),
                                radius = 9.dp.toPx(),
                                center = pt
                            )
                            val flagWidth = 38.dp.toPx()
                            val flagHeight = 16.dp.toPx()
                            val flagLeft = (pt.x - flagWidth / 2).coerceIn(paddingLeft, canvasWidth - paddingRight - flagWidth)
                            val flagTop = (pt.y + 7.dp.toPx()).coerceAtMost(canvasHeight - paddingBottom - flagHeight)
                            drawRoundRect(
                                color = PriceDown,
                                topLeft = Offset(flagLeft, flagTop),
                                size = androidx.compose.ui.geometry.Size(flagWidth, flagHeight),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx())
                            )
                            val pointerPath = Path().apply {
                                moveTo(pt.x - 3.dp.toPx(), flagTop)
                                lineTo(pt.x + 3.dp.toPx(), flagTop)
                                lineTo(pt.x, pt.y + 2.dp.toPx())
                                close()
                            }
                            drawPath(pointerPath, color = PriceDown)
                        }

                        drawCircle(
                            color = if (isSel) Color.White else ChartCanolaGreen,
                            radius = if (isSel) 6.dp.toPx() else (if (isPeak || isDip) 5.dp.toPx() else 3.5.dp.toPx()),
                            center = pt
                        )
                        drawCircle(
                            color = ChartDarkBg,
                            radius = if (isSel) 3.dp.toPx() else 1.8.dp.toPx(),
                            center = pt
                        )
                    }
                }

                // 3. Draw vertical dashed crosshair when a point is selected
                selectedIndex?.let { idx ->
                    val x = paddingLeft + (idx.toFloat() / (dataCount - 1).coerceAtLeast(1)) * plotWidth
                    drawLine(
                        color = Color.White.copy(alpha = 0.6f),
                        start = Offset(x, paddingTop),
                        end = Offset(x, paddingTop + plotHeight),
                        strokeWidth = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
                    )
                }
            }

            // X-Axis Date Ticks Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val dates = if (sarsonObservations.isNotEmpty()) {
                    sarsonObservations.map {
                        Instant.ofEpochSecond(it.observedAt)
                            .atZone(ZoneId.systemDefault())
                            .format(DateTimeFormatter.ofPattern("MMM dd"))
                    }
                } else {
                    listOf("Day 1", "Day 2", "Day 3", "Day 4", "Day 5", "Day 6", "Today")
                }

                val firstDate = dates.firstOrNull() ?: "7 Days Ago"
                val midDate = dates.getOrNull(dates.size / 2) ?: "Mid"
                val lastDate = dates.lastOrNull() ?: "Today"

                Text(text = firstDate, color = ChartMuted, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                Text(text = midDate, color = ChartMuted, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                Text(text = lastDate, color = ChartMuted, fontSize = 11.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}


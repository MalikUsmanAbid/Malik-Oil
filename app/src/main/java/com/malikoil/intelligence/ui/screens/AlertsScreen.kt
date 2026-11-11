package com.malikoil.intelligence.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.malikoil.intelligence.model.PriceCondition
import com.malikoil.intelligence.ui.MarketViewModel
import com.malikoil.intelligence.ui.components.Translations
import com.malikoil.intelligence.ui.theme.*
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun AlertsScreen(
    viewModel: MarketViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val language by viewModel.language.collectAsState()
    val alerts by viewModel.alerts.collectAsState()
    val targetAlerts by viewModel.targetAlerts.collectAsState()
    val rates by viewModel.rates.collectAsState()

    // Sub-tab: "target" (Target Purchase Alerts) or "movement" (±3% Policy Log)
    var selectedAlertTab by remember { mutableStateOf("target") }

    // Form state for creating target alert
    var alertCommodity by remember { mutableStateOf("sarson") }
    var alertTargetPrice by remember { mutableStateOf("8080") }
    var alertUnit by remember { mutableStateOf("40kg") } // "40kg" or "kg"
    var alertCondition by remember { mutableStateOf(PriceCondition.AT_OR_BELOW) }
    var alertMarket by remember { mutableStateOf("Samundri") }
    var alertNotes by remember { mutableStateOf("") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SandBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = Translations.t("alerts", language),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ForestGreen,
                        letterSpacing = 0.8.sp
                    )
                    Text(
                        text = Translations.t("target_alerts", language),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextInk
                    )
                }

                Surface(
                    color = PriceUpLight,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, ForestGreen.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Surface(shape = androidx.compose.foundation.shape.CircleShape, color = ForestGreen, modifier = Modifier.size(6.dp)) {}
                        Text("PUSH ACTIVE", color = ForestGreenDark, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Sub-Tab Switcher: Target Purchase Alerts vs ±3% Movement Policy
        item {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = RaisedBackground,
                border = BorderStroke(1.dp, LineBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                ) {
                    // Tab 1: Target Alerts
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (selectedAlertTab == "target") PanelBackground else Color.Transparent,
                        border = if (selectedAlertTab == "target") BorderStroke(1.dp, LineBorder) else null,
                        shadowElevation = if (selectedAlertTab == "target") 2.dp else 0.dp,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedAlertTab = "target" }
                            .testTag("target_alerts_tab")
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.TrackChanges,
                                contentDescription = null,
                                tint = if (selectedAlertTab == "target") ForestGreen else MutedInk,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${Translations.t("target_alerts", language)} (${targetAlerts.size})",
                                fontSize = 12.sp,
                                fontWeight = if (selectedAlertTab == "target") FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedAlertTab == "target") TextInk else MutedInk
                            )
                        }
                    }

                    // Tab 2: 3% Movement History
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (selectedAlertTab == "movement") PanelBackground else Color.Transparent,
                        border = if (selectedAlertTab == "movement") BorderStroke(1.dp, LineBorder) else null,
                        shadowElevation = if (selectedAlertTab == "movement") 2.dp else 0.dp,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedAlertTab = "movement" }
                            .testTag("movement_alerts_tab")
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = if (selectedAlertTab == "movement") ForestGreen else MutedInk,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "±3% History (${alerts.size})",
                                fontSize = 12.sp,
                                fontWeight = if (selectedAlertTab == "movement") FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedAlertTab == "movement") TextInk else MutedInk
                            )
                        }
                    }
                }
            }
        }

        // ==========================================
        // TAB 1: TARGET PURCHASE PRICE ALERTS
        // ==========================================
        if (selectedAlertTab == "target") {
            // Target Alert Setup Form Card
            item {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = PanelBackground,
                    border = BorderStroke(1.dp, Color(0xFFE2DDD1)),
                    shadowElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth().testTag("target_alert_creator_card")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(shape = androidx.compose.foundation.shape.CircleShape, color = ForestGreen, modifier = Modifier.size(28.dp)) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.AddAlert, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                }
                            }
                            Column {
                                Text(
                                    text = Translations.t("set_target_alert", language),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextInk
                                )
                                Text(
                                    text = Translations.t("target_alerts_sub", language),
                                    fontSize = 11.sp,
                                    color = MutedInk
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Commodity Selector Buttons: Sarson vs Canola
                        Text(text = Translations.t("commodity", language) + ":", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextInk)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            listOf("sarson" to "Sarson (سرسوں)", "canola" to "Canola (کینولا)").forEach { (id, label) ->
                                val selected = alertCommodity == id
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (selected) ForestGreen else RaisedBackground,
                                    border = BorderStroke(1.dp, if (selected) ForestGreen else LineBorder),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { alertCommodity = id }
                                        .testTag("select_commodity_$id")
                                ) {
                                    Box(modifier = Modifier.padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                                        Text(
                                            text = label,
                                            fontSize = 13.sp,
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (selected) Color.White else TextInk
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Target Price Input + Unit Toggle (40kg Maund vs kg)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = alertTargetPrice,
                                onValueChange = { alertTargetPrice = it },
                                label = { Text(Translations.t("target_price", language)) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1.4f).testTag("input_target_price")
                            )

                            // Unit Toggle (40kg vs kg)
                            Row(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                listOf("40kg" to "40kg (من)", "kg" to "kg (کلو)").forEach { (unitVal, label) ->
                                    val isUnitSelected = alertUnit == unitVal
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (isUnitSelected) ForestGreen else RaisedBackground,
                                        border = BorderStroke(1.dp, if (isUnitSelected) ForestGreen else LineBorder),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { alertUnit = unitVal }
                                    ) {
                                        Box(modifier = Modifier.padding(vertical = 14.dp), contentAlignment = Alignment.Center) {
                                            Text(
                                                text = if (unitVal == "40kg") "40kg" else "kg",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isUnitSelected) Color.White else TextInk
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Real-time conversion helper
                        val enteredVal = alertTargetPrice.toDoubleOrNull() ?: 0.0
                        if (enteredVal > 0) {
                            val equivPkrKg = if (alertUnit == "40kg") enteredVal / 40.0 else enteredVal
                            val equivPkrMaund = if (alertUnit == "40kg") enteredVal else enteredVal * 40.0
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Rate equivalent: PKR %.2f / kg  =  PKR %.0f / 40kg Maund".format(equivPkrKg, equivPkrMaund),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = ForestGreen
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Mandi & Condition Rows
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = alertMarket,
                                onValueChange = { alertMarket = it },
                                label = { Text(Translations.t("mandi", language)) },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f).testTag("input_target_market")
                            )

                            OutlinedTextField(
                                value = alertNotes,
                                onValueChange = { alertNotes = it },
                                label = { Text("Notes (optional)") },
                                placeholder = { Text("e.g. Expeller lot 1") },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Trigger Condition Selector: Buy (At or Below) vs Sell (At or Above)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                PriceCondition.AT_OR_BELOW to Translations.t("condition_drop_below", language),
                                PriceCondition.AT_OR_ABOVE to Translations.t("condition_rise_above", language)
                            ).forEach { (cond, label) ->
                                val selected = alertCondition == cond
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (selected) PriceUpLight else PanelBackground,
                                    border = BorderStroke(1.dp, if (selected) ForestGreen else LineBorder),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { alertCondition = cond }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        RadioButton(
                                            selected = selected,
                                            onClick = { alertCondition = cond },
                                            colors = RadioButtonDefaults.colors(selectedColor = ForestGreen),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(text = label, fontSize = 11.sp, color = TextInk, fontWeight = FontWeight.Medium)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                val price = alertTargetPrice.toDoubleOrNull()
                                if (price != null && price > 0) {
                                    viewModel.addTargetPriceAlert(
                                        commodity = alertCommodity,
                                        targetPrice = price,
                                        unit = alertUnit,
                                        condition = alertCondition,
                                        market = alertMarket,
                                        notes = alertNotes
                                    )
                                    Toast.makeText(context, "Target Alert Activated! Push notification armed.", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Please enter a valid price.", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("btn_set_target_alert")
                        ) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(Translations.t("set_target_alert", language), fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }

            // Section Header: Active Target Alerts
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${Translations.t("active_target_alerts", language)} (${targetAlerts.size})",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextInk
                    )
                    Text(
                        text = "Real-time Monitoring",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MutedInk
                    )
                }
            }

            // Target Alerts List
            if (targetAlerts.isEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = PanelBackground,
                        border = BorderStroke(1.dp, LineBorder),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.TrackChanges, contentDescription = null, tint = MutedInk, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = "No active target price alerts.", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextInk)
                            Text(text = "Set a purchase threshold above to receive push notifications.", fontSize = 12.sp, color = MutedInk)
                        }
                    }
                }
            } else {
                items(targetAlerts) { alert ->
                    // Current market observation for this commodity
                    val currentRate = rates.filter { it.commodity.equals(alert.commodity, ignoreCase = true) && !it.isSample }
                        .maxByOrNull { it.observedAt }
                    val currentPriceKg = currentRate?.pricePkrKg ?: 206.0
                    val currentPriceMaund = currentRate?.pricePkrMaund ?: (currentPriceKg * 40.0)
                    val priceDiff = currentPriceKg - alert.targetPricePkrKg

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (alert.isTriggered) Color(0xFFF3F9F5) else PanelBackground,
                        border = BorderStroke(
                            if (alert.isTriggered) 2.dp else 1.dp,
                            if (alert.isTriggered) ForestGreen else Color(0xFFE2DDD1)
                        ),
                        shadowElevation = 2.dp,
                        modifier = Modifier.fillMaxWidth().testTag("target_alert_item_${alert.id}")
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Top Row: Commodity, Mandi, and Status Pill
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    val commName = alert.commodity.replaceFirstChar { it.uppercase() }
                                    Text(
                                        text = "$commName · ${alert.market}",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextInk
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (alert.isTriggered) ForestGreen else PriceUpLight
                                    ) {
                                        Text(
                                            text = if (alert.isTriggered) "TARGET HIT 🎯" else "ACTIVE 🟢",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (alert.isTriggered) Color.White else ForestGreenDark,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    IconButton(
                                        onClick = { viewModel.removeTargetPriceAlert(alert.id) },
                                        modifier = Modifier.size(28.dp).testTag("btn_delete_target_alert_${alert.id}")
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MutedInk, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Target Price vs Current Price Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = "TARGET PURCHASE PRICE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ForestGreen)
                                    Text(
                                        text = "PKR %.2f / kg".format(alert.targetPricePkrKg),
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Black,
                                        color = TextInk
                                    )
                                    Text(
                                        text = "PKR %.0f / 40kg (من)".format(alert.targetPricePkrMaund),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ForestGreen
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(text = "CURRENT MANDI RATE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MutedInk)
                                    Text(
                                        text = "PKR %.2f / kg".format(currentPriceKg),
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextInk
                                    )
                                    val diffText = if (priceDiff > 0) "+PKR %.2f away".format(priceDiff) else "Threshold met!"
                                    Text(
                                        text = diffText,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (priceDiff <= 0) ForestGreen else Color(0xFFC0392B)
                                    )
                                }
                            }

                            if (alert.notes.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(text = "Note: ${alert.notes}", fontSize = 11.sp, color = MutedInk)
                            }

                            if (alert.isTriggered && alert.triggeredAt != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                val trigDate = Instant.ofEpochSecond(alert.triggeredAt)
                                    .atZone(ZoneId.systemDefault())
                                    .format(DateTimeFormatter.ofPattern("MMM dd, HH:mm"))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = PriceUpLight,
                                    border = BorderStroke(1.dp, ForestGreen.copy(alpha = 0.4f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(16.dp))
                                        Text(
                                            text = "Push Notification sent at $trigDate (Hit PKR %.2f/kg)".format(alert.triggeredPricePkrKg ?: alert.targetPricePkrKg),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ForestGreenDark
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = LineBorder.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(10.dp))

                            // Test Trigger Action Button (Instant Push Notification Test)
                            Button(
                                onClick = {
                                    viewModel.simulateTargetAlertTrigger(context, alert.id)
                                    Toast.makeText(context, "🎯 Mandi rate hit simulated! Check phone notification bar.", Toast.LENGTH_LONG).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = RaisedBackground, contentColor = TextInk),
                                border = BorderStroke(1.dp, ForestGreen.copy(alpha = 0.5f)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("btn_simulate_target_hit_${alert.id}")
                            ) {
                                Icon(Icons.Default.VolumeUp, contentDescription = null, tint = ForestGreen, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = Translations.t("simulate_mandi_hit", language),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ForestGreenDark
                                )
                            }
                        }
                    }
                }
            }
        }

        // ==========================================
        // TAB 2: ±3% MOVEMENT POLICY LOG
        // ==========================================
        if (selectedAlertTab == "movement") {
            // Rules explanation card
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = PanelBackground,
                    border = BorderStroke(1.dp, LineBorder),
                    modifier = Modifier.fillMaxWidth().testTag("alert_rules_card")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = Translations.t("alert_rules", language),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = ForestGreen
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = Translations.t("rules_note", language),
                            fontSize = 13.sp,
                            color = TextInk
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = RaisedBackground,
                                border = BorderStroke(1.dp, LineBorder)
                            ) {
                                Text(
                                    text = Translations.t("dedupe_pill", language),
                                    fontSize = 11.sp,
                                    color = MutedInk,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = RaisedBackground,
                                border = BorderStroke(1.dp, LineBorder)
                            ) {
                                Text(
                                    text = Translations.t("no_orders_pill", language),
                                    fontSize = 11.sp,
                                    color = MutedInk,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Alert Items List
            if (alerts.isEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = PanelBackground,
                        border = BorderStroke(1.dp, LineBorder),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = MutedInk,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No 3% movement alerts yet.",
                                fontSize = 14.sp,
                                color = MutedInk
                            )
                            Text(
                                text = "Approve rate changes of 3% or more to trigger alerts.",
                                fontSize = 12.sp,
                                color = MutedInk
                            )
                        }
                    }
                }
            } else {
                items(alerts) { alert ->
                    val dateFmt = Instant.ofEpochSecond(alert.createdAt)
                        .atZone(ZoneId.systemDefault())
                        .format(DateTimeFormatter.ofPattern("MMM dd, yyyy · HH:mm"))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = PanelBackground,
                        border = BorderStroke(1.dp, if (alert.isRise) Color(0xFFCFDDBB) else Color(0xFFF3C4B8)),
                        modifier = Modifier.fillMaxWidth().testTag("alert_item_${alert.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = if (alert.isRise) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                        contentDescription = null,
                                        tint = if (alert.isRise) PriceUp else PriceDown,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "%+.2f%% · %s".format(alert.deltaPercent, if (alert.isRise) "Rise" else "Fall"),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (alert.isRise) PriceUp else PriceDown
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${alert.commodity.replaceFirstChar { it.uppercase() }} · ${alert.market}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextInk
                                )
                                Text(
                                    text = "PKR %.2f → PKR %.2f / kg".format(alert.oldPricePkr, alert.newPricePkr),
                                    fontSize = 13.sp,
                                    color = TextInk
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "$dateFmt · ${alert.stream}",
                                    fontSize = 11.sp,
                                    color = MutedInk
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

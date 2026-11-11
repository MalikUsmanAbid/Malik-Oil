package com.malikoil.intelligence.ui.screens

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.malikoil.intelligence.model.LandedCostInput
import com.malikoil.intelligence.model.MoistureDeductionInput
import com.malikoil.intelligence.model.OilYieldInput
import com.malikoil.intelligence.ui.MarketViewModel
import com.malikoil.intelligence.ui.components.Translations
import com.malikoil.intelligence.ui.theme.*

@Composable
fun BusinessScreen(
    viewModel: MarketViewModel,
    modifier: Modifier = Modifier
) {
    val language by viewModel.language.collectAsState()
    val activeSubTab by viewModel.activeBusinessSubTab.collectAsState()

    // Landed Cost state
    val marginInput by viewModel.marginInput.collectAsState()
    val marginResult by viewModel.marginResult.collectAsState()

    var qtyText by remember(marginInput) { mutableStateOf(marginInput.quantityKg.toString()) }
    var buyText by remember(marginInput) { mutableStateOf(marginInput.buyPkrKg.toString()) }
    var sellText by remember(marginInput) { mutableStateOf(marginInput.sellPkrKg.toString()) }
    var transportText by remember(marginInput) { mutableStateOf(marginInput.transportPkr.toString()) }
    var otherText by remember(marginInput) { mutableStateOf(marginInput.otherCostsPkr.toString()) }

    // Oil Yield state
    val oilInput by viewModel.oilYieldInput.collectAsState()
    val oilResult by viewModel.oilYieldResult.collectAsState()

    var seedMaundText by remember(oilInput) { mutableStateOf(oilInput.seedQuantityMaund.toString()) }
    var seedPriceText by remember(oilInput) { mutableStateOf(oilInput.seedPricePerMaund.toString()) }
    var expellerCostText by remember(oilInput) { mutableStateOf(oilInput.crushingCostPerMaund.toString()) }
    var oilPctText by remember(oilInput) { mutableStateOf(oilInput.oilRecoveryPercent.toString()) }
    var khaliPctText by remember(oilInput) { mutableStateOf(oilInput.khaliRecoveryPercent.toString()) }
    var oilPriceText by remember(oilInput) { mutableStateOf(oilInput.oilSellingPriceKg.toString()) }
    var khaliPriceText by remember(oilInput) { mutableStateOf(oilInput.khaliSellingPriceMaund.toString()) }

    // Moisture Deduction state
    val moistureInput by viewModel.moistureInput.collectAsState()
    val moistureResult by viewModel.moistureResult.collectAsState()

    var moistGrossText by remember(moistureInput) { mutableStateOf(moistureInput.grossWeightMaund.toString()) }
    var moistRateText by remember(moistureInput) { mutableStateOf(moistureInput.quotedPriceMaund.toString()) }
    var moistActualText by remember(moistureInput) { mutableStateOf(moistureInput.actualMoisturePercent.toString()) }
    var moistStandardText by remember(moistureInput) { mutableStateOf(moistureInput.standardMoisturePercent.toString()) }
    var moistDirtText by remember(moistureInput) { mutableStateOf(moistureInput.foreignMatterPercent.toString()) }
    var moistBagsText by remember(moistureInput) { mutableStateOf(moistureInput.bagsCount.toString()) }
    var moistTareText by remember(moistureInput) { mutableStateOf(moistureInput.bagTareKgPerBag.toString()) }

    // Freight Vehicle capacity calculation
    var vehicleLoadMaunds by remember { mutableStateOf("175") } // Default Mazda 7 ton

    fun triggerMarginCalc() {
        val qty = qtyText.toDoubleOrNull() ?: 1000.0
        val buy = buyText.toDoubleOrNull() ?: 200.0
        val sell = sellText.toDoubleOrNull() ?: 210.0
        val trans = transportText.toDoubleOrNull() ?: 2000.0
        val other = otherText.toDoubleOrNull() ?: 500.0
        viewModel.updateMarginInput(
            LandedCostInput(
                quantityKg = qty,
                buyPkrKg = buy,
                sellPkrKg = sell,
                transportPkr = trans,
                otherCostsPkr = other
            )
        )
    }

    fun triggerOilYieldCalc() {
        val maunds = seedMaundText.toDoubleOrNull() ?: 25.0
        val price = seedPriceText.toDoubleOrNull() ?: 8600.0
        val expeller = expellerCostText.toDoubleOrNull() ?: 180.0
        val oilPct = oilPctText.toDoubleOrNull() ?: 34.0
        val khaliPct = khaliPctText.toDoubleOrNull() ?: 63.0
        val oilSell = oilPriceText.toDoubleOrNull() ?: 560.0
        val khaliSell = khaliPriceText.toDoubleOrNull() ?: 3400.0
        viewModel.updateOilYieldInput(
            OilYieldInput(
                seedQuantityMaund = maunds,
                seedPricePerMaund = price,
                crushingCostPerMaund = expeller,
                oilRecoveryPercent = oilPct,
                khaliRecoveryPercent = khaliPct,
                oilSellingPriceKg = oilSell,
                khaliSellingPriceMaund = khaliSell
            )
        )
    }

    fun triggerMoistureCalc() {
        val gross = moistGrossText.toDoubleOrNull() ?: 100.0
        val rate = moistRateText.toDoubleOrNull() ?: 8600.0
        val actual = moistActualText.toDoubleOrNull() ?: 11.0
        val standard = moistStandardText.toDoubleOrNull() ?: 8.0
        val dirt = moistDirtText.toDoubleOrNull() ?: 1.0
        val bags = moistBagsText.toIntOrNull() ?: 100
        val tare = moistTareText.toDoubleOrNull() ?: 1.0
        viewModel.updateMoistureInput(
            MoistureDeductionInput(
                grossWeightMaund = gross,
                quotedPriceMaund = rate,
                actualMoisturePercent = actual,
                standardMoisturePercent = standard,
                foreignMatterPercent = dirt,
                bagTareKgPerBag = tare,
                bagsCount = bags
            )
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SandBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Column {
                Text(
                    text = Translations.t("business", language),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MutedInk,
                    letterSpacing = 1.sp
                )
                Text(
                    text = when (activeSubTab) {
                        "oil_yield" -> "Oil & Khali Recovery Calculator"
                        "moisture" -> "Mandi Moisture & Tare Deduction"
                        "freight" -> "Punjab Mandi Freight & Distance"
                        else -> Translations.t("landed_cost_heading", language)
                    },
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextInk
                )
            }
        }

        // Sub-Tabs Selector Row (Horizontal Scroll)
        item {
            val tabs = listOf(
                "landed" to ("Landed Cost" to "کل لاگت"),
                "oil_yield" to ("Oil & Khali Yield" to "تیل اور کھل پیداوار"),
                "moisture" to ("Moisture & Tare" to "نمی اور کاٹ"),
                "freight" to ("Mandi Freight" to "منڈی کرایہ")
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth().testTag("business_subtabs_row")
            ) {
                items(tabs) { (key, labels) ->
                    val isSelected = activeSubTab == key
                    val label = if (language == "ur") labels.second else labels.first
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) ForestGreen else RaisedBackground,
                        border = BorderStroke(1.dp, if (isSelected) ForestGreen else LineBorder),
                        modifier = Modifier
                            .clickable { viewModel.selectBusinessSubTab(key) }
                            .testTag("subtab_$key")
                    ) {
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                            color = if (isSelected) Color.White else TextInk,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }

        // TAB 1: LANDED COST & SCENARIO MARGIN
        if (activeSubTab == "landed") {
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = PanelBackground,
                    border = BorderStroke(1.dp, LineBorder),
                    modifier = Modifier.fillMaxWidth().testTag("business_calc_card")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Cost & Price Variables",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextInk
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = qtyText,
                            onValueChange = {
                                qtyText = it
                                triggerMarginCalc()
                            },
                            label = { Text(Translations.t("quantity_kg", language)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth().testTag("input_margin_qty")
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = buyText,
                                onValueChange = {
                                    buyText = it
                                    triggerMarginCalc()
                                },
                                label = { Text(Translations.t("purchase_pkr", language)) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f).testTag("input_margin_buy")
                            )
                            OutlinedTextField(
                                value = sellText,
                                onValueChange = {
                                    sellText = it
                                    triggerMarginCalc()
                                },
                                label = { Text(Translations.t("sale_pkr", language)) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f).testTag("input_margin_sell")
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = transportText,
                                onValueChange = {
                                    transportText = it
                                    triggerMarginCalc()
                                },
                                label = { Text(Translations.t("transport_pkr", language)) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f).testTag("input_margin_transport")
                            )
                            OutlinedTextField(
                                value = otherText,
                                onValueChange = {
                                    otherText = it
                                    triggerMarginCalc()
                                },
                                label = { Text(Translations.t("other_costs_pkr", language)) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f).testTag("input_margin_other")
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = { triggerMarginCalc() },
                            colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                            modifier = Modifier.fillMaxWidth().testTag("btn_calculate_margin")
                        ) {
                            Text(Translations.t("calculate_margin", language), color = Color.White)
                        }
                    }
                }
            }

            // Results Card
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = PanelBackground,
                    border = BorderStroke(1.dp, LineBorder),
                    modifier = Modifier.fillMaxWidth().testTag("scenario_result_card")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Scenario Analysis Result",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = ForestGreen
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = RaisedBackground,
                                border = BorderStroke(1.dp, LineBorder),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(text = Translations.t("landed_cost", language), fontSize = 11.sp, color = MutedInk)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "PKR %,.0f".format(marginResult.landedCostPkr),
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextInk
                                    )
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = RaisedBackground,
                                border = BorderStroke(1.dp, LineBorder),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(text = Translations.t("revenue", language), fontSize = 11.sp, color = MutedInk)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "PKR %,.0f".format(marginResult.revenuePkr),
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextInk
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (marginResult.grossMarginPkr >= 0) PriceUpLight else PriceDownLight,
                                border = BorderStroke(1.dp, if (marginResult.grossMarginPkr >= 0) Color(0xFFCFDDBB) else Color(0xFFF3C4B8)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(text = Translations.t("gross_margin", language), fontSize = 11.sp, color = MutedInk)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "PKR %,.0f (%+.1f%%)".format(marginResult.grossMarginPkr, marginResult.marginPercent),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (marginResult.grossMarginPkr >= 0) PriceUp else PriceDown
                                    )
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = RaisedBackground,
                                border = BorderStroke(1.dp, LineBorder),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(text = Translations.t("break_even", language), fontSize = 11.sp, color = MutedInk)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "PKR %.2f / kg".format(marginResult.breakEvenPkrKg),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextInk
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = marginResult.notice, fontSize = 12.sp, color = MutedInk)
                    }
                }
            }
        }

        // TAB 2: OIL & KHALI RECOVERY CALCULATOR (تیل اور کھل کی پیداوار)
        if (activeSubTab == "oil_yield") {
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = PanelBackground,
                    border = BorderStroke(1.dp, LineBorder),
                    modifier = Modifier.fillMaxWidth().testTag("oil_yield_input_card")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Expeller Seed & Recovery Inputs",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextInk
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = seedMaundText,
                                onValueChange = {
                                    seedMaundText = it
                                    triggerOilYieldCalc()
                                },
                                label = { Text("Seed Quantity (Maunds / ۴۰ کلو)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f).testTag("input_oil_seed_maund")
                            )
                            OutlinedTextField(
                                value = seedPriceText,
                                onValueChange = {
                                    seedPriceText = it
                                    triggerOilYieldCalc()
                                },
                                label = { Text("Seed Rate (PKR / Maund)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f).testTag("input_oil_seed_price")
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = expellerCostText,
                                onValueChange = {
                                    expellerCostText = it
                                    triggerOilYieldCalc()
                                },
                                label = { Text("Crushing Cost (PKR/40kg)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f).testTag("input_oil_crushing_cost")
                            )
                            OutlinedTextField(
                                value = oilPctText,
                                onValueChange = {
                                    oilPctText = it
                                    triggerOilYieldCalc()
                                },
                                label = { Text("Oil Yield % (تیل %)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f).testTag("input_oil_recovery_pct")
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = khaliPctText,
                                onValueChange = {
                                    khaliPctText = it
                                    triggerOilYieldCalc()
                                },
                                label = { Text("Khali Yield % (کھل %)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f).testTag("input_oil_khali_pct")
                            )
                            OutlinedTextField(
                                value = oilPriceText,
                                onValueChange = {
                                    oilPriceText = it
                                    triggerOilYieldCalc()
                                },
                                label = { Text("Oil Sale (PKR/kg)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f).testTag("input_oil_sell_price")
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = khaliPriceText,
                            onValueChange = {
                                khaliPriceText = it
                                triggerOilYieldCalc()
                            },
                            label = { Text("Khali Sale (PKR / 40kg Bag)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth().testTag("input_oil_khali_price")
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = { triggerOilYieldCalc() },
                            colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                            modifier = Modifier.fillMaxWidth().testTag("btn_recalculate_oil_yield")
                        ) {
                            Text("Calculate Crushing Yield & Profit", color = Color.White)
                        }
                    }
                }
            }

            // Crushing Yield Result Summary Card
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = PanelBackground,
                    border = BorderStroke(1.dp, LineBorder),
                    modifier = Modifier.fillMaxWidth().testTag("oil_yield_result_card")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Crushing Extraction P&L",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = ForestGreen
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (oilResult.netProfitPkr >= 0) PriceUpLight else PriceDownLight
                            ) {
                                Text(
                                    text = if (oilResult.netProfitPkr >= 0) "PROFITABLE" else "LOSS DEFICIT",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (oilResult.netProfitPkr >= 0) PriceUp else PriceDown,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Net Profit banner
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (oilResult.netProfitPkr >= 0) PriceUpLight else PriceDownLight,
                            border = BorderStroke(1.dp, if (oilResult.netProfitPkr >= 0) Color(0xFFCFDDBB) else Color(0xFFF3C4B8)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = Translations.t("net_profit", language), fontSize = 11.sp, color = MutedInk)
                                    Text(
                                        text = "PKR %,.0f".format(oilResult.netProfitPkr),
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (oilResult.netProfitPkr >= 0) PriceUp else PriceDown
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(text = Translations.t("profit_per_maund", language), fontSize = 11.sp, color = MutedInk)
                                    Text(
                                        text = "PKR %+.1f / من".format(oilResult.profitPerMaundPkr),
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (oilResult.netProfitPkr >= 0) PriceUp else PriceDown
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Production & Revenue Breakdown
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = RaisedBackground,
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(text = "Oil Output (تیل)", fontSize = 11.sp, color = MutedInk)
                                    Text(
                                        text = "%,.1f kg".format(oilResult.oilProducedKg),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextInk
                                    )
                                    Text(
                                        text = "PKR %,.0f".format(oilResult.oilRevenuePkr),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = GoldAccent
                                    )
                                }
                            }
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = RaisedBackground,
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(text = "Khali Output (کھل)", fontSize = 11.sp, color = MutedInk)
                                    Text(
                                        text = "%,.1f bags (%.0f kg)".format(oilResult.khaliBags40kg, oilResult.khaliProducedKg),
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextInk
                                    )
                                    Text(
                                        text = "PKR %,.0f".format(oilResult.khaliRevenuePkr),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = ForestGreen
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Max Seed Bid Rate
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = PriceDownLight,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Maximum Break-Even Seed Purchase Rate (زیادہ سے زیادہ خرید قیمت):",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF725019)
                                )
                                Text(
                                    text = "PKR %,.0f / 40kg Maund (PKR %.1f/kg)".format(
                                        oilResult.breakEvenSeedPriceMaund,
                                        oilResult.breakEvenSeedPriceMaund / 40.0
                                    ),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF725019)
                                )
                                Text(
                                    text = "Bidding above this rate in the mandi will result in negative crushing margins.",
                                    fontSize = 10.sp,
                                    color = MutedInk
                                )
                            }
                        }
                    }
                }
            }
        }

        // TAB 3: MOISTURE & QUALITY TARE DEDUCTION (کاٹ، نمی اور باردانہ)
        if (activeSubTab == "moisture") {
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = PanelBackground,
                    border = BorderStroke(1.dp, LineBorder),
                    modifier = Modifier.fillMaxWidth().testTag("moisture_input_card")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Mandi Moisture & Tare Parameters",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextInk
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = moistGrossText,
                                onValueChange = {
                                    moistGrossText = it
                                    triggerMoistureCalc()
                                },
                                label = { Text("Gross Maunds (کل من)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f).testTag("input_moist_gross")
                            )
                            OutlinedTextField(
                                value = moistRateText,
                                onValueChange = {
                                    moistRateText = it
                                    triggerMoistureCalc()
                                },
                                label = { Text("Quoted Rate (PKR/40kg)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f).testTag("input_moist_rate")
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = moistActualText,
                                onValueChange = {
                                    moistActualText = it
                                    triggerMoistureCalc()
                                },
                                label = { Text("Actual Moisture % (نمی)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f).testTag("input_moist_actual")
                            )
                            OutlinedTextField(
                                value = moistStandardText,
                                onValueChange = {
                                    moistStandardText = it
                                    triggerMoistureCalc()
                                },
                                label = { Text("Standard % (معیار)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f).testTag("input_moist_standard")
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = moistDirtText,
                                onValueChange = {
                                    moistDirtText = it
                                    triggerMoistureCalc()
                                },
                                label = { Text("Dirt / Foreign % (کاٹ)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.weight(1f).testTag("input_moist_dirt")
                            )
                            OutlinedTextField(
                                value = moistBagsText,
                                onValueChange = {
                                    moistBagsText = it
                                    triggerMoistureCalc()
                                },
                                label = { Text("Bag Count (بوریاں)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f).testTag("input_moist_bags")
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = moistTareText,
                            onValueChange = {
                                moistTareText = it
                                triggerMoistureCalc()
                            },
                            label = { Text("Bag Tare (باردانہ کٹوتی kg/bag)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth().testTag("input_moist_tare")
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = { triggerMoistureCalc() },
                            colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                            modifier = Modifier.fillMaxWidth().testTag("btn_recalculate_moisture")
                        ) {
                            Text("Calculate Net Weight & Deductions", color = Color.White)
                        }
                    }
                }
            }

            // Moisture Deduction Result Card
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = PanelBackground,
                    border = BorderStroke(1.dp, LineBorder),
                    modifier = Modifier.fillMaxWidth().testTag("moisture_result_card")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Net Settlement & Billable Weight",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = ForestGreen
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Net Payable Summary
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = PriceUpLight,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = "Net Payable Amount (خالص رقم):", fontSize = 11.sp, color = MutedInk)
                                    Text(
                                        text = "PKR %,.0f".format(moistureResult.netPayablePkr),
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ForestGreenDark
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(text = "Deduction Savings (بچت):", fontSize = 11.sp, color = MutedInk)
                                    Text(
                                        text = "PKR %,.0f".format(moistureResult.totalDeductionSavingsPkr),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PriceDown
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Weights Breakdown (2 columns)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = RaisedBackground,
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(text = "Total Deduction Cut", fontSize = 11.sp, color = MutedInk)
                                    Text(
                                        text = "%,.1f kg".format(moistureResult.totalDeductionKg),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = PriceDown
                                    )
                                    Text(
                                        text = "Moisture: %.1fkg | Tare: %.1fkg".format(
                                            moistureResult.moistureCutKg,
                                            moistureResult.bagTareCutKg
                                        ),
                                        fontSize = 10.sp,
                                        color = MutedInk
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = RaisedBackground,
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(text = "Net Billable Weight", fontSize = 11.sp, color = MutedInk)
                                    Text(
                                        text = "%,.2f Maunds".format(moistureResult.netBillableMaund),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextInk
                                    )
                                    Text(
                                        text = "%,.0f kg net".format(moistureResult.netBillableWeightKg),
                                        fontSize = 11.sp,
                                        color = ForestGreen
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Effective Cost per Maund Banner
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = PriceDownLight,
                            border = BorderStroke(1.dp, Color(0xFFE2D1AC)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Effective Cost per Maund (اصل لاگت فی من):",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF725019)
                                    )
                                    Text(
                                        text = "PKR %,.0f / من (PKR %.2f/kg)".format(
                                            moistureResult.effectivePricePerMaundPkr,
                                            moistureResult.effectivePricePerMaundPkr / 40.0
                                        ),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF725019)
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFF725019)
                                ) {
                                    Text(
                                        text = "SAVED %,.0f".format(moistureResult.totalDeductionSavingsPkr),
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // TAB 4: PUNJAB MANDI FREIGHT & DISTANCE DIRECTORY (منڈی کرایہ اور فاصلہ)
        if (activeSubTab == "freight") {
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = PanelBackground,
                    border = BorderStroke(1.dp, LineBorder),
                    modifier = Modifier.fillMaxWidth().testTag("freight_guide_card")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Punjab Grain Mandis · Freight from Samundri",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextInk
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Estimated truck and Mazda freight per 40kg maund from regional procurement mandis to Samundri mill gate.",
                            fontSize = 12.sp,
                            color = MutedInk
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Vehicle load estimator
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = vehicleLoadMaunds,
                                onValueChange = { vehicleLoadMaunds = it },
                                label = { Text("Trip Load (Maunds / من)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f).testTag("input_vehicle_load")
                            )

                            val loadMaunds = vehicleLoadMaunds.toDoubleOrNull() ?: 175.0
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "Total Truck Load:", fontSize = 11.sp, color = MutedInk)
                                Text(
                                    text = "%,.1f Tons".format(loadMaunds * 40.0 / 1000.0),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ForestGreen
                                )
                            }
                        }

                        // Quick Vehicle Presets
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(text = "Quick Vehicle Presets (گاڑی کا انتخاب):", fontSize = 11.sp, color = MutedInk)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val presets = listOf(
                                "Shahzore (90 من)" to "90",
                                "Mazda (175 من)" to "175",
                                "6-Wheeler (375 من)" to "375"
                            )
                            presets.forEach { (label, maunds) ->
                                val isSelected = vehicleLoadMaunds == maunds
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) ForestGreen else RaisedBackground,
                                    border = BorderStroke(1.dp, if (isSelected) ForestGreen else LineBorder),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { vehicleLoadMaunds = maunds }
                                        .testTag("btn_vehicle_preset_$maunds")
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else TextInk,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // List of Punjab Mandis
            items(viewModel.mandiFreightList) { mandi ->
                val load = vehicleLoadMaunds.toDoubleOrNull() ?: 175.0
                val totalTripCost = load * mandi.defaultFreightPerMaund

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = PanelBackground,
                    border = BorderStroke(1.dp, LineBorder),
                    modifier = Modifier.fillMaxWidth().testTag("mandi_freight_${mandi.mandi}")
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = mandi.mandi,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextInk
                            )
                            Text(
                                text = "${mandi.distanceKm} km · ${mandi.typicalTransitHours}",
                                fontSize = 12.sp,
                                color = MutedInk
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = if (mandi.defaultFreightPerMaund > 0) "PKR %.0f / من".format(mandi.defaultFreightPerMaund) else "Local Hub",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (mandi.defaultFreightPerMaund > 0) ForestGreenDark else ForestGreen
                            )
                            if (mandi.defaultFreightPerMaund > 0) {
                                Text(
                                    text = "Trip: PKR %,.0f".format(totalTripCost),
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

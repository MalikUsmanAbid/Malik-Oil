package com.malikoil.intelligence.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.malikoil.intelligence.model.ResearchSession
import com.malikoil.intelligence.ui.MarketViewModel
import com.malikoil.intelligence.ui.components.Translations
import com.malikoil.intelligence.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResearchScreen(
    viewModel: MarketViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val language by viewModel.language.collectAsState()
    val sessions by viewModel.researchSessions.collectAsState()
    val activeSessionId by viewModel.activeSessionId.collectAsState()
    val isResearching by viewModel.isResearching.collectAsState()

    var questionText by remember { mutableStateOf("") }
    var selectedCommodity by remember { mutableStateOf("sarson") }
    var marketText by remember { mutableStateOf("Samundri") }
    var researchDepth by remember { mutableStateOf("quick") }
    var showPlugins by remember { mutableStateOf(false) }

    // Plugin states
    var converterAmount by remember { mutableStateOf("8640") }
    var converterUnit by remember { mutableStateOf("40kg") }
    var converterOutput by remember { mutableStateOf("") }

    var checkerText by remember { mutableStateOf("sarson | Samundri | standard | 8640 | 40kg | 2026-11-08T14:30:00+05:00") }
    var checkerResult by remember { mutableStateOf("") }

    val activeSession = remember(sessions, activeSessionId) {
        sessions.find { it.id == activeSessionId } ?: sessions.firstOrNull()
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
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Surface(
                        color = PriceUpLight,
                        shape = RoundedCornerShape(4.dp),
                        border = BorderStroke(1.dp, ForestGreen.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = "SOURCE-LED AI",
                            color = ForestGreen,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = Translations.t("research_heading", language),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextInk
                    )
                }

                // Actions: Insights + Plugins
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilledTonalButton(
                        onClick = { viewModel.setTab("insights") },
                        colors = ButtonDefaults.filledTonalButtonColors(containerColor = PriceUpLight, contentColor = ForestGreenDark),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("btn_research_to_insights")
                    ) {
                        Icon(Icons.Default.Newspaper, contentDescription = null, modifier = Modifier.size(16.dp), tint = ForestGreen)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = Translations.t("insights", language), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = { showPlugins = !showPlugins },
                        modifier = Modifier.testTag("btn_toggle_plugins")
                    ) {
                        Icon(Icons.Default.Extension, contentDescription = null, modifier = Modifier.size(16.dp), tint = GoldAccent)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = Translations.t("plugins", language), fontSize = 12.sp, color = TextInk)
                    }
                }
            }
        }

        // Sessions Bar
        item {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = PanelBackground,
                border = BorderStroke(1.dp, LineBorder),
                modifier = Modifier.fillMaxWidth().testTag("research_sessions_bar")
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${Translations.t("sessions", language)} (${sessions.size})",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextInk
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(
                                onClick = { viewModel.createNewResearchSession() },
                                modifier = Modifier.testTag("btn_new_research_session")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(Translations.t("new_research", language), fontSize = 12.sp)
                            }
                            if (activeSession != null) {
                                TextButton(
                                    onClick = { viewModel.deleteResearchSession(activeSession.id) },
                                    modifier = Modifier.testTag("btn_delete_session")
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp), tint = PriceDown)
                                }
                            }
                        }
                    }

                    // Session horizontal chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    ) {
                        items(sessions) { session ->
                            val isSelected = session.id == activeSession?.id
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) PriceUpLight else RaisedBackground,
                                border = BorderStroke(1.dp, if (isSelected) ForestGreen else LineBorder),
                                modifier = Modifier
                                    .clickable { viewModel.selectResearchSession(session.id) }
                                    .testTag("session_chip_${session.id}")
                            ) {
                                Text(
                                    text = session.title.ifBlank { "Research Session" },
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) ForestGreenDark else TextInk,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Optional Plugins Drawer
        if (showPlugins) {
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = RaisedBackground,
                    border = BorderStroke(1.dp, LineBorder),
                    modifier = Modifier.fillMaxWidth().testTag("plugins_drawer")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Built-in Research Plugins",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextInk
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        // Rate Converter Plugin
                        Text(
                            text = Translations.t("rate_converter", language),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ForestGreen
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = converterAmount,
                                onValueChange = { converterAmount = it },
                                label = { Text("Amount PKR") },
                                modifier = Modifier.weight(1f).testTag("input_converter_amount")
                            )
                            OutlinedTextField(
                                value = converterUnit,
                                onValueChange = { converterUnit = it },
                                label = { Text("Unit") },
                                modifier = Modifier.weight(0.7f).testTag("input_converter_unit")
                            )
                            Button(
                                onClick = {
                                    val amt = converterAmount.toDoubleOrNull() ?: 8000.0
                                    val factor = if (converterUnit == "kg") 1.0 else if (converterUnit == "100kg") 100.0 else 40.0
                                    val pkrKg = amt / factor
                                    converterOutput = "PKR %.2f / kg · PKR %.0f / 40kg · PKR %.0f / 100kg".format(
                                        pkrKg, pkrKg * 40.0, pkrKg * 100.0
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                                modifier = Modifier.testTag("btn_run_converter")
                            ) {
                                Text("Convert", fontSize = 12.sp)
                            }
                        }
                        if (converterOutput.isNotBlank()) {
                            Text(
                                text = converterOutput,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextInk,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = LineBorder)
                        Spacer(modifier = Modifier.height(14.dp))

                        // WhatsApp Row Checker Plugin
                        Text(
                            text = Translations.t("whatsapp_row_checker", language),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ForestGreen
                        )
                        Text(
                            text = "commodity | market | grade | amount | unit | ISO timestamp",
                            fontSize = 10.sp,
                            color = MutedInk
                        )
                        OutlinedTextField(
                            value = checkerText,
                            onValueChange = { checkerText = it },
                            modifier = Modifier.fillMaxWidth().padding(top = 6.dp).testTag("input_row_checker")
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    val parsed = viewModel.repository.checkAndParseRows(checkerText)
                                    checkerResult = "${parsed.size} valid rows verified and ready for review."
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = RaisedBackground, contentColor = TextInk),
                                border = BorderStroke(1.dp, LineBorder),
                                modifier = Modifier.testTag("btn_check_rows")
                            ) {
                                Text("Check Rows", fontSize = 12.sp)
                            }
                            Button(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(checkerText))
                                    Toast.makeText(context, "Rows copied to clipboard", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = RaisedBackground, contentColor = TextInk),
                                border = BorderStroke(1.dp, LineBorder),
                                modifier = Modifier.testTag("btn_copy_checked_rows")
                            ) {
                                Text("Copy Rows", fontSize = 12.sp)
                            }
                        }
                        if (checkerResult.isNotBlank()) {
                            Text(text = checkerResult, fontSize = 12.sp, color = ForestGreen, modifier = Modifier.padding(top = 4.dp))
                        }
                    }
                }
            }
        }

        // Active Turns in Conversation
        if (activeSession != null && activeSession.turns.isNotEmpty()) {
            items(activeSession.turns) { turn ->
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Question bubble
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFFEEF0ED),
                            modifier = Modifier.widthIn(max = 300.dp).testTag("question_bubble")
                        ) {
                            Text(
                                text = turn.question,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = ForestGreenDark,
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                    }

                    // Answer panel
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = PanelBackground,
                        border = BorderStroke(1.dp, LineBorder),
                        modifier = Modifier.fillMaxWidth().testTag("answer_panel")
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Analyzed in ${turn.durationMs / 1000.0}s · ${turn.commodity} / ${turn.market}",
                                    fontSize = 11.sp,
                                    color = MutedInk
                                )
                                IconButton(
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(turn.text))
                                        Toast.makeText(context, "Answer copied", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = MutedInk, modifier = Modifier.size(16.dp))
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = turn.text,
                                fontSize = 15.sp,
                                lineHeight = 22.sp,
                                color = TextInk
                            )

                            // Findings with inline citation references
                            if (turn.findings.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Key Verified Findings:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ForestGreen
                                )
                                turn.findings.forEach { finding ->
                                    Row(
                                        modifier = Modifier.padding(top = 4.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Text(text = "• ", fontSize = 13.sp, color = ForestGreen)
                                        Text(
                                            text = finding.summary + " [${finding.sourceId}]",
                                            fontSize = 13.sp,
                                            lineHeight = 18.sp,
                                            color = TextInk
                                        )
                                    }
                                }
                            }

                            // Cited Sources
                            if (turn.sources.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = "Dated Sources & Citations:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ForestGreen
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                turn.sources.forEach { source ->
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = RaisedBackground,
                                        border = BorderStroke(1.dp, LineBorder),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                            .clickable {
                                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(source.url))
                                                context.startActivity(intent)
                                            }
                                            .testTag("source_card_${source.id}")
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Text(
                                                text = "[${source.id}] ${source.title}",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = ForestGreenDark
                                            )
                                            Text(
                                                text = "${source.publisher} · ${source.publishedAt}",
                                                fontSize = 11.sp,
                                                color = MutedInk
                                            )

                                            // Evidence quotes
                                            val quotes = turn.supports.filter { it.sourceId == source.id }
                                            quotes.forEach { q ->
                                                Text(
                                                    text = "“${q.quote}”",
                                                    fontSize = 11.sp,
                                                    color = TextInk.copy(alpha = 0.8f),
                                                    modifier = Modifier.padding(top = 4.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Question Composer Box
        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = PanelBackground,
                border = BorderStroke(1.dp, ForestGreen.copy(alpha = 0.4f)),
                shadowElevation = 4.dp,
                modifier = Modifier.fillMaxWidth().testTag("research_composer_box")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = Translations.t("ask_question", language),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MutedInk
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = questionText,
                        onValueChange = { questionText = it },
                        placeholder = { Text("e.g. Latest mustard arrivals and crushing margins in Punjab") },
                        minLines = 2,
                        maxLines = 4,
                        modifier = Modifier.fillMaxWidth().testTag("input_research_question")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Commodity, Mandi, Depth selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = selectedCommodity,
                            onValueChange = { selectedCommodity = it },
                            label = { Text(Translations.t("commodity", language)) },
                            modifier = Modifier.weight(1f).testTag("input_research_commodity")
                        )
                        OutlinedTextField(
                            value = marketText,
                            onValueChange = { marketText = it },
                            label = { Text(Translations.t("mandi", language)) },
                            modifier = Modifier.weight(1f).testTag("input_research_market")
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            viewModel.runResearch(questionText, selectedCommodity, marketText, researchDepth)
                            questionText = ""
                        },
                        enabled = !isResearching && questionText.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                        modifier = Modifier.fillMaxWidth().testTag("btn_run_research")
                    ) {
                        if (isResearching) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Searching Web Evidence…", color = Color.White)
                        } else {
                            Icon(Icons.Default.TravelExplore, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(Translations.t("research_web", language), color = Color.White)
                        }
                    }

                    // Prompt suggestions
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf(
                            "prompt_news" to "News",
                            "prompt_supply" to "Supply",
                            "prompt_verify" to "Verify"
                        ).forEach { (key, label) ->
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = RaisedBackground,
                                border = BorderStroke(1.dp, LineBorder),
                                modifier = Modifier
                                    .clickable {
                                        questionText = "${Translations.t(key, language)} — $selectedCommodity / $marketText"
                                    }
                                    .testTag("prompt_chip_$label")
                            ) {
                                Text(
                                    text = Translations.t(key, language),
                                    fontSize = 11.sp,
                                    color = ForestGreenDark,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
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

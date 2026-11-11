package com.malikoil.intelligence.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Schedule
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
import com.malikoil.intelligence.ui.MarketViewModel
import com.malikoil.intelligence.ui.components.Translations
import com.malikoil.intelligence.ui.theme.*
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun ReportsScreen(
    viewModel: MarketViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val language by viewModel.language.collectAsState()
    val dailyReports by viewModel.dailyReports.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val sarsonOnly by viewModel.sarsonOnly.collectAsState()
    val nearbyOnly by viewModel.nearbyOnly.collectAsState()

    var sourceLabelText by remember { mutableStateOf("Owner reviewed/manual") }
    var reportRowsText by remember { mutableStateOf("") }
    var reviewConfirmed by remember { mutableStateOf(false) }

    // Simulation fields
    var simGroupText by remember { mutableStateOf("TEST Ramzan group") }
    var simMsgText by remember { mutableStateOf("Samundri mandi: sarson dry seed 8640 per 40kg, fresh arrivals 300 bags.") }

    var selectedSnapshotDate by remember(dailyReports) {
        mutableStateOf(dailyReports.firstOrNull()?.date)
    }
    val currentSnapshot = remember(dailyReports, selectedSnapshotDate) {
        dailyReports.find { it.date == selectedSnapshotDate } ?: dailyReports.firstOrNull()
    }

    val filteredNotifications = remember(notifications, sarsonOnly, nearbyOnly) {
        notifications.filter {
            (!sarsonOnly || it.isSarson) && (!nearbyOnly || it.cities.isNotEmpty())
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SandBackground)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {com.malikoil.intelligence.ui.components.AutomationControls(viewModel,currentSnapshot?.summary,language)}
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = Translations.t("reports", language),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MutedInk,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Daily Excel Reports",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextInk
                    )
                }

                Surface(
                    color = PriceUpLight,
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, Color(0xFFCFDDBB))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(12.dp), tint = ForestGreen)
                        Text(
                            text = "18:00 Asia/Karachi",
                            color = ForestGreenDark,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Daily Reports Generation & Saved Snapshots
        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = PanelBackground,
                border = BorderStroke(1.dp, LineBorder),
                modifier = Modifier.fillMaxWidth().testTag("daily_reports_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Daily Summary & Snapshots",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextInk
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Generate and export encrypted daily market summaries. Contains all approved rate observations.",
                        fontSize = 12.sp,
                        color = MutedInk
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.generateDailyReport()
                                Toast.makeText(context, "Today's report generated!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                            modifier = Modifier.weight(1f).testTag("btn_generate_daily_report")
                        ) {
                            Icon(Icons.Default.Assessment, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Generate Today", fontSize = 13.sp)
                        }

                        Button(
                            onClick = {
                                if (currentSnapshot != null) {
                                    clipboardManager.setText(AnnotatedString(currentSnapshot.rawCsv))
                                    Toast.makeText(context, "Report CSV copied to clipboard (${currentSnapshot.date})", Toast.LENGTH_SHORT).show()
                                }
                            },
                            enabled = currentSnapshot != null,
                            colors = ButtonDefaults.buttonColors(containerColor = RaisedBackground, contentColor = TextInk),
                            border = BorderStroke(1.dp, LineBorder),
                            modifier = Modifier.weight(1f).testTag("btn_download_daily_report")
                        ) {
                            Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Copy CSV", fontSize = 13.sp)
                        }
                    }

                    // Display Current Snapshot Summary
                    if (currentSnapshot != null) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = RaisedBackground,
                            border = BorderStroke(1.dp, LineBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Report: ${currentSnapshot.date}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ForestGreen
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = currentSnapshot.summary,
                                    fontSize = 12.sp,
                                    lineHeight = 18.sp,
                                    color = TextInk
                                )
                            }
                        }
                    }
                }
            }
        }

        // Review Rates for Reports
        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = PanelBackground,
                border = BorderStroke(1.dp, LineBorder),
                modifier = Modifier.fillMaxWidth().testTag("review_rates_report_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Review Rates for Reports",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextInk
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "commodity | market | grade | amount | unit | ISO date + timezone",
                        fontSize = 11.sp,
                        color = MutedInk
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = sourceLabelText,
                        onValueChange = { sourceLabelText = it },
                        label = { Text("Source Label") },
                        modifier = Modifier.fillMaxWidth().testTag("input_report_source_label")
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = reportRowsText,
                        onValueChange = { reportRowsText = it },
                        label = { Text("Rate rows") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth().testTag("input_report_rows")
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().clickable { reviewConfirmed = !reviewConfirmed }
                    ) {
                        Checkbox(
                            checked = reviewConfirmed,
                            onCheckedChange = { reviewConfirmed = it },
                            modifier = Modifier.testTag("chk_report_review_confirmed")
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = Translations.t("confirm_reviewed", language),
                            fontSize = 12.sp,
                            color = TextInk
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            if (!reviewConfirmed) {
                                Toast.makeText(context, "Please confirm the review check first", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            viewModel.approveDailyRows(reportRowsText, sourceLabelText)
                            Toast.makeText(context, "Rows approved for reports and review queue", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                        modifier = Modifier.fillMaxWidth().testTag("btn_approve_report_rows")
                    ) {
                        Text(Translations.t("approve_daily_rows", language), color = Color.White)
                    }
                }
            }
        }

        // WhatsApp Notification Simulator & Filter
        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = PanelBackground,
                border = BorderStroke(1.dp, LineBorder),
                modifier = Modifier.fillMaxWidth().testTag("notification_simulator_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "WhatsApp Group Notification Reader (Simulated)",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextInk
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = Translations.t("whatsapp_preview_note", language),
                        fontSize = 12.sp,
                        color = MutedInk
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    // Filter checkboxes
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = sarsonOnly,
                                onCheckedChange = { viewModel.toggleSarsonOnly(it) },
                                modifier = Modifier.testTag("chk_sarson_only")
                            )
                            Text(text = "Sarson keyword", fontSize = 12.sp, color = TextInk)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = nearbyOnly,
                                onCheckedChange = { viewModel.toggleNearbyOnly(it) },
                                modifier = Modifier.testTag("chk_nearby_only")
                            )
                            Text(text = "Priority towns", fontSize = 12.sp, color = TextInk)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = simGroupText,
                        onValueChange = { simGroupText = it },
                        label = { Text("Selected Group Title") },
                        modifier = Modifier.fillMaxWidth().testTag("input_sim_group")
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = simMsgText,
                        onValueChange = { simMsgText = it },
                        label = { Text("Sample Notification Text") },
                        modifier = Modifier.fillMaxWidth().testTag("input_sim_msg")
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            viewModel.simulateNotification(simGroupText, simMsgText)
                            Toast.makeText(context, "Sample notification received!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RaisedBackground, contentColor = TextInk),
                        border = BorderStroke(1.dp, LineBorder),
                        modifier = Modifier.fillMaxWidth().testTag("btn_receive_sim_msg")
                    ) {
                        Text(Translations.t("receive_sim", language))
                    }
                }
            }
        }

        // Captured Notifications List
        item {
            Text(
                text = "Unreviewed Received Notifications (${filteredNotifications.size})",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = ForestGreen
            )
        }

        items(filteredNotifications) { item ->
            val dateFmt = Instant.ofEpochSecond(item.capturedAt)
                .atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("HH:mm · MMM dd"))

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = PanelBackground,
                border = BorderStroke(1.dp, LineBorder),
                modifier = Modifier.fillMaxWidth().testTag("notification_item_${item.id}")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = item.group,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = ForestGreenDark
                        )
                        Text(text = dateFmt, fontSize = 11.sp, color = MutedInk)
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.text,
                        fontSize = 13.sp,
                        color = TextInk
                    )

                    if (item.cities.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Detected nearby: ${item.cities.joinToString(", ")}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = GoldAccent
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                reportRowsText = com.malikoil.intelligence.MarketRateParser.drafts(item.text,viewModel.repository.commodities.value.map{it.id}).ifBlank{item.text}
                                sourceLabelText = "WhatsApp: ${item.group}"
                                Toast.makeText(context, "Loaded into review form above.", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = RaisedBackground, contentColor = TextInk),
                            border = BorderStroke(1.dp, LineBorder),
                            modifier = Modifier.testTag("btn_load_row_${item.id}")
                        ) {
                            Text("Use in Review", fontSize = 11.sp)
                        }

                        Button(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(item.text))
                                Toast.makeText(context, "Message copied", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = RaisedBackground, contentColor = TextInk),
                            border = BorderStroke(1.dp, LineBorder)
                        ) {
                            Text("Copy", fontSize = 11.sp)
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

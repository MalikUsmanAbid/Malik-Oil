package com.malikoil.intelligence.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Rule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
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
fun ReviewScreen(
    viewModel: MarketViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val language by viewModel.language.collectAsState()
    val candidates by viewModel.candidates.collectAsState()

    var importSource by remember { mutableStateOf("WhatsApp owner export") }
    var importText by remember { mutableStateOf("sarson | Lahore | standard | 8240 | 40kg | 2026-11-08T10:00:00+05:00") }

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
                        text = Translations.t("review", language),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MutedInk,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Review Before Publishing",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextInk
                    )
                }

                Surface(
                    color = PriceDownLight,
                    shape = RoundedCornerShape(6.dp),
                    border = BorderStroke(1.dp, Color(0xFFE2D1AC))
                ) {
                    Text(
                        text = "${candidates.size} PENDING",
                        color = Color(0xFF725019),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        item {
            Text(
                text = "Inspect the commodity, unit, and source before a rate enters verified market history.",
                fontSize = 13.sp,
                color = MutedInk
            )
        }

        // Candidates List
        if (candidates.isEmpty()) {
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
                            imageVector = Icons.Default.Rule,
                            contentDescription = null,
                            tint = ForestGreen,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No pending candidates for review.",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextInk
                        )
                        Text(
                            text = "Import rate text below or add a rate from the Market tab.",
                            fontSize = 12.sp,
                            color = MutedInk
                        )
                    }
                }
            }
        } else {
            items(candidates) { candidate ->
                val dateFmt = Instant.ofEpochSecond(candidate.observedAt)
                    .atZone(ZoneId.systemDefault())
                    .format(DateTimeFormatter.ofPattern("MMM dd, HH:mm"))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = PanelBackground,
                    border = BorderStroke(1.dp, LineBorder),
                    modifier = Modifier.fillMaxWidth().testTag("candidate_card_${candidate.id}")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column {
                                Text(
                                    text = "${candidate.commodity.replaceFirstChar { it.uppercase() }} · ${candidate.market}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextInk
                                )
                                Text(
                                    text = "${candidate.grade} · ${candidate.source}",
                                    fontSize = 12.sp,
                                    color = MutedInk
                                )
                            }
                            Text(
                                text = dateFmt,
                                fontSize = 11.sp,
                                color = MutedInk
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "PKR %.2f / %s".format(candidate.amount, candidate.unit),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextInk
                                )
                                Text(
                                    text = "→ PKR %.2f / kg equivalent".format(candidate.pricePaisaKg / 100.0),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = ForestGreen
                                )
                            }

                            Button(
                                onClick = {
                                    viewModel.approveCandidate(candidate.id)
                                    Toast.makeText(context, "Rate approved!", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                                modifier = Modifier.testTag("btn_approve_${candidate.id}")
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Approve")
                            }
                        }
                    }
                }
            }
        }

        // WhatsApp Import Section
        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = PanelBackground,
                border = BorderStroke(1.dp, LineBorder),
                modifier = Modifier.fillMaxWidth().testTag("whatsapp_import_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "WhatsApp Export / Shared Text",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextInk
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Paste rate rows in explicit format:\ncommodity | market | grade | amount | unit | ISO timestamp",
                        fontSize = 11.sp,
                        color = MutedInk
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = importSource,
                        onValueChange = { importSource = it },
                        label = { Text("Source Label") },
                        modifier = Modifier.fillMaxWidth().testTag("input_import_source")
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = importText,
                        onValueChange = { importText = it },
                        label = { Text("Paste rate rows") },
                        minLines = 3,
                        maxLines = 6,
                        modifier = Modifier.fillMaxWidth().testTag("input_import_text")
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            viewModel.approveDailyRows(importText, importSource)
                            Toast.makeText(context, "Candidates created from pasted text.", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = RaisedBackground, contentColor = TextInk),
                        border = BorderStroke(1.dp, LineBorder),
                        modifier = Modifier.fillMaxWidth().testTag("btn_import_rows")
                    ) {
                        Text("Create Review Candidates", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

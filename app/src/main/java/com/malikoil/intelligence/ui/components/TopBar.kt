package com.malikoil.intelligence.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.malikoil.intelligence.R
import com.malikoil.intelligence.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MalikTopAppBar(
    selectedLanguage: String,
    onLanguageSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Surface(
        color = Color(0xFF08271A),
        shadowElevation = 4.dp,
        modifier = modifier
            .fillMaxWidth()
            .testTag("malik_top_bar")
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    androidx.compose.ui.graphics.Brush.verticalGradient(
                        listOf(Color(0xFF041910), Color(0xFF0C4128))
                    )
                )
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Brand Mark + Title + Live Mandi Pill
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White)
                            .border(1.5.dp, GoldBright.copy(alpha = 0.6f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.brand_icon),
                            contentDescription = "Malik Oil Brand Icon",
                            modifier = Modifier.size(34.dp)
                        )
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "MALIK OIL",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFFFFDF8),
                                letterSpacing = 0.5.sp
                            )
                            // Live status dot
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF144D31),
                                border = androidx.compose.foundation.BorderStroke(1.dp, ChartCanolaGreen.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Surface(
                                        shape = androidx.compose.foundation.shape.CircleShape,
                                        color = ChartCanolaGreen,
                                        modifier = Modifier.size(6.dp)
                                    ) {}
                                    Text(
                                        text = Translations.t("market_live_badge", selectedLanguage).uppercase(),
                                        color = ChartCanolaGreen,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Text(
                            text = Translations.t("home_sub", selectedLanguage) + " · Mill Intelligence",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFB4C8BD)
                        )
                    }
                }

                // Language Selector Dropdown (Frosted Pill)
                Box {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White.copy(alpha = 0.12f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)),
                        modifier = Modifier
                            .clickable { expanded = true }
                            .testTag("language_selector_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = "Language",
                                tint = GoldBright,
                                modifier = Modifier.size(15.dp)
                            )
                            val langLabel = when (selectedLanguage) {
                                "ur" -> "اردو"
                                "roman" -> "Roman"
                                else -> "English"
                            }
                            Text(
                                text = langLabel,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(PanelBackground)
                    ) {
                        DropdownMenuItem(
                            text = { Text("English", color = TextInk, fontWeight = FontWeight.Medium) },
                            onClick = {
                                onLanguageSelected("en")
                                expanded = false
                            },
                            modifier = Modifier.testTag("lang_option_en")
                        )
                        DropdownMenuItem(
                            text = { Text("اردو (Urdu)", color = TextInk, fontWeight = FontWeight.Medium) },
                            onClick = {
                                onLanguageSelected("ur")
                                expanded = false
                            },
                            modifier = Modifier.testTag("lang_option_ur")
                        )
                        DropdownMenuItem(
                            text = { Text("Roman Urdu", color = TextInk, fontWeight = FontWeight.Medium) },
                            onClick = {
                                onLanguageSelected("roman")
                                expanded = false
                            },
                            modifier = Modifier.testTag("lang_option_roman")
                        )
                    }
                }
            }
        }
    }
}

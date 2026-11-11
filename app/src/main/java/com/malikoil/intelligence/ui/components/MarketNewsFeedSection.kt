package com.malikoil.intelligence.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.malikoil.intelligence.model.MarketNewsArticle
import com.malikoil.intelligence.model.MarketSentimentSummary
import com.malikoil.intelligence.model.SearchGroundingStatus
import com.malikoil.intelligence.ui.MarketViewModel
import com.malikoil.intelligence.ui.theme.*

@Composable
fun MarketNewsFeedSection(
    viewModel: MarketViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val language by viewModel.language.collectAsState()
    val articles by viewModel.filteredNewsArticles.collectAsState()
    val isNewsLoading by viewModel.isNewsLoading.collectAsState()
    val sentiment by viewModel.marketSentiment.collectAsState()
    val groundingStatus by viewModel.searchGroundingStatus.collectAsState()
    val selectedCategory by viewModel.selectedNewsCategory.collectAsState()
    val searchQuery by viewModel.newsSearchQuery.collectAsState()

    var isExpandedList by remember { mutableStateOf(false) }

    val categories = listOf(
        "all" to "all_news",
        "sarson" to "cat_sarson",
        "canola" to "cat_canola",
        "crushing" to "cat_crushing",
        "policy" to "cat_policy"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("market_news_feed_section"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Section Header with Title, Grounding Active Pill & Refresh Action
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = Translations.t("news_feed_title", language),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextInk
                    )

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = PriceUpLight,
                        border = BorderStroke(1.dp, ForestGreen.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = ForestGreen,
                                modifier = Modifier.size(6.dp)
                            ) {}
                            Text(
                                text = "Search Grounding",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = ForestGreenDark
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = Translations.t("news_feed_sub", language),
                    fontSize = 11.sp,
                    color = MutedInk,
                    lineHeight = 15.sp
                )
            }

            // Refresh Button
            FilledTonalIconButton(
                onClick = { viewModel.refreshMarketNews() },
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = PriceUpLight,
                    contentColor = ForestGreenDark
                ),
                modifier = Modifier
                    .size(40.dp)
                    .testTag("btn_refresh_news_feed")
            ) {
                if (isNewsLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = ForestGreen
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = Translations.t("refresh_news", language),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // 2. Search Grounding Tool Status & Active Query Card
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = PanelBackground,
            border = BorderStroke(1.dp, ForestGreen.copy(alpha = 0.35f)),
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth().testTag("search_grounding_status_card")
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = ForestGreen,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                        Text(
                            text = groundingStatus.engine,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = ForestGreenDark
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFE8F5E9)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = ForestGreen,
                                modifier = Modifier.size(11.dp)
                            )
                            Text(
                                text = "98% Grounded",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = ForestGreen
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Active Grounding Query
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = RaisedBackground.copy(alpha = 0.7f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Query:",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MutedInk
                        )
                        Text(
                            text = "\"${groundingStatus.activeQuery}\"",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextInk
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Grounding Provenance & Sources
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Sources: Google News · PAMRA · AgriWatch · Dawn",
                        fontSize = 10.sp,
                        color = MutedInk,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = groundingStatus.lastGroundedTime,
                        fontSize = 10.sp,
                        color = ForestGreen,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // 3. Mandi Sentiment Quick Bar
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (sentiment.overallSentiment.contains("Bullish", true)) PriceUpLight else RaisedBackground,
            border = BorderStroke(1.dp, Color(0xFFC7DAB3).copy(alpha = 0.7f)),
            modifier = Modifier.fillMaxWidth().testTag("market_sentiment_bar")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.TrendingUp,
                        contentDescription = null,
                        tint = ForestGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "${Translations.t("market_sentiment", language)}: ${sentiment.overallSentiment} (+${sentiment.sentimentDeltaPercent}%)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = ForestGreenDark
                    )
                }

                Text(
                    text = sentiment.crushingDemand,
                    fontSize = 11.sp,
                    color = MutedInk,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // 4. Category Filter Chips (All, Sarson, Canola, Crushing, Policy)
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth().testTag("news_feed_categories_row")
        ) {
            items(categories) { (catKey, labelKey) ->
                val isSelected = selectedCategory == catKey
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) ForestGreen else PanelBackground,
                    border = BorderStroke(1.dp, if (isSelected) ForestGreen else LineBorder),
                    shadowElevation = if (isSelected) 2.dp else 0.5.dp,
                    modifier = Modifier
                        .clickable { viewModel.setNewsCategory(catKey) }
                        .testTag("chip_market_news_category_$catKey")
                ) {
                    Text(
                        text = Translations.t(labelKey, language),
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) Color.White else TextInk,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                    )
                }
            }
        }

        // 5. In-Place Search Bar for Quick Filtering
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.setNewsSearchQuery(it) },
            placeholder = { Text(Translations.t("search_news_placeholder", language), fontSize = 12.sp) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = ForestGreen,
                    modifier = Modifier.size(18.dp)
                )
            },
            trailingIcon = {
                if (searchQuery.isNotBlank()) {
                    IconButton(onClick = { viewModel.setNewsSearchQuery("") }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear",
                            tint = MutedInk,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ForestGreen,
                unfocusedBorderColor = LineBorder,
                focusedContainerColor = PanelBackground,
                unfocusedContainerColor = PanelBackground
            ),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("news_feed_search_input")
        )

        // 6. Headlines Feed Cards (Top 3 by default, or all if expanded)
        val displayedArticles = if (isExpandedList) articles else articles.take(3)

        if (displayedArticles.isEmpty()) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = PanelBackground,
                border = BorderStroke(1.dp, LineBorder),
                modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Newspaper,
                        contentDescription = null,
                        tint = MutedInk,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No matching oilseed news headlines",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = TextInk
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Try clearing search or click Refresh to re-query with Search Grounding.",
                        fontSize = 11.sp,
                        color = MutedInk
                    )
                }
            }
        } else {
            displayedArticles.forEach { article ->
                MarketNewsFeedCard(
                    article = article,
                    language = language,
                    onReadSource = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(article.url))
                            context.startActivity(intent)
                        } catch (_: Exception) {
                            Toast.makeText(context, "Could not open URL: ${article.url}", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onShare = {
                        val shareText = """
                            📰 *${article.title}*
                            📍 *Mandi / Region:* ${article.mandiTag}
                            🏛️ *Source:* ${article.sourceName} (${article.publishedAt})
                            📈 *Impact:* ${article.impactOnPrices}
                            🔍 *Grounded Against:* ${article.searchGroundingQuery}

                            ${article.summary}

                            🔗 Read more: ${article.url}
                            — Shared from Malik Oil Intelligence
                        """.trimIndent()

                        val sendIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, shareText)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Share Punjab Oilseed News"))
                    },
                    onResearchTopic = {
                        viewModel.runResearch(
                            question = "Analyze price impact: ${article.title}",
                            commodity = if (article.category == "canola") "canola" else "sarson",
                            market = "Samundri",
                            depth = "quick"
                        )
                        viewModel.setTab("research")
                    }
                )
            }
        }

        // 7. Toggle Expand / View All Headlines CTA
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (articles.size > 3) {
                OutlinedButton(
                    onClick = { isExpandedList = !isExpandedList },
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, ForestGreen.copy(alpha = 0.6f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ForestGreen),
                    modifier = Modifier.weight(1f).testTag("btn_toggle_expand_news")
                ) {
                    Icon(
                        imageVector = if (isExpandedList) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isExpandedList) "Show Less" else "Show All (${articles.size}) Headlines",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Button(
                onClick = { viewModel.setTab("insights") },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                modifier = Modifier.weight(1f).testTag("btn_view_full_insights")
            ) {
                Text(
                    text = Translations.t("view_all_news", language),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun MarketNewsFeedCard(
    article: MarketNewsArticle,
    language: String,
    onReadSource: () -> Unit,
    onShare: () -> Unit,
    onResearchTopic: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = PanelBackground,
        border = BorderStroke(1.dp, Color(0xFFE2DDD1)),
        shadowElevation = 1.5.dp,
        modifier = modifier
            .fillMaxWidth()
            .testTag("market_news_card_${article.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Mandi Tag + Category + Price Impact Chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = Color(0xFFF1F3ED),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = article.mandiTag,
                            color = ForestGreenDark,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }

                    Surface(
                        color = RaisedBackground,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = article.category.uppercase(),
                            color = MutedInk,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                // Price impact badge
                Surface(
                    color = if (article.impactOnPrices.contains("Bullish", true)) PriceUpLight else PriceDownLight,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = article.impactOnPrices,
                        color = if (article.impactOnPrices.contains("Bullish", true)) PriceUp else PriceDown,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Headline
            Text(
                text = article.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextInk,
                lineHeight = 21.sp
            )

            // Source, Date & Search Grounding Badge
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Source,
                        contentDescription = null,
                        tint = GoldAccent,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = "${article.sourceName} · ${article.publishedAt}",
                        fontSize = 11.sp,
                        color = MutedInk,
                        fontWeight = FontWeight.Medium
                    )
                }

                if (article.isSearchGrounded) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = PriceUpLight
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.TravelExplore,
                                contentDescription = null,
                                tint = ForestGreen,
                                modifier = Modifier.size(10.dp)
                            )
                            Text(
                                text = "Grounded",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = ForestGreen
                            )
                        }
                    }
                }
            }

            // Summary
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = article.summary,
                fontSize = 12.sp,
                color = TextInk.copy(alpha = 0.85f),
                lineHeight = 17.sp
            )

            // Key Takeaways for Millers
            if (article.keyTakeaways.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = RaisedBackground.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = Translations.t("key_takeaways", language),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = ForestGreenDark
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        article.keyTakeaways.take(2).forEach { point ->
                            Row(
                                modifier = Modifier.padding(top = 2.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = "• ",
                                    fontSize = 11.sp,
                                    color = ForestGreen,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = point,
                                    fontSize = 11.sp,
                                    color = TextInk,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = LineBorder.copy(alpha = 0.6f))
            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons (Read Source, Research, Share)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onReadSource,
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("btn_feed_read_source_${article.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.OpenInNew,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp),
                        tint = ForestGreen
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = Translations.t("read_source", language),
                        fontSize = 11.sp,
                        color = ForestGreen,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedButton(
                        onClick = onResearchTopic,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("btn_feed_research_${article.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.TravelExplore,
                            contentDescription = null,
                            modifier = Modifier.size(13.dp),
                            tint = GoldAccent
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Research", fontSize = 11.sp, color = TextInk)
                    }

                    FilledTonalIconButton(
                        onClick = onShare,
                        modifier = Modifier
                            .size(34.dp)
                            .testTag("btn_feed_share_${article.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            modifier = Modifier.size(15.dp),
                            tint = ForestGreen
                        )
                    }
                }
            }
        }
    }
}

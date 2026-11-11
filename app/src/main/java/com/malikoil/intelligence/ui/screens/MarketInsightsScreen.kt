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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.malikoil.intelligence.model.MarketNewsArticle
import com.malikoil.intelligence.ui.MarketViewModel
import com.malikoil.intelligence.ui.components.Translations
import com.malikoil.intelligence.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketInsightsScreen(
    viewModel: MarketViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val language by viewModel.language.collectAsState()
    val articles by viewModel.filteredNewsArticles.collectAsState()
    val isNewsLoading by viewModel.isNewsLoading.collectAsState()
    val sentiment by viewModel.marketSentiment.collectAsState()
    val selectedCategory by viewModel.selectedNewsCategory.collectAsState()
    val searchQuery by viewModel.newsSearchQuery.collectAsState()

    val categories = listOf(
        "all" to "all_news",
        "sarson" to "cat_sarson",
        "canola" to "cat_canola",
        "crushing" to "cat_crushing",
        "policy" to "cat_policy"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SandBackground)
            .padding(horizontal = 16.dp)
            .testTag("market_insights_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            // Navigation Back Header & Refresh Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = { viewModel.setTab("rates") },
                    modifier = Modifier.testTag("btn_back_to_rates")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = ForestGreen,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = Translations.t("back_to_rates", language),
                        color = ForestGreen,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                FilledTonalButton(
                    onClick = { viewModel.refreshMarketNews() },
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = PriceUpLight,
                        contentColor = ForestGreenDark
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("btn_refresh_news")
                ) {
                    if (isNewsLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = ForestGreen)
                    } else {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = Translations.t("refresh_news", language),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Title and Subtitle
        item {
            Column {
                Surface(
                    color = PriceUpLight,
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(1.dp, ForestGreen.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = "DAILY INTELLIGENCE · PUNJAB MANDIS",
                        color = ForestGreen,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = Translations.t("insights_heading", language),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextInk
                )
                Text(
                    text = Translations.t("insights_sub", language),
                    fontSize = 12.sp,
                    color = MutedInk,
                    lineHeight = 16.sp
                )
            }
        }

        // Live Mandi Sentiment Hero Card
        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = PanelBackground,
                border = BorderStroke(1.dp, ForestGreen.copy(alpha = 0.3f)),
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth().testTag("market_sentiment_hero_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = Translations.t("market_sentiment", language),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MutedInk,
                            letterSpacing = 1.sp
                        )
                        Surface(
                            color = if (sentiment.overallSentiment.contains("Bullish", true)) PriceUpLight else PriceDownLight,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "${sentiment.overallSentiment} (+${sentiment.sentimentDeltaPercent}%)",
                                color = if (sentiment.overallSentiment.contains("Bullish", true)) PriceUp else PriceDown,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = sentiment.dominantFactor,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextInk,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = LineBorder.copy(alpha = 0.6f))
                    Spacer(modifier = Modifier.height(12.dp))

                    // 3 Sentiment Indicator Columns
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Sarson / Mustard", fontSize = 10.sp, color = MutedInk)
                            Text(text = sentiment.sarsonTrend, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ForestGreenDark)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Canola", fontSize = 10.sp, color = MutedInk)
                            Text(text = sentiment.canolaTrend, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextInk)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Expellers", fontSize = 10.sp, color = MutedInk)
                            Text(text = sentiment.crushingDemand, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ForestGreen)
                        }
                    }
                }
            }
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setNewsSearchQuery(it) },
                placeholder = { Text(Translations.t("search_news_placeholder", language)) },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = ForestGreen)
                },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { viewModel.setNewsSearchQuery("") }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Clear", tint = MutedInk)
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
                    .testTag("news_search_input")
            )
        }

        // Category Filter Chips
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth().testTag("news_categories_row")
            ) {
                items(categories) { (catKey, labelKey) ->
                    val isSelected = selectedCategory == catKey
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) ForestGreen else RaisedBackground,
                        border = BorderStroke(1.dp, if (isSelected) ForestGreen else LineBorder),
                        modifier = Modifier
                            .clickable { viewModel.setNewsCategory(catKey) }
                            .testTag("chip_news_category_$catKey")
                    ) {
                        Text(
                            text = Translations.t(labelKey, language),
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else TextInk,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        }

        // Articles Count Indicator
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${articles.size} verified reports found",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MutedInk
                )
                Text(
                    text = Translations.t("live_aggregator", language),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = ForestGreen
                )
            }
        }

        // News Articles List
        if (articles.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = PanelBackground,
                    border = BorderStroke(1.dp, LineBorder),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(imageVector = Icons.Default.Newspaper, contentDescription = null, tint = MutedInk, modifier = Modifier.size(40.dp))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(text = "No matching market news reports", fontWeight = FontWeight.Bold, color = TextInk)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(text = "Try clearing search filter or refresh news aggregator.", fontSize = 12.sp, color = MutedInk)
                    }
                }
            }
        } else {
            items(articles) { article ->
                NewsArticleCard(
                    article = article,
                    language = language,
                    onReadSource = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(article.url))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Could not open URL: ${article.url}", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onShare = {
                        val shareText = """
                            📰 *${article.title}*
                            📍 *Mandi / Region:* ${article.mandiTag}
                            🏛️ *Source:* ${article.sourceName} (${article.publishedAt})
                            📈 *Impact:* ${article.impactOnPrices}

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
                            question = "Explain impact of: ${article.title}",
                            commodity = if (article.category == "canola") "canola" else "sarson",
                            market = "Samundri",
                            depth = "quick"
                        )
                        viewModel.setTab("research")
                    }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun NewsArticleCard(
    article: MarketNewsArticle,
    language: String,
    onReadSource: () -> Unit,
    onShare: () -> Unit,
    onResearchTopic: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = PanelBackground,
        border = BorderStroke(1.dp, LineBorder),
        shadowElevation = 1.dp,
        modifier = modifier
            .fillMaxWidth()
            .testTag("news_card_${article.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Mandi Tag, Category & Impact
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        color = Color(0xFFF1F3ED),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = article.mandiTag,
                            color = ForestGreenDark,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Surface(
                        color = RaisedBackground,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = article.category.uppercase(),
                            color = MutedInk,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                // Impact badge
                Surface(
                    color = if (article.impactOnPrices.contains("Bullish", true)) PriceUpLight else PriceDownLight,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = article.impactOnPrices,
                        color = if (article.impactOnPrices.contains("Bullish", true)) PriceUp else PriceDown,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Headline
            Text(
                text = article.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextInk,
                lineHeight = 21.sp
            )

            // Source & Date with Search Grounding Badge
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Source,
                        contentDescription = null,
                        tint = GoldAccent,
                        modifier = Modifier.size(14.dp)
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
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.TravelExplore,
                                contentDescription = null,
                                tint = ForestGreen,
                                modifier = Modifier.size(11.dp)
                            )
                            Text(
                                text = "Search Grounded",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = ForestGreen
                            )
                        }
                    }
                }
            }

            // Summary snippet
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = article.summary,
                fontSize = 13.sp,
                color = TextInk.copy(alpha = 0.85f),
                lineHeight = 18.sp
            )

            // Key Takeaways bullets if present
            if (article.keyTakeaways.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = Translations.t("key_takeaways", language),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = ForestGreen
                )
                article.keyTakeaways.forEach { takeaway ->
                    Row(
                        modifier = Modifier.padding(top = 3.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(text = "• ", fontSize = 12.sp, color = ForestGreen, fontWeight = FontWeight.Bold)
                        Text(text = takeaway, fontSize = 11.sp, color = TextInk, lineHeight = 15.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = LineBorder.copy(alpha = 0.6f))
            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Read Source Link Button
                TextButton(
                    onClick = onReadSource,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("btn_read_source_${article.id}")
                ) {
                    Icon(imageVector = Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp), tint = ForestGreen)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = Translations.t("read_source", language), fontSize = 12.sp, color = ForestGreen, fontWeight = FontWeight.Bold)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Research Topic in AI
                    OutlinedButton(
                        onClick = onResearchTopic,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("btn_research_topic_${article.id}")
                    ) {
                        Icon(imageVector = Icons.Default.TravelExplore, contentDescription = null, modifier = Modifier.size(14.dp), tint = GoldAccent)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Research", fontSize = 11.sp, color = TextInk)
                    }

                    // Share button
                    FilledTonalIconButton(
                        onClick = onShare,
                        modifier = Modifier.size(36.dp).testTag("btn_share_news_${article.id}")
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(16.dp), tint = ForestGreen)
                    }
                }
            }
        }
    }
}

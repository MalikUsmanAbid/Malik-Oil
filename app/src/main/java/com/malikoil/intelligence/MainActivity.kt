package com.malikoil.intelligence

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.malikoil.intelligence.ui.MarketViewModel
import com.malikoil.intelligence.ui.components.MalikBottomNavBar
import com.malikoil.intelligence.ui.components.MalikTopAppBar
import com.malikoil.intelligence.ui.screens.*
import com.malikoil.intelligence.ui.theme.MalikOilTheme
import com.malikoil.intelligence.ui.theme.SandBackground

class MainActivity : ComponentActivity() {

    private val viewModel: MarketViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Ensure notification channel is initialized
        com.malikoil.intelligence.notification.PriceAlertNotificationManager.ensureChannel(this)

        if (intent?.getStringExtra("open_tab") == "alerts") {
            viewModel.setTab("alerts")
        }

        // Handle shared text from WhatsApp or other apps
        if (intent?.action == Intent.ACTION_SEND && intent?.type == "text/plain") {
            val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
            if (!sharedText.isNullOrBlank()) {
                viewModel.approveDailyRows(sharedText, "Shared WhatsApp Text")
                viewModel.setTab("review")
            }
        }

        setContent {
            MalikOilTheme {
                val currentTab by viewModel.currentTab.collectAsState()
                val language by viewModel.language.collectAsState()
                val alerts by viewModel.alerts.collectAsState()
                val candidates by viewModel.candidates.collectAsState()
                val userMessage by viewModel.userMessage.collectAsState()

                val snackbarHostState = remember { SnackbarHostState() }

                LaunchedEffect(userMessage) {
                    userMessage?.let {
                        snackbarHostState.showSnackbar(it)
                        viewModel.dismissUserMessage()
                    }
                }

                // Handle back press: if on another tab, return to "rates"
                BackHandler(enabled = currentTab != "rates") {
                    viewModel.setTab("rates")
                }

                val layoutDirection = if (language == "ur") LayoutDirection.Rtl else LayoutDirection.Ltr

                CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                    Scaffold(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(SandBackground),
                        topBar = {
                            MalikTopAppBar(
                                selectedLanguage = language,
                                onLanguageSelected = { viewModel.setLanguage(it) },
                                modifier = Modifier.statusBarsPadding()
                            )
                        },
                        bottomBar = {
                            MalikBottomNavBar(
                                currentTab = currentTab,
                                language = language,
                                alertsCount = alerts.size,
                                reviewCount = candidates.size,
                                onTabSelected = { viewModel.setTab(it) },
                                modifier = Modifier.navigationBarsPadding()
                            )
                        },
                        snackbarHost = { SnackbarHost(snackbarHostState) },
                        containerColor = SandBackground
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            when (currentTab) {
                                "rates" -> MarketScreen(viewModel = viewModel)
                                "insights" -> MarketInsightsScreen(viewModel = viewModel)
                                "research" -> ResearchScreen(viewModel = viewModel)
                                "alerts" -> AlertsScreen(viewModel = viewModel)
                                "review" -> ReviewScreen(viewModel = viewModel)
                                "business" -> BusinessScreen(viewModel = viewModel)
                                "reports" -> ReportsScreen(viewModel = viewModel)
                                else -> MarketScreen(viewModel = viewModel)
                            }
                        }
                    }
                }
            }
        }
    }
}

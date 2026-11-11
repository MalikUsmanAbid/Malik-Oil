package com.malikoil.intelligence.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.malikoil.intelligence.ui.theme.*

data class NavItem(
    val id: String,
    val translationKey: String,
    val icon: ImageVector,
    val badgeCount: Int = 0
)

@Composable
fun MalikBottomNavBar(
    currentTab: String,
    language: String,
    alertsCount: Int,
    reviewCount: Int,
    onTabSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        NavItem("rates", "market", Icons.Default.ShowChart),
        NavItem("research", "research", Icons.Default.TravelExplore),
        NavItem("alerts", "alerts", Icons.Default.Notifications, badgeCount = alertsCount),
        NavItem("review", "review", Icons.Default.Rule, badgeCount = reviewCount),
        NavItem("business", "business", Icons.Default.Calculate),
        NavItem("reports", "reports", Icons.Default.Assessment)
    )

    NavigationBar(
        containerColor = PanelBackground,
        contentColor = TextInk,
        tonalElevation = 6.dp,
        modifier = modifier.testTag("malik_bottom_nav_bar")
    ) {
        items.forEach { item ->
            val isSelected = currentTab == item.id
            val label = Translations.t(item.translationKey, language)

            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(item.id) },
                icon = {
                    BadgedBox(
                        badge = {
                            if (item.badgeCount > 0) {
                                Badge(
                                    containerColor = if (item.id == "alerts") PriceDown else GoldAccent,
                                    contentColor = androidx.compose.ui.graphics.Color.White
                                ) {
                                    Text(item.badgeCount.toString(), fontSize = 10.sp)
                                }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = label,
                            tint = if (isSelected) ForestGreen else MutedInk
                        )
                    }
                },
                label = {
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        color = if (isSelected) ForestGreenDark else MutedInk
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = PriceUpLight
                ),
                modifier = Modifier.testTag("nav_tab_${item.id}")
            )
        }
    }
}

package com.malikoil.intelligence.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val MalikLightColorScheme = lightColorScheme(
    primary = ForestGreen,
    onPrimary = Color.White,
    primaryContainer = PriceUpLight,
    onPrimaryContainer = ForestGreenDark,
    secondary = GoldAccent,
    onSecondary = Color.White,
    secondaryContainer = PriceDownLight,
    onSecondaryContainer = ForestGreenDark,
    background = SandBackground,
    onBackground = TextInk,
    surface = PanelBackground,
    onSurface = TextInk,
    surfaceVariant = RaisedBackground,
    onSurfaceVariant = MutedInk,
    outline = LineBorder
)

@Composable
fun MalikOilTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = MalikLightColorScheme,
        typography = Typography,
        content = content
    )
}

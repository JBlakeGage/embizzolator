package net.embizzolator.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val GeneralBusinessColorScheme = lightColorScheme(
    primary = NavyBlue,
    secondary = SlateGray,
    tertiary = SkyBlue,
    background = Color(0xFFFFFFFF),
    surface = Color(0xFFFFFFFF),
    onSurface = Color.Black
)

private val ExecutiveMahoganyColorScheme = lightColorScheme(
    primary = Mahogany,
    secondary = GoldAccent,
    tertiary = Cream,
    background = Cream,
    surface = Cream,
    onSurface = Mahogany
)

private val CubeFarmChicColorScheme = lightColorScheme(
    primary = OfficePlantGreen,
    secondary = CubicleGray,
    tertiary = Beige,
    background = Beige,
    surface = Beige,
    onSurface = Color.DarkGray
)

private val MarketingColorScheme = lightColorScheme(
    primary = HotPink,
    secondary = BrightOrange,
    tertiary = LimeGreen,
    onSurface = Color.Black
)

@Composable
fun EmbizzolatorTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // MODIFICATION: The theme now takes the name of the brand guideline
    // and determines the color scheme internally.
    brandGuideline: String = "General Business",
    content: @Composable () -> Unit
) {
    val colorScheme = when (brandGuideline) {
        "Executive Mahogany" -> ExecutiveMahoganyColorScheme
        "Cube Farm Chic" -> CubeFarmChicColorScheme
        "Marketing" -> MarketingColorScheme
        else -> GeneralBusinessColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
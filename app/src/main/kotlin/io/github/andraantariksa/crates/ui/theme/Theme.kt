package io.github.andraantariksa.crates.ui.theme

import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// crates.io brand: forest green, crate yellow, parchment background.
private val Light = lightColorScheme(
    primary = Color(0xFF3B6837),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFBCF0B3),
    onPrimaryContainer = Color(0xFF002204),
    secondary = Color(0xFF765B00),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFC933),
    onSecondaryContainer = Color(0xFF261A00),
    background = Color(0xFFF9F7EC),
    onBackground = Color(0xFF383838),
    surface = Color(0xFFF9F7EC),
    onSurface = Color(0xFF383838),
    surfaceVariant = Color(0xFFE2E4D8),
    onSurfaceVariant = Color(0xFF4A4F45),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF4F2E7),
    surfaceContainer = Color(0xFFEFEDE2),
    surfaceContainerHigh = Color(0xFFE9E7DC),
    surfaceContainerHighest = Color(0xFFE3E1D7),
    outline = Color(0xFF74796E),
    outlineVariant = Color(0xFFC4C8BC),
)

private val Dark = darkColorScheme(
    primary = Color(0xFF8ED18A),
    onPrimary = Color(0xFF0B3910),
    primaryContainer = Color(0xFF23502A),
    onPrimaryContainer = Color(0xFFAAEFA3),
    secondary = Color(0xFFFFC933),
    onSecondary = Color(0xFF3A2E00),
    secondaryContainer = Color(0xFFFFC933),
    onSecondaryContainer = Color(0xFF261A00),
    background = Color(0xFF161615),
    onBackground = Color(0xFFE6E4DD),
    surface = Color(0xFF161615),
    onSurface = Color(0xFFE6E4DD),
    surfaceVariant = Color(0xFF43483F),
    onSurfaceVariant = Color(0xFFC4C7BE),
    surfaceContainerLowest = Color(0xFF0E0E0D),
    surfaceContainerLow = Color(0xFF1E1E1D),
    surfaceContainer = Color(0xFF232321),
    surfaceContainerHigh = Color(0xFF2C2C2A),
    surfaceContainerHighest = Color(0xFF363634),
    outline = Color(0xFF8E9288),
    outlineVariant = Color(0xFF43483F),
)

/** Background of the hero header: green in light mode, near-black in dark mode, like crates.io. */
val heroColor: Color
    @Composable @ReadOnlyComposable
    get() = if (MaterialTheme.colorScheme.background.luminance() < 0.5f) Color(0xFF0E0E0D) else Color(0xFF3B6837)

/** Raised cards and fields: white on parchment in light mode, a lighter grey than the background in dark mode. */
val cardColor: Color
    @Composable @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.run { if (background.luminance() < 0.5f) surfaceContainerHigh else surfaceContainerLowest }

enum class ThemeMode { System, Light, Dark }

class ThemePreference(context: Context) {
    private val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
    private val _mode = MutableStateFlow(
        prefs.getString("theme", null)?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() } ?: ThemeMode.System
    )
    val mode: StateFlow<ThemeMode> = _mode.asStateFlow()

    fun set(mode: ThemeMode) {
        prefs.edit { putString("theme", mode.name) }
        _mode.value = mode
    }
}

@Composable
fun CratesTheme(mode: ThemeMode = ThemeMode.System, content: @Composable () -> Unit) {
    val dark = when (mode) {
        ThemeMode.System -> isSystemInDarkTheme()
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
    }
    MaterialTheme(colorScheme = if (dark) Dark else Light, content = content)
}

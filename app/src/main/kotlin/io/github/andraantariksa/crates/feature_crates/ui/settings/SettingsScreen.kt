package io.github.andraantariksa.crates.feature_crates.ui.settings

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.andraantariksa.crates.feature_crates.ui.main.screens.misc.components.SettingsItem

private val nightModeCycle = listOf(
    AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM,
    AppCompatDelegate.MODE_NIGHT_NO,
    AppCompatDelegate.MODE_NIGHT_YES,
)

private fun nightModeLabel(mode: Int) = when (mode) {
    AppCompatDelegate.MODE_NIGHT_NO -> "Light"
    AppCompatDelegate.MODE_NIGHT_YES -> "Dark"
    else -> "System default"
}

@Composable
fun SettingsScreen() {
    var nightMode by remember { mutableStateOf(AppCompatDelegate.getDefaultNightMode()) }

    LazyColumn() {
        item {
            SettingsItem(
                "Theme",
                subtitle = nightModeLabel(nightMode),
                modifier = Modifier
                    .padding(horizontal = 10.dp, vertical = 18.dp)
                    .fillMaxWidth(),
                icon = {
                    Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings")
                }
            ) {
                val nextMode = nightModeCycle[(nightModeCycle.indexOf(nightMode) + 1) % nightModeCycle.size]
                AppCompatDelegate.setDefaultNightMode(nextMode)
                nightMode = nextMode
            }
        }
    }
}
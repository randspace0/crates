package io.github.andraantariksa.crates.ui.account

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.OpenInBrowser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.andraantariksa.crates.ui.theme.cardColor
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import io.github.andraantariksa.crates.BuildConfig
import io.github.andraantariksa.crates.data.Session
import io.github.andraantariksa.crates.data.UserRef
import io.github.andraantariksa.crates.ui.Avatar
import io.github.andraantariksa.crates.ui.ListRow
import io.github.andraantariksa.crates.ui.SectionTitle
import io.github.andraantariksa.crates.ui.theme.ThemeMode
import io.github.andraantariksa.crates.ui.theme.ThemePreference
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

class AccountViewModel(private val session: Session, private val theme: ThemePreference) : ViewModel() {
    val user = session.user
    val themeMode = theme.mode
    fun setTheme(mode: ThemeMode) = theme.set(mode)
    fun signOut() = viewModelScope.launch { session.signOut() }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(onSignIn: () -> Unit, viewModel: AccountViewModel = koinViewModel()) {
    val user by viewModel.user.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val uriHandler = LocalUriHandler.current

    Scaffold(topBar = { TopAppBar(title = { Text("Account") }) }) { padding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier
                    .widthIn(max = 640.dp)
                    .padding(16.dp),
            ) {
                user?.let { ProfileCard(it, onSignOut = viewModel::signOut) } ?: SignInCard(onSignIn)

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    SectionTitle("Theme")
                    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                        ThemeMode.entries.forEachIndexed { i, mode ->
                            SegmentedButton(
                                selected = themeMode == mode,
                                onClick = { viewModel.setTheme(mode) },
                                shape = SegmentedButtonDefaults.itemShape(i, ThemeMode.entries.size),
                                label = { Text(mode.name) },
                            )
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    SectionTitle("About")
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(8.dp),
                    ) {
                        Icon(Icons.Outlined.Info, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Column {
                            Text("Crates for Android", style = MaterialTheme.typography.bodyLarge)
                            Text(
                                "Version ${BuildConfig.VERSION_NAME} · unofficial crates.io client",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    ListRow(
                        "Source code",
                        supporting = "github.com/randspace0/crates",
                        leading = { Icon(Icons.Outlined.Code, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                        onClick = { uriHandler.openUri("https://github.com/randspace0/crates") },
                    )
                }
            }
        }
    }
}

@Composable
private fun SignInCard(onSignIn: () -> Unit) {
    Surface(shape = MaterialTheme.shapes.large, color = cardColor) {
        Column(Modifier.padding(20.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("You're not signed in", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                "crates.io accounts use GitHub. Sign in to see the crates you own.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Button(onClick = onSignIn) { Text("Log in with GitHub") }
        }
    }
}

@Composable
private fun ProfileCard(user: UserRef, onSignOut: () -> Unit) {
    var confirm by rememberSaveable { mutableStateOf(false) }
    val uriHandler = LocalUriHandler.current
    if (confirm) {
        AlertDialog(
            onDismissRequest = { confirm = false },
            title = { Text("Sign out?") },
            text = { Text("You'll need to log in with GitHub again to see your crates.") },
            confirmButton = {
                TextButton(
                    onClick = { confirm = false; onSignOut() },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                ) { Text("Sign out") }
            },
            dismissButton = { TextButton(onClick = { confirm = false }) { Text("Cancel") } },
        )
    }
    Surface(shape = MaterialTheme.shapes.large, color = cardColor) {
        Column(Modifier.padding(20.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Avatar(user.avatar, 56.dp)
                Column {
                    Text(user.name ?: user.login, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("@${user.login}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { uriHandler.openUri("https://crates.io/users/${user.login}") }) {
                    Icon(Icons.Outlined.OpenInBrowser, null, Modifier.padding(end = 8.dp))
                    Text("Profile")
                }
                OutlinedButton(
                    onClick = { confirm = true },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                ) {
                    Icon(Icons.AutoMirrored.Outlined.Logout, null, Modifier.padding(end = 8.dp))
                    Text("Sign out")
                }
            }
        }
    }
}

package io.github.andraantariksa.crates.ui.mycrates

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import io.github.andraantariksa.crates.data.Crate
import io.github.andraantariksa.crates.data.CratesApi
import io.github.andraantariksa.crates.data.Session
import io.github.andraantariksa.crates.data.UserRef
import io.github.andraantariksa.crates.ui.Avatar
import io.github.andraantariksa.crates.ui.CrateCard
import io.github.andraantariksa.crates.ui.ErrorBox
import io.github.andraantariksa.crates.ui.Load
import io.github.andraantariksa.crates.ui.LoadingBox
import io.github.andraantariksa.crates.ui.MessageBox
import io.github.andraantariksa.crates.ui.load
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

class MyCratesViewModel(private val api: CratesApi, private val session: Session) : ViewModel() {
    val user = session.user
    private val _crates = MutableStateFlow<Load<List<Crate>>>(Load.Loading)
    val crates = _crates.asStateFlow()
    private val _refreshing = MutableStateFlow(false)
    val refreshing = _refreshing.asStateFlow()

    init {
        viewModelScope.launch {
            session.user.collectLatest { user ->
                _crates.value = Load.Loading
                if (user != null) _crates.value = fetch(user)
            }
        }
    }

    // ponytail: first 100 crates only; add paging when someone owns more.
    private suspend fun fetch(user: UserRef) =
        load { api.crates(userId = user.id, sort = "recent-updates", perPage = 100).crates }

    fun refresh() = viewModelScope.launch {
        val user = session.user.value ?: return@launch
        _refreshing.value = true
        _crates.value = fetch(user)
        _refreshing.value = false
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyCratesScreen(
    onCrate: (String) -> Unit,
    onSignIn: () -> Unit,
    viewModel: MyCratesViewModel = koinViewModel(),
) {
    val user by viewModel.user.collectAsStateWithLifecycle()
    val crates by viewModel.crates.collectAsStateWithLifecycle()
    val refreshing by viewModel.refreshing.collectAsStateWithLifecycle()

    Scaffold(topBar = { TopAppBar(title = { Text("My crates") }) }) { padding ->
        val modifier = Modifier.padding(padding)
        val signedIn = user
        if (signedIn == null) {
            MessageBox(
                Icons.Outlined.Lock,
                "Sign in to see your crates",
                "Log in with the GitHub account you publish to crates.io with.",
                action = "Log in with GitHub",
                onAction = onSignIn,
                modifier = modifier,
            )
            return@Scaffold
        }
        PullToRefreshBox(isRefreshing = refreshing, onRefresh = { viewModel.refresh() }, modifier = modifier) {
            when (val c = crates) {
                Load.Loading -> LoadingBox()
                is Load.Failed -> ErrorBox(c.error, onRetry = { viewModel.refresh() })
                is Load.Ready -> if (c.data.isEmpty()) {
                    MessageBox(
                        Icons.Outlined.Inventory2,
                        "No crates yet",
                        "Crates you publish with `cargo publish` show up here.",
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        item {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.widthIn(max = 840.dp).fillMaxWidth().padding(bottom = 8.dp),
                            ) {
                                Avatar(signedIn.avatar, 40.dp)
                                Text(
                                    "${c.data.size} crates owned by @${signedIn.login}",
                                    style = MaterialTheme.typography.titleSmall,
                                )
                            }
                        }
                        items(c.data, key = { it.id }) {
                            CrateCard(it, onClick = { onCrate(it.id) }, modifier = Modifier.widthIn(max = 840.dp))
                        }
                    }
                }
            }
        }
    }
}

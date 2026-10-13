package io.github.andraantariksa.crates.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.andraantariksa.crates.ui.theme.cardColor
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import io.github.andraantariksa.crates.data.Crate
import io.github.andraantariksa.crates.data.CratesApi
import io.github.andraantariksa.crates.data.Summary
import io.github.andraantariksa.crates.ui.CrateRow
import io.github.andraantariksa.crates.ui.CrateStat
import io.github.andraantariksa.crates.ui.ErrorBox
import io.github.andraantariksa.crates.ui.Load
import io.github.andraantariksa.crates.ui.LoadingBox
import io.github.andraantariksa.crates.ui.SearchRoute
import io.github.andraantariksa.crates.ui.SectionTitle
import io.github.andraantariksa.crates.ui.formatCount
import io.github.andraantariksa.crates.ui.load
import io.github.andraantariksa.crates.ui.theme.heroColor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

class HomeViewModel(private val api: CratesApi) : ViewModel() {
    private val _summary = MutableStateFlow<Load<Summary>>(Load.Loading)
    val summary = _summary.asStateFlow()
    private val _refreshing = MutableStateFlow(false)
    val refreshing = _refreshing.asStateFlow()

    init {
        viewModelScope.launch { _summary.value = load { api.summary() } }
    }

    fun refresh() = viewModelScope.launch {
        _refreshing.value = true
        val result = load { api.summary() }
        // Keep showing old data if a refresh fails.
        if (result is Load.Ready || _summary.value !is Load.Ready) _summary.value = result
        _refreshing.value = false
    }

    fun retry() = viewModelScope.launch {
        _summary.value = Load.Loading
        _summary.value = load { api.summary() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onCrate: (String) -> Unit,
    onSearch: (SearchRoute) -> Unit,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val summary by viewModel.summary.collectAsStateWithLifecycle()
    val refreshing by viewModel.refreshing.collectAsStateWithLifecycle()

    PullToRefreshBox(isRefreshing = refreshing, onRefresh = { viewModel.refresh() }) {
        BoxWithConstraints(Modifier.fillMaxSize()) {
            // One column on phones, up to three side by side on tablets, like crates.io.
            val columns = (maxWidth / 340.dp).toInt().coerceIn(1, 3)
            LazyVerticalGrid(
                columns = GridCells.Fixed(columns),
                contentPadding = PaddingValues(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.navigationBars.only(WindowInsetsSides.Bottom)),
            ) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Hero(onSearch = { onSearch(SearchRoute(query = it)) })
                }
                when (val s = summary) {
                    Load.Loading -> item(span = { GridItemSpan(maxLineSpan) }) { LoadingBox(Modifier.padding(48.dp)) }
                    is Load.Failed -> item(span = { GridItemSpan(maxLineSpan) }) {
                        ErrorBox(s.error, onRetry = { viewModel.retry() })
                    }
                    is Load.Ready -> {
                        val data = s.data
                        item(span = { GridItemSpan(maxLineSpan) }) { Stats(data) }
                        val sections = listOf(
                            Triple("New Crates", data.newCrates, CrateStat.Version),
                            Triple("Most Downloaded", data.mostDownloaded, CrateStat.Downloads),
                            Triple("Just Updated", data.justUpdated, CrateStat.Version),
                            Triple("Most Recent Downloads", data.mostRecentlyDownloaded, CrateStat.RecentDownloads),
                        )
                        sections.forEach { (title, crates, stat) ->
                            item { CrateSection(title, crates, stat, onCrate) }
                        }
                        item {
                            ChipSection("Popular Keywords", data.popularKeywords.map { "#${it.id}" to formatCount(it.cratesCount.toLong()) }) { i ->
                                onSearch(SearchRoute(keyword = data.popularKeywords[i].id))
                            }
                        }
                        item {
                            ChipSection("Popular Categories", data.popularCategories.map { it.category to formatCount((it.cratesCount ?: 0).toLong()) }) { i ->
                                onSearch(SearchRoute(category = data.popularCategories[i].slug))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Hero(onSearch: (String) -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    val submit = { if (query.isNotBlank()) onSearch(query.trim()) }
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars)
            .background(heroColor)
            .padding(horizontal = 16.dp, vertical = 32.dp),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp),
            modifier = Modifier.widthIn(max = 640.dp),
        ) {
            Text(
                "The Rust community’s crate registry",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
            )
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Search crates") },
                    singleLine = true,
                    shape = MaterialTheme.shapes.extraLarge,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { submit() }),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedContainerColor = cardColor,
                        unfocusedContainerColor = cardColor,
                    ),
                    modifier = Modifier.weight(1f),
                )
                FilledIconButton(
                    onClick = submit,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    ),
                    modifier = Modifier.size(56.dp),
                ) { Icon(Icons.Outlined.Search, contentDescription = "Search") }
            }
        }
    }
}

@Composable
private fun Stats(summary: Summary) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(32.dp, Alignment.CenterHorizontally),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
    ) {
        Stat(Icons.Outlined.CloudDownload, formatCount(summary.numDownloads), "Downloads")
        Stat(Icons.Outlined.Inventory2, formatCount(summary.numCrates), "Crates in stock")
    }
}

@Composable
private fun Stat(icon: androidx.compose.ui.graphics.vector.ImageVector, value: String, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(horizontalAlignment = Alignment.End) {
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(icon, contentDescription = null, Modifier.size(36.dp), tint = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun CrateSection(title: String, crates: List<Crate>, stat: CrateStat, onCrate: (String) -> Unit) {
    Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionTitle(title)
        crates.forEach { CrateRow(it, onClick = { onCrate(it.id) }, stat = stat) }
    }
}

@Composable
private fun ChipSection(title: String, chips: List<Pair<String, String>>, onClick: (Int) -> Unit) {
    Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionTitle(title)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            chips.forEachIndexed { i, (label, count) ->
                SuggestionChip(onClick = { onClick(i) }, label = { Text("$label  ·  $count") })
            }
        }
    }
}

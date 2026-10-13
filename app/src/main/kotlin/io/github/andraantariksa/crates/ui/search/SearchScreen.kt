package io.github.andraantariksa.crates.ui.search

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import io.github.andraantariksa.crates.data.Crate
import io.github.andraantariksa.crates.data.CratesApi
import io.github.andraantariksa.crates.ui.CrateCard
import io.github.andraantariksa.crates.ui.ErrorBox
import io.github.andraantariksa.crates.ui.LoadingBox
import io.github.andraantariksa.crates.ui.MessageBox
import io.github.andraantariksa.crates.ui.SearchRoute
import io.github.andraantariksa.crates.ui.Load
import io.github.andraantariksa.crates.ui.formatCount
import io.github.andraantariksa.crates.ui.load
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class Sort(val api: String?, val label: String) {
    Relevance(null, "Relevance"),
    Downloads("downloads", "All-time downloads"),
    RecentDownloads("recent-downloads", "Recent downloads"),
    Updated("recent-updates", "Recent updates"),
    New("new", "Newly added"),
}

data class SearchState(
    val crates: List<Crate> = emptyList(),
    val total: Int = 0,
    val nextPage: String? = null,
    val loading: Boolean = true,
    val error: Throwable? = null,
)

class SearchViewModel(handle: SavedStateHandle, private val api: CratesApi) : ViewModel() {
    val route = handle.toRoute<SearchRoute>()
    private val _query = MutableStateFlow(route.query)
    val query = _query.asStateFlow()
    private val _sort = MutableStateFlow(if (route.query.isEmpty()) Sort.Downloads else Sort.Relevance)
    val sort = _sort.asStateFlow()
    private val _state = MutableStateFlow(SearchState())
    val state = _state.asStateFlow()
    private var job: Job? = null

    init {
        search()
    }

    fun submit(query: String, sort: Sort = _sort.value) {
        _query.value = query
        _sort.value = sort
        search()
    }

    fun search() {
        job?.cancel()
        _state.value = SearchState()
        job = viewModelScope.launch {
            val result = load {
                api.crates(
                    query = _query.value.ifBlank { null },
                    keyword = route.keyword,
                    category = route.category,
                    sort = _sort.value.api,
                )
            }
            _state.value = when (result) {
                is Load.Ready -> SearchState(result.data.crates, result.data.meta.total, result.data.meta.nextPage, loading = false)
                is Load.Failed -> SearchState(loading = false, error = result.error)
                Load.Loading -> return@launch
            }
        }
    }

    fun loadMore() {
        val next = _state.value.nextPage ?: return
        if (_state.value.loading) return
        _state.update { it.copy(loading = true, error = null) }
        job = viewModelScope.launch {
            when (val result = load { api.cratesPage("v1/crates$next") }) {
                is Load.Ready -> _state.update {
                    it.copy(crates = it.crates + result.data.crates, nextPage = result.data.meta.nextPage, loading = false)
                }
                is Load.Failed -> _state.update { it.copy(loading = false, error = result.error) }
                Load.Loading -> Unit
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onCrate: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: SearchViewModel = org.koin.compose.viewmodel.koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val sort by viewModel.sort.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()
    val route = viewModel.route
    val filter = route.keyword?.let { "#$it" } ?: route.category

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                title = {
                    if (filter != null) Text(filter)
                    else SearchField(query, onSubmit = { viewModel.submit(it) })
                },
            )
        },
    ) { padding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
            ) {
                Sort.entries.forEach {
                    FilterChip(
                        selected = sort == it,
                        onClick = { viewModel.submit(query, it) },
                        label = { Text(it.label) },
                    )
                }
            }
            Results(state, onCrate, onRetry = viewModel::search, onLoadMore = viewModel::loadMore)
        }
    }
}

@Composable
private fun SearchField(initial: String, onSubmit: (String) -> Unit) {
    var text by rememberSaveable { mutableStateOf(initial) }
    TextField(
        value = text,
        onValueChange = { text = it },
        placeholder = { Text("Search crates") },
        singleLine = true,
        trailingIcon = {
            if (text.isNotEmpty()) IconButton(onClick = { text = "" }) {
                Icon(Icons.Outlined.Clear, contentDescription = "Clear search")
            }
        },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSubmit(text.trim()) }),
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
        ),
        shape = MaterialTheme.shapes.extraLarge,
        modifier = Modifier
            .fillMaxWidth()
            .padding(end = 8.dp),
    )
}

@Composable
private fun Results(state: SearchState, onCrate: (String) -> Unit, onRetry: () -> Unit, onLoadMore: () -> Unit) {
    when {
        state.crates.isEmpty() && state.loading -> LoadingBox()
        state.crates.isEmpty() && state.error != null -> ErrorBox(state.error, onRetry)
        state.crates.isEmpty() -> MessageBox(
            Icons.Outlined.SearchOff,
            "No crates found",
            "Try a different name or keyword.",
        )
        else -> {
            val listState = rememberLazyListState()
            val nearEnd by remember {
                derivedStateOf {
                    val last = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                    last >= listState.layoutInfo.totalItemsCount - 5
                }
            }
            LaunchedEffect(nearEnd) { if (nearEnd) onLoadMore() }
            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxSize(),
            ) {
                item {
                    Text(
                        "${formatCount(state.total.toLong())} crates",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.widthIn(max = 840.dp).fillMaxWidth(),
                    )
                }
                items(state.crates, key = { it.id }) {
                    CrateCard(it, onClick = { onCrate(it.id) }, modifier = Modifier.widthIn(max = 840.dp))
                }
                if (state.nextPage != null || state.error != null) item {
                    Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                        if (state.error != null) TextButton(onClick = onLoadMore) { Text("Couldn't load more. Retry") }
                        else CircularProgressIndicator()
                    }
                }
            }
        }
    }
}

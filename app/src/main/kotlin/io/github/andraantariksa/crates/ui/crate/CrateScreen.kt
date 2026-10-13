package io.github.andraantariksa.crates.ui.crate

import android.content.ClipData
import android.text.format.Formatter
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.outlined.Balance
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.OpenInBrowser
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.andraantariksa.crates.ui.theme.cardColor
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.andraantariksa.crates.data.CrateDetail
import io.github.andraantariksa.crates.data.Dependency
import io.github.andraantariksa.crates.data.Owner
import io.github.andraantariksa.crates.data.Version
import io.github.andraantariksa.crates.ui.Avatar
import io.github.andraantariksa.crates.ui.ErrorBox
import io.github.andraantariksa.crates.ui.ListRow
import io.github.andraantariksa.crates.ui.Load
import io.github.andraantariksa.crates.ui.LoadingBox
import io.github.andraantariksa.crates.ui.MessageBox
import io.github.andraantariksa.crates.ui.SectionTitle
import io.github.andraantariksa.crates.ui.StatText
import io.github.andraantariksa.crates.ui.formatCompact
import io.github.andraantariksa.crates.ui.formatCount
import io.github.andraantariksa.crates.ui.openWeb
import io.github.andraantariksa.crates.ui.relativeTime
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

private enum class Tab(val label: String) { Readme("Readme"), Info("Info"), Versions("Versions"), Dependencies("Dependencies") }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrateScreen(
    onCrate: (String) -> Unit,
    onKeyword: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: CrateViewModel = koinViewModel(),
) {
    val detail by viewModel.detail.collectAsStateWithLifecycle()
    val uriHandler = LocalUriHandler.current
    val snackbar = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbar) },
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { uriHandler.openUri("https://crates.io/crates/${viewModel.name}") }) {
                        Icon(Icons.Outlined.OpenInBrowser, contentDescription = "Open on crates.io")
                    }
                },
            )
        },
    ) { padding ->
        when (val d = detail) {
            Load.Loading -> LoadingBox(Modifier.padding(padding))
            is Load.Failed -> ErrorBox(d.error, onRetry = viewModel::reload, modifier = Modifier.padding(padding))
            is Load.Ready -> BoxWithConstraints(Modifier.padding(padding).fillMaxSize()) {
                // Tablets get the crates.io layout: content on the left, metadata sidebar on the right.
                val wide = maxWidth >= 840.dp
                val tabs = if (wide) Tab.entries - Tab.Info else Tab.entries
                var selected by rememberSaveable { mutableIntStateOf(0) }
                val tab = tabs[selected.coerceAtMost(tabs.lastIndex)]
                val info = @Composable { modifier: Modifier ->
                    InfoPanel(d.data, viewModel.owners.collectAsStateWithLifecycle().value, snackbar, modifier)
                }
                Row {
                    Column(Modifier.weight(1f)) {
                        Header(d.data, onKeyword)
                        PrimaryScrollableTabRow(selectedTabIndex = tabs.indexOf(tab), edgePadding = 16.dp) {
                            tabs.forEachIndexed { i, t ->
                                Tab(selected = t == tab, onClick = { selected = i }, text = { Text(t.label) })
                            }
                        }
                        when (tab) {
                            Tab.Readme -> ReadmeTab(viewModel)
                            Tab.Info -> info(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp))
                            Tab.Versions -> VersionsTab(d.data.versions)
                            Tab.Dependencies -> DependenciesTab(viewModel, onCrate)
                        }
                    }
                    if (wide) {
                        Surface(color = MaterialTheme.colorScheme.surfaceContainerLow, modifier = Modifier.width(340.dp).fillMaxHeight()) {
                            info(Modifier.verticalScroll(rememberScrollState()).padding(20.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Header(detail: CrateDetail, onKeyword: (String) -> Unit) {
    val crate = detail.crate
    Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(crate.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("v${crate.version}", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        crate.description?.let { Text(it.trim(), style = MaterialTheme.typography.bodyMedium) }
        crate.keywords?.takeIf { it.isNotEmpty() }?.let { keywords ->
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                keywords.forEach { AssistChip(onClick = { onKeyword(it) }, label = { Text("#$it") }) }
            }
        }
    }
}

@Composable
private fun ReadmeTab(viewModel: CrateViewModel) {
    when (val readme = viewModel.readme.collectAsStateWithLifecycle().value) {
        Load.Loading -> LoadingBox()
        is Load.Failed -> ErrorBox(readme.error, onRetry = viewModel::reload)
        is Load.Ready -> readme.data
            ?.let { ReadmeView(it, baseUrl = "https://crates.io/crates/${viewModel.name}") }
            ?: MessageBox(Icons.Outlined.Description, "No README", "This crate doesn't publish a README.")
    }
}

@Composable
private fun InfoPanel(detail: CrateDetail, owners: List<Owner>, snackbar: SnackbarHostState, modifier: Modifier) {
    val crate = detail.crate
    val version = detail.versions.firstOrNull { it.num == crate.version }
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current

    Column(modifier, verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Section("Downloads") {
            StatText(Icons.Outlined.Download, "${formatCount(crate.downloads)} all-time")
            crate.recentDownloads?.let { StatText(Icons.AutoMirrored.Outlined.TrendingUp, "${formatCount(it)} in the last 90 days") }
        }
        Section("Metadata") {
            version?.let { v ->
                StatText(Icons.Outlined.CalendarMonth, "Published ${relativeTime(v.createdAt)}")
                v.rustVersion?.let { StatText(Icons.Outlined.Settings, "Rust $it or newer") }
                v.license?.let { StatText(Icons.Outlined.Balance, it) }
                v.edition?.let { StatText(Icons.Outlined.Code, "$it edition") }
                v.crateSize?.let { StatText(Icons.Outlined.Inventory2, Formatter.formatShortFileSize(context, it)) }
            }
        }
        Section("Install") {
            CopyBox("cargo add ${crate.name}", snackbar)
            Text(
                "Or add to Cargo.toml:",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            CopyBox("${crate.name} = \"${crate.version}\"", snackbar)
        }
        val links = listOfNotNull(
            crate.homepage?.let { Triple("Homepage", it, Icons.Outlined.Link) },
            Triple("Documentation", crate.documentation ?: "https://docs.rs/${crate.name}/${crate.version}", Icons.AutoMirrored.Outlined.MenuBook),
            crate.repository?.let { Triple("Repository", it, Icons.Outlined.Code) },
        )
        Section("Links") {
            links.forEach { (label, url, icon) ->
                ListRow(
                    text = label,
                    supporting = url.removePrefix("https://").removePrefix("http://").removeSuffix("/"),
                    leading = { Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                    onClick = { uriHandler.openWeb(url) },
                )
            }
        }
        if (owners.isNotEmpty()) Section("Owners") {
            owners.forEach { owner ->
                ListRow(
                    text = owner.name ?: owner.login,
                    supporting = if (owner.kind == "team") owner.login else "@${owner.login}",
                    leading = { Avatar(owner.avatar, 32.dp) },
                    onClick = {
                        uriHandler.openUri(
                            if (owner.kind == "team") "https://crates.io/teams/${owner.login}"
                            else "https://crates.io/users/${owner.login}"
                        )
                    },
                )
            }
        }
        if (detail.categories.isNotEmpty()) Section("Categories") {
            detail.categories.forEach {
                ListRow(it.category, onClick = { uriHandler.openUri("https://crates.io/categories/${it.slug}") })
            }
        }
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionTitle(title)
        content()
    }
}

@Composable
private fun CopyBox(text: String, snackbar: SnackbarHostState) {
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()
    Surface(
        onClick = {
            scope.launch {
                clipboard.setClipEntry(ClipEntry(ClipData.newPlainText(text, text)))
                snackbar.showSnackbar("Copied “$text”")
            }
        },
        shape = MaterialTheme.shapes.small,
        color = cardColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.heightIn(min = 48.dp).padding(horizontal = 12.dp),
        ) {
            Text(text, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
            Icon(Icons.Outlined.ContentCopy, contentDescription = "Copy", Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun VersionsTab(versions: List<Version>) {
    LazyColumn(contentPadding = PaddingValues(vertical = 8.dp), modifier = Modifier.fillMaxSize()) {
        items(versions, key = { it.id }) { v ->
            Column(Modifier.padding(horizontal = 16.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        v.num,
                        style = MaterialTheme.typography.titleSmall,
                        color = if (v.yanked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                    )
                    if (v.yanked) Text("yanked", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                }
                Text(
                    listOfNotNull(
                        relativeTime(v.createdAt),
                        "${formatCompact(v.downloads)} downloads",
                        v.license,
                        v.rustVersion?.let { "Rust $it" },
                    ).joinToString("  ·  "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        }
    }
}

@Composable
private fun DependenciesTab(viewModel: CrateViewModel, onCrate: (String) -> Unit) {
    when (val deps = viewModel.dependencies.collectAsStateWithLifecycle().value) {
        Load.Loading -> LoadingBox()
        is Load.Failed -> ErrorBox(deps.error, onRetry = viewModel::reload)
        is Load.Ready -> if (deps.data.isEmpty()) {
            MessageBox(Icons.Outlined.Inventory2, "No dependencies", null)
        } else {
            val groups = listOf("normal" to "Dependencies", "build" to "Build-dependencies", "dev" to "Dev-dependencies")
                .map { (kind, title) -> title to deps.data.filter { it.kind == kind }.sortedBy(Dependency::crateId) }
                .filter { it.second.isNotEmpty() }
            LazyColumn(contentPadding = PaddingValues(16.dp), modifier = Modifier.fillMaxSize()) {
                groups.forEach { (title, list) ->
                    item { SectionTitle(title, Modifier.padding(top = 8.dp, bottom = 4.dp)) }
                    // No keys: the same crate can be listed twice for different targets.
                    items(list) { dep ->
                        ListRow(
                            text = dep.crateId,
                            supporting = listOfNotNull(dep.req, "optional".takeIf { dep.optional }).joinToString("  ·  "),
                            onClick = { onCrate(dep.crateId) },
                        )
                    }
                }
            }
        }
    }
}

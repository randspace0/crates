package io.github.andraantariksa.crates.feature_crates.ui.crate

import android.app.Activity
import android.widget.TextView
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddToQueue
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Balance
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import io.github.andraantariksa.crates.common.util.CratesResult
import io.github.andraantariksa.crates.feature_crates.data.source.remote.model.detail.CrateDetail
import org.koin.androidx.compose.koinViewModel

@Composable
fun CrateScreen(crateId: String?, crateViewModel: CrateViewModel = koinViewModel()) {
    LaunchedEffect(crateId) {
        crateViewModel.load(crateId)
    }

    when (val state = crateViewModel.crateDetail) {
        is CratesResult.Loading -> Crate(CrateDetail.EXAMPLE)
        is CratesResult.Error -> Text("Error: ${state.error.message}")
        is CratesResult.Loaded -> Crate(state.data)
    }
}

enum class CrateTabs {
    Readme,
    Versions,
    Dependencies,
    Dependents
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun Crate(
    crateDetail: CrateDetail
) {
    val context = LocalContext.current
    val pagerState = rememberPagerState(pageCount = { 4 })
    val crate = crateDetail.crate
    val latestVersion = crateDetail.versions.firstOrNull()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column {
                        Row {
                            Text(crate.name)
                            Text(crate.maxVersion)
                        }
                        Text(
                            crate.description,
                            fontSize = MaterialTheme.typography.bodySmall.fontSize
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        (context as? Activity)?.finish()
                    }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = null
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.padding(paddingValues)
        ) {
            HorizontalPager(state = pagerState) { page ->
                when (page) {
                    0 -> Readme(latestVersion)
                    else -> {}
                }
            }
        }
    }
}

@Preview
@Composable
fun CratePreview() {
    Crate(CrateDetail.EXAMPLE)
}

@Composable
fun Readme(version: io.github.andraantariksa.crates.feature_crates.data.source.remote.model.detail.Version?) {
    Column {
        // ponytail: README markdown needs a separate fetch of version.readmePath; add when the tab is prioritized
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = {
                TextView(it).apply {
                    text = "README not loaded yet"
                }
            },
            update = {

            }
        )
    }
    Column {
        Text("Metadata")
        Row {
            Icon(
                imageVector = Icons.Default.CalendarMonth,
                contentDescription = null
            )
            Text(version?.updatedAt ?: "-")
        }
        Row {
            Icon(imageVector = Icons.Default.Balance, contentDescription = null)
            Text(version?.license ?: "-")
        }
        Row {
            Icon(
                imageVector = Icons.Default.AddToQueue,
                contentDescription = null
            )
            Text(version?.crateSize?.let { "$it bytes" } ?: "-")
        }
    }
}

@Preview
@Composable
fun PreviewReadme() {
    Readme(null)
}
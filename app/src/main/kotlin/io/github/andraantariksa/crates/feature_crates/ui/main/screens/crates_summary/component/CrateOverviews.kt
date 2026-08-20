package io.github.andraantariksa.crates.feature_crates.ui.main.screens.crates_summary.component

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.andraantariksa.crates.feature_crates.domain.entity.crate.Crate
import io.github.andraantariksa.crates.feature_crates.ui.common.components.CrateOverview
import io.github.andraantariksa.crates.feature_crates.ui.crate.CrateActivity

@Composable
fun CrateOverviews(title: String, crates: List<Crate>? = null) {
    val context = LocalContext.current

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .padding(vertical = 2.dp)
        ) {
            if (crates != null) {
                for (item in crates) {
                    CrateOverview(item) {
                        context.startActivity(
                            CrateActivity.init(context, item.id)
                        )
                    }
                }
            } else {
                repeat(5) {
                    CrateOverview()
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewCrateListLoading() {
    CrateOverviews("New Crates")
}

@Preview
@Composable
fun CrateListPreview() {
    CrateOverviews("New Crates", crates = List(5) { Crate.dummy })
}
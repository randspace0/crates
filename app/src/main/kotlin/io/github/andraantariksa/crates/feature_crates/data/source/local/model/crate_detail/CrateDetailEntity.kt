package io.github.andraantariksa.crates.feature_crates.data.source.local.model.crate_detail

import androidx.room.Entity
import androidx.room.PrimaryKey
import io.github.andraantariksa.crates.feature_crates.data.source.remote.model.detail.CrateDetail

@Entity(tableName = "crate_detail")
data class CrateDetailEntity(
    @PrimaryKey
    val id: String,
    val crateDetail: CrateDetail,
)

package io.github.andraantariksa.crates.feature_crates.data.source.local

import io.github.andraantariksa.crates.feature_crates.data.source.remote.model.detail.CrateDetail
import io.github.andraantariksa.crates.feature_crates.domain.entity.summary.CratesSummary

interface CratesIoDataSourceLocal {
    suspend fun getCratesSummary(): CratesSummary
    suspend fun saveCratesSummary(cratesSummary: CratesSummary)
    suspend fun getCrateDetails(id: String): CrateDetail
    suspend fun saveCrateDetails(id: String, crateDetail: CrateDetail)
}

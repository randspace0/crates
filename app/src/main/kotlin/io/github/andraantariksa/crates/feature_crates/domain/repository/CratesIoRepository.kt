package io.github.andraantariksa.crates.feature_crates.domain.repository

import io.github.andraantariksa.crates.feature_crates.data.source.remote.model.detail.CrateDetail
import io.github.andraantariksa.crates.feature_crates.data.source.remote.model.sign_in.AuthBegin
import io.github.andraantariksa.crates.feature_crates.domain.entity.summary.CratesSummary
import io.github.andraantariksa.crates.feature_crates.domain.entity.user.User

interface CratesIoRepository {
    suspend fun getCratesSummary(): Result<CratesSummary>
    suspend fun getCrateDetails(id: String): Result<CrateDetail>
    suspend fun getBeginAuthData(): AuthBegin
    suspend fun authorizeOauth(code: String, state: String): Result<User>
}
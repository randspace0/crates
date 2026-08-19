package io.github.andraantariksa.crates.feature_crates.data.source.remote

import io.github.andraantariksa.crates.feature_crates.data.source.remote.model.detail.CrateDetail
import io.github.andraantariksa.crates.feature_crates.data.source.remote.model.me.MyUser
import io.github.andraantariksa.crates.feature_crates.data.source.remote.model.me.User
import io.github.andraantariksa.crates.feature_crates.data.source.remote.model.sign_in.AuthBegin
import io.github.andraantariksa.crates.feature_crates.domain.entity.summary.CratesSummary

interface CratesIoDataSourceRemote {
    suspend fun getCratesSummary(): CratesSummary
    suspend fun getCrateDetail(id: String): CrateDetail
    suspend fun getBeginAuthData(): AuthBegin
    suspend fun authorizeOauth(code: String, state: String): MyUser
    suspend fun getMe(): Result<MyUser>
    suspend fun signOut(): Result<Unit>
}

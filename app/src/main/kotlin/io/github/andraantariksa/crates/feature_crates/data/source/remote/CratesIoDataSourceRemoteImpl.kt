package io.github.andraantariksa.crates.feature_crates.data.source.remote

import io.github.andraantariksa.crates.feature_crates.data.exception.UnauthenticatedException
import io.github.andraantariksa.crates.feature_crates.data.source.remote.model.detail.CrateDetail
import io.github.andraantariksa.crates.feature_crates.data.source.remote.model.me.MyUser
import io.github.andraantariksa.crates.feature_crates.data.source.remote.model.me.User
import io.github.andraantariksa.crates.feature_crates.data.source.remote.model.sign_in.AuthBegin
import io.github.andraantariksa.crates.feature_crates.data.source.remote.service.CratesIoAPIService
import io.github.andraantariksa.crates.feature_crates.domain.entity.summary.CratesSummary
import retrofit2.HttpException
import javax.inject.Inject

class CratesIoDataSourceRemoteImpl
@Inject constructor(
    private val cratesIoAPIService: CratesIoAPIService
) : CratesIoDataSourceRemote {
    override suspend fun getCratesSummary(): CratesSummary = cratesIoAPIService.getSummary()

    override suspend fun getCrateDetail(id: String): CrateDetail =
        cratesIoAPIService.getCrateDetail(id)

    override suspend fun getBeginAuthData(): AuthBegin = cratesIoAPIService.getBeginAuthData()

    override suspend fun authorizeOauth(code: String, state: String): MyUser {
        return cratesIoAPIService.authorizeOauth(code, state)
    }

    override suspend fun getMe(): Result<MyUser> {
        val resp = cratesIoAPIService.getMe()
        if (resp.isSuccessful) {
            return Result.success(resp.body()!!)
        }
        if (resp.code() == 403) {
            return Result.failure(UnauthenticatedException())
        }
        return Result.failure(HttpException(resp))
    }

    override suspend fun signOut(): Result<Unit> {
        val resp = cratesIoAPIService.signOut()
        if (resp.isSuccessful) {
            return Result.success(Unit)
        }
        return Result.failure(HttpException(resp))
    }
}

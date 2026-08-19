package io.github.andraantariksa.crates.feature_crates.data.source.remote.service

import io.github.andraantariksa.crates.feature_crates.data.source.remote.model.detail.CrateDetail
import io.github.andraantariksa.crates.feature_crates.data.source.remote.model.me.MyUser
import io.github.andraantariksa.crates.feature_crates.data.source.remote.model.me.User
import io.github.andraantariksa.crates.feature_crates.data.source.remote.model.search.CratesSearch
import io.github.andraantariksa.crates.feature_crates.data.source.remote.model.sign_in.AuthBegin
import io.github.andraantariksa.crates.feature_crates.data.source.remote.model.summary.CratesSummary
import retrofit2.Response
import retrofit2.http.*

interface CratesIoAPIService {
    @GET("v1/summary")
    suspend fun getSummary(): CratesSummary

    @GET("v1/crates/{cratesId}")
    suspend fun getCrateDetail(@Path("cratesId") cratesId: String): CrateDetail

    @GET("v1/crates?page={page}&per_page={perPage}&q={query}")
    suspend fun searchCrate(
        @Path("page") page: Int,
        @Path("perPage") perPage: Int,
        @Path("query") query: String
    ): CratesSearch

    @GET("private/session/begin")
    suspend fun getBeginAuthData(): AuthBegin

    @Headers("referer: https://crates.io/")
    @GET("private/session/authorize")
    suspend fun authorizeOauth(@Query("code") code: String, @Query("state") state: String): MyUser

    @DELETE("private/session")
    suspend fun signOut(): Response<Unit>

    @GET("v1/me")
    suspend fun getMe(): Response<MyUser>
}


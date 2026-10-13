package io.github.andraantariksa.crates.data

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url

interface CratesApi {
    @GET("v1/summary")
    suspend fun summary(): Summary

    @GET("v1/crates/{name}")
    suspend fun crate(@Path("name") name: String): CrateDetail

    @GET("v1/crates/{name}/{version}/dependencies")
    suspend fun dependencies(
        @Path("name") name: String,
        @Path("version") version: String,
    ): Dependencies

    @GET("v1/crates/{name}/owners")
    suspend fun owners(@Path("name") name: String): Owners

    /** Readme is pre-rendered HTML; 403/404 means the crate has none. */
    @GET("https://static.crates.io/readmes/{name}/{name}-{version}.html")
    suspend fun readme(
        @Path("name") name: String,
        @Path("version") version: String,
    ): Response<ResponseBody>

    @GET("v1/crates")
    suspend fun crates(
        @Query("q") query: String? = null,
        @Query("keyword") keyword: String? = null,
        @Query("category") category: String? = null,
        @Query("user_id") userId: Int? = null,
        @Query("sort") sort: String? = null,
        @Query("per_page") perPage: Int = 20,
    ): CratesPage

    /** Follows `meta.next_page`, which is a query string relative to `v1/crates`. */
    @GET
    suspend fun cratesPage(@Url url: String): CratesPage
}

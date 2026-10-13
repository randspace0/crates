package io.github.andraantariksa.crates.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// ponytail: only the fields the UI reads; everything crates.io may send as null is nullable.

@JsonClass(generateAdapter = true)
data class Crate(
    val id: String,
    val name: String,
    val description: String?,
    @Json(name = "default_version") val defaultVersion: String?,
    @Json(name = "max_version") val maxVersion: String,
    val downloads: Long,
    @Json(name = "recent_downloads") val recentDownloads: Long?,
    @Json(name = "updated_at") val updatedAt: String,
    @Json(name = "created_at") val createdAt: String,
    val homepage: String?,
    val documentation: String?,
    val repository: String?,
    val keywords: List<String>?,
) {
    val version get() = defaultVersion ?: maxVersion
}

@JsonClass(generateAdapter = true)
data class Keyword(
    val id: String,
    @Json(name = "crates_cnt") val cratesCount: Int,
)

@JsonClass(generateAdapter = true)
data class Category(
    val id: String,
    val category: String,
    val slug: String,
    @Json(name = "crates_cnt") val cratesCount: Int?,
)

@JsonClass(generateAdapter = true)
data class Summary(
    @Json(name = "num_downloads") val numDownloads: Long,
    @Json(name = "num_crates") val numCrates: Long,
    @Json(name = "new_crates") val newCrates: List<Crate>,
    @Json(name = "most_downloaded") val mostDownloaded: List<Crate>,
    @Json(name = "just_updated") val justUpdated: List<Crate>,
    @Json(name = "most_recently_downloaded") val mostRecentlyDownloaded: List<Crate>,
    @Json(name = "popular_keywords") val popularKeywords: List<Keyword>,
    @Json(name = "popular_categories") val popularCategories: List<Category>,
)

@JsonClass(generateAdapter = true)
data class UserRef(
    val id: Int,
    val login: String,
    val name: String?,
    val avatar: String?,
    val url: String?,
)

@JsonClass(generateAdapter = true)
data class Version(
    val id: Int,
    val num: String,
    @Json(name = "created_at") val createdAt: String,
    val downloads: Long,
    val yanked: Boolean,
    val license: String?,
    @Json(name = "crate_size") val crateSize: Long?,
    @Json(name = "rust_version") val rustVersion: String?,
    val edition: String?,
    @Json(name = "published_by") val publishedBy: UserRef?,
)

@JsonClass(generateAdapter = true)
data class CrateDetail(
    val crate: Crate,
    val versions: List<Version>,
    val categories: List<Category>,
)

@JsonClass(generateAdapter = true)
data class Dependency(
    @Json(name = "crate_id") val crateId: String,
    val req: String,
    val optional: Boolean,
    val kind: String,
)

@JsonClass(generateAdapter = true)
data class Dependencies(val dependencies: List<Dependency>)

@JsonClass(generateAdapter = true)
data class Owner(
    val id: Int,
    val login: String,
    val name: String?,
    val avatar: String?,
    val kind: String,
)

@JsonClass(generateAdapter = true)
data class Owners(val users: List<Owner>)

@JsonClass(generateAdapter = true)
data class SearchMeta(
    val total: Int,
    @Json(name = "next_page") val nextPage: String?,
)

@JsonClass(generateAdapter = true)
data class CratesPage(
    val crates: List<Crate>,
    val meta: SearchMeta,
)

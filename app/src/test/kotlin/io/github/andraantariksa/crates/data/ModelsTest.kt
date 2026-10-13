package io.github.andraantariksa.crates.data

import com.squareup.moshi.Moshi
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Parses real crates.io responses (trimmed, in test resources) to catch nullability mismatches. */
class ModelsTest {
    private val moshi = Moshi.Builder().build()

    private inline fun <reified T> parse(resource: String): T {
        val json = javaClass.classLoader!!.getResource(resource)!!.readText()
        return moshi.adapter(T::class.java).fromJson(json)!!
    }

    @Test
    fun summary() {
        val summary = parse<Summary>("summary.json")
        assertTrue(summary.numDownloads > Int.MAX_VALUE)
        assertEquals(2, summary.newCrates.size)
        assertEquals(2, summary.popularCategories.size)
    }

    @Test
    fun crateDetail() {
        val detail = parse<CrateDetail>("crate.json")
        assertEquals("serde", detail.crate.name)
        assertEquals(detail.crate.defaultVersion, detail.crate.version)
        assertNotNull(detail.versions.first().publishedBy)
    }

    @Test
    fun searchPage() {
        val page = parse<CratesPage>("search.json")
        assertEquals(2, page.crates.size)
        assertTrue(page.meta.nextPage!!.startsWith("?"))
    }

    @Test
    fun ownersAndDependencies() {
        assertTrue(parse<Owners>("owners.json").users.isNotEmpty())
        assertTrue(parse<Dependencies>("dependencies.json").dependencies.any { it.kind == "dev" })
    }

    @Test
    fun authorizeSignupRequiredHasNoUser() {
        val result = moshi.adapter(AuthorizeResult::class.java).fromJson("""{"status":"signup_required"}""")!!
        assertEquals("signup_required", result.status)
        assertNull(result.user)
    }

    @Test
    fun authorizeSignedInCarriesUser() {
        val json = """{"status":"signed_in","user":{"id":1,"login":"ferris","name":null,"avatar":null,"url":null,"email":"f@example.com"},"owned_crates":[]}"""
        val result = moshi.adapter(AuthorizeResult::class.java).fromJson(json)!!
        assertEquals("ferris", result.user!!.login)
    }
}

package io.github.andraantariksa.crates.data

import android.content.Context
import android.webkit.CookieManager
import androidx.core.content.edit
import com.squareup.moshi.Moshi
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl

/**
 * Stores cookies in the WebView cookie store, so the `cargo_session` cookie set by
 * crates.io survives restarts and is shared with the sign-in WebView.
 */
class WebViewCookieJar : CookieJar {
    private val cookies get() = CookieManager.getInstance()

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        cookies.forEach { this.cookies.setCookie(url.toString(), it.toString()) }
        this.cookies.flush()
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> =
        cookies.getCookie(url.toString())
            ?.split("; ")
            ?.mapNotNull { Cookie.parse(url, it) }
            .orEmpty()
}

/** The signed-in crates.io user. The session itself lives in the cookie jar. */
class Session(context: Context, moshi: Moshi, private val api: CratesApi) {
    private val prefs = context.getSharedPreferences("session", Context.MODE_PRIVATE)
    private val adapter = moshi.adapter(UserRef::class.java)

    private val _user = MutableStateFlow(prefs.getString(KEY_USER, null)?.let(adapter::fromJson))
    val user: StateFlow<UserRef?> = _user.asStateFlow()

    fun signedIn(user: UserRef) {
        prefs.edit { putString(KEY_USER, adapter.toJson(user)) }
        _user.value = user
    }

    /** Re-checks the cookie session; drops the user when crates.io no longer accepts it. */
    suspend fun refresh() {
        if (_user.value == null) return
        val response = runCatching { api.me() }.getOrNull() ?: return // offline: keep cached user
        when {
            response.isSuccessful -> response.body()?.let { signedIn(it.user) }
            response.code() == 401 || response.code() == 403 -> clear()
        }
    }

    suspend fun signOut() {
        // Update the UI right away; the DELETE still needs the cookie, so drop it afterwards.
        prefs.edit { remove(KEY_USER) }
        _user.value = null
        try {
            api.signOut()
        } catch (e: Exception) {
            if (e is CancellationException) throw e // offline is fine: the cookie is removed below anyway
        } finally {
            CookieManager.getInstance().removeAllCookies(null)
        }
    }

    private fun clear() {
        prefs.edit { remove(KEY_USER) }
        CookieManager.getInstance().removeAllCookies(null)
        _user.value = null
    }

    private companion object {
        const val KEY_USER = "user"
    }
}

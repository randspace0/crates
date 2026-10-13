package io.github.andraantariksa.crates.ui.signin

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.net.Uri
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import io.github.andraantariksa.crates.data.AuthorizeBody
import io.github.andraantariksa.crates.data.CratesApi
import io.github.andraantariksa.crates.data.Session
import io.github.andraantariksa.crates.ui.LoadingBox
import io.github.andraantariksa.crates.ui.MessageBox
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

/** GitHub sends the user back here after authorizing the crates.io OAuth app. */
private const val REDIRECT = "https://crates.io/github-redirect.html"

sealed interface SignInState {
    data object Starting : SignInState
    data class GitHub(val url: String) : SignInState
    data object Authorizing : SignInState
    data object SignedIn : SignInState
    data object SignupRequired : SignInState
    data class Failed(val message: String) : SignInState
}

/**
 * Mirrors the crates.io web login: `begin` sets the OAuth state in the session cookie,
 * the user authorizes on GitHub, then `authorize` exchanges the code for a signed-in session.
 */
class SignInViewModel(private val api: CratesApi, private val session: Session) : ViewModel() {
    private val _state = MutableStateFlow<SignInState>(SignInState.Starting)
    val state = _state.asStateFlow()

    init {
        start()
    }

    fun start() = viewModelScope.launch {
        _state.value = SignInState.Starting
        _state.value = runCatching { SignInState.GitHub(api.beginAuth().url) }
            .getOrElse { SignInState.Failed("Couldn't reach crates.io. Check your connection.") }
    }

    fun onRedirect(uri: Uri) {
        if (_state.value !is SignInState.GitHub) return // the WebView can report the same redirect twice
        val code = uri.getQueryParameter("code")
        val state = uri.getQueryParameter("state")
        if (code == null || state == null) {
            _state.value = SignInState.Failed(uri.getQueryParameter("error_description") ?: "GitHub login was cancelled.")
            return
        }
        _state.value = SignInState.Authorizing
        viewModelScope.launch {
            _state.value = runCatching { api.authorize(AuthorizeBody(code, state)) }.fold(
                onSuccess = { result ->
                    val user = result.user
                    when {
                        result.status == "signed_in" && user != null -> {
                            session.signedIn(user)
                            SignInState.SignedIn
                        }
                        result.status == "signup_required" -> SignInState.SignupRequired
                        else -> SignInState.Failed("Unexpected response from crates.io.")
                    }
                },
                onFailure = { SignInState.Failed("crates.io rejected the login. Try again.") },
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignInScreen(onDone: () -> Unit, viewModel: SignInViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val uriHandler = LocalUriHandler.current
    LaunchedEffect(state) { if (state == SignInState.SignedIn) onDone() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Log in with GitHub") },
                navigationIcon = {
                    IconButton(onClick = onDone) { Icon(Icons.Filled.Close, contentDescription = "Cancel login") }
                },
            )
        },
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when (val s = state) {
                SignInState.Starting, SignInState.Authorizing, SignInState.SignedIn -> LoadingBox()
                is SignInState.GitHub -> GitHubWebView(s.url, onRedirect = viewModel::onRedirect)
                SignInState.SignupRequired -> MessageBox(
                    Icons.Outlined.PersonAdd,
                    "No crates.io account yet",
                    "This GitHub account hasn't signed up on crates.io. Create the account on the website, then log in here.",
                    action = "Sign up on crates.io",
                    onAction = { uriHandler.openUri("https://crates.io/") },
                )
                is SignInState.Failed -> MessageBox(
                    Icons.Outlined.ErrorOutline,
                    "Login failed",
                    s.message,
                    action = "Try again",
                    onAction = { viewModel.start() },
                )
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled") // GitHub's login page needs JavaScript.
@Composable
private fun GitHubWebView(url: String, onRedirect: (Uri) -> Unit) {
    var loading by remember { mutableStateOf(true) }
    Column {
        if (loading) LinearProgressIndicator(Modifier.fillMaxWidth())
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                WebView(context).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    webViewClient = object : WebViewClient() {
                        // Stop at the redirect: the crates.io page would otherwise consume the one-time code.
                        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                            if (!request.url.toString().startsWith(REDIRECT)) return false
                            onRedirect(request.url)
                            return true
                        }

                        override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                            loading = true
                            if (url.startsWith(REDIRECT)) {
                                view.stopLoading()
                                onRedirect(url.toUri())
                            }
                        }

                        override fun onPageFinished(view: WebView, url: String) {
                            loading = false
                        }
                    }
                    loadUrl(url)
                }
            },
        )
    }
}

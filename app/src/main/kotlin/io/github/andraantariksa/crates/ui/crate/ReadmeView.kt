package io.github.andraantariksa.crates.ui.crate

import android.graphics.Color as AndroidColor
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.viewinterop.AndroidView

/** Renders the README HTML crates.io pre-generates, styled with the app theme. */
@Composable
fun ReadmeView(html: String, baseUrl: String) {
    val colors = MaterialTheme.colorScheme
    val uriHandler = LocalUriHandler.current
    val page = remember(html, colors) {
        """
        <!doctype html><html><head>
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <style>
          body { font-family: sans-serif; color: ${colors.onSurface.css()}; background: transparent;
                 margin: 16px; line-height: 1.6; overflow-wrap: anywhere; }
          a { color: ${colors.primary.css()}; }
          h1, h2, h3 { line-height: 1.3; }
          h1, h2 { border-bottom: 1px solid ${colors.outlineVariant.css()}; padding-bottom: .3em; }
          pre { background: ${colors.surfaceContainerHigh.css()}; padding: 12px; border-radius: 8px; overflow-x: auto; }
          code { font-family: monospace; font-size: .9em; background: ${colors.surfaceContainerHigh.css()};
                 padding: .1em .3em; border-radius: 4px; }
          pre code { padding: 0; background: none; }
          img { max-width: 100%; height: auto; }
          table { border-collapse: collapse; display: block; overflow-x: auto; }
          th, td { border: 1px solid ${colors.outlineVariant.css()}; padding: 6px 12px; }
          blockquote { margin: 0; padding-left: 12px; border-left: 4px solid ${colors.outlineVariant.css()};
                       color: ${colors.onSurfaceVariant.css()}; }
        </style></head><body>$html</body></html>
        """.trimIndent()
    }
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            WebView(context).apply {
                setBackgroundColor(AndroidColor.TRANSPARENT)
                settings.javaScriptEnabled = false
                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                        val url = request.url.toString()
                        if (url.startsWith("$baseUrl#")) return false // in-page anchor
                        uriHandler.openUri(url)
                        return true
                    }
                }
            }
        },
        update = {
            // update runs on every recomposition; only reload when the page actually changed.
            if (it.tag != page) {
                it.tag = page
                it.loadDataWithBaseURL(baseUrl, page, "text/html", "utf-8", null)
            }
        },
    )
}

private fun Color.css() = "#%06X".format(toArgb() and 0xFFFFFF)

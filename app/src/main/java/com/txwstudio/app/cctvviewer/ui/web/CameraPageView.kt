package com.txwstudio.app.cctvviewer.ui.web

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import android.util.Log
import android.view.ViewGroup
import android.webkit.SslErrorHandler
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient

@SuppressLint("SetJavaScriptEnabled")
internal class CameraPageView(
    context: Context,
    url: String,
    onStateChanged: (CameraPageState) -> Unit,
) : WebView(context) {
    private var state = CameraPageState()
    private var stateListener: ((CameraPageState) -> Unit)? = onStateChanged
    var isReleased = false
        private set

    init {
        Log.d(TAG, "Created")
        isFocusable = false
        isFocusableInTouchMode = false
        descendantFocusability = ViewGroup.FOCUS_BLOCK_DESCENDANTS
        settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            mediaPlaybackRequiresUserGesture = false
            useWideViewPort = true
            loadWithOverviewMode = true
            allowFileAccess = false
            allowContentAccess = false
        }
        webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
                return !supportsUrl(request.url.toString())
            }

            override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
                publish(CameraPageState())
            }

            override fun onPageFinished(view: WebView, url: String?) {
                publish(state.copy(progress = 100))
            }

            override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
                if (request.isForMainFrame) fail(CameraPageError.LOAD_FAILED)
            }

            override fun onReceivedHttpError(view: WebView, request: WebResourceRequest, response: WebResourceResponse) {
                if (request.isForMainFrame) fail(CameraPageError.LOAD_FAILED)
            }

            override fun onReceivedSslError(view: WebView, handler: SslErrorHandler, error: SslError) {
                handler.cancel()
                fail(CameraPageError.CERTIFICATE_ERROR)
            }
        }
        webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView, newProgress: Int) {
                publish(state.copy(progress = newProgress.coerceIn(0, 100)))
            }
        }
        loadUrl(url)
    }

    private fun fail(error: CameraPageError) {
        publish(state.copy(progress = 100, error = error))
    }

    private fun publish(next: CameraPageState) {
        if (isReleased) return
        state = next
        stateListener?.invoke(state)
    }

    fun release() {
        if (isReleased) return
        isReleased = true
        stateListener = null
        webChromeClient = null
        webViewClient = WebViewClient()
        stopLoading()
        onPause()
        (parent as? ViewGroup)?.removeView(this)
        removeAllViews()
        destroy()
        Log.d(TAG, "Destroyed")
    }

    companion object {
        private const val TAG = "CameraPageView"

        fun supportsUrl(url: String): Boolean {
            val uri = Uri.parse(url)
            return (uri.scheme.equals("http", true) || uri.scheme.equals("https", true)) &&
                !uri.host.isNullOrBlank()
        }
    }
}

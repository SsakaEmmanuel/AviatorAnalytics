package com.example.data.local

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Singleton Persistent Browser Manager
 * Maintains the live WebView instance, session cookies, JavaScript scrapers,
 * and Aviator detection across all Jetpack Compose navigation events.
 */
class PersistentBrowserManager private constructor(private val appContext: Context) {

    private var persistentWebView: WebView? = null

    private val _currentUrl = MutableStateFlow("https://www.betika.com/en-ug/login")
    val currentUrl: StateFlow<String> = _currentUrl.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _loadProgress = MutableStateFlow(0f)
    val loadProgress: StateFlow<Float> = _loadProgress.asStateFlow()

    private val _isAviatorDetected = MutableStateFlow(false)
    val isAviatorDetected: StateFlow<Boolean> = _isAviatorDetected.asStateFlow()

    private val _detectedSiteName = MutableStateFlow("Betika")
    val detectedSiteName: StateFlow<String> = _detectedSiteName.asStateFlow()

    private val _detectionMethod = MutableStateFlow("URL Engine")
    val detectionMethod: StateFlow<String> = _detectionMethod.asStateFlow()

    // Callbacks for live telemetry and auth
    var onLiveFlightReceived: ((Double) -> Unit)? = null
    var onFlightsBatchReceived: ((List<Double>) -> Unit)? = null
    var onAuthStatusChanged: ((Boolean) -> Unit)? = null

    init {
        // Pre-initialize internal cache directories to prevent Chromium simple_index_file creation errors
        try {
            appContext.cacheDir?.mkdirs()
            java.io.File(appContext.cacheDir, "org.chromium.android_webview").mkdirs()
            java.io.File(appContext.cacheDir, "WebView").mkdirs()
            java.io.File(appContext.cacheDir, "webview_cache").mkdirs()
            java.io.File(appContext.dataDir, "app_webview").mkdirs()
        } catch (_: Exception) {}
    }

    companion object {
        @Volatile
        private var INSTANCE: PersistentBrowserManager? = null

        fun getInstance(context: Context): PersistentBrowserManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: PersistentBrowserManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    /**
     * Checks multiple heuristics to detect Aviator in real time.
     * When words like 'aviator', 'spribe', 'crash', 'fly', 'plane' appear, detector turns super active.
     * When these words are not in the URL/page, detector turns OFF.
     */
    fun evaluateUrlDetection(url: String?) {
        if (url.isNullOrBlank()) return
        _currentUrl.value = url
        val lower = url.lowercase()

        val isAviatorUrl = lower.contains("aviator") ||
                lower.contains("spribe") ||
                lower.contains("crash") ||
                lower.contains("fly") ||
                lower.contains("plane") ||
                lower.contains("/game/") ||
                lower.contains("casino/play") ||
                lower.contains("games/aviator")

        if (isAviatorUrl) {
            _isAviatorDetected.value = true
            _detectionMethod.value = "URL String Pattern Match ('aviator')"
            when {
                lower.contains("betika") -> _detectedSiteName.value = "Betika (UG)"
                lower.contains("betpawa") -> _detectedSiteName.value = "betPawa Uganda"
                lower.contains("fortebet") -> _detectedSiteName.value = "Fortebet"
                lower.contains("1win") -> _detectedSiteName.value = "1Win"
                else -> _detectedSiteName.value = "Inbuilt Game Engine"
            }
        } else {
            // Words not found in URL -> Turn detector OFF
            _isAviatorDetected.value = false
            _detectionMethod.value = "Standby (No Aviator keywords in URL)"
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    fun getOrCreateWebView(context: Context? = null): WebView {
        persistentWebView?.let { existing ->
            (existing.parent as? ViewGroup)?.removeView(existing)
            return existing
        }

        val targetContext = context ?: appContext
        val webView = WebView(targetContext).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.databaseEnabled = true
            settings.loadWithOverviewMode = true
            settings.useWideViewPort = true
            settings.allowFileAccess = true
            settings.allowContentAccess = true
            settings.cacheMode = WebSettings.LOAD_DEFAULT
            settings.mediaPlaybackRequiresUserGesture = false
            settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            settings.userAgentString = "Mozilla/5.0 (Linux; Android 13; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/122.0.0.0 Mobile Safari/537.36"

            // Enable third-party cookies for Spribe / iframe games
            try {
                val cookieMgr = android.webkit.CookieManager.getInstance()
                cookieMgr.setAcceptCookie(true)
                cookieMgr.setAcceptThirdPartyCookies(this, true)
            } catch (_: Exception) {}

            // Attach Native JavaScript Bridge
            addJavascriptInterface(
                object {
                    @JavascriptInterface
                    fun onNewFlightResult(multiplierStr: String) {
                        multiplierStr.toDoubleOrNull()?.let { mult ->
                            if (mult >= 1.0) {
                                _isAviatorDetected.value = true
                                _detectionMethod.value = "Live WebSocket / DOM Flight"
                                onLiveFlightReceived?.invoke(mult)
                            }
                        }
                    }

                    @JavascriptInterface
                    fun onFlightsBatchLogged(jsonArrayStr: String) {
                        try {
                            val mults = mutableListOf<Double>()
                            val cleaned = jsonArrayStr.trim('[', ']')
                            cleaned.split(",").forEach { token ->
                                token.trim('"', ' ', '\n').toDoubleOrNull()?.let { num ->
                                    if (num >= 1.0) mults.add(num)
                                }
                            }
                            if (mults.isNotEmpty()) {
                                _isAviatorDetected.value = true
                                _detectionMethod.value = "DOM Multiplier Strip"
                                onFlightsBatchReceived?.invoke(mults)
                            }
                        } catch (_: Exception) {}
                    }

                    @JavascriptInterface
                    fun onAuthStatus(isLoggedIn: Boolean) {
                        onAuthStatusChanged?.invoke(isLoggedIn)
                    }

                    @JavascriptInterface
                    fun onAviatorDetected(detected: Boolean, siteName: String?, method: String?) {
                        if (detected) {
                            _isAviatorDetected.value = true
                            if (!siteName.isNullOrBlank()) _detectedSiteName.value = siteName
                            if (!method.isNullOrBlank()) _detectionMethod.value = method
                        }
                    }
                },
                "AndroidAviatorBridge"
            )

            webViewClient = object : WebViewClient() {
                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    _isLoading.value = true
                    evaluateUrlDetection(url)
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    _isLoading.value = false
                    evaluateUrlDetection(url)
                    // Continuous Multi-Method Injected Observer
                    injectSuperActiveDetector(view)
                }

                override fun doUpdateVisitedHistory(view: WebView?, url: String?, isReload: Boolean) {
                    super.doUpdateVisitedHistory(view, url, isReload)
                    evaluateUrlDetection(url)
                }

                override fun onLoadResource(view: WebView?, url: String?) {
                    super.onLoadResource(view, url)
                    val lower = (url ?: "").lowercase()
                    if (lower.contains("spribe") || lower.contains("aviator") || lower.contains("crash")) {
                        _isAviatorDetected.value = true
                        _detectionMethod.value = "Network Resource Hook"
                    }
                }

                override fun onRenderProcessGone(view: WebView?, detail: android.webkit.RenderProcessGoneDetail?): Boolean {
                    // Gracefully recover if WebView render process encounters a GPU/memory reset
                    try {
                        persistentWebView = null
                        view?.destroy()
                    } catch (_: Exception) {}
                    return true
                }
            }

            webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                    _loadProgress.value = newProgress / 100f
                }

                override fun onReceivedTitle(view: WebView?, title: String?) {
                    super.onReceivedTitle(view, title)
                    val t = (title ?: "").lowercase()
                    if (t.contains("aviator") || t.contains("spribe") || t.contains("crash")) {
                        _isAviatorDetected.value = true
                        _detectionMethod.value = "Page Title Detector"
                    }
                }
            }

            loadUrl(_currentUrl.value)
        }

        persistentWebView = webView
        return webView
    }

    fun loadUrl(url: String) {
        _currentUrl.value = url
        evaluateUrlDetection(url)
        persistentWebView?.loadUrl(url)
    }

    fun setAviatorActive(active: Boolean, siteName: String = "Betika", method: String = "Manual / System") {
        _isAviatorDetected.value = active
        if (active) {
            _detectedSiteName.value = siteName
            _detectionMethod.value = method
        }
    }

    /**
     * Injects comprehensive multi-method Aviator Detector
     */
    fun injectSuperActiveDetector(webView: WebView?) {
        if (webView == null) return

        val script = """
            (function() {
                var detected = false;
                var method = "";
                var site = "";
                var url = (window.location.href || "").toLowerCase();
                var title = (document.title || "").toLowerCase();

                // 1. URL Analysis
                if (url.indexOf("aviator") !== -1 || url.indexOf("spribe") !== -1 || url.indexOf("crash") !== -1 || url.indexOf("fly") !== -1) {
                    detected = true;
                    method = "URL Bar Match";
                }

                // 2. Title & Meta Analysis
                if (!detected && (title.indexOf("aviator") !== -1 || title.indexOf("spribe") !== -1)) {
                    detected = true;
                    method = "DOM Page Title";
                }

                // 3. Game Element & Canvas Selectors
                if (!detected) {
                    var selectors = [
                        'iframe[src*="aviator" i]',
                        'iframe[src*="spribe" i]',
                        'iframe[src*="crash" i]',
                        '.payouts-block',
                        '.bubble-multiplier',
                        '.stage-board',
                        'app-game-play',
                        'app-stats-item',
                        '.game-container',
                        '.payouts',
                        'canvas',
                        'div.bubble',
                        'span.bubble',
                        '[class*="bubble-multiplier"]',
                        '.bet-control',
                        'div[class*="aviator" i]'
                    ];
                    for (var i = 0; i < selectors.length; i++) {
                        if (document.querySelector(selectors[i])) {
                            detected = true;
                            method = "DOM Element Match (" + selectors[i] + ")";
                            break;
                        }
                    }
                }

                // 4. Nested IFrames Search
                if (!detected) {
                    var frames = document.querySelectorAll('iframe');
                    for (var f = 0; f < frames.length; f++) {
                        try {
                            var src = (frames[f].src || "").toLowerCase();
                            if (src.indexOf("aviator") !== -1 || src.indexOf("spribe") !== -1 || src.indexOf("crash") !== -1) {
                                detected = true;
                                method = "IFrame Engine Source";
                                break;
                            }
                        } catch(e) {}
                    }
                }

                // Site name identification
                if (url.indexOf("betika") !== -1) site = "Betika (UG)";
                else if (url.indexOf("betpawa") !== -1) site = "betPawa Uganda";
                else if (url.indexOf("fortebet") !== -1) site = "Fortebet";
                else if (url.indexOf("1win") !== -1) site = "1Win";
                else site = "Identified Bet Site";

                if (detected && window.AndroidAviatorBridge && window.AndroidAviatorBridge.onAviatorDetected) {
                    window.AndroidAviatorBridge.onAviatorDetected(true, site, method);
                }

                // Setup dynamic MutationObserver to react to live DOM multiplier updates
                if (!window.__aviator_observer_set) {
                    window.__aviator_observer_set = true;
                    try {
                        var obs = new MutationObserver(function(mutations) {
                            var bubbles = document.querySelectorAll('.bubble-multiplier, .payouts-block, app-stats-item, span.bubble');
                            if (bubbles.length > 0 && window.AndroidAviatorBridge) {
                                window.AndroidAviatorBridge.onAviatorDetected(true, site, "Live Mutation Observer");
                            }
                        });
                        obs.observe(document.body, { childList: true, subtree: true });
                    } catch(e) {}
                }

                return detected ? ("1|" + site + "|" + method) : "0";
            })();
        """.trimIndent()

        webView.evaluateJavascript(script) { result ->
            if (result != null && result != "null" && result.startsWith("\"1")) {
                val clean = result.trim('"', ' ')
                val parts = clean.split("|")
                val site = parts.getOrNull(1) ?: "Betika (UG)"
                val method = parts.getOrNull(2) ?: "DOM Scan"
                _isAviatorDetected.value = true
                _detectedSiteName.value = site
                _detectionMethod.value = method
            } else {
                // If DOM did not detect Aviator elements, check if URL has keywords
                val lower = (_currentUrl.value).lowercase()
                val isAviatorUrl = lower.contains("aviator") ||
                        lower.contains("spribe") ||
                        lower.contains("crash") ||
                        lower.contains("fly") ||
                        lower.contains("plane") ||
                        lower.contains("/game/") ||
                        lower.contains("casino/play") ||
                        lower.contains("games/aviator")

                if (!isAviatorUrl) {
                    _isAviatorDetected.value = false
                    _detectionMethod.value = "Off (Words not detected in URL or Page)"
                }
            }
        }
    }
}

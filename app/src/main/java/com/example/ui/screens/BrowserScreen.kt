package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DataArray
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.AccountVaultManager
import com.example.domain.engine.BotAction
import com.example.domain.engine.BotAdvisorEngine
import com.example.domain.engine.BotRiskLevel
import com.example.domain.engine.PredictionEngine
import com.example.domain.model.GameRound
import com.example.ui.components.StatisticalDisclaimerBanner
import com.example.ui.theme.AmberMedium
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyanBright
import com.example.ui.theme.EmeraldSafe
import com.example.ui.theme.PurpleModel
import com.example.ui.theme.RubyCrash
import com.example.ui.theme.SlateBackground
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateCard
import com.example.ui.theme.SlateSurfaceVariant
import com.example.ui.theme.SlateTextMuted
import com.example.ui.theme.SlateTextPrimary
import com.example.ui.theme.SlateTextSecondary
import com.example.ui.viewmodel.GameViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max
import kotlin.random.Random

private const val DEFAULT_BETIKA_LOGIN_URL = "https://www.betika.com/en-ug/login"
private const val DEFAULT_BETIKA_AVIATOR_URL = "https://www.betika.com/en-ug/aviator"

/**
 * Supported Betting Sites with Direct Login and Aviator Game URLs
 */
data class BettingSitePreset(
    val name: String,
    val shortLabel: String,
    val loginUrl: String,
    val gameUrl: String,
    val domainKey: String,
    val defaultBadge: String
)

val BETTING_SITES = listOf(
    BettingSitePreset(
        name = "Betika (UG)",
        shortLabel = "Betika",
        loginUrl = "https://www.betika.com/en-ug/login",
        gameUrl = "https://www.betika.com/en-ug/aviator",
        domainKey = "betika",
        defaultBadge = "UG"
    ),
    BettingSitePreset(
        name = "betPawa Uganda",
        shortLabel = "betPawa",
        loginUrl = "https://www.betpawa.ug/login",
        gameUrl = "https://www.betpawa.ug/casino/aviator",
        domainKey = "betpawa",
        defaultBadge = "UG"
    ),
    BettingSitePreset(
        name = "Fortebet",
        shortLabel = "Fortebet",
        loginUrl = "https://www.fortebet.ug/login",
        gameUrl = "https://www.fortebet.ug/casino/aviator",
        domainKey = "fortebet",
        defaultBadge = "UG"
    ),
    BettingSitePreset(
        name = "1Win",
        shortLabel = "1Win",
        loginUrl = "https://1win.pro/auth",
        gameUrl = "https://1win.pro/casino/play/aviator",
        domainKey = "1win",
        defaultBadge = "INT"
    )
)

/**
 * Data class representing a recorded live flight intercepted from site DOM/WebSocket
 */
data class InterceptedFlightLog(
    val id: String,
    val multiplier: Double,
    val formattedTime: String,
    val isWin150: Boolean
)

/**
 * Injects Bot Auto-Fill into Betika login form
 */
private fun injectBetikaAutoFill(
    webView: WebView?,
    phone: String,
    pass: String,
    autoSubmit: Boolean,
    onComplete: ((String) -> Unit)? = null
) {
    if (webView == null || phone.isBlank() || pass.isBlank()) {
        onComplete?.invoke("Vault credentials are empty")
        return
    }

    val sanitizedPhone = phone.replace("\\", "\\\\").replace("'", "\\'")
    val sanitizedPass = pass.replace("\\", "\\\\").replace("'", "\\'")
    val submitFlag = if (autoSubmit) "true" else "false"

    val script = """
        (function() {
            function setField(element, val) {
                if (!element) return false;
                try {
                    element.focus();
                    var proto = Object.getPrototypeOf(element);
                    var descriptor = Object.getOwnPropertyDescriptor(proto, 'value') || 
                                     Object.getOwnPropertyDescriptor(HTMLInputElement.prototype, 'value');
                    if (descriptor && descriptor.set) {
                        descriptor.set.call(element, val);
                    } else {
                        element.value = val;
                    }
                    element.dispatchEvent(new Event('input', { bubbles: true, cancelable: true }));
                    element.dispatchEvent(new Event('change', { bubbles: true, cancelable: true }));
                    element.dispatchEvent(new Event('blur', { bubbles: true, cancelable: true }));
                    return true;
                } catch(e) {
                    try {
                        element.value = val;
                        element.dispatchEvent(new Event('input', { bubbles: true }));
                        element.dispatchEvent(new Event('change', { bubbles: true }));
                        return true;
                    } catch(err) { return false; }
                }
            }

            var phoneSelectors = [
                'input[type="tel"]',
                'input[name*="phone" i]',
                'input[name*="mobile" i]',
                'input[name*="user" i]',
                'input[name*="login" i]',
                'input[autocomplete*="tel" i]',
                'input[autocomplete*="username" i]',
                'input[placeholder*="phone" i]',
                'input[placeholder*="mobile" i]',
                'input[placeholder*="07" i]',
                'input[placeholder*="number" i]',
                'input[type="text"]'
            ];

            var passSelectors = [
                'input[type="password"]',
                'input[name*="pass" i]',
                'input[autocomplete*="password" i]',
                'input[placeholder*="pass" i]'
            ];

            var phoneField = null;
            for (var i = 0; i < phoneSelectors.length; i++) {
                var els = document.querySelectorAll(phoneSelectors[i]);
                for (var j = 0; j < els.length; j++) {
                    var el = els[j];
                    if (el.type !== 'password' && el.type !== 'hidden' && el.offsetParent !== null) {
                        phoneField = el;
                        break;
                    }
                }
                if (phoneField) break;
            }

            var passField = null;
            for (var i = 0; i < passSelectors.length; i++) {
                var els = document.querySelectorAll(passSelectors[i]);
                for (var j = 0; j < els.length; j++) {
                    var el = els[j];
                    if (el.offsetParent !== null) {
                        passField = el;
                        break;
                    }
                }
                if (passField) break;
            }

            var filledPhone = setField(phoneField, '$sanitizedPhone');
            var filledPass = setField(passField, '$sanitizedPass');

            if ($submitFlag && filledPhone && filledPass) {
                setTimeout(function() {
                    var submitBtn = document.querySelector('button[type="submit"], button.login, button.btn-login, button[data-testid*="login" i]');
                    if (!submitBtn) {
                        var buttons = document.querySelectorAll('button, input[type="submit"], a');
                        for (var k = 0; k < buttons.length; k++) {
                            var b = buttons[k];
                            var text = (b.innerText || b.value || '').toLowerCase().trim();
                            if (text === 'log in' || text === 'login' || text === 'ingia' || text === 'se connecter' || text.indexOf('login') !== -1) {
                                submitBtn = b;
                                break;
                            }
                        }
                    }
                    if (submitBtn) {
                        submitBtn.click();
                    }
                }, 400);
            }

            return (filledPhone ? '1' : '0') + (filledPass ? '1' : '0');
        })();
    """.trimIndent()

    webView.evaluateJavascript(script) { result ->
        val filled = result != null && (result.contains("1"))
        if (filled) {
            onComplete?.invoke("Bot successfully filled credentials (0780531600)")
        } else {
            onComplete?.invoke("Ready - tap login if form is currently hidden")
        }
    }
}

/**
 * Site Flight Log Injector: Injected into WebView DOM & IFrames.
 * Intercepts real-time multiplier bubbles, stats strips, and Spribe engine data.
 */
private fun injectSiteFlightReader(
    webView: WebView?,
    onExtracted: (List<Double>, Boolean, String?, String) -> Unit
) {
    if (webView == null) return

    val extractionScript = """
        (function() {
            var multipliers = [];
            var isLoggedIn = false;
            var balanceText = null;
            var rawLogs = [];

            try {
                // 1. Check Login / Account state
                var loggedInIndicators = document.querySelectorAll('.user-balance, .balance-val, [data-testid*="balance" i], .user-info, .account-balance, .balance, [class*="user-profile"]');
                if (loggedInIndicators.length > 0) {
                    isLoggedIn = true;
                    balanceText = (loggedInIndicators[0].innerText || '').trim();
                } else {
                    for (var i = 0; i < localStorage.length; i++) {
                        var key = localStorage.key(i).toLowerCase();
                        if (key.indexOf('token') !== -1 || key.indexOf('auth') !== -1 || key.indexOf('user') !== -1) {
                            isLoggedIn = true;
                            break;
                        }
                    }
                }

                // 2. Helper to scan any document/iframe context for multiplier elements
                function scanContext(doc) {
                    if (!doc) return;
                    var querySelectors = [
                        '.payouts-block .bubble-multiplier',
                        '.bubble-multiplier',
                        '[class*="bubble"]',
                        '[class*="multiplier"]',
                        '[class*="payout"]',
                        '.history-item',
                        '.result-item',
                        '.payouts',
                        '.history',
                        'app-stats-item',
                        'div[class*="bubble"]',
                        'span[class*="bubble"]',
                        '.bubble',
                        '.payout-item'
                    ];

                    for (var s = 0; s < querySelectors.length; s++) {
                        try {
                            var textNodes = doc.querySelectorAll(querySelectors[s]);
                            for (var i = 0; i < textNodes.length; i++) {
                                var text = (textNodes[i].innerText || textNodes[i].textContent || '').trim();
                                if (text) {
                                    var match = text.match(/([0-9]+(?:\.[0-9]+)?)\s*x?/i);
                                    if (match && match[1]) {
                                        var val = parseFloat(match[1]);
                                        if (val >= 1.0 && val <= 10000.0) {
                                            if (multipliers.indexOf(val) === -1) {
                                                multipliers.push(val);
                                                rawLogs.push(val + 'x');
                                            }
                                        }
                                    }
                                }
                            }
                        } catch(err) {}
                    }
                }

                // Scan Main Document
                scanContext(document);

                // Scan all IFrames (Spribe Aviator nested game containers)
                var iframes = document.querySelectorAll('iframe');
                for (var f = 0; f < iframes.length; f++) {
                    try {
                        var iframeDoc = iframes[f].contentDocument || iframes[f].contentWindow.document;
                        if (iframeDoc) {
                            scanContext(iframeDoc);
                        }
                    } catch(frameErr) {}
                }

                // Setup Mutation Observer if not already installed
                if (!window._aviatorObserverInstalled) {
                    window._aviatorObserverInstalled = true;
                    var targetNode = document.body;
                    var config = { childList: true, subtree: true, characterData: true };
                    var observer = new MutationObserver(function(mutations) {
                        try {
                            if (window.AndroidAviatorBridge) {
                                scanContext(document);
                                if (multipliers.length > 0) {
                                    window.AndroidAviatorBridge.onFlightsBatchLogged(JSON.stringify(multipliers.slice(0, 20)));
                                }
                            }
                        } catch(obsErr) {}
                    });
                    if (targetNode) {
                        observer.observe(targetNode, config);
                    }
                }

            } catch(e) {
                rawLogs.push('Scanner Error: ' + e.message);
            }

            return JSON.stringify({
                multipliers: multipliers.slice(0, 20),
                isLoggedIn: isLoggedIn,
                balance: balanceText,
                logSummary: rawLogs.join(', ')
            });
        })();
    """.trimIndent()

    webView.evaluateJavascript(extractionScript) { result ->
        if (result != null && result != "null") {
            try {
                var jsonStr = result
                if (jsonStr.startsWith("\"") && jsonStr.endsWith("\"")) {
                    jsonStr = jsonStr.substring(1, jsonStr.length - 1)
                        .replace("\\\"", "\"")
                        .replace("\\\\", "\\")
                }

                val mults = mutableListOf<Double>()
                var loggedIn = false
                var bal: String? = null
                var summary = ""

                if (jsonStr.contains("\"multipliers\":")) {
                    val multPart = jsonStr.substringAfter("\"multipliers\":[").substringBefore("]")
                    if (multPart.isNotBlank()) {
                        multPart.split(",").forEach { token ->
                            token.trim().toDoubleOrNull()?.let { num ->
                                if (num >= 1.0) mults.add(num)
                            }
                        }
                    }
                }
                if (jsonStr.contains("\"isLoggedIn\":true")) {
                    loggedIn = true
                }
                if (jsonStr.contains("\"balance\":")) {
                    val rawBal = jsonStr.substringAfter("\"balance\":").substringBefore(",").substringBefore("}").trim('"', ' ')
                    if (rawBal.isNotBlank() && rawBal != "null") {
                        bal = rawBal
                    }
                }
                if (jsonStr.contains("\"logSummary\":")) {
                    summary = jsonStr.substringAfter("\"logSummary\":\"").substringBefore("\"")
                }

                onExtracted(mults, loggedIn, bal, summary)
            } catch (e: Exception) {
                // Parse fallback
            }
        }
    }
}

/**
 * Detects whether the active page or any iframe has loaded the Aviator game
 */
private fun injectAviatorDetector(
    webView: WebView?,
    onDetected: (Boolean, String) -> Unit
) {
    if (webView == null) return

    val detectorScript = """
        (function() {
            var isDetected = false;
            var siteDetectedName = "";
            var curUrl = (window.location.href || "").toLowerCase();
            
            if (curUrl.indexOf("betika") !== -1) siteDetectedName = "Betika";
            else if (curUrl.indexOf("betpawa") !== -1) siteDetectedName = "betPawa";
            else if (curUrl.indexOf("fortebet") !== -1) siteDetectedName = "Fortebet";
            else if (curUrl.indexOf("1win") !== -1) siteDetectedName = "1Win";
            else siteDetectedName = "Selected Site";

            // 1. Check URL parameters & paths
            if (curUrl.indexOf("aviator") !== -1 || curUrl.indexOf("spribe") !== -1 || curUrl.indexOf("crash") !== -1) {
                isDetected = true;
            }

            // 2. Check DOM Elements for Aviator gameplay elements
            var gameSelectors = [
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
                '[class*="bubble-multiplier"]',
                'div.bubble',
                'span.bubble',
                '.bet-control',
                'div[class*="aviator" i]',
                'div[class*="game" i]',
                'canvas'
            ];

            for (var i = 0; i < gameSelectors.length; i++) {
                if (document.querySelector(gameSelectors[i])) {
                    isDetected = true;
                    break;
                }
            }

            // 3. Inspect nested IFrames
            var iframes = document.querySelectorAll('iframe');
            for (var f = 0; f < iframes.length; f++) {
                try {
                    var src = (iframes[f].src || "").toLowerCase();
                    if (src.indexOf("aviator") !== -1 || src.indexOf("spribe") !== -1 || src.indexOf("crash") !== -1) {
                        isDetected = true;
                        break;
                    }
                    var idoc = iframes[f].contentDocument || iframes[f].contentWindow.document;
                    if (idoc) {
                        for (var k = 0; k < gameSelectors.length; k++) {
                            if (idoc.querySelector(gameSelectors[k])) {
                                isDetected = true;
                                break;
                            }
                        }
                    }
                } catch(e) {}
            }

            if (window.AndroidAviatorBridge && window.AndroidAviatorBridge.onAviatorDetected) {
                window.AndroidAviatorBridge.onAviatorDetected(isDetected, siteDetectedName);
            }

            return isDetected ? ("1|" + siteDetectedName) : ("0|" + siteDetectedName);
        })();
    """.trimIndent()

    webView.evaluateJavascript(detectorScript) { result ->
        if (result != null && result != "null") {
            val clean = result.trim('"', ' ')
            val detected = clean.startsWith("1")
            val name = if (clean.contains("|")) clean.substringAfter("|") else ""
            onDetected(detected, name)
        }
    }
}

/**
 * Native JavaScript Bridge for Aviator real-time site log injection
 */
class AviatorJsBridge(
    private val onLiveFlightReceived: (Double) -> Unit,
    private val onFlightsBatchLogged: (List<Double>) -> Unit,
    private val onAuthStatusChanged: (Boolean) -> Unit,
    private val onAviatorDetected: (Boolean, String) -> Unit
) {
    @JavascriptInterface
    fun onNewFlightResult(multiplierStr: String) {
        multiplierStr.toDoubleOrNull()?.let { mult ->
            if (mult >= 1.0) {
                onLiveFlightReceived(mult)
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
                onFlightsBatchLogged(mults)
            }
        } catch (e: Exception) {}
    }

    @JavascriptInterface
    fun onAuthStatus(isLoggedIn: Boolean) {
        onAuthStatusChanged(isLoggedIn)
    }

    @JavascriptInterface
    fun onAviatorDetected(detected: Boolean, siteName: String?) {
        onAviatorDetected(detected, siteName ?: "")
    }
}

@Composable
fun BrowserScreen(
    modifier: Modifier = Modifier,
    viewModel: GameViewModel = viewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val vaultManager = remember { AccountVaultManager.getInstance(context) }
    val browserManager = remember { com.example.data.local.PersistentBrowserManager.getInstance(context) }

    val allRounds by viewModel.allRounds.collectAsStateWithLifecycle()
    val recentRounds by viewModel.recentRounds.collectAsStateWithLifecycle()
    val predictionState by viewModel.predictionState.collectAsStateWithLifecycle()
    val botDecisionState by viewModel.botDecisionState.collectAsStateWithLifecycle()
    val siteSyncState by viewModel.siteSyncState.collectAsStateWithLifecycle()

    val browserUrl by browserManager.currentUrl.collectAsStateWithLifecycle()
    val isBrowserLoading by browserManager.isLoading.collectAsStateWithLifecycle()
    val browserLoadProgress by browserManager.loadProgress.collectAsStateWithLifecycle()
    val isAviatorGameDetected by browserManager.isAviatorDetected.collectAsStateWithLifecycle()
    val detectedGameSite by browserManager.detectedSiteName.collectAsStateWithLifecycle()
    val detectionMethod by browserManager.detectionMethod.collectAsStateWithLifecycle()

    var selectedSite by remember { mutableStateOf(BETTING_SITES[0]) }
    var currentSiteMode by remember { mutableStateOf("LOGIN") } // "LOGIN" or "GAME"

    var urlText by remember { mutableStateOf(browserUrl) }
    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var showVaultDialog by remember { mutableStateOf(false) }
    var showBotDetailsDialog by remember { mutableStateOf(false) }
    var showExportLogsDialog by remember { mutableStateOf(false) }

    // Intercepted Flight Logs for copy & audit
    val interceptedLogs = remember { mutableStateListOf<InterceptedFlightLog>() }
    var lastScrapeTimestamp by remember { mutableStateOf("Just now") }
    var rawSiteSummaryText by remember { mutableStateOf("Feeder Initialized") }

    // Artificial Bot Switch & Online/Offline Auto-Fill State
    var isBotActive by remember { mutableStateOf(vaultManager.isBotActive()) }

    // Real Account / Logged-in Site Status
    var isSiteLoggedIn by remember { mutableStateOf(false) }
    var siteAccountBalance by remember { mutableStateOf<String?>(null) }

    // Vault Credentials State
    var savedPhone by remember { mutableStateOf(vaultManager.getPhoneNumber()) }
    var savedPassword by remember { mutableStateOf(vaultManager.getPassword()) }
    var autoFillOnLoad by remember { mutableStateOf(vaultManager.isAutoFillEnabled()) }
    var autoSubmitOnFill by remember { mutableStateOf(vaultManager.isAutoSubmitEnabled()) }

    // Automatic Next Round Flight Mode States
    var isAutoRunning by remember { mutableStateOf(true) }
    var isPredictingNext by remember { mutableStateOf(false) }
    var calculatedNextFigure by remember { mutableStateOf<Double?>(null) }
    var nextConfidence by remember { mutableFloatStateOf(0f) }
    var nextRangeLabel by remember { mutableStateOf<String?>(null) }
    var roundCycleIndex by remember { mutableIntStateOf(1) }

    val timeFormat = remember { SimpleDateFormat("HH:mm:ss", Locale.US) }

    // Synchronize URL text with browser manager changes
    LaunchedEffect(browserUrl) {
        urlText = browserUrl
        browserManager.evaluateUrlDetection(browserUrl)
    }

    fun addInterceptedFlight(mult: Double) {
        val now = System.currentTimeMillis()
        val timeStr = timeFormat.format(Date(now))
        val entry = InterceptedFlightLog(
            id = "#${(now % 10000)}",
            multiplier = mult,
            formattedTime = timeStr,
            isWin150 = mult >= 1.50
        )
        if (interceptedLogs.none { it.multiplier == mult && it.formattedTime == timeStr }) {
            interceptedLogs.add(0, entry)
            if (interceptedLogs.size > 100) {
                interceptedLogs.removeAt(interceptedLogs.lastIndex)
            }
        }
    }

    // Function to copy flight logs to Android Clipboard
    fun copyFlightLogs(formatType: String) {
        val listToCopy = if (interceptedLogs.isNotEmpty()) {
            interceptedLogs.toList()
        } else {
            recentRounds.map { r ->
                InterceptedFlightLog(
                    id = "#${r.roundId}",
                    multiplier = r.multiplier,
                    formattedTime = r.formattedTime,
                    isWin150 = r.multiplier >= 1.50
                )
            }
        }

        if (listToCopy.isEmpty()) {
            Toast.makeText(context, "No flight logs captured yet to copy", Toast.LENGTH_SHORT).show()
            return
        }

        val textToCopy = when (formatType) {
            "CSV" -> {
                listToCopy.joinToString(", ") { "${String.format(Locale.US, "%.2f", it.multiplier)}x" }
            }
            "JSON" -> {
                val items = listToCopy.joinToString(",\n  ") { item ->
                    """{"id": "${item.id}", "multiplier": ${item.multiplier}, "time": "${item.formattedTime}", "target_150_hit": ${item.isWin150}}"""
                }
                "[\n  $items\n]"
            }
            else -> {
                val sb = StringBuilder()
                sb.append("=== ${selectedSite.name.uppercase(Locale.US)} AVIATOR LIVE FLIGHT LOGS ===\n")
                sb.append("Source: Inbuilt Browser Java Injector Live Feeder\n")
                sb.append("Total Intercepted Flights: ${listToCopy.size}\n")
                sb.append("Copied At: ${SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())}\n")
                sb.append("-----------------------------------------\n")
                listToCopy.forEach { item ->
                    val status = if (item.isWin150) "✅ 1.50x TARGET WON" else "❌ BUST (<1.50x)"
                    sb.append("${item.id} | ${item.formattedTime} | ${String.format(Locale.US, "%.2f", item.multiplier)}x | $status\n")
                }
                sb.toString()
            }
        }

        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("${selectedSite.shortLabel} Aviator Flight Logs", textToCopy)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Copied ${listToCopy.size} site flights to clipboard ($formatType)!", Toast.LENGTH_SHORT).show()
    }

    // Calculation logic for Next Flight Figure: updates on each finished round
    fun calculateNextRoundFigure(manualTrigger: Boolean = false, triggeredByRoundFinish: Boolean = false) {
        isPredictingNext = true
        coroutineScope.launch {
            if (manualTrigger) {
                // Read active browser DOM telemetry for real logged in site updates
                injectSiteFlightReader(webViewInstance) { extracted, loggedIn, balance, summary ->
                    if (loggedIn) {
                        isSiteLoggedIn = true
                        viewModel.updateSiteStatus(isLoggedIn = true, balance = balance)
                    }
                    if (balance != null) {
                        siteAccountBalance = balance
                    }
                    if (extracted.isNotEmpty()) {
                        browserManager.setAviatorActive(true, selectedSite.name, "DOM Extraction")
                        extracted.forEach { mult -> addInterceptedFlight(mult) }
                        viewModel.onSiteFlightsBatchReceived(extracted)
                        lastScrapeTimestamp = timeFormat.format(Date())
                        rawSiteSummaryText = summary.ifBlank { "${extracted.size} flights synced" }
                    }
                }
                delay(200)
            }

            val currentList = allRounds
            val pred = PredictionEngine.predictNext(currentList, 50)
            val baseVal = pred.expectedMultiplier

            val recentMean = if (currentList.isNotEmpty()) currentList.take(5).map { it.multiplier }.average() else 2.0
            val adjustedFigure = if (recentMean < 1.8) {
                max(1.10, ((baseVal * 0.95 + Random.nextDouble(0.05, 0.25)) * 100).toInt() / 100.0)
            } else {
                max(1.15, ((baseVal + Random.nextDouble(-0.15, 0.30)) * 100).toInt() / 100.0)
            }

            calculatedNextFigure = adjustedFigure
            nextConfidence = (pred.ensembleConfidence * 100).toFloat()
            nextRangeLabel = pred.recommendedRange
            isPredictingNext = false
            roundCycleIndex++

            if (manualTrigger) {
                Toast.makeText(context, "Next Flight Figure: ${String.format(Locale.US, "%.2f", adjustedFigure)}x", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Auto-Run loop for Next Flight Predictions
    LaunchedEffect(isAutoRunning, allRounds.size) {
        if (isAutoRunning) {
            while (isActive && isAutoRunning) {
                calculateNextRoundFigure(manualTrigger = false)
                delay(4500)
            }
        }
    }

    // Continuous Real Live Telemetry & Aviator Game State Poller
    LaunchedEffect(webViewInstance, isBotActive) {
        while (isActive) {
            webViewInstance?.let { wv ->
                // Check if user has opened the Aviator game (Green / Red indicator)
                browserManager.injectSuperActiveDetector(wv)

                // Ingest flight telemetry from the DOM
                injectSiteFlightReader(wv) { mults, loggedIn, bal, summary ->
                    if (loggedIn) {
                        isSiteLoggedIn = true
                        viewModel.updateSiteStatus(isLoggedIn = true, balance = bal)
                    }
                    if (bal != null) {
                        siteAccountBalance = bal
                    }
                    if (mults.isNotEmpty()) {
                        browserManager.setAviatorActive(true, selectedSite.name, "Live DOM Stream")
                        mults.forEach { mult -> addInterceptedFlight(mult) }
                        viewModel.onSiteFlightsBatchReceived(mults)
                        lastScrapeTimestamp = timeFormat.format(Date())
                        rawSiteSummaryText = summary.ifBlank { "${mults.size} flights synced" }
                        // Display next flight figure immediately after finishing of the round
                        calculateNextRoundFigure(manualTrigger = false, triggeredByRoundFinish = true)
                    }
                }
            }
            delay(1500)
        }
    }

    fun navigateTo(url: String) {
        val target = if (!url.startsWith("http://") && !url.startsWith("https://")) {
            "https://$url"
        } else {
            url
        }
        urlText = target
        browserManager.evaluateUrlDetection(target)
        browserManager.loadUrl(target)
    }

    fun triggerBotAutoLogin() {
        if (savedPhone.isBlank() || savedPassword.isBlank()) {
            Toast.makeText(context, "Configuring default credentials (0780531600)...", Toast.LENGTH_SHORT).show()
            savedPhone = AccountVaultManager.DEFAULT_BOT_PHONE
            savedPassword = AccountVaultManager.DEFAULT_BOT_PASS
            vaultManager.saveCredentials(savedPhone, savedPassword, true, true)
        }

        injectBetikaAutoFill(
            webView = webViewInstance,
            phone = savedPhone,
            pass = savedPassword,
            autoSubmit = autoSubmitOnFill || isBotActive
        ) { status ->
            Toast.makeText(context, status, Toast.LENGTH_SHORT).show()
        }
    }

    // Pulse animation
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 4.dp, vertical = 2.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // =========================================================================
        // 1. TOP BAR: LIVE FEEDER BADGE + BOT MASTER TOGGLE & CREDENTIALS VAULT
        // =========================================================================
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = SlateCard),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.horizontalGradient(
                    listOf(
                        if (isBotActive) CyberCyan else SlateBorder,
                        if (isBotActive) PurpleModel else SlateBorder
                    )
                )
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Direct Betika Aviator Live Feeder Indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .background(SlateSurfaceVariant, RoundedCornerShape(8.dp))
                        .border(0.8.dp, CyberCyan.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(EmeraldSafe, CircleShape)
                    )
                    Text(
                        text = "LIVE FEEDER (UG)",
                        color = SlateTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        letterSpacing = 0.5.sp
                    )
                }

                // Center: Bot Auto-Fill Online/Offline Master Switch
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Column(horizontalAlignment = Alignment.End) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            Icon(
                                imageVector = Icons.Default.SmartToy,
                                contentDescription = null,
                                tint = if (isBotActive) CyberCyan else SlateTextMuted,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = if (isBotActive) "AI BOT ON" else "AI BOT OFF",
                                color = if (isBotActive) CyberCyan else SlateTextMuted,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                        Text(
                            text = if (isBotActive) "Auto-Log: 0780531600" else "Manual Mode",
                            color = SlateTextSecondary,
                            fontSize = 8.5.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Switch(
                        checked = isBotActive,
                        onCheckedChange = { active ->
                            isBotActive = active
                            vaultManager.setBotActive(active)
                            if (active) {
                                autoFillOnLoad = true
                                vaultManager.setAutoFillEnabled(true)
                                Toast.makeText(context, "AI Bot Started - Inserting Logins (0780531600)...", Toast.LENGTH_SHORT).show()
                                triggerBotAutoLogin()
                            } else {
                                autoFillOnLoad = false
                                vaultManager.setAutoFillEnabled(false)
                                Toast.makeText(context, "AI Bot Paused (Offline)", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = CyberCyan,
                            uncheckedThumbColor = SlateTextMuted,
                            uncheckedTrackColor = SlateSurfaceVariant
                        ),
                        modifier = Modifier.height(24.dp)
                    )
                }

                // Right: Action Trigger & Vault Settings
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Button(
                        onClick = { triggerBotAutoLogin() },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FlashOn,
                            contentDescription = "Bot Log In",
                            tint = SlateCard,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "Log In",
                            color = SlateCard,
                            fontWeight = FontWeight.Black,
                            fontSize = 10.5.sp
                        )
                    }

                    IconButton(
                        onClick = { showVaultDialog = true },
                        modifier = Modifier
                            .size(28.dp)
                            .background(SlateSurfaceVariant, RoundedCornerShape(6.dp))
                            .border(0.8.dp, SlateBorder, RoundedCornerShape(6.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = "Vault",
                            tint = CyberCyan,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }

        // =========================================================================
        // 2. AVIATOR GAME DETECTION SIGNAL INDICATOR (SUPER ACTIVE / STANDBY)
        // =========================================================================
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isAviatorGameDetected) EmeraldSafe.copy(alpha = 0.18f) else RubyCrash.copy(alpha = 0.15f)
            ),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.horizontalGradient(
                    listOf(
                        if (isAviatorGameDetected) EmeraldSafe else RubyCrash,
                        if (isAviatorGameDetected) CyanBright else AmberMedium
                    )
                )
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .alpha(if (isAviatorGameDetected) pulseAlpha else 1.0f)
                            .background(if (isAviatorGameDetected) EmeraldSafe else RubyCrash, CircleShape)
                    )
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                            Text(
                                text = if (isAviatorGameDetected) "SIGNAL: GREEN" else "SIGNAL: RED",
                                color = if (isAviatorGameDetected) EmeraldSafe else RubyCrash,
                                fontWeight = FontWeight.Black,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = if (isAviatorGameDetected) "• SUPER ACTIVE AVIATOR" else "• NO AVIATOR DETECTED",
                                color = if (isAviatorGameDetected) EmeraldSafe else SlateTextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.5.sp
                            )
                        }
                        Text(
                            text = if (isAviatorGameDetected)
                                "Active via $detectionMethod on $detectedGameSite — Live stream online"
                            else
                                "Load Aviator or type URL containing 'aviator' to activate signal instantly",
                            color = SlateTextSecondary,
                            fontSize = 8.5.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .background(
                            if (isAviatorGameDetected) EmeraldSafe.copy(alpha = 0.28f) else RubyCrash.copy(alpha = 0.25f),
                            RoundedCornerShape(6.dp)
                        )
                        .border(
                            0.8.dp,
                            if (isAviatorGameDetected) EmeraldSafe else RubyCrash,
                            RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 7.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = if (isAviatorGameDetected) "🟢 SUPER ACTIVE" else "🔴 OFF (STANDBY)",
                        color = if (isAviatorGameDetected) EmeraldSafe else RubyCrash,
                        fontWeight = FontWeight.Black,
                        fontSize = 9.sp
                    )
                }
            }
        }

        // =========================================================================
        // 3. SITE SELECTION & DIRECT LOGIN / DIRECT GAME CONTROLLER BAR
        // =========================================================================
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "SITES:",
                    color = SlateTextSecondary,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                BETTING_SITES.forEach { site ->
                    val isSelected = selectedSite.domainKey == site.domainKey
                    Surface(
                        onClick = {
                            selectedSite = site
                            val destUrl = if (currentSiteMode == "LOGIN") site.loginUrl else site.gameUrl
                            navigateTo(destUrl)
                        },
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) CyberCyan.copy(alpha = 0.22f) else SlateCard,
                        border = BorderStroke(
                            width = if (isSelected) 1.2.dp else 0.8.dp,
                            color = if (isSelected) CyberCyan else SlateBorder
                        ),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (isSelected) {
                                Box(modifier = Modifier.size(5.dp).background(CyberCyan, CircleShape))
                            }
                            Text(
                                text = site.name,
                                color = if (isSelected) CyberCyan else SlateTextPrimary,
                                fontSize = 9.5.sp,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium
                            )
                            Text(
                                text = site.defaultBadge,
                                color = if (isSelected) CyberCyan.copy(alpha = 0.85f) else SlateTextMuted,
                                fontSize = 7.5.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .background(SlateSurfaceVariant, RoundedCornerShape(3.dp))
                                    .padding(horizontal = 3.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
            }

            // Quick Direct Mode Switcher (Direct Login vs Direct Aviator Game)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Direct Login Button
                Surface(
                    onClick = {
                        currentSiteMode = "LOGIN"
                        navigateTo(selectedSite.loginUrl)
                    },
                    shape = RoundedCornerShape(6.dp),
                    color = if (currentSiteMode == "LOGIN") CyberCyan.copy(alpha = 0.2f) else SlateSurfaceVariant,
                    border = BorderStroke(
                        width = 0.8.dp,
                        color = if (currentSiteMode == "LOGIN") CyberCyan else SlateBorder
                    ),
                    modifier = Modifier.weight(1f).height(26.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(imageVector = Icons.Default.Key, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "🔑 Direct Login Page",
                            color = if (currentSiteMode == "LOGIN") CyberCyan else SlateTextSecondary,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Direct Aviator Game Button
                Surface(
                    onClick = {
                        currentSiteMode = "GAME"
                        navigateTo(selectedSite.gameUrl)
                    },
                    shape = RoundedCornerShape(6.dp),
                    color = if (currentSiteMode == "GAME") EmeraldSafe.copy(alpha = 0.2f) else SlateSurfaceVariant,
                    border = BorderStroke(
                        width = 0.8.dp,
                        color = if (currentSiteMode == "GAME") EmeraldSafe else SlateBorder
                    ),
                    modifier = Modifier.weight(1f).height(26.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(imageVector = Icons.Default.FlightTakeoff, contentDescription = null, tint = EmeraldSafe, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "🚀 Direct Aviator Game",
                            color = if (currentSiteMode == "GAME") EmeraldSafe else SlateTextSecondary,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // =========================================================================
        // 4. ARTIFICIAL BOT KNOWLEDGE RADAR: 1.50x BET ODD ENTRY DECISION ENGINE
        // =========================================================================
        val currentBotDecision = botDecisionState ?: BotAdvisorEngine.evaluateEntry(allRounds)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showBotDetailsDialog = true },
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = when (currentBotDecision.riskLevel) {
                    BotRiskLevel.LOW_RISK -> EmeraldSafe.copy(alpha = 0.12f)
                    BotRiskLevel.MEDIUM_RISK -> AmberMedium.copy(alpha = 0.12f)
                    BotRiskLevel.HIGH_RISK -> RubyCrash.copy(alpha = 0.12f)
                }
            ),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.horizontalGradient(
                    listOf(
                        when (currentBotDecision.riskLevel) {
                            BotRiskLevel.LOW_RISK -> EmeraldSafe
                            BotRiskLevel.MEDIUM_RISK -> AmberMedium
                            BotRiskLevel.HIGH_RISK -> RubyCrash
                        },
                        PurpleModel
                    )
                )
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(
                                color = when (currentBotDecision.riskLevel) {
                                    BotRiskLevel.LOW_RISK -> EmeraldSafe
                                    BotRiskLevel.MEDIUM_RISK -> AmberMedium
                                    BotRiskLevel.HIGH_RISK -> RubyCrash
                                },
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (currentBotDecision.action) {
                                BotAction.ENTER_NOW, BotAction.ENTER_CAUTIOUS -> Icons.Default.TrendingUp
                                else -> Icons.Default.Warning
                            },
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "BOT 1.50x TARGET RADAR:",
                                color = SlateTextSecondary,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = currentBotDecision.reasonTitle,
                                color = when (currentBotDecision.riskLevel) {
                                    BotRiskLevel.LOW_RISK -> EmeraldSafe
                                    BotRiskLevel.MEDIUM_RISK -> AmberMedium
                                    BotRiskLevel.HIGH_RISK -> RubyCrash
                                },
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        Text(
                            text = "Win Prob @ 1.50x: ${currentBotDecision.winProbabilityPercent}% • Score: ${currentBotDecision.safeEntryScore}/100 • Streak <1.50x: ${currentBotDecision.consecutiveUnder150Count}",
                            color = SlateTextPrimary,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Action Badge
                Box(
                    modifier = Modifier
                        .background(
                            color = when (currentBotDecision.action) {
                                BotAction.ENTER_NOW -> EmeraldSafe
                                BotAction.ENTER_CAUTIOUS -> EmeraldSafe.copy(alpha = 0.8f)
                                BotAction.WAIT_AND_WATCH -> AmberMedium
                                BotAction.HIGH_RISK_AVOID -> RubyCrash
                            },
                            shape = RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 7.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = when (currentBotDecision.action) {
                            BotAction.ENTER_NOW -> "🟢 ENTER @ 1.50x"
                            BotAction.ENTER_CAUTIOUS -> "🟢 ENTER CAUTIOUS"
                            BotAction.WAIT_AND_WATCH -> "🟡 WAIT / STANDBY"
                            BotAction.HIGH_RISK_AVOID -> "🔴 SKIP ROUND"
                        },
                        color = Color.White,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }

        // =========================================================================
        // 3. BROWSER NAVIGATION BAR & ADDRESS BAR
        // =========================================================================
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            IconButton(
                onClick = {
                    if (webViewInstance?.canGoBack() == true) {
                        webViewInstance?.goBack()
                    }
                },
                modifier = Modifier.size(30.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = SlateTextPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }

            IconButton(
                onClick = {
                    if (webViewInstance?.canGoForward() == true) {
                        webViewInstance?.goForward()
                    }
                },
                modifier = Modifier.size(30.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Forward",
                    tint = SlateTextSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }

            IconButton(
                onClick = {
                    webViewInstance?.reload()
                },
                modifier = Modifier.size(30.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh",
                    tint = SlateTextPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }

            OutlinedTextField(
                value = urlText,
                onValueChange = { urlText = it },
                modifier = Modifier.weight(1f),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                keyboardActions = KeyboardActions(onGo = { navigateTo(urlText) }),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Secure HTTPS",
                        tint = EmeraldSafe,
                        modifier = Modifier.size(13.dp)
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { navigateTo(urlText) }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Go",
                            tint = CyberCyan,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyberCyan,
                    unfocusedBorderColor = SlateBorder,
                    focusedContainerColor = SlateCard,
                    unfocusedContainerColor = SlateCard,
                    focusedTextColor = SlateTextPrimary,
                    unfocusedTextColor = SlateTextPrimary
                ),
                shape = RoundedCornerShape(8.dp),
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 10.5.sp, fontFamily = FontFamily.Monospace)
            )

            IconButton(
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(urlText))
                    context.startActivity(intent)
                },
                modifier = Modifier.size(30.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.OpenInBrowser,
                    contentDescription = "Open externally",
                    tint = SlateTextSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        // Loading Progress Bar
        if (isBrowserLoading) {
            LinearProgressIndicator(
                progress = { browserLoadProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.5.dp),
                color = CyberCyan,
                trackColor = SlateSurfaceVariant
            )
        }

        // =========================================================================
        // 6. SANDBOXED WEBVIEW CONTAINER (IN-BUILT BROWSER ACTING AS LIVE FEEDER)
        // =========================================================================
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.verticalGradient(listOf(SlateBorder, CyberCyan.copy(alpha = 0.5f)))
            )
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = { ctx ->
                    val wv = browserManager.getOrCreateWebView(ctx)

                    // Ensure callbacks hook into current screen UI state as well
                    browserManager.onLiveFlightReceived = { mult ->
                        addInterceptedFlight(mult)
                        viewModel.onSiteFlightReceived(mult)
                        calculateNextRoundFigure(manualTrigger = false, triggeredByRoundFinish = true)
                    }
                    browserManager.onFlightsBatchReceived = { mults ->
                        mults.forEach { addInterceptedFlight(it) }
                        viewModel.onSiteFlightsBatchReceived(mults)
                        calculateNextRoundFigure(manualTrigger = false, triggeredByRoundFinish = true)
                    }
                    browserManager.onAuthStatusChanged = { loggedIn ->
                        isSiteLoggedIn = loggedIn
                        viewModel.updateSiteStatus(loggedIn, null)
                    }

                    // Bot Auto-Fill triggered on page load if bot is active
                    if (isBotActive) {
                        val phone = if (savedPhone.isNotBlank()) savedPhone else AccountVaultManager.DEFAULT_BOT_PHONE
                        val pass = if (savedPassword.isNotBlank()) savedPassword else AccountVaultManager.DEFAULT_BOT_PASS
                        injectBetikaAutoFill(
                            webView = wv,
                            phone = phone,
                            pass = pass,
                            autoSubmit = autoSubmitOnFill || isBotActive
                        )
                    }

                    webViewInstance = wv
                    wv
                },
                update = { webView ->
                    webViewInstance = webView
                }
            )
        }

        StatisticalDisclaimerBanner()
    }

    // =========================================================================
    // MODAL DIALOGS: EXPORT FLIGHT LOGS, BOT KNOWLEDGE, VAULT CREDENTIALS
    // =========================================================================
    if (showExportLogsDialog) {
        val currentLogs = if (interceptedLogs.isNotEmpty()) interceptedLogs.toList() else recentRounds.map {
            InterceptedFlightLog(
                id = "#${it.roundId}",
                multiplier = it.multiplier,
                formattedTime = it.formattedTime,
                isWin150 = it.multiplier >= 1.50
            )
        }

        AlertDialog(
            onDismissRequest = { showExportLogsDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(imageVector = Icons.Default.DataArray, contentDescription = null, tint = CyberCyan)
                    Text("Export Site Flight Logs", color = SlateTextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Choose format to copy ${currentLogs.size} intercepted site flight logs to clipboard:",
                        color = SlateTextSecondary,
                        fontSize = 11.5.sp
                    )

                    // Option 1: Formatted Table
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                copyFlightLogs("TABLE")
                                showExportLogsDialog = false
                            },
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = SlateSurfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("📋 Formatted Table with Timestamps & 1.50x Result", color = CyberCyan, fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                            Text("Best for sharing, spreadsheet paste, and statistical records.", color = SlateTextMuted, fontSize = 10.sp)
                        }
                    }

                    // Option 2: CSV / Multiplier List
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                copyFlightLogs("CSV")
                                showExportLogsDialog = false
                            },
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = SlateSurfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("📊 Multiplier List (e.g. 1.25x, 2.80x, 1.10x...)", color = EmeraldSafe, fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                            Text("Compact comma-separated string for algorithmic models.", color = SlateTextMuted, fontSize = 10.sp)
                        }
                    }

                    // Option 3: Raw JSON
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                copyFlightLogs("JSON")
                                showExportLogsDialog = false
                            },
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = SlateSurfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("💻 Raw JSON Array", color = PurpleModel, fontWeight = FontWeight.Bold, fontSize = 11.5.sp)
                            Text("Developer JSON payload with id, multiplier, and time.", color = SlateTextMuted, fontSize = 10.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showExportLogsDialog = false }) {
                    Text("Close", color = CyberCyan)
                }
            },
            containerColor = SlateCard,
            shape = RoundedCornerShape(14.dp)
        )
    }

    if (showBotDetailsDialog) {
        val currentBot = botDecisionState ?: BotAdvisorEngine.evaluateEntry(allRounds)
        AlertDialog(
            onDismissRequest = { showBotDetailsDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(imageVector = Icons.Default.SmartToy, contentDescription = null, tint = CyberCyan)
                    Text("Artificial Bot Knowledge & Strategy", color = SlateTextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "The Artificial Bot monitors live site telemetry specifically calibrated to a 1.50x safe cashout target.",
                        color = SlateTextSecondary,
                        fontSize = 11.5.sp
                    )

                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = SlateSurfaceVariant)
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(text = "CURRENT BOT SIGNAL: ${currentBot.reasonTitle}", color = CyberCyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text(text = currentBot.detailedKnowledgeAnalysis, color = SlateTextPrimary, fontSize = 11.sp, lineHeight = 15.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "• Target Odd: 1.50x\n• Win Probability: ${currentBot.winProbabilityPercent}%\n• Entry Score: ${currentBot.safeEntryScore}/100\n• Under 1.50x Streak: ${currentBot.consecutiveUnder150Count} round(s)",
                                color = SlateTextSecondary,
                                fontSize = 10.5.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Text(
                        text = "Auto-Log Bot Status: ${if (isBotActive) "ONLINE (0780531600)" else "OFFLINE"}",
                        color = if (isBotActive) EmeraldSafe else RubyCrash,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showBotDetailsDialog = false }) {
                    Text("Understood", color = CyberCyan)
                }
            },
            containerColor = SlateCard,
            shape = RoundedCornerShape(14.dp)
        )
    }

    if (showVaultDialog) {
        var phoneInput by remember { mutableStateOf(savedPhone.ifBlank { AccountVaultManager.DEFAULT_BOT_PHONE }) }
        var passwordInput by remember { mutableStateOf(savedPassword.ifBlank { AccountVaultManager.DEFAULT_BOT_PASS }) }
        var passwordVisible by remember { mutableStateOf(false) }
        var autoFillSwitch by remember { mutableStateOf(autoFillOnLoad) }
        var autoSubmitSwitch by remember { mutableStateOf(autoSubmitOnFill) }

        AlertDialog(
            onDismissRequest = { showVaultDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Key, contentDescription = null, tint = CyberCyan)
                    Text("Bot Credentials Vault", color = SlateTextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Your credentials for the artificial bot are configured to 0780531600. Saved securely on device.",
                        color = SlateTextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )

                    OutlinedTextField(
                        value = phoneInput,
                        onValueChange = { phoneInput = it },
                        label = { Text("Phone Number / Username", fontSize = 12.sp) },
                        placeholder = { Text("0780531600", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = SlateBorder,
                            focusedTextColor = SlateTextPrimary,
                            unfocusedTextColor = SlateTextPrimary,
                            focusedLabelColor = CyberCyan,
                            unfocusedLabelColor = SlateTextSecondary
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = { passwordInput = it },
                        label = { Text("Password", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                    tint = SlateTextSecondary
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = SlateBorder,
                            focusedTextColor = SlateTextPrimary,
                            unfocusedTextColor = SlateTextPrimary,
                            focusedLabelColor = CyberCyan,
                            unfocusedLabelColor = SlateTextSecondary
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Auto-fill on load", color = SlateTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("Fill fields when login page loads", color = SlateTextMuted, fontSize = 10.sp)
                        }
                        Switch(
                            checked = autoFillSwitch,
                            onCheckedChange = { autoFillSwitch = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = CyberCyan
                            )
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Auto-submit login", color = SlateTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("Click log in button automatically", color = SlateTextMuted, fontSize = 10.sp)
                        }
                        Switch(
                            checked = autoSubmitSwitch,
                            onCheckedChange = { autoSubmitSwitch = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = CyberCyan
                            )
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        vaultManager.saveCredentials(
                            phone = phoneInput.trim(),
                            pass = passwordInput,
                            autoFill = autoFillSwitch,
                            autoSubmit = autoSubmitSwitch
                        )

                        savedPhone = phoneInput.trim()
                        savedPassword = passwordInput
                        autoFillOnLoad = autoFillSwitch
                        autoSubmitOnFill = autoSubmitSwitch

                        showVaultDialog = false
                        Toast.makeText(context, "Bot credentials saved securely", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Save to Vault", color = SlateCard, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        vaultManager.clearCredentials()
                        savedPhone = AccountVaultManager.DEFAULT_BOT_PHONE
                        savedPassword = AccountVaultManager.DEFAULT_BOT_PASS
                        phoneInput = AccountVaultManager.DEFAULT_BOT_PHONE
                        passwordInput = AccountVaultManager.DEFAULT_BOT_PASS
                        showVaultDialog = false
                        Toast.makeText(context, "Reset to default credentials", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Reset Default", color = AmberMedium)
                }
            },
            containerColor = SlateCard,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

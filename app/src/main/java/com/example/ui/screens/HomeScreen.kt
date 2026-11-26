package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.AccountVaultManager
import com.example.data.local.PersistentBrowserManager
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.EmeraldSafe
import com.example.ui.theme.PurpleModel
import com.example.ui.theme.RubyCrash
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.SlateCard
import com.example.ui.theme.SlateSurfaceVariant
import com.example.ui.theme.SlateTextMuted
import com.example.ui.theme.SlateTextPrimary
import com.example.ui.theme.SlateTextSecondary
import com.example.ui.viewmodel.GameViewModel

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    viewModel: GameViewModel = viewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val vaultManager = remember { AccountVaultManager.getInstance(context) }
    val browserManager = remember { PersistentBrowserManager.getInstance(context) }

    val allRounds by viewModel.allRounds.collectAsStateWithLifecycle()
    val isAviatorDetected by browserManager.isAviatorDetected.collectAsStateWithLifecycle()
    val detectedSiteName by browserManager.detectedSiteName.collectAsStateWithLifecycle()
    val currentBrowserUrl by browserManager.currentUrl.collectAsStateWithLifecycle()
    val isBrowserLoading by browserManager.isLoading.collectAsStateWithLifecycle()
    val browserProgress by browserManager.loadProgress.collectAsStateWithLifecycle()

    var isBotActive by remember { mutableStateOf(vaultManager.isBotActive()) }
    var savedPhone by remember { mutableStateOf(vaultManager.getPhoneNumber()) }
    var savedPassword by remember { mutableStateOf(vaultManager.getPassword()) }
    var autoFillOnLoad by remember { mutableStateOf(vaultManager.isAutoFillEnabled()) }
    var autoSubmitOnFill by remember { mutableStateOf(vaultManager.isAutoSubmitEnabled()) }

    var showBotCredentialsDialog by remember { mutableStateOf(false) }
    var currentUrlInput by remember { mutableStateOf(currentBrowserUrl.ifBlank { "https://www.betika.com/en-ug/login" }) }

    // Sync input when URL updates
    androidx.compose.runtime.LaunchedEffect(currentBrowserUrl) {
        if (currentBrowserUrl.isNotBlank()) {
            currentUrlInput = currentBrowserUrl
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // =========================================================================
        // 1. MINIMIZED BOT BAR (ONLY BOT CONTROLS & CREDENTIALS)
        // =========================================================================
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
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
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(
                                if (isBotActive) CyberCyan.copy(alpha = 0.2f) else SlateSurfaceVariant,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SmartToy,
                            contentDescription = "AI Bot",
                            tint = if (isBotActive) CyberCyan else SlateTextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "BOT ASSISTANT",
                                color = SlateTextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (isBotActive) EmeraldSafe.copy(alpha = 0.2f) else RubyCrash.copy(alpha = 0.2f),
                                        RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = if (isBotActive) "ONLINE" else "OFFLINE",
                                    color = if (isBotActive) EmeraldSafe else RubyCrash,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Text(
                            text = if (savedPhone.isNotBlank()) "Target: $savedPhone" else "Bot Ready",
                            color = SlateTextMuted,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    OutlinedButton(
                        onClick = { showBotCredentialsDialog = true },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = SlateSurfaceVariant),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Key, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Bot Config", color = SlateTextPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }

                    Switch(
                        checked = isBotActive,
                        onCheckedChange = { active ->
                            isBotActive = active
                            vaultManager.setBotActive(active)
                            if (active) {
                                autoFillOnLoad = true
                                vaultManager.setAutoFillEnabled(true)
                                Toast.makeText(context, "Bot Active: Auto-log enabled", Toast.LENGTH_SHORT).show()
                            } else {
                                autoFillOnLoad = false
                                vaultManager.setAutoFillEnabled(false)
                                Toast.makeText(context, "Bot Offline", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = CyberCyan,
                            uncheckedThumbColor = SlateTextMuted,
                            uncheckedTrackColor = SlateSurfaceVariant
                        )
                    )
                }
            }
        }

        // =========================================================================
        // 2. INBUILT BROWSER SECTION ON HOME PAGE (EMBEDDED DIRECTLY)
        // =========================================================================
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = SlateCard),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = Brush.horizontalGradient(
                    listOf(
                        if (isAviatorDetected) EmeraldSafe else SlateBorder,
                        if (isAviatorDetected) CyberCyan else SlateBorder
                    )
                )
            )
        ) {
            val webView = remember { browserManager.getOrCreateWebView(context) }

            Column(modifier = Modifier.fillMaxSize()) {
                // Browser URL Bar & Controls
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SlateSurfaceVariant)
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = { if (webView.canGoBack()) webView.goBack() },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = SlateTextSecondary, modifier = Modifier.size(16.dp))
                    }

                    IconButton(
                        onClick = { if (webView.canGoForward()) webView.goForward() },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Forward", tint = SlateTextSecondary, modifier = Modifier.size(16.dp))
                    }

                    IconButton(
                        onClick = { webView.reload() },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Reload", tint = CyberCyan, modifier = Modifier.size(16.dp))
                    }

                    OutlinedTextField(
                        value = currentUrlInput,
                        onValueChange = { currentUrlInput = it },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        singleLine = true,
                        textStyle = TextStyle(fontSize = 11.sp, color = SlateTextPrimary),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
                        keyboardActions = KeyboardActions(onGo = {
                            var url = currentUrlInput.trim()
                            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                                url = "https://$url"
                            }
                            webView.loadUrl(url)
                        }),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = SlateBorder,
                            focusedContainerColor = SlateCard,
                            unfocusedContainerColor = SlateCard
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )

                    IconButton(
                        onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(currentBrowserUrl))
                                context.startActivity(intent)
                            } catch (_: Exception) {
                                Toast.makeText(context, "Could not open external browser", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.OpenInBrowser, contentDescription = "Open in external browser", tint = SlateTextSecondary, modifier = Modifier.size(16.dp))
                    }
                }

                // Quick Navigation Preset Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SlateSurfaceVariant.copy(alpha = 0.5f))
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val presets = listOf(
                        "Betika Login" to "https://www.betika.com/en-ug/login",
                        "Betika Aviator" to "https://www.betika.com/en-ug/aviator",
                        "Betpawa UG" to "https://www.betpawa.ug",
                        "Gal Sport" to "https://gsb.ug"
                    )

                    presets.forEach { (label, url) ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = SlateCard,
                            border = BorderStroke(0.6.dp, SlateBorder),
                            modifier = Modifier.clickable {
                                currentUrlInput = url
                                webView.loadUrl(url)
                            }
                        ) {
                            Text(
                                text = label,
                                color = CyberCyan,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                // Browser Progress Indicator
                if (isBrowserLoading) {
                    LinearProgressIndicator(
                        progress = { browserProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp),
                        color = CyberCyan,
                        trackColor = Color.Transparent
                    )
                }

                // WebView Container
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(Color.White)
                ) {
                    AndroidView(
                        factory = {
                            (webView.parent as? ViewGroup)?.removeView(webView)
                            webView
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        // =========================================================================
        // 3. COMPACT FOOTER STATUS
        // =========================================================================
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = SlateSurfaceVariant,
            border = BorderStroke(0.6.dp, SlateBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .background(if (isAviatorDetected) EmeraldSafe else SlateTextMuted, CircleShape)
                    )
                    Text(
                        text = if (isAviatorDetected) "Site: $detectedSiteName (Active)" else "Engine Ready",
                        color = SlateTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Text(
                    text = "${allRounds.size} Rounds Recorded",
                    color = CyberCyan,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    // =========================================================================
    // BOT CREDENTIALS CONFIGURATION DIALOG
    // =========================================================================
    if (showBotCredentialsDialog) {
        var phoneInput by remember { mutableStateOf(savedPhone.ifBlank { "" }) }
        var passwordInput by remember { mutableStateOf(savedPassword.ifBlank { "" }) }
        var passwordVisible by remember { mutableStateOf(false) }
        var autoFillSwitch by remember { mutableStateOf(autoFillOnLoad) }
        var autoSubmitSwitch by remember { mutableStateOf(autoSubmitOnFill) }

        AlertDialog(
            onDismissRequest = { showBotCredentialsDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Key, contentDescription = null, tint = CyberCyan)
                    Text("Bot Auto-Fill Config", color = SlateTextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Configured phone & password for the bot to automatically insert into site login forms.",
                        color = SlateTextSecondary,
                        fontSize = 11.sp
                    )

                    OutlinedTextField(
                        value = phoneInput,
                        onValueChange = { phoneInput = it },
                        label = { Text("Phone / Username", fontSize = 12.sp) },
                        placeholder = { Text("0780531600", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = SlateBorder,
                            focusedTextColor = SlateTextPrimary,
                            unfocusedTextColor = SlateTextPrimary
                        ),
                        shape = RoundedCornerShape(8.dp)
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
                                    contentDescription = null,
                                    tint = SlateTextSecondary
                                )
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = SlateBorder,
                            focusedTextColor = SlateTextPrimary,
                            unfocusedTextColor = SlateTextPrimary
                        ),
                        shape = RoundedCornerShape(8.dp)
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

                        showBotCredentialsDialog = false
                        Toast.makeText(context, "Bot configuration saved", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Save Config", color = SlateCard, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showBotCredentialsDialog = false }
                ) {
                    Text("Cancel", color = SlateTextSecondary)
                }
            },
            containerColor = SlateCard,
            shape = RoundedCornerShape(14.dp)
        )
    }
}

package com.example.data.local

import android.content.Context
import android.content.SharedPreferences

/**
 * Stores the user's optional betting-site autofill details.
 *
 * Credentials are deliberately never pre-populated: the user must enter and
 * explicitly save their own details on this device.
 */
class AccountVaultManager(context: Context) {
    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "betika_account_vault"
        private const val KEY_PHONE = "vault_phone_number"
        private const val KEY_PASSWORD = "vault_password"
        private const val KEY_AUTO_FILL_ENABLED = "vault_auto_fill_enabled"
        private const val KEY_AUTO_SUBMIT = "vault_auto_submit"
        private const val KEY_BOT_ACTIVE = "vault_bot_active"
        private const val KEY_PREFERRED_COUNTRY = "vault_preferred_country"

        @Volatile
        private var INSTANCE: AccountVaultManager? = null

        fun getInstance(context: Context): AccountVaultManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AccountVaultManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    fun saveCredentials(
        phone: String,
        pass: String,
        autoFill: Boolean = true,
        autoSubmit: Boolean = false
    ) {
        prefs.edit()
            .putString(KEY_PHONE, phone.trim())
            .putString(KEY_PASSWORD, pass)
            .putBoolean(KEY_AUTO_FILL_ENABLED, autoFill)
            .putBoolean(KEY_AUTO_SUBMIT, autoSubmit)
            .apply()
    }

    fun getPhoneNumber(): String = prefs.getString(KEY_PHONE, "").orEmpty()

    fun getPassword(): String = prefs.getString(KEY_PASSWORD, "").orEmpty()

    fun isAutoFillEnabled(): Boolean = prefs.getBoolean(KEY_AUTO_FILL_ENABLED, false)

    fun isAutoSubmitEnabled(): Boolean = prefs.getBoolean(KEY_AUTO_SUBMIT, false)

    fun isBotActive(): Boolean = prefs.getBoolean(KEY_BOT_ACTIVE, false)

    fun setBotActive(active: Boolean) {
        prefs.edit().putBoolean(KEY_BOT_ACTIVE, active).apply()
    }

    fun setAutoFillEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_FILL_ENABLED, enabled).apply()
    }

    fun setAutoSubmitEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_SUBMIT, enabled).apply()
    }

    fun hasCredentials(): Boolean =
        getPhoneNumber().isNotBlank() && getPassword().isNotBlank()

    fun clearCredentials() {
        prefs.edit()
            .remove(KEY_PHONE)
            .remove(KEY_PASSWORD)
            .remove(KEY_AUTO_FILL_ENABLED)
            .remove(KEY_AUTO_SUBMIT)
            .apply()
    }

    fun getPreferredCountryCode(): String =
        prefs.getString(KEY_PREFERRED_COUNTRY, "GLOBAL") ?: "GLOBAL"

    fun setPreferredCountryCode(code: String) {
        prefs.edit().putString(KEY_PREFERRED_COUNTRY, code).apply()
    }
}

package com.example.data.local

import android.content.Context
import android.content.SharedPreferences

class AccountVaultManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "betika_account_vault"
        private const val KEY_PHONE = "vault_phone_number"
        private const val KEY_PASSWORD = "vault_password"
        private const val KEY_AUTO_FILL_ENABLED = "vault_auto_fill_enabled"
        private const val KEY_AUTO_SUBMIT = "vault_auto_submit"
        private const val KEY_BOT_ACTIVE = "vault_bot_active"
        private const val KEY_PREFERRED_COUNTRY = "vault_preferred_country"

        const val DEFAULT_BOT_PHONE = "0780531600"
        const val DEFAULT_BOT_PASS = "Knowledge31"

        @Volatile
        private var INSTANCE: AccountVaultManager? = null

        fun getInstance(context: Context): AccountVaultManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AccountVaultManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    fun saveCredentials(phone: String, pass: String, autoFill: Boolean = true, autoSubmit: Boolean = false) {
        prefs.edit()
            .putString(KEY_PHONE, phone.trim())
            .putString(KEY_PASSWORD, pass)
            .putBoolean(KEY_AUTO_FILL_ENABLED, autoFill)
            .putBoolean(KEY_AUTO_SUBMIT, autoSubmit)
            .apply()
    }

    fun getPhoneNumber(): String {
        return prefs.getString(KEY_PHONE, DEFAULT_BOT_PHONE) ?: DEFAULT_BOT_PHONE
    }

    fun getPassword(): String {
        return prefs.getString(KEY_PASSWORD, DEFAULT_BOT_PASS) ?: DEFAULT_BOT_PASS
    }

    fun isAutoFillEnabled(): Boolean {
        return prefs.getBoolean(KEY_AUTO_FILL_ENABLED, false)
    }

    fun isAutoSubmitEnabled(): Boolean {
        return prefs.getBoolean(KEY_AUTO_SUBMIT, false)
    }

    fun isBotActive(): Boolean {
        return prefs.getBoolean(KEY_BOT_ACTIVE, false)
    }

    fun setBotActive(active: Boolean) {
        prefs.edit().putBoolean(KEY_BOT_ACTIVE, active).apply()
    }

    fun setAutoFillEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_FILL_ENABLED, enabled).apply()
    }

    fun setAutoSubmitEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_SUBMIT, enabled).apply()
    }

    fun hasCredentials(): Boolean {
        return getPhoneNumber().isNotBlank() && getPassword().isNotBlank()
    }

    fun clearCredentials() {
        prefs.edit()
            .remove(KEY_PHONE)
            .remove(KEY_PASSWORD)
            .remove(KEY_AUTO_FILL_ENABLED)
            .remove(KEY_AUTO_SUBMIT)
            .apply()
    }

    fun getPreferredCountryCode(): String {
        return prefs.getString(KEY_PREFERRED_COUNTRY, "GLOBAL") ?: "GLOBAL"
    }

    fun setPreferredCountryCode(code: String) {
        prefs.edit().putString(KEY_PREFERRED_COUNTRY, code).apply()
    }
}

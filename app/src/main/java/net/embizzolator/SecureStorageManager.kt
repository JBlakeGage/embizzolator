package net.embizzolator

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey // FIX: Replaced MasterKeys with MasterKey

// Data class for all the settings together
data class AppSettings(
    val apiUrl: String,
    val apiKey: String,
    val modelName: String
)

object SecureStorageManager {

    private const val PREFERENCE_FILE_NAME = "embizzolator_secure_prefs"
    private const val KEY_API_URL = "api_url"
    private const val KEY_API_KEY = "api_key"
    private const val KEY_MODEL_NAME = "model_name"
    private const val KEY_API_PASSWORD = "api_password"

    private fun getEncryptedSharedPreferences(context: Context): EncryptedSharedPreferences {
        // FIX: Replaced deprecated MasterKeys with the current MasterKey.Builder
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return EncryptedSharedPreferences.create(
            context,
            PREFERENCE_FILE_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        ) as EncryptedSharedPreferences
    }

    fun saveSettings(context: Context, settings: AppSettings) {
        val prefs = getEncryptedSharedPreferences(context)
        with(prefs.edit()) {
            putString(KEY_API_URL, settings.apiUrl)
            putString(KEY_API_KEY, settings.apiKey)
            putString(KEY_MODEL_NAME, settings.modelName)
            apply()
        }
    }

    fun getSettings(context: Context): AppSettings? {
        val prefs = getEncryptedSharedPreferences(context)
        val apiUrl = prefs.getString(KEY_API_URL, null)
        val apiKey = prefs.getString(KEY_API_KEY, null)
        val modelName = prefs.getString(KEY_MODEL_NAME, null)

        return if (apiUrl != null && apiKey != null && modelName != null) {
            AppSettings(apiUrl, apiKey, modelName)
        } else {
            null
        }
    }

    fun savePassword(context: Context, password: String) {
        getEncryptedSharedPreferences(context).edit().putString(KEY_API_PASSWORD, password).apply()
    }

    fun getPassword(context: Context): String? {
        return getEncryptedSharedPreferences(context).getString(KEY_API_PASSWORD, null)
    }

    fun isPasswordSet(context: Context): Boolean {
        return getPassword(context) != null
    }
}
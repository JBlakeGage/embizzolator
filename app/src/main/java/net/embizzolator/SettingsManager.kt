package net.embizzolator

import android.content.Context

// Data class for all non-sensitive preferences
data class AppPreferences(
    val jargonDensity: Float = 0.5f,
    val urgencyMeter: Float = 0.5f,
    val verbosity: Float = 0.5f,
    val corporateStyle: String = "Business Executive",
    val brandingTheme: String = "General Business"
)

object SettingsManager {
    private const val PREF_NAME = "embizzolator_settings"
    private const val KEY_JARGON = "jargon_density"
    private const val KEY_URGENCY = "urgency_meter"
    private const val KEY_VERBOSITY = "verbosity"
    private const val KEY_STYLE = "corporate_style"
    private const val KEY_THEME = "branding_theme"

    private fun getSharedPreferences(context: Context) =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun savePreferences(context: Context, prefs: AppPreferences) {
        with(getSharedPreferences(context).edit()) {
            putFloat(KEY_JARGON, prefs.jargonDensity)
            putFloat(KEY_URGENCY, prefs.urgencyMeter)
            putFloat(KEY_VERBOSITY, prefs.verbosity)
            putString(KEY_STYLE, prefs.corporateStyle)
            putString(KEY_THEME, prefs.brandingTheme)
            apply()
        }
    }

    fun getPreferences(context: Context): AppPreferences {
        val prefs = getSharedPreferences(context)
        return AppPreferences(
            jargonDensity = prefs.getFloat(KEY_JARGON, 0.5f),
            urgencyMeter = prefs.getFloat(KEY_URGENCY, 0.5f),
            verbosity = prefs.getFloat(KEY_VERBOSITY, 0.5f),
            corporateStyle = prefs.getString(KEY_STYLE, "Business Executive") ?: "Business Executive",
            brandingTheme = prefs.getString(KEY_THEME, "General Business") ?: "General Business"
        )
    }
}
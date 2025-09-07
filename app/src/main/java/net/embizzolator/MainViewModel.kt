package net.embizzolator

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Represents the state of our UI, now including preferences
data class AppUiState(
    val areSettingsConfigured: Boolean = false,
    val settings: AppSettings? = null,
    val preferences: AppPreferences = AppPreferences()
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    init {
        checkSettings()
    }

    fun checkSettings() {
        viewModelScope.launch {
            val savedSettings = SecureStorageManager.getSettings(getApplication())
            val savedPrefs = SettingsManager.getPreferences(getApplication())
            _uiState.update {
                it.copy(
                    areSettingsConfigured = savedSettings != null && savedSettings.apiKey.isNotBlank(),
                    settings = savedSettings,
                    preferences = savedPrefs
                )
            }
        }
    }
}
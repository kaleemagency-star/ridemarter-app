package com.ridemarter.app.viewmodel

import android.app.Application
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ridemarter.app.dataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

enum class ThemeMode {
    DARK, LIGHT, SYSTEM
}

class ThemeViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private val THEME_KEY = stringPreferencesKey("theme_mode")
    }

    private val _themeMode = MutableStateFlow(ThemeMode.DARK)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    init {
        loadThemeFromDataStore()
    }

    private fun loadThemeFromDataStore() {
        viewModelScope.launch {
            try {
                val prefs = getApplication<Application>()
                    .dataStore.data.first()
                val savedTheme = prefs[THEME_KEY] ?: ThemeMode.DARK.name
                _themeMode.value = ThemeMode.valueOf(savedTheme)
            } catch (e: Exception) {
                Log.w("ThemeViewModel", "Error loading theme: ${e.message}")
                _themeMode.value = ThemeMode.DARK
            }
        }
    }

    fun setTheme(mode: ThemeMode) {
        _themeMode.value = mode
        viewModelScope.launch {
            try {
                getApplication<Application>().dataStore.edit { prefs ->
                    prefs[THEME_KEY] = mode.name
                }
            } catch (e: Exception) {
                Log.w("ThemeViewModel", "Error saving theme: ${e.message}")
            }
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        setTheme(mode)
    }

    fun toggleTheme() {
        val next = when (_themeMode.value) {
            ThemeMode.DARK -> ThemeMode.LIGHT
            ThemeMode.LIGHT -> ThemeMode.SYSTEM
            ThemeMode.SYSTEM -> ThemeMode.DARK
        }
        setTheme(next)
    }
}

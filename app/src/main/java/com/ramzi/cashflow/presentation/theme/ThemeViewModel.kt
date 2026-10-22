package com.ramzi.cashflow.presentation.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ramzi.cashflow.data.local.AccentPreference
import com.ramzi.cashflow.data.local.ThemePreference
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ThemeViewModel(
    private val themePreference: ThemePreference,
    private val accentPreference: AccentPreference
) : ViewModel() {

    private val _themeMode = MutableStateFlow(ThemePreference.MODE_SYSTEM)
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    private val _accentId = MutableStateFlow(AccentPreference.DEFAULT)
    val accentId: StateFlow<String> = _accentId.asStateFlow()

    init {
        viewModelScope.launch {
            themePreference.themeMode.collect { _themeMode.value = it }
        }
        viewModelScope.launch {
            accentPreference.accentId.collect { _accentId.value = it }
        }
    }

    fun toggleLightDark(isSystemInDark: Boolean) {
        val current = _themeMode.value
        val currentlyDark = when (current) {
            ThemePreference.MODE_DARK -> true
            ThemePreference.MODE_LIGHT -> false
            else -> isSystemInDark
        }
        val next = if (currentlyDark) ThemePreference.MODE_LIGHT else ThemePreference.MODE_DARK
        viewModelScope.launch { themePreference.setThemeMode(next) }
    }
}
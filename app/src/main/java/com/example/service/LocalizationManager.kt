package com.example.service

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.LayoutDirection

enum class AppLanguage(val code: String, val displayName: String, val layoutDirection: LayoutDirection) {
    ARABIC("ar", "العربية", LayoutDirection.Rtl),
    ENGLISH("en", "English", LayoutDirection.Ltr)
}

object LocalizationManager {
    var currentLanguage by mutableStateOf(AppLanguage.ARABIC)

    fun toggleLanguage() {
        currentLanguage = if (currentLanguage == AppLanguage.ARABIC) AppLanguage.ENGLISH else AppLanguage.ARABIC
    }

    // Localized string resolver
    fun str(ar: String, en: String): String {
        return if (currentLanguage == AppLanguage.ARABIC) ar else en
    }
}

val LocalAppLanguage = compositionLocalOf { LocalizationManager.currentLanguage }

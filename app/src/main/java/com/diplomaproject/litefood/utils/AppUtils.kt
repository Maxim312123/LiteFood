package com.diplomaproject.litefood.utils

import android.content.Context
import androidx.preference.PreferenceManager
import java.util.Locale

object AppUtils {

    private lateinit var appContext: Context

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    fun getCurrentUserLanguage(): String {
        val defaultUserLanguage = getUserLanguage()
        val prefs = PreferenceManager.getDefaultSharedPreferences(appContext)
        val selectedUserLanguage = prefs.getString("list", "ru")

        return selectedUserLanguage?.takeIf { it.isNotEmpty() } ?: defaultUserLanguage
    }

    private fun getUserLanguage(): String {
        return Locale.getDefault().language
    }
}
package com.diplomaproject.litefood

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import androidx.compose.ui.text.intl.Locale
import androidx.preference.PreferenceManager
import com.diplomaproject.litefood.utils.AppUtils
import com.google.firebase.FirebaseApp
import com.google.firebase.database.FirebaseDatabase

class MyApplication : Application() {


    override fun onTerminate() {
        super.onTerminate()
    }

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(applicationContext)
        FirebaseApp.initializeApp(this)
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
        AppUtils.init(this)
    }

    companion object {
        @JvmStatic
        fun isNetworkAvailable(context: Context): Boolean {
            val connectivityManager =
                context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            return activeNetworkInfo != null && activeNetworkInfo.isConnected
        }
    }
}

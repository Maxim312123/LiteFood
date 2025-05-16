package com.diplomaproject.litefood

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import com.diplomaproject.litefood.activities.MainActivity


class SplashScreen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(
            this
        )
        val theme = sharedPreferences.getString("theme", "Светлая")
        if (theme == "Светлая") {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        } else if (theme == "Темная") {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }

        if (theme == "Темная") {
            setTheme(R.style.SplashScreenDark)
        } else {
            setTheme(R.style.SplashScreenLight)
        }

        super.onCreate(savedInstanceState)




        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()


    }


}
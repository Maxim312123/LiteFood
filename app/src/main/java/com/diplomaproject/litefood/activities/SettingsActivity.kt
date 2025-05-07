package com.diplomaproject.litefood.activities

import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.diplomaproject.litefood.R
import com.diplomaproject.litefood.databinding.SettingsActivityBinding
import com.diplomaproject.litefood.managers.FirebaseRealtimeDatabaseRepository
import com.google.android.material.appbar.MaterialToolbar
import java.util.Locale

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: SettingsActivityBinding
    private lateinit var toolbar: MaterialToolbar

    private val firebaseRealtimeDatabaseManager = FirebaseRealtimeDatabaseRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SettingsActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
        toolbar = binding.toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return false
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            val languagePreference = findPreference<ListPreference>("list")

            languagePreference?.setOnPreferenceChangeListener(object :
                Preference.OnPreferenceChangeListener {
                override fun onPreferenceChange(
                    preference: Preference,
                    newLanguage: Any?
                ): Boolean {
                    val languageCode = newLanguage.toString();
                    setLocale(languageCode)

//                    lifecycleScope.launch {
//                        updateUIWithNewLanguage(languageCode)
//                    }
                    return true
                }

//                private suspend fun updateUIWithNewLanguage(languageCode: String) {
//                    val firebaseRealtimeDatabaseManager = FirebaseRealtimeDatabaseManager()
//                    firebaseRealtimeDatabaseManager.translateShoppingBasketProductsNames(viewLifecycleOwner,languageCode)
//                }

            })

            val themePreference = findPreference<ListPreference>("theme")

            if (themePreference != null) {
                themePreference.setOnPreferenceChangeListener(object :
                    Preference.OnPreferenceChangeListener {
                    override fun onPreferenceChange(
                        preference: Preference,
                        newTheme: Any?
                    ): Boolean {
                        if (newTheme != null) {
                            if (newTheme.equals("Светлая")) {
                                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                            } else if (newTheme.equals("Темная")) {
                                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                            }
                        }
                        return true
                    }

                })
            }
        }

        private fun setLocale(languageCode: String) {
            val locale = Locale(languageCode)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Locale.setDefault(locale)
                val config = Configuration()
                config.setLocale(locale)
                requireContext().resources.updateConfiguration(
                    config,
                    requireContext().resources.displayMetrics
                )
            } else {
                val config = requireActivity().resources.configuration
                config.locale = locale
                requireActivity().resources.updateConfiguration(
                    config,
                    requireActivity().resources.displayMetrics
                )
            }
            requireActivity().recreate()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }


}
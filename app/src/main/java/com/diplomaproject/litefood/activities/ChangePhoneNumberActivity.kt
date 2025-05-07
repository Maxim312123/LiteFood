package com.diplomaproject.litefood.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.diplomaproject.litefood.R
import com.diplomaproject.litefood.databinding.ActivityChangePhoneNumberBinding
import com.diplomaproject.litefood.fragments.ChangePhoneNumberFragment


class ChangePhoneNumberActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChangePhoneNumberBinding
    private lateinit var toolbar: Toolbar
    private val fragmentManager = supportFragmentManager

    @SuppressLint("ClickableViewAccessibility", "ResourceAsColor")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangePhoneNumberBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
        setUpToolbar()
        setChangePhoneNumberFragment()
    }

    private fun init() {
        toolbar = binding.toolbar
    }

    private fun setUpToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
    }

    private fun setChangePhoneNumberFragment() {
        val changePhoneNumberFragment = ChangePhoneNumberFragment()
        fragmentManager.beginTransaction()
            .replace(
                R.id.fragment_container_change_phone_number,
                changePhoneNumberFragment
            ).commit()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

//    override fun onBackPressed() {
//        finish()
//    }

    private fun getCurrentFragment(): Fragment? {
        return fragmentManager.findFragmentById(R.id.fragment_container_change_phone_number)
    }
}
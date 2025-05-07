package com.diplomaproject.litefood.activities

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.diplomaproject.litefood.R
import com.diplomaproject.litefood.databinding.ActivityPaymentMethodsBinding
import com.diplomaproject.litefood.fragments.PaymentMethodsFragment

class PaymentMethodsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPaymentMethodsBinding
    private lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPaymentMethodsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
        setupToolbar()
        supportFragmentManager.beginTransaction()
            .add(R.id.main_fragment_container, PaymentMethodsFragment.newInstance())
            .commit()
    }

    private fun init() {
        toolbar = binding.paymentMethodsToolbar
    }

    private fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(false)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            true
        }
        return super.onOptionsItemSelected(item)
    }


}
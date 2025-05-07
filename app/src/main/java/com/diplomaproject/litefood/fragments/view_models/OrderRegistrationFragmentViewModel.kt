package com.diplomaproject.litefood.fragments.view_models

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.diplomaproject.litefood.data.Address
import com.diplomaproject.litefood.data.CreditCard
import com.diplomaproject.litefood.managers.FirebaseRealtimeDatabaseRepository

class OrderRegistrationFragmentViewModel(application: Application) : AndroidViewModel(application) {

    private val firebaseRealtimeRepository =
        FirebaseRealtimeDatabaseRepository.getInstance()

    private val _mainAddress = MutableLiveData<Address?>()
    val mainAddress: LiveData<Address?> get() = _mainAddress

    private val _mainPaymentMethod = MutableLiveData<CreditCard?>()
    val mainPaymentMethod: LiveData<CreditCard?> get() = _mainPaymentMethod

    private val _navigateToAddressFragment = MutableLiveData<Boolean>()
    val navigateToAddressFragment: LiveData<Boolean> get() = _navigateToAddressFragment


    fun fetchMainUserAddress() {
        firebaseRealtimeRepository.fetchMainUserAddress { mainAddress ->
            _mainAddress.value = mainAddress
        }
    }

    fun navigateToAddressFragment() {
        _navigateToAddressFragment.value = true
    }

    fun onNavigatedToAddressFragment() {
        _navigateToAddressFragment.value = false
    }

    fun fetchUserMainPaymentMethod() {
        firebaseRealtimeRepository.fetchMainUserPaymentMethod { creditCard ->
            _mainPaymentMethod.value = creditCard
        }
    }


}
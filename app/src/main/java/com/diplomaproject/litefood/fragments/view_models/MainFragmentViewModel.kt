package com.diplomaproject.litefood.fragments.view_models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.diplomaproject.litefood.FirebaseService
import com.diplomaproject.litefood.data.FoodSection
import com.diplomaproject.litefood.data.HitSalesProduct
import com.diplomaproject.litefood.data.Product

class MainFragmentViewModel : ViewModel() {

    private val firestoreRepository = FirebaseService.firestoreDatabaseRepository

    private val _foodSections = MutableLiveData<MutableList<FoodSection>>()
    val foodSections: LiveData<MutableList<FoodSection>> get() = _foodSections

    private val _hitSalesProducts = MutableLiveData<MutableList<HitSalesProduct>>()
    val hitSalesProducts: LiveData<MutableList<HitSalesProduct>> get() = _hitSalesProducts

    private val _selectedFoodSectionPosition = MutableLiveData<Int>(-1)
    val selectedFoodSectionPosition: LiveData<Int> get() = _selectedFoodSectionPosition

    private val _isNavigatedToFoodSectionProducts = MutableLiveData<Boolean>()
    val isNavigatedToFoodSectionProducts: LiveData<Boolean> get() = _isNavigatedToFoodSectionProducts

    suspend fun fetchFoodSections() {
        val foodSections = firestoreRepository.fetchFoodSections()
        _foodSections.value = foodSections
    }

    suspend fun fetchHitSalesProducts() {
        val hitSalesProducts = firestoreRepository.fetchHitSalesProducts()
        _hitSalesProducts.value = hitSalesProducts
    }

    fun onFoodSectionClick(position: Int) {
        _selectedFoodSectionPosition.value = position
    }


    suspend fun navigateToFoodSectionProducts(sectionName: String, onResult: (MutableList<Product>) -> Unit) {
         val products= firestoreRepository.fetchFoodSectionProducts(sectionName)
        _isNavigatedToFoodSectionProducts.value = true
    }

    fun navigatedToFoodSectionProducts() {
        _isNavigatedToFoodSectionProducts.value = false
    }

}
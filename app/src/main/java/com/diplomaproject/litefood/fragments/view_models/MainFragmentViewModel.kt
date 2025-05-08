package com.diplomaproject.litefood.fragments.view_models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.diplomaproject.litefood.FirebaseService
import com.diplomaproject.litefood.data.CarouselProduct
import com.diplomaproject.litefood.data.FoodSection
import com.diplomaproject.litefood.data.Product
import com.google.firebase.firestore.DocumentReference

class MainFragmentViewModel : ViewModel() {

    private val firestoreRepository = FirebaseService.firestoreDatabaseRepository

    private val _foodSections = MutableLiveData<MutableList<FoodSection>>()
    val foodSections: LiveData<MutableList<FoodSection>> get() = _foodSections

    private val _salesLeaderProducts = MutableLiveData<MutableList<out CarouselProduct>>()
    val salesLeaderProducts: LiveData<MutableList<out CarouselProduct>> get() = _salesLeaderProducts

    private val _vegetarianProducts = MutableLiveData<MutableList<out CarouselProduct>>()
    val vegetarianProducts: LiveData<MutableList<out CarouselProduct>> get() = _vegetarianProducts

    private val _selectedFoodSectionPosition = MutableLiveData<Int>(-1)
    val selectedFoodSectionPosition: LiveData<Int> get() = _selectedFoodSectionPosition

    private val _isNavigatedToFoodSectionProducts = MutableLiveData<Boolean>()
    val isNavigatedToFoodSectionProducts: LiveData<Boolean> get() = _isNavigatedToFoodSectionProducts

    private val _selectedHitSalesProductPosition = MutableLiveData<Int>(-1)
    val selectedHitSalesProductPosition: LiveData<Int> get() = _selectedHitSalesProductPosition

    private val _hitSalesProduct = MutableLiveData<Product?>(null)
    val hitSalesProduct: LiveData<Product?> get() = _hitSalesProduct

    suspend fun fetchFoodSections() {
        val foodSections = firestoreRepository.fetchFoodSections()
        _foodSections.value = foodSections
    }

    suspend fun fetchSalesLeaderProducts() {
        val salesLeaderProducts = firestoreRepository.fetchSalesLeaderProducts()
        _salesLeaderProducts.value = salesLeaderProducts
    }

    suspend fun fetchVegetarianProducts(){
        val vegetarianProducts = firestoreRepository.fetchVegetarianProducts()
        _vegetarianProducts.value = vegetarianProducts
    }

    fun onFoodSectionClicked(flag: Int) {
        _selectedFoodSectionPosition.value = flag
    }


    suspend fun navigateToFoodSectionProducts(
        sectionName: String,
        onResult: (MutableList<Product>) -> Unit
    ) {
        val products = firestoreRepository.fetchFoodSectionProducts(sectionName)
        _isNavigatedToFoodSectionProducts.value = true
    }

    fun navigatedToFoodSectionProducts() {
        _isNavigatedToFoodSectionProducts.value = false
    }

    fun onHitSalesProductCLick(position: Int) {
        _selectedHitSalesProductPosition.value = position
    }

    fun onHitSalesProductClicked(flag: Int) {
        _selectedHitSalesProductPosition.value = flag
    }

    suspend fun fetchHitSalesProductData(productRef: DocumentReference) {
        val product = firestoreRepository.fetchHitSalesProductData(productRef)
        _hitSalesProduct.value = product
    }

    fun onFetchedHitSalesProductData() {
        _hitSalesProduct.value = null
    }

}
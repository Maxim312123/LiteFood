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

    private val _spicyProducts = MutableLiveData<MutableList<out CarouselProduct>>()
    val spicyProducts: LiveData<MutableList<out CarouselProduct>> get() = _spicyProducts

    private val _selectedFoodSectionPosition = MutableLiveData(-1)
    val selectedFoodSectionPosition: LiveData<Int> get() = _selectedFoodSectionPosition

    private val _clickedSalesLeaderProductPosition = MutableLiveData(-1)
    val clickedSalesLeaderProductPosition: LiveData<Int> get() = _clickedSalesLeaderProductPosition

    private val _clickedVegetarianProductPosition = MutableLiveData(-1)
    val clickedVegetarianProductPosition: LiveData<Int> get() = _clickedVegetarianProductPosition

    private val _clickedSpicyProductPosition = MutableLiveData(-1)
    val clickedSpicyProductPosition: LiveData<Int> get() = _clickedSpicyProductPosition

    private val _carouselProduct = MutableLiveData<Product?>(null)
    val carouselProduct: LiveData<Product?> get() = _carouselProduct

    suspend fun fetchFoodSections() {
        val foodSections = firestoreRepository.fetchFoodSections()
        _foodSections.value = foodSections
    }

    suspend fun fetchSalesLeaderCarouselProducts() {
        val salesLeaderProducts = firestoreRepository.fetchSalesLeaderCarouselProducts()
        _salesLeaderProducts.value = salesLeaderProducts
    }

    suspend fun fetchVegetarianCarouselProducts() {
        val vegetarianProducts = firestoreRepository.fetchVegetarianCarouselProducts()
        _vegetarianProducts.value = vegetarianProducts
    }

    suspend fun fetchSpicyCarouselProducts() {
        val spicyProducts = firestoreRepository.fetchSpicyCarouselProducts()
        _spicyProducts.value = spicyProducts
    }

    fun onFoodSectionClicked(flag: Int) {
        _selectedFoodSectionPosition.value = flag
    }

    fun onSalesLeaderProductCLick(position: Int) {
        _clickedSalesLeaderProductPosition.value = position
    }

    fun onSalesLeaderProductClicked(flag: Int) {
        _clickedSalesLeaderProductPosition.value = flag
    }

    fun onVegetarianProductCLick(position: Int) {
        _clickedVegetarianProductPosition.value = position
    }

    fun onVegetarianProductClicked(flag: Int) {
        _clickedVegetarianProductPosition.value = flag
    }

    fun onSpicyProductCLick(position: Int) {
        _clickedSpicyProductPosition.value = position
    }

    fun onSpicyProductClicked(flag: Int) {
        _clickedSpicyProductPosition.value = flag
    }

    suspend fun fetchCarouselProductData(productRef: DocumentReference) {
        val product = firestoreRepository.fetchCarouselProductData(productRef)
        _carouselProduct.value = product
    }

    fun onFetchedCarouselProductData() {
        _carouselProduct.value = null
    }


}
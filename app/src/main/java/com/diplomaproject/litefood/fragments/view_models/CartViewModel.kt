package com.diplomaproject.litefood.fragments.view_models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.diplomaproject.litefood.data.CartProduct
import com.diplomaproject.litefood.data.User
import com.diplomaproject.litefood.repository.FirebaseRealtimeDatabaseRepository

class CartViewModel : ViewModel() {

    private var _productAmount: MutableLiveData<Int> = MutableLiveData(0)
    val productAmount: LiveData<Int> = _productAmount

    private var _productsForBuying = MutableLiveData<HashSet<CartProduct>>(hashSetOf())
    val productsForBuying: LiveData<HashSet<CartProduct>> = _productsForBuying

    private val firebaseRealtimeDatabaseRepository = FirebaseRealtimeDatabaseRepository()

    private val _cartProducts = MutableLiveData<MutableList<CartProduct>>()
    val cartProducts: LiveData<MutableList<CartProduct>> get() = _cartProducts

    fun fetchProducts(onResult: (MutableList<CartProduct>) -> Unit) {
        firebaseRealtimeDatabaseRepository.fetchCartProducts { cartProducts ->
            _cartProducts.value = cartProducts
            onResult(cartProducts)
        }
    }

    fun deleteProduct(productId: String) {
        firebaseRealtimeDatabaseRepository.deleteProduct(productId) { success ->
            if (success) {
                _cartProducts.value =
                    _cartProducts.value?.filter { it.id != productId } as MutableList<CartProduct>?
            }
        }
    }

    fun changeProductAmount(productId: String, newAmount: Int) {
        firebaseRealtimeDatabaseRepository.changeProductAmount(productId, newAmount) { success ->
            if (success) {
                val productIndex = _cartProducts.value?.indexOfFirst { it.id == productId }

                val product = _cartProducts.value?.find { it.id == productId }
                product?.amount = newAmount

                if (productIndex != null) {
                    _cartProducts.value?.removeAt(productIndex)
                }
                if (product != null) {
                    _cartProducts.value?.add(product)
                }
            }
        }
    }

    fun setProductAmount(amount: Int) {
        _productAmount.value = amount
    }

    fun addProductForBuying(product: CartProduct) {
        val products = _productsForBuying.value ?: hashSetOf()
        products.add(product)
        _productsForBuying.value = products
    }

    fun removeProductFromForBuying(product: CartProduct) {
        val products = _productsForBuying.value ?: hashSetOf()
        products.remove(product)
        _productsForBuying.value = products
    }

    fun updateProductForBuyingAmount(productId: String, newAmount: Int) {
        val currentProducts = _productsForBuying.value ?: hashSetOf()
        val productToUpdate = currentProducts.find { it.id == productId }

        if (productToUpdate != null) {
            currentProducts.remove(productToUpdate)

            productToUpdate.amount = newAmount

            currentProducts.add(productToUpdate)

            _productsForBuying.value = HashSet(currentProducts)
        }
    }

    fun isProductsForBuyingEmpty(): Boolean? {
        return _productsForBuying.value?.isEmpty()
    }

    fun updateFirebaseRealtimeDatabase(user: User){

    }

}
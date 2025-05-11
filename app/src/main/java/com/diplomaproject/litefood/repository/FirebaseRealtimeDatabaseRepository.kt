package com.diplomaproject.litefood.repository

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.diplomaproject.litefood.FirebaseAuthService
import com.diplomaproject.litefood.data.Address
import com.diplomaproject.litefood.data.BaseProduct
import com.diplomaproject.litefood.data.CartProduct
import com.diplomaproject.litefood.data.CreditCard
import com.diplomaproject.litefood.data.FavoriteProductMainFragment
import com.diplomaproject.litefood.data.Product
import com.diplomaproject.litefood.data.User
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CompletableDeferred

private const val TAG = "FirebaseRealTimeDataBaseRepository"

class FirebaseRealtimeDatabaseRepository {


    var databaseReference: DatabaseReference
        get() = field
    val userUid: String = FirebaseAuthService().getCurrentUserUid()
    var userProfileNodeRef: DatabaseReference
    val userShoppingBasketNodeRef: DatabaseReference
    val userFavoriteProductsNodeRef: DatabaseReference

    init {
        databaseReference = FirebaseDatabase.getInstance().reference
        userProfileNodeRef = databaseReference.child("Users/$userUid")
        userShoppingBasketNodeRef = userProfileNodeRef.child("basket")
        userFavoriteProductsNodeRef = userProfileNodeRef.child("favoriteProducts")
    }

    companion object {
        @Volatile
        private var instance: FirebaseRealtimeDatabaseRepository? = null
        fun getInstance(): FirebaseRealtimeDatabaseRepository {
            return instance ?: synchronized(this) {
                instance ?: FirebaseRealtimeDatabaseRepository().also { instance = it }
            }
        }
    }

    interface ShoppingBasketDataCallback {
        fun onDataRetrieved(retrievedProducts: MutableList<CartProduct>)
    }

    interface FavoriteProductsDataCallback {
        fun onDataRetrieved(retrievedProducts: MutableList<Product>)
    }


    fun writeToCart(product: CartProduct) {
        val basketNodeRef = userProfileNodeRef.child("basket").child(product.id)
        basketNodeRef.setValue(product)
    }

    fun deleteProductFromCart(product: BaseProduct) {
        userShoppingBasketNodeRef.child(product.id).removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("Firebase Realtime Database", "Узел успешно удален.")
            } else {
                Log.e(
                    "Firebase Realtime Database",
                    "Ошибка при удалении узла: ${task.exception?.message}"
                )
            }
        }
    }

    fun deleteProduct(productId: String, onComplete: (Boolean) -> Unit) {
        userShoppingBasketNodeRef.child(productId).removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                onComplete(true)
                Log.d("Firebase Realtime Database", "Узел успешно удален.")
            } else {
                onComplete(false)
                Log.e(
                    "Firebase Realtime Database",
                    "Ошибка при удалении узла: ${task.exception?.message}"
                )
            }
        }
    }

    fun writeToFavoriteProducts(product: Product) {
        userFavoriteProductsNodeRef.child(product.id).setValue(product)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("Firebase Realtime Database", "Узел успешно добавлен.")
                } else {
                    Log.e(
                        "Firebase Realtime Database",
                        "Ошибка при записи узла: ${task.exception?.message}"
                    )
                }
            }
    }

    //Deprecated
    fun retrieveProductsFromFavorite(callback: FavoriteProductsDataCallback) {
        userFavoriteProductsNodeRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val retrievedProducts: MutableList<Product> = mutableListOf()
                if (dataSnapshot.exists()) {
                    for (itemSnapshot in dataSnapshot.children) {
                        val shoppingBasketProduct = itemSnapshot.getValue(Product::class.java)
                        shoppingBasketProduct?.let { retrievedProducts.add(shoppingBasketProduct) }
                    }
                }
                callback.onDataRetrieved(retrievedProducts)
            }

            override fun onCancelled(p0: DatabaseError) {
                Log.d("Firebase", "cannot read products from favorite products")
            }
        })
    }

    //New
    fun retrieveProductsFromFavorite(): CompletableDeferred<MutableList<Product>> {
        val deferredFavoriteProducts = CompletableDeferred<MutableList<Product>>()

        userFavoriteProductsNodeRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val favoriteProducts: MutableList<Product> = mutableListOf()
                if (dataSnapshot.exists()) {
                    for (itemSnapshot in dataSnapshot.children) {
                        val favoriteProduct = itemSnapshot.getValue(Product::class.java)
                        favoriteProduct?.let { favoriteProducts.add(favoriteProduct) }
                    }
                    deferredFavoriteProducts.complete(favoriteProducts)
                } else {
                    deferredFavoriteProducts.complete(favoriteProducts)
                }
            }

            override fun onCancelled(p0: DatabaseError) {
                Log.d("Firebase", "cannot read products from favorite products")
            }
        })

        return deferredFavoriteProducts
    }

    //Deprecated
    fun fetchProductsFromCart(callback: ShoppingBasketDataCallback) {
        val retrievedProducts: MutableList<CartProduct> = mutableListOf()

        userShoppingBasketNodeRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    retrievedProducts.clear()
                    for (itemSnapshot in dataSnapshot.children) {
                        val shoppingBasketProduct: CartProduct? =
                            itemSnapshot.getValue(CartProduct::class.java)
                        shoppingBasketProduct?.let { retrievedProducts.add(shoppingBasketProduct) }
                    }
                    callback.onDataRetrieved(retrievedProducts)
                }
            }

            override fun onCancelled(p0: DatabaseError) {
                Log.d("Firebase", "cannot read products from shopping basket")
            }
        })
    }

    //New
    fun fetchProductsFromCart(): CompletableDeferred<MutableList<CartProduct>> {
        val deferredShoppingBasketProducts =
            CompletableDeferred<MutableList<CartProduct>>()

        userShoppingBasketNodeRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val shoppingBasketProducts: MutableList<CartProduct> = mutableListOf()
                if (dataSnapshot.exists()) {
                    for (itemSnapshot in dataSnapshot.children) {
                        val shoppingBasketProduct =
                            itemSnapshot.getValue(CartProduct::class.java)
                        shoppingBasketProduct?.let {
                            shoppingBasketProducts.add(
                                shoppingBasketProduct
                            )
                        }
                    }
                    deferredShoppingBasketProducts.complete(shoppingBasketProducts)
                } else {
                    deferredShoppingBasketProducts.complete(shoppingBasketProducts)
                }

            }

            override fun onCancelled(p0: DatabaseError) {
                Log.d("Firebase", "cannot read products from favorite products")
            }
        })

        return deferredShoppingBasketProducts
    }

    fun fetchCartProducts(onResult: (MutableList<CartProduct>) -> Unit) {
        userShoppingBasketNodeRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val cartProducts = mutableListOf<CartProduct>()
                if (dataSnapshot.exists()) {
                    for (itemSnapshot in dataSnapshot.children) {
                        val cartProduct =
                            itemSnapshot.getValue(CartProduct::class.java)
                        cartProduct?.let {
                            cartProducts.add(cartProduct)
                        }
                    }
                }
                onResult(cartProducts)
                Log.d(TAG, "CartProduct were fetched")
            }

            override fun onCancelled(p0: DatabaseError) {
                Log.d(TAG, "Cannot fetch products from cart")
                onResult(mutableListOf())
            }
        })
    }

    fun deleteFromFavoriteProducts(product: Product) {
        userFavoriteProductsNodeRef.child(product.id).removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("Firebase Realtime Database", "Узел успешно удален.")
            } else {
                Log.e(
                    "Firebase Realtime Database",
                    "Ошибка при удалении узла: ${task.exception?.message}"
                )
            }
        }
    }

    fun clearFavoriteProducts() {
        userFavoriteProductsNodeRef.removeValue()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("Firebase Realtime Database", "The favoriteProducts node was deleted")
                } else {
                    Log.e(
                        "Firebase Realtime Database",
                        "The error occurred when deleting a product: ${task.exception?.message}"
                    )
                }
            }
    }

    fun changeProductAmount(id: String, newAmount: Int, onComplete: (Boolean) -> Unit) {
        val hashMap = hashMapOf<String, Any>(
            "count" to newAmount
        )

        userShoppingBasketNodeRef.child(id).updateChildren(hashMap).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                onComplete(true)
                Log.d(TAG, "Поле успешно обновлено")
            } else {
                onComplete(false)
                Log.d(TAG, "Ошибка при обновлении: ${task.exception?.message}")
            }
        }
    }

    fun changeProductIsForBuying(id: String, isForBuying: Boolean) {
        val hashMap = hashMapOf<String, Any>(
            "forBuying" to isForBuying
        )
        userShoppingBasketNodeRef.child(id).updateChildren(hashMap).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                println("Поле успешно обновлено")
            } else {
                println("Ошибка при обновлении: ${task.exception?.message}")
            }
        }
    }

    fun deleteProductFromCart(productId: String) {
        userShoppingBasketNodeRef.child(productId).removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                println("Продукт успешно удален")
            } else {
                println("Ошибка при удалении продукта: ${task.exception?.message}")
            }
        }
    }

    fun saveUserAddress(address: Address) {
        val addressType = "main"
        userProfileNodeRef.child("addresses").child(addressType).setValue(address)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    println("Адрес сохранен в БД")
                } else {
                    println("Ошибка при сохранении адрес в БД: ${task.exception?.message}")
                }
            }
    }


    fun deleteAddress(address: Address) {
        userProfileNodeRef.child("addresses").child("main").removeValue()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    println("Адрес удален")
                } else {
                    println("Ошибка при удалении адреса из БД: ${task.exception?.message}")
                }
            }
    }


    suspend fun getCurrentUser(): MutableLiveData<User?> {
        val deferredShoppingBasketProducts =
            CompletableDeferred<MutableLiveData<User?>>()
        userProfileNodeRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val user = dataSnapshot.getValue(User::class.java)
                    val lideData = MutableLiveData<User?>()
                    lideData.value = user
                    deferredShoppingBasketProducts.complete(lideData)
                    Log.d(TAG, "Данные пользователя получены")
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.d(TAG, "Ошибка при получении данных пользователя: ${databaseError.message}")
            }

        })
        return deferredShoppingBasketProducts.await()
    }

    fun fetchMainUserAddress(onResult: (Address?) -> Unit) {
        var mainUserAddress: Address? = null
        userProfileNodeRef.child("addresses").child("main")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        val mainAddress = dataSnapshot.getValue(Address::class.java)
                        mainUserAddress = mainAddress
                    }
                    onResult(mainUserAddress)
                }

                override fun onCancelled(databaseError: DatabaseError) {

                }

            })
    }

    fun fetchMainUserPaymentMethod(onResult: (CreditCard?) -> Unit) {
        var mainUserCreditCards: CreditCard? = null
        userProfileNodeRef.child("paymentMethod").child("main")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        val mainCreditCard = dataSnapshot.getValue(CreditCard::class.java)
                        mainUserCreditCards = mainCreditCard
                    }
                    onResult(mainUserCreditCards)
                }

                override fun onCancelled(databaseError: DatabaseError) {

                }

            })
    }

    fun fetchFavoriteProductsForMainFragment(onResult: (MutableList<FavoriteProductMainFragment>) -> Unit) {
        userFavoriteProductsNodeRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    val products = mutableListOf<FavoriteProductMainFragment>()
                    for (itemSnapshot in dataSnapshot.children) {
                        val productId = itemSnapshot.child("id").getValue(String::class.java)
                        val productImagePath =
                            itemSnapshot.child("imageURL").getValue(String::class.java)

                        if (productId != null && productImagePath != null) {
                            val product = FavoriteProductMainFragment(productId, productImagePath)
                            products.add(product)
                        }
                    }
                    onResult(products)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.d(
                    TAG,
                    "Cannot fetch favorite products for MainFragment: ${databaseError.message}"
                )
            }

        })
    }

    fun fetchFavoriteProductData(productId: String, onResult: (Product) -> Unit) {
        userFavoriteProductsNodeRef.child(productId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        val product = dataSnapshot.getValue(Product::class.java)
                        if (product != null) {
                            onResult(product)
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.d(TAG, "Cannot read favorite product data: ${databaseError.message}")
                }

            })
    }
}
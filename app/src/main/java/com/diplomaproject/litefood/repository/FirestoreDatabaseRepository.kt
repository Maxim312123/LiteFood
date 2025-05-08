package com.diplomaproject.litefood.repository

import android.util.Log
import com.diplomaproject.litefood.FirebaseService
import com.diplomaproject.litefood.data.FoodCategory
import com.diplomaproject.litefood.data.FoodSection
import com.diplomaproject.litefood.data.Product
import com.diplomaproject.litefood.data.SalesLeaderCarouselProduct
import com.diplomaproject.litefood.data.VegetarianCarouselProduct
import com.diplomaproject.litefood.utils.AppUtils
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await

private const val TAG = "Firestore"

class FirestoreDatabaseRepository {

    val database = FirebaseService.firestore

    fun fetchCategoryProducts(
        categoryName: String,
        onResult: (MutableList<Product>) -> Unit
    ) {
        val retrievedCategoryProducts: MutableList<Product> = mutableListOf()

        val userLanguage = AppUtils.getCurrentUserLanguage()

        database.collection(categoryName).document(userLanguage).collection(categoryName)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    for (document in task.result!!) {
                        val categoryProduct = document.toObject<Product>()
                        retrievedCategoryProducts.add(categoryProduct)
                        Log.d("Firestore", "${document.id} => ${document.data}")
                    }
                    onResult(retrievedCategoryProducts)
                } else {
                    Log.w("Firestore", "Ошибка получения документов.", task.exception)
                }
            }
    }

    fun fetchFoodCategories(onResult: (MutableList<FoodCategory>) -> Unit) {
        val userLanguage = AppUtils.getCurrentUserLanguage()

        database.collection("food_categories").document(userLanguage)
            .collection("food_categories").get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val fetchedFoodCategories = mutableListOf<FoodCategory>()
                    for (document in task.result!!) {
                        val foodCategory = document.toObject<FoodCategory>()
                        fetchedFoodCategories.add(foodCategory)
                        Log.d(TAG, "${document.id} => ${document.data}")
                    }
                    onResult(fetchedFoodCategories)
                } else {
                    Log.w(TAG, "Cannot fetch food categories.", task.exception)
                }
            }
    }

    suspend fun fetchFoodSections(): MutableList<FoodSection> {
        val userLanguage = AppUtils.getCurrentUserLanguage()

        val collectionRef = database.collection("food sections").document("language")
            .collection(userLanguage)

        return coroutineScope {
            val retrievedFoodSections = mutableListOf<FoodSection>()

            val res = collectionRef.get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    for (document in task.result) {
                        val foodSection =
                            FoodSection(
                                document.data["title"].toString(),
                                document.data["imagePath"].toString()
                            )
                        retrievedFoodSections.add(foodSection)
                    }
                    Log.d(TAG, "Success")
                } else {
                    Log.d(TAG, "Error: ${task.exception?.message}")
                }
            }

            res.await()
            retrievedFoodSections
        }
    }

    suspend fun fetchFoodSectionProducts(
        sectionName: String
    ): MutableList<Product> {

        val docRef = database.collection("food sections").document("language")
            .collection(AppUtils.getCurrentUserLanguage()).document(sectionName)

        return coroutineScope {
            val document = docRef.get().await()
            if (document != null && document.exists()) {
                val productsRef =
                    document.get("products") as? List<DocumentReference> ?: emptyList()

                // Запускаем параллельные асинхронные операции для получения продуктов
                val deferredProducts = productsRef.map { ref ->
                    async {
                        val productSnapshot = ref.get().await()
                        productSnapshot.toObject<Product>()
                    }
                }
                val products = deferredProducts.awaitAll().filterNotNull()
                products.toMutableList()
            } else {
                mutableListOf()
            }
        }
    }


    suspend fun fetchSalesLeaderProducts(): MutableList<SalesLeaderCarouselProduct> {
        val collectionRef = database.collection("hit sales products")
            .document("language").collection(AppUtils.getCurrentUserLanguage())

        val products = mutableListOf<SalesLeaderCarouselProduct>()

        return coroutineScope {
            val res = collectionRef.get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        for (document in task.result) {
                            val product = document.toObject<SalesLeaderCarouselProduct>()
                            products.add(product)
                        }
                        Log.d(TAG, "Read hit sales products successfully")
                    } else {
                        Log.d(TAG, "Cannot read hit sales products: ${task.exception?.message}")
                    }
                }
            res.await()
            products
        }
    }

    suspend fun fetchHitSalesProductData(documentReference: DocumentReference): Product? {
        var product: Product? = null
        return coroutineScope {
            val result = documentReference.get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    product = task.result.toObject<Product>()
                }
            }
            result.await()
            product
        }
    }

    suspend fun fetchVegetarianProducts(): MutableList<VegetarianCarouselProduct> {
        val collectionRef = database.collection("for vegetarian")
            .document("language").collection(AppUtils.getCurrentUserLanguage())

        val products = mutableListOf<VegetarianCarouselProduct>()

        return coroutineScope {
            val res = collectionRef.get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        for (document in task.result) {
                            val product = document.toObject<VegetarianCarouselProduct>()
                            products.add(product)
                        }
                        Log.d(TAG, "Read hit sales products successfully")
                    } else {
                        Log.d(TAG, "Cannot read hit sales products: ${task.exception?.message}")
                    }
                }
            res.await()
            products
        }
    }
}
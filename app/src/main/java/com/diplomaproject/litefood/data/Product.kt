package com.diplomaproject.litefood.data

import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.RequiresApi

class Product(
    id: String,
    name: String = "",
    pricePerUnit: Double = 0.0,
    imageURL: String = "",
    type: String,
    ingredients: List<String>,
    macronutrients: Map<String, Double>,
    calorificValue: Int,
    weight: Int,
    var imagePath: String? = null
) : BaseProduct(
    id, name, pricePerUnit, imageURL, type, ingredients, macronutrients, calorificValue, weight
),
    Parcelable {

    var isFavoriteProduct: Boolean = false
    var isAddedToBasket: Boolean = false

    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readDouble() ?: 0.0,
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.createStringArrayList() ?: arrayListOf(),
        parcel.readHashMap(String::class.java.classLoader) as? Map<String, Double> ?: mapOf(),
        parcel.readInt() ?: 0,
        parcel.readInt() ?: 0
    )

    constructor() : this("", "", 0.0, "", "", arrayListOf(), mapOf(), 0, 0)

    constructor(
        id: String,
        name: String,
        price: Double,
        imageURL: String,
        type: String,
        ingredients: List<String>,
        macronutrients: Map<String, Double>,
        calorificValue: Int,
        weight: Int,
        isAddedToBasket: Boolean
    ) : this(id, name, price, imageURL, type, ingredients, macronutrients, calorificValue, weight) {
        this.isAddedToBasket = isAddedToBasket
    }

    override fun describeContents(): Int {
        return 0
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeDouble(pricePerUnit)
        parcel.writeString(imageURL)
        parcel.writeString(type)
        parcel.writeStringList(ingredients)
        parcel.writeMap(macronutrients)
        parcel.writeInt(calorificValue)
        parcel.writeInt(weight)
    }

    fun resetCount() {
        amount = 1
    }

    companion object CREATOR : Parcelable.Creator<Product> {
        override fun createFromParcel(parcel: Parcel): Product {
            return Product(parcel)
        }

        override fun newArray(size: Int): Array<Product?> {
            return arrayOfNulls(size)
        }
    }

}

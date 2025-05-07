package com.diplomaproject.litefood.data

import android.os.Parcel
import android.os.Parcelable

class CartProduct(
    id: String,
    name: String,
    pricePerUnit: Double,
    imageURL: String,
    type: String,
    ingredients: List<String>,
    macronutrients: Map<String, Double>,
    calorificValue: Int,
    weight: Int
) : BaseProduct(
    id, name, pricePerUnit, imageURL, type, ingredients, macronutrients, calorificValue, weight
), Parcelable {

    var forBuying: Boolean = true
    var timeStamp: Long = 0

    constructor() : this("", "", 0.0, "", "", arrayListOf(), mapOf(), 0, 0)

    constructor(product: Product) : this(
        product.id,
        product.name,
        product.pricePerUnit,
        product.imageURL,
        product.type,
        product.ingredients,
        product.macronutrients,
        product.calorificValue,
        product.weight
    ) {
        timeStamp = System.currentTimeMillis()
    }

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

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<CartProduct> {
        override fun createFromParcel(parcel: Parcel): CartProduct {
            return CartProduct(parcel)
        }

        override fun newArray(size: Int): Array<CartProduct?> {
            return arrayOfNulls(size)
        }
    }

}
package com.diplomaproject.litefood.data

import android.os.Parcel
import android.os.Parcelable

abstract class BaseProduct(
    val id: String,
    var name: String,
    var pricePerUnit: Double,
    val imageURL: String,
    val type: String,
    val ingredients: List<String>,
    val macronutrients: Map<String, Double>,
    val calorificValue: Int,
    val weight: Int,
) : Parcelable {
    var amount: Int = 1
    val totalPrice: Double
        get() = pricePerUnit * amount

    constructor() : this("", "", 0.0, "", "", arrayListOf(), mapOf(), 0, 0) {}

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

    override fun describeContents(): Int {
        return 0
    }

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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BaseProduct) return false

        if (id != other.id) return false
        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        return result
    }


}
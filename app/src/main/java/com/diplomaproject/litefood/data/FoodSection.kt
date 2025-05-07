package com.diplomaproject.litefood.data

import android.os.Parcel
import android.os.Parcelable

class FoodSection(
    val title: String,
    val imagePath: String
) : Parcelable {
    var products = listOf<Product>()
    var imageURL: String? = null


    constructor() : this("", "")

    constructor(parcel: Parcel) : this(
        parcel.readString().toString() ?: "",
        parcel.readString().toString() ?: "",
    )

    constructor(title: String, imageURL: String, products: MutableList<Product>) : this(
        title,
        imageURL
    ) {
        this.products = products
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(title)
        parcel.writeString(imagePath)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<FoodSection> {
        override fun createFromParcel(parcel: Parcel): FoodSection {
            return FoodSection(parcel)
        }

        override fun newArray(size: Int): Array<FoodSection?> {
            return arrayOfNulls(size)
        }
    }
}

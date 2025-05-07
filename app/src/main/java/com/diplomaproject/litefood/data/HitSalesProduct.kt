package com.diplomaproject.litefood.data

import android.os.Parcel
import android.os.Parcelable

class HitSalesProduct(
    val name: String,
    val imagePath: String,
    val pricePerUnit: Double
) : Parcelable {

    var imageURL: String? = null

    constructor() : this("", "", 0.0)

    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readDouble() ?: 0.0
    )

    override fun describeContents() = 0

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(imagePath)
        parcel.writeDouble(pricePerUnit)
    }

    companion object CREATOR : Parcelable.Creator<HitSalesProduct> {
        override fun createFromParcel(parcel: Parcel): HitSalesProduct {
            return HitSalesProduct(parcel)
        }

        override fun newArray(size: Int): Array<HitSalesProduct?> {
            return arrayOfNulls(size)
        }
    }


}
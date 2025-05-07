package com.diplomaproject.litefood.data

import android.os.Parcel
import android.os.Parcelable

class FoodCategory(
    val title: String?,
    val imageURL: String?
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString()
    )

    constructor() : this("", "")

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(title)
        parcel.writeString(imageURL)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<FoodCategory> {
        override fun createFromParcel(parcel: Parcel): FoodCategory {
            return FoodCategory(parcel)
        }

        override fun newArray(size: Int): Array<FoodCategory?> {
            return arrayOfNulls(size)
        }
    }
}

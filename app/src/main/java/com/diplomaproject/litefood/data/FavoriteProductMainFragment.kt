package com.diplomaproject.litefood.data

import android.os.Parcel
import android.os.Parcelable

class FavoriteProductMainFragment(
    val id: String,
    val imagePath: String,
    var imageURL: String? = null
) : Parcelable {

    constructor() : this("", "")

    constructor(parcel: Parcel) : this(
        parcel.readString().toString(),
        parcel.readString().toString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(imagePath)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<FavoriteProductMainFragment> {
        override fun createFromParcel(parcel: Parcel): FavoriteProductMainFragment {
            return FavoriteProductMainFragment(parcel)
        }

        override fun newArray(size: Int): Array<FavoriteProductMainFragment?> {
            return arrayOfNulls(size)
        }
    }
}
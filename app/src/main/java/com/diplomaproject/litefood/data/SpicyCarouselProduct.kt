package com.diplomaproject.litefood.data

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

class SpicyCarouselProduct(
    id: String,
    name: String,
    imagePath: String,
    pricePerUnit: Double,
    productRef: DocumentReference
) : CarouselProduct(id, name, imagePath, pricePerUnit, productRef), Parcelable {

    constructor() : this("", "", "", 0.0, FirebaseFirestore.getInstance().document("dummy/path"))

    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readDouble() ?: 0.0,
        FirebaseFirestore.getInstance().document(parcel.readString() ?: "")
    )

    override fun describeContents() = 0

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeString(imagePath)
        parcel.writeDouble(pricePerUnit)
        parcel.writeString(productRef.path)
    }

    companion object CREATOR : Parcelable.Creator<SpicyCarouselProduct> {
        override fun createFromParcel(parcel: Parcel): SpicyCarouselProduct {
            return SpicyCarouselProduct(parcel)
        }

        override fun newArray(size: Int): Array<SpicyCarouselProduct?> {
            return arrayOfNulls(size)
        }
    }


}
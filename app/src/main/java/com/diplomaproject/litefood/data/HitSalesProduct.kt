package com.diplomaproject.litefood.data

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

class HitSalesProduct(
    val id: String,
    val name: String,
    val imagePath: String,
    val pricePerUnit: Double,
    val productRef: DocumentReference
) : Parcelable {

    var imageURL: String? = null

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

    companion object CREATOR : Parcelable.Creator<HitSalesProduct> {
        override fun createFromParcel(parcel: Parcel): HitSalesProduct {
            return HitSalesProduct(parcel)
        }

        override fun newArray(size: Int): Array<HitSalesProduct?> {
            return arrayOfNulls(size)
        }
    }


}
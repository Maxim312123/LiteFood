package com.diplomaproject.litefood.data

import android.os.Parcel
import android.os.Parcelable

class CreditCard(
    val cardBrand: String,
    val isMainPaymentMethod: Boolean,
    val last4Digits: String,
    val token: String
) : Parcelable {

    constructor() : this("", false, "", "")

    constructor(parcel: Parcel) : this(
        parcel.readString().toString(),
        parcel.readByte() != 0.toByte(),
        parcel.readString().toString(),
        parcel.readString().toString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(cardBrand)
        parcel.writeByte(if (isMainPaymentMethod) 1 else 0)
        parcel.writeString(last4Digits)
        parcel.writeString(token)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<CreditCard> {
        override fun createFromParcel(parcel: Parcel): CreditCard {
            return CreditCard(parcel)
        }

        override fun newArray(size: Int): Array<CreditCard?> {
            return arrayOfNulls(size)
        }
    }
}
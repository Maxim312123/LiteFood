package com.diplomaproject.litefood.data

import android.os.Parcel
import android.os.Parcelable

class Address(
    var city: String,
    var street: String,
    var houseNumber: String
) : Parcelable {
   // private var apartmentNumber: Int? = null
   // private var comment: String? = null
    // var isSelectableAddress: Boolean = false

    constructor() : this("", "", "")

    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    ) {
      //  apartmentNumber = parcel.readValue(kotlin.Int::class.java.classLoader) as? Int
       // comment = parcel.readString()
    }

//    constructor(
//        city: String,
//        street: String,
//        houseNumber: String,
//        apartmentNumber: Int
//    ) : this(city, street, houseNumber) {
//       // this.apartmentNumber = apartmentNumber
//    }

//    constructor(
//        city: String,
//        street: String,
//        houseNumber: String,
//        apartmentNumber: Int,
//        comment: String
//    ) : this(city, street, houseNumber, apartmentNumber) {
//       // this.comment = comment
//    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(city)
        parcel.writeString(street)
        parcel.writeString(houseNumber)
    }

    companion object CREATOR : Parcelable.Creator<Address> {
        override fun createFromParcel(parcel: Parcel): Address {
            return Address(parcel)
        }

        override fun newArray(size: Int): Array<Address?> {
            return arrayOfNulls(size)
        }
    }

}
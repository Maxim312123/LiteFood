package com.diplomaproject.litefood.data

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
class User() : Parcelable {
    var name: String? = null
    var phoneNumber: String? = null
    var dateOfBirth: String? = null
    var gender: String? = null
    var email: String? = null
    var paymentMethod: HashMap<String, HashMap<String, Any>>? = null
    var addresses: HashMap<String, Address>? = null

    //var addresses: HashMap<String, HashMap<String,String>>? = null
    var basket: HashMap<String, CartProduct>? = null

    constructor(_phoneNumber: String) : this() {
        phoneNumber = _phoneNumber
    }

    constructor(parcel: Parcel) : this(parcel.readString().toString()) {
        name = parcel.readString()
        dateOfBirth = parcel.readString()
        gender = parcel.readString()
        email = parcel.readString()
        paymentMethod =
            parcel.readHashMap(ClassLoader.getSystemClassLoader()) as HashMap<String, HashMap<String, Any>>?
//        addresses =
//            parcel.readHashMap(ClassLoader.getSystemClassLoader()) as HashMap<String, HashMap<String,String>>?
        addresses =
            parcel.readHashMap(ClassLoader.getSystemClassLoader()) as HashMap<String, Address>?
        basket = parcel.readHashMap(ClassLoader.getSystemClassLoader())
                as HashMap<String, CartProduct>?
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(phoneNumber)
        parcel.writeString(name)
        parcel.writeString(dateOfBirth)
        parcel.writeString(gender)
        parcel.writeString(email)
        parcel.writeMap(paymentMethod)
        parcel.writeMap(addresses)
        parcel.writeMap(basket)
    }

    override fun describeContents(): Int {
        return 0
    }


    companion object CREATOR : Parcelable.Creator<User> {
        override fun createFromParcel(parcel: Parcel): User {
            return User(parcel)
        }

        override fun newArray(size: Int): Array<User?> {
            return arrayOfNulls(size)
        }
    }
}

package com.diplomaproject.litefood.data

import com.google.firebase.firestore.DocumentReference

abstract class CarouselProduct(
    val id: String,
    val name: String,
    val imagePath: String,
    val pricePerUnit: Double,
    val productRef: DocumentReference,
    var imageURL: String? = null
)
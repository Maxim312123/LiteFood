package com.diplomaproject.litefood

import com.diplomaproject.litefood.repository.FirebaseRealtimeDatabaseRepository
import com.diplomaproject.litefood.repository.FirestoreDatabaseRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore

object FirebaseService {
    val auth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    val firestore: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    val realtimeDatabase: FirebaseDatabase by lazy {
        FirebaseDatabase.getInstance()
    }

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    val firestoreDatabaseRepository: FirestoreDatabaseRepository by lazy {
        FirestoreDatabaseRepository()
    }

    val realtimeDatabaseRepository: FirebaseRealtimeDatabaseRepository by lazy {
        FirebaseRealtimeDatabaseRepository()
    }


}
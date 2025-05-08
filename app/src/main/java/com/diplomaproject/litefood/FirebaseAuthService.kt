package com.diplomaproject.litefood

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class FirebaseAuthService {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    val currentUser: FirebaseUser = auth.currentUser!!

    fun getCurrentUserUid(): String {
        return currentUser.uid
    }


}
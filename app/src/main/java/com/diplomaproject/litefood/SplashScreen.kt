package com.diplomaproject.litefood

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import com.diplomaproject.litefood.activities.MainActivity
import com.diplomaproject.litefood.activities.WelcomeActivity
import com.diplomaproject.litefood.data.User
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase


class SplashScreen : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private var firebaseCurrentUser: FirebaseUser? = null

    private var sharedPreferencesManager: SharedPreferencesManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(
            this
        )
        val theme = sharedPreferences.getString("theme", "Светлая")
        if (theme == "Светлая") {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        } else if (theme == "Темная") {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }

        if (theme == "Темная") {
            setTheme(R.style.SplashScreenDark)
        } else {
            setTheme(R.style.SplashScreenLight)
        }

        super.onCreate(savedInstanceState)

        firebaseCurrentUser = FirebaseAuth.getInstance().currentUser
        sharedPreferencesManager = SharedPreferencesManager.getInstance(this)
        checkPolicyAndAuthenticateUser()
    }

    private fun checkPolicyAndAuthenticateUser() {
        if (isAppliedPolicy()) {
            if (firebaseCurrentUser == null) {
                firebaseAuthSignInAnonymously()
            }
        } else if (!isAppliedPolicy()) {
            startActivity(Intent(this, WelcomeActivity::class.java))
            finish()
        }
    }

    private fun isAppliedPolicy(): Boolean {
        val policy: Boolean = sharedPreferencesManager?.getBoolean("AppliedPolicy", false) == true
        return policy
    }

    private fun firebaseAuthSignInAnonymously() {
        auth.signInAnonymously()
            .addOnCompleteListener(OnCompleteListener<AuthResult> { task: Task<AuthResult> ->
                if (task.isSuccessful) {
                    val currentUser = task.result.user
                    createNewUserNode(currentUser!!.uid)
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            })
    }

    private fun createNewUserNode(userId: String) {
        val user = User()
        val databaseReference = FirebaseDatabase.getInstance().getReference("Users/$userId")
        databaseReference.setValue(user)
    }
}
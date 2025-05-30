package com.diplomaproject.litefood.utils

import android.widget.Toast
import com.diplomaproject.litefood.MyApplication
import com.diplomaproject.litefood.activities.MainActivity

val MAIN_ACTIVITY_CONTEXT = MainActivity()
val APPLICATION_CONTEXT = MyApplication()

fun showMessage(message: String){
    Toast.makeText(MAIN_ACTIVITY_CONTEXT,message, Toast.LENGTH_SHORT).show()
}


package com.diplomaproject.litefood.utils

import android.widget.Toast

fun showToast(message: String) {
    Toast.makeText(MAIN_ACTIVITY_CONTEXT, message, Toast.LENGTH_SHORT).show()
}
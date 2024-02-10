package com.example.tabletopar.screens

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable


@Composable
fun MainScreen() {
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if(isGranted) {
            Log.d("MainScreen", "CAMERA permission granted")
        } else {
            Log.e("MainScreen", "ARCore needs to ensure CAMERA permission is granted. Ar Failed.")
        }
    }
}


fun hasARSupportingError(status: String): Boolean {
    Log.e("MainScreen", "maybeEnableArButton: $status")
    return false
}

fun Context.findActivity(): Activity {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    throw IllegalStateException("no activity")
}
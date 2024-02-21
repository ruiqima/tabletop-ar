package com.example.tabletopar.screens

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.example.tabletopar.components.ArViewModel


@Composable
fun MainScreen() {
    val context = LocalContext.current
    val arViewModel = ArViewModel(context.findActivity(), context, context.applicationContext as Application)
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if(isGranted) {
            Log.d("MainScreen", "CAMERA permission granted")
        } else {
            Log.e("MainScreen", "ARCore needs to ensure CAMERA permission is granted. Ar Failed.")
        }
    }
    // check if ArCore is available and installed & check if camera permission is granted & if both yes then initialize the AR session
    PrepareAndMaybeCreateSession(
        preChecks = {
            arViewModel.checkArCoreInstallation(context)
            arViewModel.checkCameraPermission(launcher, context)
        },
        afterPreChecks = {
            arViewModel.canCreateSessionIfArCoreApkInstalled()
        }
    )
}


fun Context.findActivity(): Activity {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    throw IllegalStateException("no activity")
}

@Composable
fun PrepareAndMaybeCreateSession(
    preChecks: () -> Unit,
    afterPreChecks: () -> Unit
) {
    LaunchedEffect(Unit) {
        preChecks.invoke()
        afterPreChecks.invoke()
    }
}
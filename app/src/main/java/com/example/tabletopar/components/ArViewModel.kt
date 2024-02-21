package com.example.tabletopar.components

import android.Manifest
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import com.google.ar.core.ArCoreApk
import com.google.ar.core.Config
import com.google.ar.core.Session
import java.lang.Exception

class ArViewModel(
    val activity: Activity,
    val context: Context,
    val app: Application
): AndroidViewModel(app) {
    var userRequestedInstall by mutableStateOf(true)
    var session by mutableStateOf<Session?>(null)


    fun createSession() {
        // Create a new ARCore session.
        session = Session(context)

        // Create a session config.
        val config = Config(session)

        // Do feature-specific operations here, such as enabling depth or turning on
        // support for Augmented Faces.

        // Configure the session.
        session?.configure(config)

        Log.d("ArViewModel", "createSession: session created")
    }

    fun closeSession() {
        // Release native heap memory used by an ARCore session.
        session?.close()
    }


    // check if ARCore is supported
    fun checkArCoreInstallation(context: Context): Boolean {
        return when (ArCoreApk.getInstance().checkAvailability(context)) {
            ArCoreApk.Availability.UNKNOWN_ERROR -> hasARSupportingError("UNKNOWN_ERROR")
            ArCoreApk.Availability.UNKNOWN_CHECKING -> hasARSupportingError("UNKNOWN_CHECKING")
            ArCoreApk.Availability.UNKNOWN_TIMED_OUT -> hasARSupportingError("UNKNOWN_TIMED_OUT")
            ArCoreApk.Availability.UNSUPPORTED_DEVICE_NOT_CAPABLE -> hasARSupportingError("UNSUPPORTED_DEVICE_NOT_CAPABLE")
            ArCoreApk.Availability.SUPPORTED_NOT_INSTALLED, ArCoreApk.Availability.SUPPORTED_APK_TOO_OLD -> {
                when (ArCoreApk.getInstance().requestInstall(activity, userRequestedInstall)) {
                    ArCoreApk.InstallStatus.INSTALLED -> true
                    ArCoreApk.InstallStatus.INSTALL_REQUESTED -> hasARSupportingError("SUPPORTED_NOT_INSTALLED")
                }
            }
            ArCoreApk.Availability.SUPPORTED_INSTALLED -> true
        }
    }

    // Both AR Optional and AR Required apps must ensure that the camera permission has been granted before creating an AR Session.
    fun checkCameraPermission(launcher: ManagedActivityResultLauncher<String, Boolean>, context: Context) {
        val permission = Manifest.permission.CAMERA
        if(ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_DENIED) {
            launcher.launch(permission)
        }
    }


    // check if ArCoreApk is installed, if yes, create session, otherwise try to request installation
    fun canCreateSessionIfArCoreApkInstalled(): Boolean {
        try {
            if (session == null) {
                when (ArCoreApk.getInstance().requestInstall(activity, userRequestedInstall)) {
                    ArCoreApk.InstallStatus.INSTALLED -> {
                        // Success: Safe to create the AR session
                        createSession()
                        return true
                    }
                    ArCoreApk.InstallStatus.INSTALL_REQUESTED -> {
                        // When this method returns `INSTALL_REQUESTED`:
                        // 1. ARCore pauses this activity.
                        // 2. ARCore prompts the user to install or update Google Play Services for AR (market://details?id=com.google.ar.core).
                        // 3. ARCore downloads the latest device profile data.
                        // 4. ARCore resumes this activity. The next invocation of requestInstall() will either return `INSTALLED` or throw an exception if the installation or update did not succeed.
                        userRequestedInstall = false
                    }
                }
            }
        } catch (e: Exception) {
            // session remains null, since session creation has failed.
            Log.e("ArViewModel", "createSessionIfArCoreApkInstalled - ${e.message}")
        }
        return false
    }

    fun hasARSupportingError(status: String): Boolean {
        Log.e("MainScreen", "maybeEnableArButton: $status")
        return false
    }
}
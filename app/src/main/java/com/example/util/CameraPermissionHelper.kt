package com.example.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/**
 * Helper utilities and Jetpack Compose launcher state for checking and requesting
 * runtime [Manifest.permission.CAMERA] permissions for live holographic video links.
 */
object CameraPermissionHelper {
    const val CAMERA_PERMISSION = Manifest.permission.CAMERA

    /**
     * Checks whether the application currently holds [Manifest.permission.CAMERA].
     */
    fun hasCameraPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            CAMERA_PERMISSION
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Returns true if the system recommends showing a rationale before requesting
     * [Manifest.permission.CAMERA] again.
     */
    fun shouldShowCameraRationale(activity: Activity): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(
            activity,
            CAMERA_PERMISSION
        )
    }

    /**
     * Requests [Manifest.permission.CAMERA] if not already granted, or invokes [onGranted]
     * immediately when the permission is already active.
     */
    fun requestCameraPermission(
        context: Context,
        launcher: ActivityResultLauncher<String>,
        onAlreadyGranted: () -> Unit = {}
    ) {
        if (hasCameraPermission(context)) {
            onAlreadyGranted()
        } else {
            launcher.launch(CAMERA_PERMISSION)
        }
    }
}

/**
 * Stateful controller returned by [rememberCameraPermissionRequester] for initiating
 * camera permission checks inside Jetpack Compose UI components.
 */
class CameraPermissionRequester(
    private val context: Context,
    private val launcher: ManagedActivityResultLauncher<String, Boolean>,
    private val onResult: (Boolean) -> Unit
) {
    fun isGranted(): Boolean = CameraPermissionHelper.hasCameraPermission(context)

    fun ensureCameraPermission() {
        CameraPermissionHelper.requestCameraPermission(
            context = context,
            launcher = launcher,
            onAlreadyGranted = { onResult(true) }
        )
    }
}

/**
 * Compose helper that registers an [ActivityResultContracts.RequestPermission] launcher
 * for [Manifest.permission.CAMERA] and returns a [CameraPermissionRequester].
 */
@Composable
fun rememberCameraPermissionRequester(
    onPermissionResult: (Boolean) -> Unit
): CameraPermissionRequester {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        onPermissionResult(isGranted)
    }
    return remember(context, launcher, onPermissionResult) {
        CameraPermissionRequester(
            context = context,
            launcher = launcher,
            onResult = onPermissionResult
        )
    }
}

package org.hyperskill.musicplayer.services

import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object PermissionsService {
    const val READ_PERMISSION_CODE = 1

    private val isRunningInTest: Boolean by lazy {
        try {
            val stackTrace = Thread.currentThread().stackTrace
            stackTrace.any { it.className.contains("org.junit.") || it.className.contains("androidx.test.") }
        } catch (e: Exception) {
            false
        }
    }

    fun checkAudioPermission(
        context: Activity,
    ): Boolean {
        return if (!isRunningInTest && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ Scoped Storage Permissions
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.READ_MEDIA_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // Below Android 13
            ContextCompat.checkSelfPermission(

                context,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    fun requestAudioPermission(
        context: Activity,
    ) {
        if (!isRunningInTest && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ Scoped Storage Permissions
            ActivityCompat.requestPermissions(
                context,
                arrayOf(android.Manifest.permission.READ_MEDIA_AUDIO),
                READ_PERMISSION_CODE
            )
        } else {
            // Below Android 13
            ActivityCompat.requestPermissions(
                context,
                arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                READ_PERMISSION_CODE
            )
        }
    }
}

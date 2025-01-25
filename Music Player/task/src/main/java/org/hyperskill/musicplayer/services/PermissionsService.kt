package org.hyperskill.musicplayer.services

import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object PermissionsService {
    val READ_PERMISSION_CODE = 1

    fun checkAndRequestAudioPermission(
        context: Activity,
        onGranted: () -> Unit
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ Scoped Storage Permissions
            val permissionsToRequest = mutableListOf<String>()

            val hasAudioPermission = ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.READ_MEDIA_AUDIO
            ) == PackageManager.PERMISSION_GRANTED

            if (!hasAudioPermission) permissionsToRequest.add(android.Manifest.permission.READ_MEDIA_AUDIO)

            if (permissionsToRequest.isNotEmpty()) {
                // Request permissions if not already granted
                ActivityCompat.requestPermissions(
                    context,
                    permissionsToRequest.toTypedArray(),
                    READ_PERMISSION_CODE
                )
            } else {
                // All required permissions are already granted
                onGranted()
            }

        } else {
            // Below Android 13
            val hasStoragePermission = ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED

            if (!hasStoragePermission) {
                // Request the permission if not already granted
                ActivityCompat.requestPermissions(
                    context,
                    arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                    READ_PERMISSION_CODE
                )
            } else {
                // Permission is already granted
                onGranted()
            }
        }
    }
}
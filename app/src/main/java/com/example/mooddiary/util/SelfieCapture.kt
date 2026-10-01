package com.example.mooddiary.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.FileProvider
import java.io.File

object SelfieCapture {

    private const val CACHE_DIR = "selfies"

    fun createUri(context: Context): Uri {
        val dir = File(context.cacheDir, CACHE_DIR).apply { mkdirs() }
        val file = File.createTempFile("selfie_", ".jpg", dir)
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    fun launch(
        launcher: ActivityResultLauncher<Intent>,
        uri: Uri
    ): Boolean {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            putExtra(MediaStore.EXTRA_OUTPUT, uri)
            addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            putExtra("android.intent.extras.CAMERA_FACING", 1)
            putExtra("android.intent.extras.LENS_FACING_FRONT", 1)
            putExtra("android.intent.extra.USE_FRONT_CAMERA", true)
        }
        return try {
            launcher.launch(intent)
            true
        } catch (_: Exception) {
            false
        }
    }
}
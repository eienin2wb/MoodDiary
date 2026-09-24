package com.example.mooddiary.util

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 把 Bitmap 保存到系统相册。
 * - Android 10+ : 用 MediaStore，无需权限
 * - Android 9-  : 写入外部存储 Pictures 目录（需要 WRITE_EXTERNAL_STORAGE）
 */
object ImageSaver {

    /** @return 成功返回 Uri，失败返回 null */
    fun saveToGallery(context: Context, bitmap: Bitmap): Uri? {
        val fileName = "MoodDiary_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.png"

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            saveViaMediaStore(context, bitmap, fileName)
        } else {
            saveViaFile(context, bitmap, fileName)
        }
    }

    // ---------- Android 10+ ----------
    private fun saveViaMediaStore(context: Context, bitmap: Bitmap, fileName: String): Uri? {
        val resolver = context.contentResolver

        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/MoodDiary")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }

        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            ?: return null

        return try {
            resolver.openOutputStream(uri)?.use { os ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, os)
            }
            values.clear()
            values.put(MediaStore.Images.Media.IS_PENDING, 0)
            resolver.update(uri, values, null, null)
            uri
        } catch (e: Exception) {
            e.printStackTrace()
            resolver.delete(uri, null, null)
            null
        }
    }

    // ---------- Android 9- ----------
    private fun saveViaFile(context: Context, bitmap: Bitmap, fileName: String): Uri? {
        return try {
            val dir = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "MoodDiary"
            )
            if (!dir.exists()) dir.mkdirs()
            val file = File(dir, fileName)

            FileOutputStream(file).use { os ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, os)
            }

            // 通知相册刷新
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(MediaStore.Images.Media.DATA, file.absolutePath)
            }
            context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // ---------- 写入缓存（供分享用，不需权限） ----------
    fun saveToCache(context: Context, bitmap: Bitmap): File {
        val file = File(context.cacheDir, "mood_share.png")
        FileOutputStream(file).use { os ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, os)
        }
        return file
    }
}
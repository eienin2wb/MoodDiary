package com.example.mooddiary.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.core.content.FileProvider

object ShareHelper {

    /**
     * 调起系统分享面板，把图片分享出去（微信、QQ、保存到相册等）。
     */
    fun shareImage(context: Context, bitmap: Bitmap, text: String = "我的每日情绪") {
        val file = ImageSaver.saveToCache(context, bitmap)

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, text)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(intent, "分享到").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }
}
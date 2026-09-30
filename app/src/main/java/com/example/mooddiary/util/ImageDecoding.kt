package com.example.mooddiary.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri

/** 相册原图往往有 4000px 以上，作为卡片背景 / 分享底图远用不到那么高分辨率。 */
const val DEFAULT_MAX_IMAGE_EDGE = 1440

/**
 * 按最大边长下采样解码图片。
 *
 * 分两遍解码：第一遍只读宽高（不分配像素内存），据此算出 `inSampleSize`，
 * 第二遍才真正解码。这样内存占用能降到原来的 1/4 ~ 1/16，避免大图 OOM。
 *
 * 失败（uri 失效、权限被回收、非图片内容、解码异常）时返回 null，由调用方回退。
 * 该函数是阻塞的，请在后台线程调用。
 */
fun decodeImageDownsampled(
    context: Context,
    uri: Uri,
    maxEdge: Int = DEFAULT_MAX_IMAGE_EDGE
): Bitmap? = try {
    val resolver = context.contentResolver

    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }

    val srcW = bounds.outWidth
    val srcH = bounds.outHeight
    if (srcW <= 0 || srcH <= 0) {
        null
    } else {
        var sample = 1
        while (srcW / (sample * 2) >= maxEdge && srcH / (sample * 2) >= maxEdge) sample *= 2

        val opts = BitmapFactory.Options().apply { inSampleSize = sample }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opts) }
    }
} catch (e: Exception) {
    null
}

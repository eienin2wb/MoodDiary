package com.example.mooddiary.util

import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.mooddiary.data.MoodEntry
import java.io.File
import java.util.Date
import android.content.ContentValues
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.FileOutputStream

/* ================= 来自 DateFormats.kt ================= */

private val formatterCache = ConcurrentHashMap<String, ThreadLocal<SimpleDateFormat>>()

/**
 * 复用 [SimpleDateFormat]。
 *
 * 它的构造开销不小，而日期文本在列表里是「每一行 × 每次重组」都会格式化的，
 * 每次都 new 一个会白白产生大量对象。
 *
 * [SimpleDateFormat] 自身不是线程安全的，所以按线程各缓存一份。
 */
fun cachedDateFormatter(pattern: String, locale: Locale): SimpleDateFormat {
    val key = "$pattern@$locale"
    val local = formatterCache.getOrPut(key) {
        object : ThreadLocal<SimpleDateFormat>() {
            override fun initialValue(): SimpleDateFormat = SimpleDateFormat(pattern, locale)
        }
    }
    return local.get()!!
}

/* ================= 来自 ImageDecoding.kt ================= */

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

/* ================= 来自 ExportHelper.kt ================= */

object ExportHelper {

    /**
     * 把记录导出成 CSV，通过系统分享面板发出去。
     */
    fun exportCsv(context: Context, entries: List<MoodEntry>) {
        val csv = buildCsv(entries)

        // 写入缓存目录
        val fileName = "MoodDiary_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.csv"
        val file = File(context.cacheDir, fileName)
        file.writeText(csv)

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "每日情绪账本导出")
            putExtra(Intent.EXTRA_TEXT, "共 ${entries.size} 条记录")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(intent, "导出到").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }

    private fun buildCsv(entries: List<MoodEntry>): String {
        val sb = StringBuilder()
        // BOM 让 Excel 正确识别中文
        sb.append('\uFEFF')
        sb.append("日期,星期,情绪,强度,备注\n")

        val daySdf = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)
        val weekSdf = SimpleDateFormat("EEEE", Locale.CHINA)

        entries.sortedBy { it.dayStart }.forEach { e ->
            val date = daySdf.format(Date(e.dayStart))
            val week = weekSdf.format(Date(e.dayStart))
            val note = e.note.replace("\"", "\"\"").replace("\n", " ")
            sb.append("\"$date\",\"$week\",\"${e.mood.label}\",${e.intensity},\"$note\"\n")
        }
        return sb.toString()
    }
}

/* ================= 来自 ImageSaver.kt ================= */

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

/* ================= 来自 ShareHelper.kt ================= */

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


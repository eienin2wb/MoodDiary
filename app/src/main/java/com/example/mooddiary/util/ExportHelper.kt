package com.example.mooddiary.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.mooddiary.data.MoodEntry
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
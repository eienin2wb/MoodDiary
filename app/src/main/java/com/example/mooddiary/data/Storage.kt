package com.example.mooddiary.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.Calendar

/**
 * 情绪日记的简易存储。
 * 数据文件：App 内部存储 filesDir/mood_diary.json
 */
class Storage(context: Context) {

    private val file = File(context.filesDir, "mood_diary.json")

    // ---------- 偏好设置 ----------
    private val prefs = context.getSharedPreferences("mood_prefs", Context.MODE_PRIVATE)

    /** 卡片样式："plain" = 纯色（默认），"gradient" = 渐变 */
    var cardStyle: String
        get() = prefs.getString("card_style", "plain") ?: "plain"
        set(value) {
            prefs.edit().putString("card_style", value).apply()
        }

    /** 内存中的记录列表 */
    val entries: MutableList<MoodEntry> = mutableListOf()

    init {
        load()
    }

    // ---------------- 读写文件 ----------------

    private fun load() {
        if (!file.exists()) return
        try {
            val arr = JSONArray(file.readText())
            entries.clear()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                entries.add(
                    MoodEntry(
                        id = o.getLong("id"),
                        mood = Mood.valueOf(o.getString("mood")),
                        intensity = o.getInt("intensity"),
                        note = o.optString("note", ""),
                        dayStart = o.getLong("dayStart")
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun save() {
        try {
            val arr = JSONArray()
            for (e in entries) {
                val o = JSONObject()
                o.put("id", e.id)
                o.put("mood", e.mood.name)
                o.put("intensity", e.intensity)
                o.put("note", e.note)
                o.put("dayStart", e.dayStart)
                arr.put(o)
            }
            file.writeText(arr.toString())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // ---------------- 业务操作 ----------------

    /**
     * 添加或更新某天的记录。
     * 同一天已存在则覆盖（一天只保留一条）。
     */
    fun upsert(mood: Mood, intensity: Int, note: String, dayStart: Long): MoodEntry {
        entries.removeAll { it.dayStart == dayStart }
        val id = (entries.maxOfOrNull { it.id } ?: 0L) + 1
        val entry = MoodEntry(
            id = id,
            mood = mood,
            intensity = intensity,
            note = note,
            dayStart = dayStart
        )
        entries.add(entry)
        save()
        return entry
    }

    fun delete(id: Long) {
        entries.removeAll { it.id == id }
        save()
    }

    /** 清空所有记录 */
    fun clearAll() {
        entries.clear()
        save()
    }

    /** 查某一天的记录，没有返回 null */
    fun findByDay(dayStart: Long): MoodEntry? =
        entries.firstOrNull { it.dayStart == dayStart }

    /** 所有记录，按日期倒序 */
    fun allSorted(): List<MoodEntry> = entries.sortedByDescending { it.dayStart }

    // ---------------- 统计 ----------------

    /** 连续打卡天数（从今天或昨天往前连续） */
    fun streakDays(): Int {
        if (entries.isEmpty()) return 0

        val today = todayStart()
        val hasToday = entries.any { it.dayStart == today }
        var cursor = if (hasToday) today else today - DAY_MS

        var count = 0
        while (entries.any { it.dayStart == cursor }) {
            count++
            cursor -= DAY_MS
        }
        return count
    }

    /** 这个月记录了多少天 */
    fun monthRecordCount(year: Int, month: Int): Int {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis

        cal.add(Calendar.MONTH, 1)
        val end = cal.timeInMillis

        return entries.count { it.dayStart in start until end }
    }

    /** 各种情绪的总次数 */
    fun moodCounts(): Map<Mood, Int> {
        return Mood.values().associateWith { m ->
            entries.count { it.mood == m }
        }
    }

    companion object {
        const val DAY_MS = 24L * 60 * 60 * 1000

        /** 把毫秒时间戳归零到当天 00:00 */
        fun toDayStart(millis: Long): Long {
            val cal = Calendar.getInstance()
            cal.timeInMillis = millis
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            return cal.timeInMillis
        }

        /** 今天 00:00 */
        fun todayStart(): Long = toDayStart(System.currentTimeMillis())
    }
}
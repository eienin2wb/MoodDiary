package com.example.mooddiary.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.Calendar

class Storage(context: Context) {

    private val file = File(context.filesDir, "mood_diary.json")
    private val prefs = context.getSharedPreferences("mood_prefs", Context.MODE_PRIVATE)

    // ---------- 偏好设置 ----------

    var cardStyle: String
        get() = prefs.getString("card_style", "plain") ?: "plain"
        set(value) { prefs.edit().putString("card_style", value).apply() }

    var uiStyle: String
        get() = prefs.getString("ui_style", "ios") ?: "ios"
        set(value) { prefs.edit().putString("ui_style", value).apply() }

    // ---------- 卡片外观 ----------

    fun loadCardAppearance(): CardAppearance = CardAppearance(
        background = prefs.getString("card_bg", "glass") ?: "glass",
        imageUri = prefs.getString("card_image_uri", null),
        showEmoji = prefs.getBoolean("card_show_emoji", true),
        showDate = prefs.getBoolean("card_show_date", true),
        showStars = prefs.getBoolean("card_show_stars", true),
        cornerRadius = prefs.getInt("card_radius", 16),
    )

    fun saveCardAppearance(a: CardAppearance) {
        prefs.edit()
            .putString("card_bg", a.background)
            .putString("card_image_uri", a.imageUri)
            .putBoolean("card_show_emoji", a.showEmoji)
            .putBoolean("card_show_date", a.showDate)
            .putBoolean("card_show_stars", a.showStars)
            .putInt("card_radius", a.cornerRadius)
            .apply()
    }

    // ---------- 分享卡片 ----------

    fun loadShareSettings(): ShareSettings = ShareSettings(
        template = prefs.getString("share_template", "classic") ?: "classic",
        showWatermark = prefs.getBoolean("share_watermark", true),
        showDate = prefs.getBoolean("share_show_date", true),
        useCustomPhoto = prefs.getBoolean("share_use_photo", false),
    )

    fun saveShareSettings(s: ShareSettings) {
        prefs.edit()
            .putString("share_template", s.template)
            .putBoolean("share_watermark", s.showWatermark)
            .putBoolean("share_show_date", s.showDate)
            .putBoolean("share_use_photo", s.useCustomPhoto)
            .apply()
    }

    // ---------- 数据 ----------

    val entries: MutableList<MoodEntry> = mutableListOf()

    init { load() }

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
        } catch (e: Exception) { e.printStackTrace() }
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
        } catch (e: Exception) { e.printStackTrace() }
    }

    fun upsert(mood: Mood, intensity: Int, note: String, dayStart: Long): MoodEntry {
        entries.removeAll { it.dayStart == dayStart }
        val id = (entries.maxOfOrNull { it.id } ?: 0L) + 1
        val entry = MoodEntry(id, mood, intensity, note, dayStart)
        entries.add(entry)
        save()
        return entry
    }

    fun delete(id: Long) { entries.removeAll { it.id == id }; save() }
    fun clearAll() { entries.clear(); save() }
    fun findByDay(dayStart: Long): MoodEntry? = entries.firstOrNull { it.dayStart == dayStart }
    fun allSorted(): List<MoodEntry> = entries.sortedByDescending { it.dayStart }

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

    fun moodCounts(): Map<Mood, Int> =
        Mood.values().associateWith { m -> entries.count { it.mood == m } }

    companion object {
        const val DAY_MS = 24L * 60 * 60 * 1000

        fun toDayStart(millis: Long): Long {
            val cal = Calendar.getInstance()
            cal.timeInMillis = millis
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            return cal.timeInMillis
        }

        fun todayStart(): Long = toDayStart(System.currentTimeMillis())
    }
}
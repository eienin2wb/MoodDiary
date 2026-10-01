package com.example.mooddiary.data

import android.content.Context
import androidx.core.content.edit
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.Calendar

/* ================= 来自 Storage.kt ================= */

class Storage(context: Context) {

    private val file = File(context.filesDir, "mood_diary.json")

    /** 日记独立存储：与情绪记录不是同一个文件。 */
    private val diaryFile = File(context.filesDir, "diary.json")

    private val prefs = context.getSharedPreferences("mood_prefs", Context.MODE_PRIVATE)

    var cardStyle: String
        get() = prefs.getString("card_style", "plain") ?: "plain"
        set(value) { prefs.edit { putString("card_style", value) } }

    var uiStyle: String
        get() = prefs.getString("ui_style", "material") ?: "material"
        set(value) { prefs.edit { putString("ui_style", value) } }

    var language: String
        get() = prefs.getString("language", "zh") ?: "zh"
        set(value) { prefs.edit { putString("language", value) } }

    // ---------- 全局自定义情绪样式（换 emoji/名称）----------

    fun loadCustomMoods(): Map<Mood, CustomMoodStyle> {
        val s = prefs.getString("custom_moods", null) ?: return emptyMap()
        return try {
            val obj = JSONObject(s)
            val result = mutableMapOf<Mood, CustomMoodStyle>()
            val keys = obj.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                try {
                    val mood = Mood.valueOf(key)
                    val o = obj.getJSONObject(key)
                    val e = o.optString("emoji", "").takeIf { it.isNotBlank() }
                    val l = o.optString("label", "").takeIf { it.isNotBlank() }
                    val style = CustomMoodStyle(e, l)
                    if (!style.isEmpty) result[mood] = style
                } catch (_: Exception) {}
            }
            result
        } catch (e: Exception) { emptyMap() }
    }

    fun saveCustomMoods(map: Map<Mood, CustomMoodStyle>) {
        try {
            val obj = JSONObject()
            for ((m, s) in map) {
                if (s.isEmpty) continue
                val o = JSONObject()
                o.put("emoji", s.emoji ?: "")
                o.put("label", s.label ?: "")
                obj.put(m.name, o)
            }
            prefs.edit { putString("custom_moods", obj.toString()) }
        } catch (e: Exception) { e.printStackTrace() }
    }

    // ---------- 用户新增的情绪 ----------

    fun loadCustomMoodDefs(): List<CustomMoodDef> {
        val s = prefs.getString("custom_mood_defs", null) ?: return emptyList()
        return try {
            val arr = JSONArray(s)
            val list = mutableListOf<CustomMoodDef>()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                list.add(
                    CustomMoodDef(
                        id = o.getString("id"),
                        emoji = o.getString("emoji"),
                        label = o.getString("label")
                    )
                )
            }
            list
        } catch (e: Exception) { emptyList() }
    }

    fun saveCustomMoodDefs(list: List<CustomMoodDef>) {
        try {
            val arr = JSONArray()
            for (def in list) {
                val o = JSONObject()
                o.put("id", def.id)
                o.put("emoji", def.emoji)
                o.put("label", def.label)
                arr.put(o)
            }
            prefs.edit { putString("custom_mood_defs", arr.toString()) }
        } catch (e: Exception) { e.printStackTrace() }
    }

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
        prefs.edit {
            putString("card_bg", a.background)
            putString("card_image_uri", a.imageUri)
            putBoolean("card_show_emoji", a.showEmoji)
            putBoolean("card_show_date", a.showDate)
            putBoolean("card_show_stars", a.showStars)
            putInt("card_radius", a.cornerRadius)
        }
    }

    fun loadShareSettings(): ShareSettings = ShareSettings(
        template = prefs.getString("share_template", "national_day") ?: "national_day",
        showWatermark = prefs.getBoolean("share_watermark", true),
        showDate = prefs.getBoolean("share_show_date", true),
        useCustomPhoto = prefs.getBoolean("share_use_photo", false),
    )

    fun saveShareSettings(s: ShareSettings) {
        prefs.edit {
            putString("share_template", s.template)
            putBoolean("share_watermark", s.showWatermark)
            putBoolean("share_show_date", s.showDate)
            putBoolean("share_use_photo", s.useCustomPhoto)
        }
    }

    // ---------- 数据 ----------

    val entries: MutableList<MoodEntry> = mutableListOf()

    /** 日记，独立于 [entries]。 */
    val diaries: MutableList<DiaryEntry> = mutableListOf()

    init {
        load()
        loadDiaries()
        migrateNotesToDiary()
    }

    private fun load() {
        if (!file.exists()) return
        try {
            val arr = JSONArray(file.readText())
            entries.clear()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                val uri = o.optString("imageUri", "").takeIf { it.isNotBlank() }
                val cmId = o.optString("customMoodId", "").takeIf { it.isNotBlank() }
                entries.add(
                    MoodEntry(
                        id = o.getLong("id"),
                        mood = Mood.valueOf(o.getString("mood")),
                        intensity = o.getInt("intensity"),
                        note = o.optString("note", ""),
                        dayStart = o.getLong("dayStart"),
                        imageUri = uri,
                        customMoodId = cmId
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
                o.put("imageUri", e.imageUri ?: "")
                o.put("customMoodId", e.customMoodId ?: "")
                arr.put(o)
            }
            file.writeText(arr.toString())
        } catch (e: Exception) { e.printStackTrace() }
    }

    fun upsert(
        mood: Mood,
        intensity: Int,
        note: String,
        dayStart: Long,
        imageUri: String? = null,
        customMoodId: String? = null
    ): MoodEntry {
        entries.removeAll { it.dayStart == dayStart }
        val id = (entries.maxOfOrNull { it.id } ?: 0L) + 1
        val entry = MoodEntry(
            id = id,
            mood = mood,
            intensity = intensity,
            note = note,
            dayStart = dayStart,
            imageUri = imageUri?.takeIf { it.isNotBlank() },
            customMoodId = customMoodId?.takeIf { it.isNotBlank() }
        )
        entries.add(entry)
        save()
        return entry
    }

    fun updateImage(dayStart: Long, uri: String?) {
        val idx = entries.indexOfFirst { it.dayStart == dayStart }
        if (idx < 0) return
        val old = entries[idx]
        entries[idx] = old.copy(imageUri = uri?.takeIf { it.isNotBlank() })
        save()
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

    // ---------- 日记（独立数据，独立文件）----------

    private fun loadDiaries() {
        if (!diaryFile.exists()) return
        try {
            val arr = JSONArray(diaryFile.readText())
            diaries.clear()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                diaries.add(
                    DiaryEntry(
                        id = o.getLong("id"),
                        content = o.optString("content", ""),
                        dayStart = o.getLong("dayStart"),
                        imageUri = o.optString("imageUri", "").takeIf { it.isNotBlank() },
                        mood = o.optString("mood", "").takeIf { it.isNotBlank() }
                            ?.let { name -> runCatching { Mood.valueOf(name) }.getOrNull() }
                    )
                )
            }
        } catch (e: Exception) { e.printStackTrace() }
    }

    private fun saveDiaries() {
        try {
            val arr = JSONArray()
            for (d in diaries) {
                val o = JSONObject()
                o.put("id", d.id)
                o.put("content", d.content)
                o.put("dayStart", d.dayStart)
                o.put("imageUri", d.imageUri ?: "")
                o.put("mood", d.mood?.name ?: "")
                arr.put(o)
            }
            diaryFile.writeText(arr.toString())
        } catch (e: Exception) { e.printStackTrace() }
    }

    /** 新建（id = null）或更新一篇日记，返回写入后的条目。 */
    fun upsertDiary(
        id: Long?,
        content: String,
        dayStart: Long,
        imageUri: String? = null,
        mood: Mood? = null
    ): DiaryEntry {
        val uri = imageUri?.takeIf { it.isNotBlank() }
        val idx = id?.let { target -> diaries.indexOfFirst { it.id == target } } ?: -1
        return if (idx >= 0) {
            val updated = diaries[idx].copy(
                content = content,
                dayStart = dayStart,
                imageUri = uri,
                mood = mood
            )
            diaries[idx] = updated
            saveDiaries()
            updated
        } else {
            val entry = DiaryEntry(
                id = (diaries.maxOfOrNull { it.id } ?: 0L) + 1,
                content = content,
                dayStart = dayStart,
                imageUri = uri,
                mood = mood
            )
            diaries.add(entry)
            saveDiaries()
            entry
        }
    }

    fun deleteDiary(id: Long) {
        diaries.removeAll { it.id == id }
        saveDiaries()
    }

    fun findDiary(id: Long): DiaryEntry? = diaries.firstOrNull { it.id == id }

    fun allDiariesSorted(): List<DiaryEntry> = diaries.sortedByDescending { it.dayStart }

    /**
     * 一次性迁移：把「情绪记录里的文字」搬进日记，并把情绪记录的文字清空。
     *
     * 迁移后职责分离——情绪只记心情，日记只管正文。用 prefs 标记保证只跑一次，
     * 所以迁移过之后再写进情绪记录的文字不会被再次搬走。
     */
    private fun migrateNotesToDiary() {
        if (prefs.getBoolean(KEY_NOTES_MIGRATED, false)) return

        val (updatedEntries, newDiaries) = planNoteMigration(entries, diaries)

        if (newDiaries.isNotEmpty()) {
            entries.clear()
            entries.addAll(updatedEntries)
            save()

            diaries.addAll(newDiaries)
            saveDiaries()
        }

        prefs.edit { putBoolean(KEY_NOTES_MIGRATED, true) }
    }

    companion object {
        const val DAY_MS = 24L * 60 * 60 * 1000

        private const val KEY_NOTES_MIGRATED = "notes_migrated_to_diary_v1"

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

/* ================= 来自 NoteMigration.kt ================= */

/**
 * 算出「把情绪记录里的文字搬进日记」需要做的改动，但不落盘。
 *
 * 做成纯函数是为了能直接单元测试：这一步会清空用户已经写下的文字，属于不可逆操作，
 * 必须可验证。真正的写盘在 [Storage.migrateNotesToDiary] 里。
 *
 * @return Pair(更新后的情绪记录列表, 需要新增到日记里的条目)
 */
internal fun planNoteMigration(
    entries: List<MoodEntry>,
    existingDiaries: List<DiaryEntry>
): Pair<List<MoodEntry>, List<DiaryEntry>> {
    var nextId = (existingDiaries.maxOfOrNull { it.id } ?: 0L) + 1
    val newDiaries = mutableListOf<DiaryEntry>()

    val updatedEntries = entries.map { e ->
        if (e.note.isBlank()) {
            e
        } else {
            newDiaries.add(
                DiaryEntry(
                    id = nextId++,
                    content = e.note,
                    dayStart = e.dayStart,
                    mood = e.mood
                )
            )
            e.copy(note = "")
        }
    }

    return updatedEntries to newDiaries
}


package com.example.mooddiary.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.mooddiary.data.CardAppearance
import com.example.mooddiary.data.CustomMoodDef
import com.example.mooddiary.data.CustomMoodStyle
import com.example.mooddiary.data.Mood
import com.example.mooddiary.data.MoodEntry
import com.example.mooddiary.data.ShareSettings
import com.example.mooddiary.data.Storage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainViewModel(app: Application) : AndroidViewModel(app) {

    private val storage = Storage(app)

    private val _entries = MutableStateFlow<List<MoodEntry>>(emptyList())
    val entries: StateFlow<List<MoodEntry>> = _entries.asStateFlow()

    private val _todayEntry = MutableStateFlow<MoodEntry?>(null)
    val todayEntry: StateFlow<MoodEntry?> = _todayEntry.asStateFlow()

    private val _streak = MutableStateFlow(0)
    val streak: StateFlow<Int> = _streak.asStateFlow()

    private val _moodCounts = MutableStateFlow<Map<Mood, Int>>(emptyMap())
    val moodCounts: StateFlow<Map<Mood, Int>> = _moodCounts.asStateFlow()

    private val _cardStyle = MutableStateFlow(storage.cardStyle)
    val cardStyle: StateFlow<String> = _cardStyle.asStateFlow()

    private val _uiStyle = MutableStateFlow(storage.uiStyle)
    val uiStyle: StateFlow<String> = _uiStyle.asStateFlow()

    private val _language = MutableStateFlow(storage.language)
    val language: StateFlow<String> = _language.asStateFlow()

    private val _cardAppearance = MutableStateFlow(storage.loadCardAppearance())
    val cardAppearance: StateFlow<CardAppearance> = _cardAppearance.asStateFlow()

    private val _shareSettings = MutableStateFlow(storage.loadShareSettings())
    val shareSettings: StateFlow<ShareSettings> = _shareSettings.asStateFlow()

    private val _customMoods = MutableStateFlow(storage.loadCustomMoods())
    val customMoods: StateFlow<Map<Mood, CustomMoodStyle>> = _customMoods.asStateFlow()

    /** 用户新增的情绪列表 */
    private val _customMoodDefs = MutableStateFlow(storage.loadCustomMoodDefs())
    val customMoodDefs: StateFlow<List<CustomMoodDef>> = _customMoodDefs.asStateFlow()

    init { refresh() }

    private fun refresh() {
        _entries.value = storage.allSorted()
        _todayEntry.value = storage.findByDay(Storage.todayStart())
        _streak.value = storage.streakDays()
        _moodCounts.value = storage.moodCounts()
    }

    fun saveEntry(
        mood: Mood,
        intensity: Int,
        note: String,
        dayStart: Long = Storage.todayStart(),
        imageUri: String? = null,
        customMoodId: String? = null
    ) {
        storage.upsert(mood, intensity, note, dayStart, imageUri, customMoodId)
        refresh()
    }

    fun deleteEntry(id: Long) { storage.delete(id); refresh() }
    fun clearAll() { storage.clearAll(); refresh() }

    fun setCardStyle(style: String) {
        storage.cardStyle = style
        _cardStyle.value = style
    }

    fun setUiStyle(style: String) {
        storage.uiStyle = style
        _uiStyle.value = style
    }

    fun setLanguage(lang: String) {
        storage.language = lang
        _language.value = lang
    }

    fun updateCardAppearance(a: CardAppearance) {
        storage.saveCardAppearance(a)
        _cardAppearance.value = a
    }

    fun updateShareSettings(s: ShareSettings) {
        storage.saveShareSettings(s)
        _shareSettings.value = s
    }

    fun setCustomMood(mood: Mood, style: CustomMoodStyle) {
        val current = _customMoods.value.toMutableMap()
        if (style.isEmpty) current.remove(mood) else current[mood] = style
        storage.saveCustomMoods(current)
        _customMoods.value = current
    }

    /** 新增一个自定义情绪，返回其 id */
    fun addCustomMoodDef(emoji: String, label: String): String {
        val id = "cm_" + System.currentTimeMillis()
        val def = CustomMoodDef(id = id, emoji = emoji, label = label)
        val newList = _customMoodDefs.value + def
        storage.saveCustomMoodDefs(newList)
        _customMoodDefs.value = newList
        return id
    }

    /** 删除一个自定义情绪 */
    fun deleteCustomMoodDef(id: String) {
        val newList = _customMoodDefs.value.filter { it.id != id }
        storage.saveCustomMoodDefs(newList)
        _customMoodDefs.value = newList
    }

    fun updateEntryImage(dayStart: Long, uri: String?) {
        storage.updateImage(dayStart, uri)
        refresh()
    }

    fun findEntry(dayStart: Long): MoodEntry? = storage.findByDay(dayStart)

    companion object {
        fun todayStart(): Long = Storage.todayStart()
        fun toDayStart(millis: Long): Long = Storage.toDayStart(millis)
    }
}
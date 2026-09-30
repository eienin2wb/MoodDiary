package com.example.mooddiary.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.mooddiary.data.CardAppearance
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

    private val _cardAppearance = MutableStateFlow(storage.loadCardAppearance())
    val cardAppearance: StateFlow<CardAppearance> = _cardAppearance.asStateFlow()

    private val _shareSettings = MutableStateFlow(storage.loadShareSettings())
    val shareSettings: StateFlow<ShareSettings> = _shareSettings.asStateFlow()

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
        dayStart: Long = Storage.todayStart()
    ) {
        storage.upsert(mood, intensity, note, dayStart)
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

    fun updateCardAppearance(a: CardAppearance) {
        storage.saveCardAppearance(a)
        _cardAppearance.value = a
    }

    fun updateShareSettings(s: ShareSettings) {
        storage.saveShareSettings(s)
        _shareSettings.value = s
    }

    /** 保存自定义背景照片的 URI（同时切到 photo 背景） */
    fun setCardImageUri(uri: String?) {
        val current = _cardAppearance.value
        val updated = current.copy(
            imageUri = uri,
            background = if (uri != null) "photo" else "glass"
        )
        storage.saveCardAppearance(updated)
        _cardAppearance.value = updated
    }

    fun findEntry(dayStart: Long): MoodEntry? = storage.findByDay(dayStart)

    companion object {
        fun todayStart(): Long = Storage.todayStart()
        fun toDayStart(millis: Long): Long = Storage.toDayStart(millis)
    }
}
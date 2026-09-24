package com.example.mooddiary.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.mooddiary.data.Mood
import com.example.mooddiary.data.MoodEntry
import com.example.mooddiary.data.Storage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainViewModel(app: Application) : AndroidViewModel(app) {

    private val storage = Storage(app)

    // ---------- 状态流 ----------

    /** 所有记录（时间倒序） */
    private val _entries = MutableStateFlow<List<MoodEntry>>(emptyList())
    val entries: StateFlow<List<MoodEntry>> = _entries.asStateFlow()

    /** 今天的记录（没记则为 null） */
    private val _todayEntry = MutableStateFlow<MoodEntry?>(null)
    val todayEntry: StateFlow<MoodEntry?> = _todayEntry.asStateFlow()

    /** 连续打卡天数 */
    private val _streak = MutableStateFlow(0)
    val streak: StateFlow<Int> = _streak.asStateFlow()

    /** 各种情绪的次数 */
    private val _moodCounts = MutableStateFlow<Map<Mood, Int>>(emptyMap())
    val moodCounts: StateFlow<Map<Mood, Int>> = _moodCounts.asStateFlow()

    /** 卡片样式："plain" = 纯色，"gradient" = 渐变 */
    private val _cardStyle = MutableStateFlow(storage.cardStyle)
    val cardStyle: StateFlow<String> = _cardStyle.asStateFlow()

    init {
        refresh()
    }

    private fun refresh() {
        _entries.value = storage.allSorted()
        _todayEntry.value = storage.findByDay(Storage.todayStart())
        _streak.value = storage.streakDays()
        _moodCounts.value = storage.moodCounts()
    }

    // ---------- 业务操作 ----------

    fun saveEntry(
        mood: Mood,
        intensity: Int,
        note: String,
        dayStart: Long = Storage.todayStart()
    ) {
        storage.upsert(mood, intensity, note, dayStart)
        refresh()
    }

    fun deleteEntry(id: Long) {
        storage.delete(id)
        refresh()
    }

    /** 清空所有记录 */
    fun clearAll() {
        storage.clearAll()
        refresh()
    }

    /** 切换卡片样式（纯色 ⇄ 渐变） */
    fun setCardStyle(style: String) {
        storage.cardStyle = style
        _cardStyle.value = style
    }

    /** 查某天的记录（用于弹窗回显） */
    fun findEntry(dayStart: Long): MoodEntry? = storage.findByDay(dayStart)

    companion object {
        fun todayStart(): Long = Storage.todayStart()
        fun toDayStart(millis: Long): Long = Storage.toDayStart(millis)
    }
}
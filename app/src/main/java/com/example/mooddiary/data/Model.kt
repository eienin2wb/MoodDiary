package com.example.mooddiary.data

enum class Mood(val emoji: String, val key: String, val label: String) {
    HAPPY("😊", "mood_happy", "开心"),
    CALM("😌", "mood_calm", "平静"),
    SAD("😔", "mood_sad", "难过"),
    ANGRY("😠", "mood_angry", "生气"),
    ANXIOUS("😰", "mood_anxious", "焦虑")
}

/** 用户自定义情绪（长期保留） */
data class CustomMoodDef(
    val id: String,        // "cm_<timestamp>"
    val emoji: String,
    val label: String
)

data class MoodEntry(
    val id: Long,
    val mood: Mood,                        // 预设情绪（当 customMoodId != null 时用于兜底）
    val intensity: Int,
    val note: String,
    val dayStart: Long,
    val imageUri: String? = null,          // 记录自带背景照片
    val customMoodId: String? = null       // 若不为空，用自定义情绪
)
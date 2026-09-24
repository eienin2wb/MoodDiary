package com.example.mooddiary.data

/**
 * 五种情绪类型
 * emoji：显示的图标
 * label：中文名
 */
enum class Mood(val emoji: String, val label: String) {
    HAPPY("😊", "开心"),
    CALM("😌", "平静"),
    SAD("😔", "难过"),
    ANGRY("😠", "生气"),
    ANXIOUS("😰", "焦虑")
}

/**
 * 一条情绪日记
 * 每天一条（同一天再记会覆盖）
 *
 * @param id       自增 ID
 * @param mood     情绪类型
 * @param intensity 强度 1~5
 * @param note     文字备注（可空）
 * @param dayStart 那一天的 00:00 时间戳（用于按天查询/去重）
 */
data class MoodEntry(
    val id: Long,
    val mood: Mood,
    val intensity: Int,
    val note: String,
    val dayStart: Long
)
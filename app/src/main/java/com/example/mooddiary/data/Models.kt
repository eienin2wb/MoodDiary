package com.example.mooddiary.data


/* ================= 来自 Model.kt ================= */

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

/**
 * 日记。
 *
 * 与 [MoodEntry] 完全独立：存在自己的 diary.json 里，两者没有外键关系，
 * 也不共享 id 空间。一天可以写多篇日记，而情绪记录一天只有一条。
 */
data class DiaryEntry(
    val id: Long,
    val content: String,
    val dayStart: Long,
    val imageUri: String? = null,
    /** 可选的情绪标签，只用来决定卡片配色 / 表情；与当天的情绪记录无关。 */
    val mood: Mood? = null
)

/* ================= 来自 CardSettings.kt ================= */

/** 卡片外观设置 */
data class CardAppearance(
    val background: String = "glass",   // "glass" | "plain" | "gradient" | "photo"
    val imageUri: String? = null,       // 自定义照片 URI
    val showEmoji: Boolean = true,
    val showDate: Boolean = true,
    val showStars: Boolean = true,
    val cornerRadius: Int = 16,         // 8~28 dp
)

/** 分享卡片设置 */
data class ShareSettings(
    val template: String = "classic",
    val showWatermark: Boolean = true,
    val showDate: Boolean = true,
    val useCustomPhoto: Boolean = false,
)

/* ================= 来自 CustomMood.kt ================= */

/** 全局自定义情绪样式 */
data class CustomMoodStyle(
    val emoji: String? = null,
    val label: String? = null
) {
    val isEmpty: Boolean
        get() = emoji.isNullOrBlank() && label.isNullOrBlank()
}


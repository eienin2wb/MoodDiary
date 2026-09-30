package com.example.mooddiary.data

/** 全局自定义情绪样式 */
data class CustomMoodStyle(
    val emoji: String? = null,
    val label: String? = null
) {
    val isEmpty: Boolean
        get() = emoji.isNullOrBlank() && label.isNullOrBlank()
}
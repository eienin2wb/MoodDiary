package com.example.mooddiary.data

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
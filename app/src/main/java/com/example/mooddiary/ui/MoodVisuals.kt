package com.example.mooddiary.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import com.example.mooddiary.data.CardAppearance
import com.example.mooddiary.data.CustomMoodDef
import com.example.mooddiary.data.CustomMoodStyle
import com.example.mooddiary.data.Mood
import com.example.mooddiary.data.MoodEntry
import com.example.mooddiary.util.L
import com.example.mooddiary.util.cachedDateFormatter
import java.util.Date
import java.util.Locale

internal fun emojiOf(mood: Mood, customs: Map<Mood, CustomMoodStyle>): String =
    customs[mood]?.emoji?.takeIf { it.isNotBlank() } ?: mood.emoji


internal fun labelOf(mood: Mood, customs: Map<Mood, CustomMoodStyle>, lang: String): String =
    customs[mood]?.label?.takeIf { it.isNotBlank() } ?: L.t(mood.key, lang)


internal fun photoOf(entry: MoodEntry?, appearance: CardAppearance): String? =
    entry?.imageUri ?: if (appearance.background == "photo") appearance.imageUri else null


internal fun entryEmoji(
    entry: MoodEntry, customs: Map<Mood, CustomMoodStyle>,
    customMoodDefs: List<CustomMoodDef>
): String {
    val cmId = entry.customMoodId
    if (cmId != null) {
        customMoodDefs.firstOrNull { it.id == cmId }?.let { return it.emoji }
    }
    return emojiOf(entry.mood, customs)
}


internal fun entryLabel(
    entry: MoodEntry, customs: Map<Mood, CustomMoodStyle>,
    customMoodDefs: List<CustomMoodDef>, lang: String
): String {
    val cmId = entry.customMoodId
    if (cmId != null) {
        customMoodDefs.firstOrNull { it.id == cmId }?.let { return it.label }
    }
    return labelOf(entry.mood, customs, lang)
}


internal fun entryColor(entry: MoodEntry): Color =
    if (entry.customMoodId != null) Color(0xFF007AFF) else entry.mood.color()


internal fun entrySoftColor(entry: MoodEntry): Color =
    if (entry.customMoodId != null) Color(0xFFE3F2FD) else entry.mood.softColor()


internal fun entryGradient(entry: MoodEntry): List<Color> =
    if (entry.customMoodId != null) listOf(Color(0xFFB0CEFF), Color(0xFF9CD9CE))
    else entry.mood.gradientColors()


internal val photoTextShadow = Shadow(
    color = Color.Black.copy(alpha = 0.55f),
    offset = Offset(0f, 2f),
    blurRadius = 6f
)


internal fun headerDate(lang: String): String {
    val locale = when (lang) {
        "zh" -> Locale.CHINA
        "en" -> Locale.US
        "ko" -> Locale.KOREA
        "ja" -> Locale.JAPAN
        "es" -> Locale.forLanguageTag("es-ES")
        else -> Locale.CHINA
    }
    return cachedDateFormatter("EEEE, MMM d", locale).format(Date())
}


internal fun starString(n: Int): String {
    val c = n.coerceIn(0, 5)
    return "★".repeat(c) + "☆".repeat(5 - c)
}


fun Mood.color(): Color = when (this) {
    Mood.HAPPY -> Color(0xFFF5A623)
    Mood.CALM -> Color(0xFF4CAF93)
    Mood.SAD -> Color(0xFF5B7DB1)
    Mood.ANGRY -> Color(0xFFE05353)
    Mood.ANXIOUS -> Color(0xFF9C6BC7)
}


fun Mood.softColor(): Color = when (this) {
    Mood.HAPPY -> Color(0xFFFFF3DC)
    Mood.CALM -> Color(0xFFE0F4F0)
    Mood.SAD -> Color(0xFFE4ECF7)
    Mood.ANGRY -> Color(0xFFFFE5E5)
    Mood.ANXIOUS -> Color(0xFFF2E4FA)
}


fun Mood.gradientColors(): List<Color> = when (this) {
    Mood.HAPPY -> listOf(Color(0xFFFFE4A1), Color(0xFFFFC06C))
    Mood.CALM -> listOf(Color(0xFFD5F3EE), Color(0xFF9CD9CE))
    Mood.SAD -> listOf(Color(0xFFDCE7F7), Color(0xFF9AB4D9))
    Mood.ANGRY -> listOf(Color(0xFFFFDCDC), Color(0xFFF19191))
    Mood.ANXIOUS -> listOf(Color(0xFFEEDCFA), Color(0xFFC09DE0))
}


val defaultGradient: List<Color> = listOf(Color(0xFFE8EAF6), Color(0xFFBFC5E0))
/* ================= 左右滑动的情绪/日记卡片模块 ================= */



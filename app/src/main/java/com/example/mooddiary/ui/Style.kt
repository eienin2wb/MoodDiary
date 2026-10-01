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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.border
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.platform.LocalConfiguration
import android.graphics.Bitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import com.example.mooddiary.util.DEFAULT_MAX_IMAGE_EDGE
import com.example.mooddiary.util.decodeImageDownsampled
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/* ================= 来自 MoodVisuals.kt ================= */

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


internal fun isCjkLang(lang: String): Boolean =
    lang.startsWith("zh") || lang.startsWith("ja") || lang.startsWith("ko")


/** 语言 → 日期 Locale。返回的都是常量，可以安全地在 composable 里调用。 */
internal fun localeFor(lang: String): Locale = when (lang) {
    "zh" -> Locale.CHINA
    "en" -> Locale.US
    "ko" -> Locale.KOREA
    "ja" -> Locale.JAPAN
    "es" -> Locale.forLanguageTag("es-ES")
    else -> Locale.CHINA
}


private fun monthDayPattern(lang: String): String = when {
    lang.startsWith("zh") || lang.startsWith("ja") -> "M月d日"
    lang.startsWith("ko") -> "M월 d일"
    else -> "d MMM"
}


/** 日记 / 记录里显示的短日期，例如「9月30日」「9월 30일」「30 Sep」。 */
internal fun shortDateText(dayStart: Long, lang: String): String =
    cachedDateFormatter(monthDayPattern(lang), localeFor(lang)).format(Date(dayStart))


/** 带星期的完整日期，例如「2026年9月30日 星期三」。 */
internal fun fullDateText(dayStart: Long, lang: String): String {
    val pattern = when {
        lang.startsWith("zh") || lang.startsWith("ja") -> "yyyy年M月d日 EEEE"
        lang.startsWith("ko") -> "yyyy년 M월 d일 EEEE"
        else -> "EEE, d MMM yyyy"
    }
    return cachedDateFormatter(pattern, localeFor(lang)).format(Date(dayStart))
}


internal fun headerDate(lang: String): String =
    cachedDateFormatter("EEEE, MMM d", localeFor(lang)).format(Date())


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

/* ================= 来自 IosStyle.kt ================= */

object IosColor {
    val bg = Color(0xFFF2F2F7)
    val card = Color(0xFFFFFFFF)
    val primary = Color(0xFF007AFF)
    val danger = Color(0xFFFF3B30)
    val success = Color(0xFF34C759)
    val textPrimary = Color(0xFF000000)
    val textSecondary = Color(0xFF8E8E93)
    val textTertiary = Color(0xFFC7C7CC)
    val separator = Color(0xFFC6C6C8)
    val groupHeader = Color(0xFF6D6D72)
}

object IosRadius {
    val card = 24.dp
    val button = 14.dp
    val smallButton = 10.dp
    val largeButton = 20.dp
    val capsule = 999.dp
}

@Composable
fun RowScope.IosIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            icon,
            contentDescription,
            tint = IosColor.primary,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
fun IosSectionHeader(text: String, trailing: String? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text.uppercase(),
            fontSize = 12.sp,
            color = IosColor.groupHeader,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.8.sp,
            modifier = Modifier.weight(1f)
        )
        if (trailing != null) {
            Text(
                trailing,
                fontSize = 12.sp,
                color = IosColor.groupHeader,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun IosDivider(startPadding: Int = 56) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = startPadding.dp)
            .height(0.5.dp)
            .background(IosColor.separator)
    )
}

/* ================= 来自 LiquidGlass.kt ================= */

/** 玻璃边框高光：左上角最亮、中间变暗、右下角再亮，模拟光线折射。 */
private val glassBorderColors = listOf(
    Color.White.copy(alpha = 0.95f),
    Color.White.copy(alpha = 0.20f),
    Color.White.copy(alpha = 0.55f)
)

/** pro 版边框高光，比普通版更亮一些。 */
private val glassProBorderColors = listOf(
    Color.White.copy(alpha = 0.98f),
    Color.White.copy(alpha = 0.15f),
    Color.White.copy(alpha = 0.65f)
)

/**
 * iOS 27 Liquid Glass 效果
 * - 半透明玻璃底
 * - 外边框高光（左亮右暗）
 *
 * 这些函数是 `@Composable` 的，为的是把 [Brush] 用 [remember] 缓存下来：
 * 它们会在重组时被反复调用，之前每次都要重新构造一遍渐变色列表。
 */
@Composable
fun Modifier.liquidGlass(
    shape: Shape,
    tint: Color = Color.White.copy(alpha = 0.48f),
    borderWidth: Dp = 1.dp
): Modifier {
    val borderBrush = remember { Brush.linearGradient(colors = glassBorderColors) }
    return this
        .clip(shape)
        .background(tint)
        .border(width = borderWidth, brush = borderBrush, shape = shape)
}

/**
 * iOS 27 增强版玻璃：内部渐变 + 高光边框
 */
@Composable
fun Modifier.liquidGlassPro(
    shape: Shape,
    tint: Color = Color.White.copy(alpha = 0.42f)
): Modifier {
    val surfaceBrush = remember(tint) {
        Brush.verticalGradient(
            colors = listOf(
                tint.copy(alpha = tint.alpha * 1.15f),
                tint.copy(alpha = tint.alpha * 0.85f)
            )
        )
    }
    val borderBrush = remember { Brush.linearGradient(colors = glassProBorderColors) }
    return this
        .clip(shape)
        .background(surfaceBrush)
        .border(width = 1.2.dp, brush = borderBrush, shape = shape)
}

/* ================= 来自 ComposeLocale.kt ================= */

/**
 * 当前系统 Locale 的可观测版本。
 *
 * 直接在 composable 里调用 `Locale.getDefault()` 读不到 Compose 的状态，
 * 系统语言切换后 UI 不会重组，日期会停留在旧语言（lint 也会报
 * NonObservableLocale 错误）。[LocalConfiguration] 则由 Compose 监听，
 * 配置变化时会正常触发重组。
 *
 * 注意：当前 Compose 版本（1.10.4）还没有 `LocalLocale`，
 * 因此这里用 `LocalConfiguration.current.locales`。
 */
@Composable
fun rememberPlatformLocale(): Locale = LocalConfiguration.current.locales[0]

/* ================= 来自 PhotoLoader.kt ================= */

/**
 * 加载用户选择的照片，用于卡片背景 / 设置页预览。
 *
 * 相比直接 `BitmapFactory.decodeStream`：
 * - 解码放在 [Dispatchers.IO]，不再阻塞主线程；
 * - 按 `inSampleSize` 下采样，内存占用降到原来的 1/4 ~ 1/16；
 * - [produceState] 以 uri 为 key，同一个 uri 不会重复解码。
 *
 * 失败时返回 null，由调用方回退到默认外观。
 *
 * 另外 [DisposableEffect] 会在 bitmap 被替换或离开组合时回收旧图，
 * 避免大图累积导致 OOM。
 */
@Composable
fun rememberPhotoBitmap(
    uri: String?,
    maxEdge: Int = DEFAULT_MAX_IMAGE_EDGE
): Bitmap? {
    val context = LocalContext.current
    val bitmap by produceState<Bitmap?>(initialValue = null, uri, maxEdge) {
        value = if (uri.isNullOrBlank()) {
            null
        } else {
            withContext(Dispatchers.IO) { decodeImageDownsampled(context, uri.toUri(), maxEdge) }
        }
    }
    // 当 bitmap 被替换或离开组合时回收旧图
    DisposableEffect(bitmap) {
        val current = bitmap
        onDispose {
            current?.takeIf { !it.isRecycled }?.recycle()
        }
    }
    return bitmap
}
package com.example.mooddiary.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mooddiary.data.CardAppearance
import com.example.mooddiary.data.CustomMoodDef
import com.example.mooddiary.data.CustomMoodStyle
import com.example.mooddiary.data.Mood
import com.example.mooddiary.data.MoodEntry
import com.example.mooddiary.util.L
import com.example.mooddiary.util.cachedDateFormatter
import java.util.Date
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import kotlinx.coroutines.delay

/* ================= 来自 TodayCard.kt ================= */

@Composable
internal fun TodayCard(
    entry: MoodEntry?, appearance: CardAppearance, isIos: Boolean,
    lang: String, customs: Map<Mood, CustomMoodStyle>,
    customMoodDefs: List<CustomMoodDef>,
    onEdit: () -> Unit, onShare: () -> Unit
) {
    val interactionSrc = remember { MutableInteractionSource() }
    val pressed by interactionSrc.collectIsPressedAsState()
    val cardScale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessLow),
        label = "cardScale"
    )
    val cardAlpha by animateFloatAsState(
        targetValue = if (pressed) 0.92f else 1f,
        animationSpec = tween(150), label = "cardAlpha"
    )
    val radius = RoundedCornerShape(appearance.cornerRadius.dp)
    val photoUri = photoOf(entry, appearance)

    Box(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
            .graphicsLayer { scaleX = cardScale; scaleY = cardScale; alpha = cardAlpha }
    ) {
        when {
            photoUri != null -> {
                Box(modifier = Modifier.fillMaxWidth().clip(radius)
                    .clickable(interactionSource = interactionSrc, indication = null) { onEdit() }) {
                    PhotoBackground(photoUri, Modifier.matchParentSize())
                    Box(Modifier.matchParentSize().background(Color.Black.copy(alpha = 0.35f)))
                    TodayCardContent(entry, appearance, Color.White, Color.White,
                        isIos, lang, customs, customMoodDefs, onShare, photoUri)
                }
            }
            appearance.background == "glass" && isIos -> {
                Box(modifier = Modifier.fillMaxWidth()
                    .liquidGlassPro(radius, Color.White.copy(alpha = 0.50f))
                    .clickable(interactionSource = interactionSrc, indication = null) { onEdit() }) {
                    TodayCardContent(entry, appearance, IosColor.textPrimary, IosColor.primary,
                        isIos, lang, customs, customMoodDefs, onShare, null)
                }
            }
            appearance.background == "gradient" -> {
                val g = if (entry != null) entryGradient(entry) else defaultGradient
                val tc = if (entry != null) entryColor(entry) else IosColor.textPrimary
                Box(modifier = Modifier.fillMaxWidth()
                    .then(if (isIos) Modifier.liquidGlassPro(radius, Color.Transparent) else Modifier)
                    .clip(radius).background(Brush.verticalGradient(g))
                    .clickable(interactionSource = interactionSrc, indication = null) { onEdit() }) {
                    TodayCardContent(entry, appearance, tc, tc,
                        isIos, lang, customs, customMoodDefs, onShare, null)
                }
            }
            isIos -> {
                Card(modifier = Modifier.fillMaxWidth()
                    .clickable(interactionSource = interactionSrc, indication = null) { onEdit() },
                    colors = CardDefaults.cardColors(containerColor = Color.White), shape = radius) {
                    TodayCardContent(entry, appearance, IosColor.textPrimary, IosColor.primary,
                        isIos, lang, customs, customMoodDefs, onShare, null)
                }
            }
            else -> {
                Card(modifier = Modifier.fillMaxWidth()
                    .clickable(interactionSource = interactionSrc, indication = null) { onEdit() },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = radius) {
                    TodayCardContent(entry, appearance,
                        MaterialTheme.colorScheme.onPrimaryContainer,
                        MaterialTheme.colorScheme.onPrimaryContainer,
                        isIos, lang, customs, customMoodDefs, onShare, null)
                }
            }
        }
    }
}


@Composable
internal fun TodayCardContent(
    entry: MoodEntry?, appearance: CardAppearance,
    textColor: Color, accentColor: Color,
    isIos: Boolean, lang: String,
    customs: Map<Mood, CustomMoodStyle>,
    customMoodDefs: List<CustomMoodDef>,
    onShare: () -> Unit, photoUri: String?
) {
    val shadowStyle = if (photoUri != null) {
        TextStyle(shadow = photoTextShadow)
    } else {
        TextStyle.Default
    }

    val todayLocale = rememberPlatformLocale()
    Column(Modifier.padding(22.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (appearance.showDate) {
                Text(
                    L.t("today_prefix", lang) +
                            cachedDateFormatter("M月d日", todayLocale).format(Date()),
                    fontSize = 13.sp, fontWeight = FontWeight.Medium,
                    color = textColor.copy(alpha = 0.6f),
                    style = shadowStyle
                )
            }
            Spacer(Modifier.weight(1f))
            if (entry != null) {
                Box(
                    modifier = Modifier.size(36.dp).clip(CircleShape)
                        .then(if (isIos && photoUri == null)
                            Modifier.liquidGlass(CircleShape, Color.White.copy(alpha = 0.40f))
                        else Modifier)
                        .clickable { onShare() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Share, L.t("share", lang), tint = accentColor,
                        modifier = Modifier.size(18.dp))
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        if (entry == null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (appearance.showEmoji) {
                    Text("😶", fontSize = 48.sp)
                    Spacer(Modifier.width(16.dp))
                }
                Column {
                    Text(L.t("not_recorded_today", lang), fontSize = 22.sp,
                        fontWeight = FontWeight.SemiBold, color = textColor,
                        style = shadowStyle)
                    Spacer(Modifier.height(2.dp))
                    Text(L.t("tap_to_record", lang), fontSize = 14.sp,
                        color = textColor.copy(alpha = 0.6f),
                        style = shadowStyle)
                }
            }
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (appearance.showEmoji) {
                    Text(entryEmoji(entry, customs, customMoodDefs), fontSize = 56.sp)
                    Spacer(Modifier.width(16.dp))
                }
                Column(Modifier.weight(1f)) {
                    Text(entryLabel(entry, customs, customMoodDefs, lang), fontSize = 26.sp,
                        fontWeight = FontWeight.Bold, color = accentColor,
                        style = shadowStyle)
                    if (appearance.showStars) {
                        Spacer(Modifier.height(4.dp))
                        Text(starString(entry.intensity), fontSize = 18.sp, color = accentColor,
                            style = shadowStyle)
                    }
                }
            }
            if (entry.note.isNotBlank()) {
                Spacer(Modifier.height(14.dp))
                Text("「${entry.note}」", fontSize = 15.sp,
                    color = textColor.copy(alpha = 0.8f),
                    style = shadowStyle)
            }
        }
    }
}


@Composable
internal fun PhotoBackground(uri: String, modifier: Modifier = Modifier) {
    val bitmap = rememberPhotoBitmap(uri)
    bitmap?.let {
        Image(it.asImageBitmap(), null, contentScale = ContentScale.Crop, modifier = modifier)
    }
}


@Composable
internal fun StreakRow(streak: Int, isIos: Boolean, lang: String) {
    val animatedStreak by animateIntAsState(
        targetValue = streak,
        animationSpec = tween(600, easing = FastOutSlowInEasing), label = "sc"
    )
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Text("🔥", fontSize = 18.sp)
        Spacer(Modifier.width(8.dp))
        if (streak > 0) {
            Text(String.format(L.t("streak_great", lang), animatedStreak),
                fontSize = 15.sp, fontWeight = FontWeight.Medium,
                color = if (isIos) IosColor.textPrimary else MaterialTheme.colorScheme.onSurface)
        } else {
            Text(L.t("streak_none", lang), fontSize = 15.sp,
                color = if (isIos) IosColor.textSecondary else MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/* ================= 来自 Timeline.kt ================= */

@Composable
internal fun MonthHeader(
    monthLabel: String, entries: List<MoodEntry>,
    isIos: Boolean, lang: String,
    customs: Map<Mood, CustomMoodStyle>,
    customMoodDefs: List<CustomMoodDef>
) {
    val summary = remember(entries, customs, customMoodDefs) {
        val moodParts = Mood.values().mapNotNull { m ->
            val c = entries.count { it.customMoodId == null && it.mood == m }
            if (c > 0) "${emojiOf(m, customs)}$c" else null
        }
        val customParts = customMoodDefs.mapNotNull { def ->
            val c = entries.count { it.customMoodId == def.id }
            if (c > 0) "${def.emoji}$c" else null
        }
        (moodParts + customParts).joinToString("  ")
    }
    var entered by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(20); entered = true }
    val alpha by animateFloatAsState(if (entered) 1f else 0f, tween(300), label = "mhA")
    val offsetY by animateFloatAsState(if (entered) 0f else -12f,
        spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessLow), label = "mhY")
    Row(
        modifier = Modifier.fillMaxWidth()
            .graphicsLayer { this.alpha = alpha; translationY = offsetY }
            .padding(start = 24.dp, end = 16.dp, top = 24.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.width(4.dp).height(18.dp).clip(RoundedCornerShape(2.dp))
            .background(if (isIos) IosColor.primary else MaterialTheme.colorScheme.primary))
        Spacer(Modifier.width(10.dp))
        Text(monthLabel, fontSize = 17.sp, fontWeight = FontWeight.Bold,
            color = if (isIos) IosColor.textPrimary else MaterialTheme.colorScheme.onSurface)
        Spacer(Modifier.width(8.dp))
        Text(String.format(L.t("total_count", lang), entries.size), fontSize = 12.sp,
            color = if (isIos) IosColor.textSecondary else MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.weight(1f))
        if (summary.isNotEmpty()) {
            Text(summary, fontSize = 13.sp,
                color = if (isIos) IosColor.textSecondary else MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun TimelineEntryRow(
    entry: MoodEntry, isFirst: Boolean, isLast: Boolean,
    isIos: Boolean, lang: String,
    customs: Map<Mood, CustomMoodStyle>,
    customMoodDefs: List<CustomMoodDef>,
    appearance: CardAppearance,
    onEdit: () -> Unit, onShare: () -> Unit, onDelete: () -> Unit
) {
    var entered by remember { mutableStateOf(false) }
    LaunchedEffect(entry.id) { delay(40); entered = true }
    val itemAlpha by animateFloatAsState(
        targetValue = if (entered) 1f else 0f,
        animationSpec = tween(400, easing = FastOutSlowInEasing), label = "itemAlpha"
    )
    val itemOffsetX by animateFloatAsState(
        targetValue = if (entered) 0f else 40f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow),
        label = "itemOffsetX"
    )
    val itemScale by animateFloatAsState(
        targetValue = if (entered) 1f else 0.92f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = Spring.StiffnessMediumLow),
        label = "itemScale"
    )

    // 长按弹删除确认
    var showDeleteConfirm by remember { mutableStateOf(false) }

    // 日期：按语言切换格式，不再硬编码中文
    val dateText = remember(entry.dayStart, lang) {
        val pattern = when {
            lang.startsWith("zh") || lang.startsWith("ja") -> "M月d日 EEEE"
            lang.startsWith("ko") -> "M월 d일 EEEE"
            else -> "EEE, d MMM"
        }
        cachedDateFormatter(pattern, localeFor(lang)).format(Date(entry.dayStart))
    }
    val lineColor = if (isIos) Color(0xFFB8C4D8) else MaterialTheme.colorScheme.outlineVariant
    val dotHalo = if (isIos) Color.White else MaterialTheme.colorScheme.surface
    val radius = RoundedCornerShape(appearance.cornerRadius.dp)
    val photoUri = photoOf(entry, appearance)
    val moodColor = entryColor(entry)
    val moodSoft = entrySoftColor(entry)

    val lineShape = RoundedCornerShape(1.dp)

    val shadowStyle = if (photoUri != null) {
        TextStyle(shadow = photoTextShadow)
    } else {
        TextStyle.Default
    }

    val textColor = when {
        photoUri != null -> Color.White
        appearance.background == "plain" && isIos -> IosColor.textPrimary
        appearance.background == "plain" -> MaterialTheme.colorScheme.onSurfaceVariant
        appearance.background == "glass" && isIos -> IosColor.textPrimary
        appearance.background == "glass" -> MaterialTheme.colorScheme.onSurface
        appearance.background == "gradient" -> moodColor
        else -> if (isIos) IosColor.textPrimary else MaterialTheme.colorScheme.onSurface
    }
    val subColor = when {
        photoUri != null -> Color.White.copy(alpha = 0.8f)
        appearance.background == "gradient" -> moodColor.copy(alpha = 0.7f)
        else -> if (isIos) IosColor.textSecondary else MaterialTheme.colorScheme.onSurfaceVariant
    }
    val accent = when {
        photoUri != null -> Color.White
        appearance.background == "gradient" -> moodColor
        appearance.background == "glass" && isIos -> IosColor.primary
        appearance.background == "plain" && isIos -> IosColor.textPrimary
        else -> moodColor
    }

    Box(
        modifier = Modifier.fillMaxWidth()
            .graphicsLayer {
                this.alpha = itemAlpha
                translationX = itemOffsetX
                scaleX = itemScale
                scaleY = itemScale
            }
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            Box(modifier = Modifier.width(40.dp).fillMaxHeight()) {
                if (!isFirst) {
                    Box(Modifier.align(Alignment.TopCenter)
                        .width(2.dp).height(20.dp)
                        .background(lineColor, lineShape))
                }
                if (!isLast) {
                    Box(Modifier.align(Alignment.TopCenter).padding(top = 20.dp)
                        .width(2.dp).fillMaxHeight()
                        .background(lineColor, lineShape))
                }
                Box(Modifier.align(Alignment.TopCenter).padding(top = 10.dp).size(20.dp)
                    .clip(CircleShape).background(dotHalo))
                DotPulse(color = moodColor, entered = entered)
            }
            Spacer(Modifier.width(4.dp))
            Box(modifier = Modifier.weight(1f).padding(end = 16.dp, top = 4.dp, bottom = 4.dp)) {
                val cardBg: @Composable (content: @Composable () -> Unit) -> Unit = { content ->
                    when {
                        photoUri != null -> {
                            Box(Modifier.fillMaxWidth().clip(radius).combinedClickable(
                                onClick = onEdit,
                                onLongClick = { showDeleteConfirm = true }
                            )) {
                                PhotoBackground(photoUri, Modifier.matchParentSize())
                                Box(Modifier.matchParentSize().background(Color.Black.copy(alpha = 0.4f)))
                                content()
                            }
                        }
                        appearance.background == "glass" && isIos -> {
                            Box(Modifier.fillMaxWidth()
                                .liquidGlass(radius, Color.White.copy(alpha = 0.55f))
                                .combinedClickable(
                                    onClick = onEdit,
                                    onLongClick = { showDeleteConfirm = true }
                                )) { content() }
                        }
                        appearance.background == "gradient" -> {
                            val g = entryGradient(entry)
                            Box(Modifier.fillMaxWidth()
                                .then(if (isIos) Modifier.liquidGlass(radius, Color.Transparent) else Modifier)
                                .clip(radius).background(Brush.verticalGradient(g))
                                .combinedClickable(
                                    onClick = onEdit,
                                    onLongClick = { showDeleteConfirm = true }
                                )) { content() }
                        }
                        isIos -> Card(
                            modifier = Modifier.fillMaxWidth().combinedClickable(
                                onClick = onEdit,
                                onLongClick = { showDeleteConfirm = true }
                            ),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = radius
                        ) { content() }
                        else -> Card(
                            modifier = Modifier.fillMaxWidth().combinedClickable(
                                onClick = onEdit,
                                onLongClick = { showDeleteConfirm = true }
                            ),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            shape = radius
                        ) { content() }
                    }
                }
                cardBg {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp, horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.size(42.dp).clip(CircleShape)
                                .background(if (photoUri != null) Color.White.copy(alpha = 0.85f) else moodSoft),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(entryEmoji(entry, customs, customMoodDefs), fontSize = 22.sp)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(entryLabel(entry, customs, customMoodDefs, lang), fontSize = 17.sp,
                                    fontWeight = FontWeight.SemiBold, color = textColor,
                                    style = shadowStyle)
                                Spacer(Modifier.width(8.dp))
                                Text(starString(entry.intensity), fontSize = 11.sp, color = accent,
                                    style = shadowStyle)
                            }
                            Spacer(Modifier.height(2.dp))
                            Text(dateText, fontSize = 12.sp, color = subColor,
                                style = shadowStyle)
                            if (entry.note.isNotBlank()) {
                                Spacer(Modifier.height(4.dp))
                                Text(entry.note, fontSize = 14.sp,
                                    color = textColor.copy(alpha = 0.85f), maxLines = 2,
                                    style = shadowStyle)
                            }
                        }
                        Box(
                            modifier = Modifier.size(34.dp).clip(CircleShape)
                                .background(
                                    if (photoUri != null) Color.White.copy(alpha = 0.35f)
                                    else if (isIos) Color.White.copy(alpha = 0.55f)
                                    else MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                                )
                                .clickable { onShare() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Share, L.t("share", lang), tint = accent,
                                modifier = Modifier.size(16.dp))
                        }
                        Spacer(Modifier.width(2.dp))
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = subColor,
                            modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }

    // ============ 长按删除确认 ============
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            containerColor = if (isIos) Color.White.copy(alpha = 0.97f)
            else MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(if (isIos) IosRadius.largeButton else 24.dp),
            title = {
                Text(
                    L.t("delete_confirm_title", lang),
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isIos) IosColor.textPrimary
                    else MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    L.t("delete_confirm_desc", lang),
                    fontSize = 14.sp,
                    color = if (isIos) IosColor.textSecondary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    onDelete()
                }) {
                    Text(
                        L.t("delete", lang),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isIos) IosColor.danger
                        else MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(
                        L.t("cancel", lang),
                        fontSize = 16.sp,
                        color = if (isIos) IosColor.primary
                        else MaterialTheme.colorScheme.primary
                    )
                }
            }
        )
    }
}


@Composable
internal fun BoxScope.DotPulse(color: Color, entered: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "dot")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ), label = "pulse"
    )
    Box(modifier = Modifier.align(Alignment.TopCenter).padding(top = 14.dp).size(12.dp)
        .graphicsLayer {
            scaleX = 1f + pulse * 0.8f
            scaleY = 1f + pulse * 0.8f
            alpha = (1f - pulse) * 0.4f
        }
        .clip(CircleShape).background(color))
    Box(modifier = Modifier.align(Alignment.TopCenter).padding(top = 14.dp).size(12.dp)
        .clip(CircleShape).background(color))
}
package com.example.mooddiary.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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



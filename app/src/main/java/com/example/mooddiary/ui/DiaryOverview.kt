package com.example.mooddiary.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mooddiary.data.CustomMoodDef
import com.example.mooddiary.data.CustomMoodStyle
import com.example.mooddiary.data.Mood
import com.example.mooddiary.data.MoodEntry
import com.example.mooddiary.data.Storage
import com.example.mooddiary.util.L
import com.example.mooddiary.util.cachedDateFormatter
import java.util.Date
import java.util.Locale

@Composable
internal fun DiaryOverviewCard(
    entries: List<MoodEntry>,
    isIos: Boolean,
    lang: String,
    customs: Map<Mood, CustomMoodStyle>,
    customMoodDefs: List<CustomMoodDef>,
    onViewAll: () -> Unit,
    onEntryClick: (MoodEntry) -> Unit
) {
    val totalDiaries = entries.count { it.note.isNotBlank() }
    val recent7DaysCount = remember(entries) {
        val now = System.currentTimeMillis()
        val sevenDaysAgo = now - 7 * Storage.DAY_MS
        entries.count { it.dayStart >= sevenDaysAgo }
    }

    val recentDiaries = remember(entries) {
        entries.filter { it.note.isNotBlank() }.take(5)
    }

    val cardBg = if (isIos) Color.White else MaterialTheme.colorScheme.surface
    val tc = if (isIos) IosColor.textPrimary else MaterialTheme.colorScheme.onSurface
    val stc = if (isIos) IosColor.textSecondary else MaterialTheme.colorScheme.onSurfaceVariant
    val primary = if (isIos) IosColor.primary else MaterialTheme.colorScheme.primary

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isIos) 0.dp else 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(primary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("📖", fontSize = 22.sp)
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        if (lang == "zh") "我的日记" else "My Diaries",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = tc
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        if (lang == "zh")
                            "共 $totalDiaries 篇 · 近 7 天 $recent7DaysCount 篇"
                        else
                            "$totalDiaries entries · $recent7DaysCount in 7 days",
                        fontSize = 12.sp,
                        color = stc
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(primary.copy(alpha = 0.1f))
                        .clickable { onViewAll() }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            if (lang == "zh") "全部" else "All",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = primary
                        )
                        Spacer(Modifier.width(2.dp))
                        Icon(
                            Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            null,
                            tint = primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            if (recentDiaries.isNotEmpty()) {
                Spacer(Modifier.height(14.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(end = 4.dp)
                ) {
                    items(recentDiaries, key = { it.id }) { entry ->
                        DiaryPreviewChip(
                            entry = entry,
                            isIos = isIos,
                            lang = lang,
                            customs = customs,
                            customMoodDefs = customMoodDefs,
                            onClick = { onEntryClick(entry) }
                        )
                    }
                }
            } else {
                Spacer(Modifier.height(14.dp))
                Text(
                    if (lang == "zh") "还没有日记，记录今天的心情吧 ✨"
                    else "No diaries yet. Start writing today ✨",
                    fontSize = 13.sp,
                    color = stc,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}


@Composable
internal fun DiaryPreviewChip(
    entry: MoodEntry,
    isIos: Boolean,
    lang: String,
    customs: Map<Mood, CustomMoodStyle>,
    customMoodDefs: List<CustomMoodDef>,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessMediumLow),
        label = "diaryChipScale"
    )

    val moodColor = entryColor(entry)
    val dateText = cachedDateFormatter("M/d", rememberPlatformLocale()).format(Date(entry.dayStart))
    val notePreview = entry.note.take(20) + if (entry.note.length > 20) "…" else ""

    Box(
        modifier = Modifier
            .width(140.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (isIos) Color(0xFFF5F5F7)
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
            .border(
                width = 1.dp,
                color = moodColor.copy(alpha = 0.2f),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(interactionSource = interactionSource, indication = null) { onClick() }
            .padding(12.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(dateText, fontSize = 11.sp, color = if (isIos) IosColor.textSecondary
                else MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.weight(1f))
                Text(entryEmoji(entry, customs, customMoodDefs), fontSize = 16.sp)
            }
            Spacer(Modifier.height(6.dp))
            Text(
                entryLabel(entry, customs, customMoodDefs, lang),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = moodColor,
                maxLines = 1
            )
            Spacer(Modifier.height(4.dp))
            Text(
                notePreview,
                fontSize = 11.sp,
                color = if (isIos) IosColor.textSecondary
                else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                lineHeight = 14.sp
            )
        }
    }
}


@Composable
internal fun DiaryListDialog(
    entries: List<MoodEntry>,
    isIos: Boolean,
    lang: String,
    customs: Map<Mood, CustomMoodStyle>,
    customMoodDefs: List<CustomMoodDef>,
    onEntryClick: (MoodEntry) -> Unit,
    onDismiss: () -> Unit
) {
    val diaryEntries = remember(entries) {
        entries.filter { it.note.isNotBlank() }
    }

    val tc = if (isIos) IosColor.textPrimary else MaterialTheme.colorScheme.onSurface
    val stc = if (isIos) IosColor.textSecondary else MaterialTheme.colorScheme.onSurfaceVariant

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = if (isIos) Color.White.copy(alpha = 0.97f)
        else MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(if (isIos) IosRadius.largeButton else 28.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("📖", fontSize = 22.sp)
                Spacer(Modifier.width(8.dp))
                Text(
                    if (lang == "zh") "我的日记" else "My Diaries",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = tc
                )
                Spacer(Modifier.weight(1f))
                Text(
                    "${diaryEntries.size} " + if (lang == "zh") "篇" else "entries",
                    fontSize = 13.sp,
                    color = stc
                )
            }
        },
        text = {
            if (diaryEntries.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📝", fontSize = 48.sp)
                        Spacer(Modifier.height(12.dp))
                        Text(
                            if (lang == "zh") "还没有日记" else "No diaries yet",
                            fontSize = 14.sp,
                            color = stc
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 500.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(diaryEntries, key = { it.id }) { entry ->
                        DiaryListItem(
                            entry = entry,
                            isIos = isIos,
                            lang = lang,
                            customs = customs,
                            customMoodDefs = customMoodDefs,
                            onClick = { onEntryClick(entry) }
                        )
                    }
                    item { Spacer(Modifier.height(8.dp)) }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    L.t("close", lang),
                    fontSize = 17.sp,
                    color = if (isIos) IosColor.primary else MaterialTheme.colorScheme.primary
                )
            }
        }
    )
}


@Composable
internal fun DiaryListItem(
    entry: MoodEntry,
    isIos: Boolean,
    lang: String,
    customs: Map<Mood, CustomMoodStyle>,
    customMoodDefs: List<CustomMoodDef>,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMedium),
        label = "diaryItemScale"
    )

    val moodColor = entryColor(entry)
    val moodSoft = entrySoftColor(entry)
    val dateText = cachedDateFormatter("yyyy年M月d日 EEEE", Locale.CHINA).format(Date(entry.dayStart))
    val tc = if (isIos) IosColor.textPrimary else MaterialTheme.colorScheme.onSurface
    val stc = if (isIos) IosColor.textSecondary else MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (isIos) Color(0xFFF5F5F7)
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
            .clickable(interactionSource = interactionSource, indication = null) { onClick() }
            .padding(14.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(moodSoft),
            contentAlignment = Alignment.Center
        ) {
            Text(entryEmoji(entry, customs, customMoodDefs), fontSize = 22.sp)
        }
        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    entryLabel(entry, customs, customMoodDefs, lang),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = moodColor
                )
                Spacer(Modifier.width(8.dp))
                Text(starString(entry.intensity), fontSize = 10.sp, color = moodColor)
            }
            Spacer(Modifier.height(4.dp))
            Text(dateText, fontSize = 11.sp, color = stc)
            Spacer(Modifier.height(6.dp))
            Text(
                entry.note,
                fontSize = 13.sp,
                color = tc,
                maxLines = 3,
                lineHeight = 18.sp
            )
        }
        Spacer(Modifier.width(8.dp))
        Icon(
            Icons.AutoMirrored.Filled.KeyboardArrowRight,
            null,
            tint = stc,
            modifier = Modifier.size(18.dp)
        )
    }
}



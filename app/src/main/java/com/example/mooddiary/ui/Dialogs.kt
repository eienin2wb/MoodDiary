package com.example.mooddiary.ui

import android.app.DatePickerDialog as AndroidDatePicker
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mooddiary.data.CardAppearance
import com.example.mooddiary.data.CustomMoodDef
import com.example.mooddiary.data.CustomMoodStyle
import com.example.mooddiary.data.DiaryEntry
import com.example.mooddiary.data.Mood
import com.example.mooddiary.data.MoodEntry
import com.example.mooddiary.data.ShareSettings
import com.example.mooddiary.data.Storage
import com.example.mooddiary.util.CardTemplate
import com.example.mooddiary.util.ImageSaver
import com.example.mooddiary.util.L
import com.example.mooddiary.util.ShareCardGenerator
import com.example.mooddiary.util.ShareHelper
import com.example.mooddiary.util.decodeImageDownsampled
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.util.Calendar as JavaCal
import java.util.Date
import androidx.core.net.toUri

/* ================= 来自 StatsDialog.kt ================= */

data class UnifiedStat(
    val emoji: String,
    val label: String,
    val color: Color,
    val count: Int,
    val percent: Float,
    val key: String
)


@Composable
internal fun StatsDialog(
    entries: List<MoodEntry>, isIos: Boolean, lang: String,
    customs: Map<Mood, CustomMoodStyle>,
    customMoodDefs: List<CustomMoodDef>,
    onDismiss: () -> Unit
) {
    val cal = JavaCal.getInstance()
    val y = cal.get(JavaCal.YEAR)
    val m = cal.get(JavaCal.MONTH)
    val monthStart = remember(y, m) {
        JavaCal.getInstance().apply {
            set(JavaCal.YEAR, y)
            set(JavaCal.MONTH, m)
            set(JavaCal.DAY_OF_MONTH, 1)
            set(JavaCal.HOUR_OF_DAY, 0)
            set(JavaCal.MINUTE, 0)
            set(JavaCal.SECOND, 0)
            set(JavaCal.MILLISECOND, 0)
        }.timeInMillis
    }
    val monthEnd = remember(monthStart) {
        JavaCal.getInstance().apply {
            timeInMillis = monthStart
            add(JavaCal.MONTH, 1)
        }.timeInMillis
    }
    val monthEntries = remember(entries, monthStart, monthEnd) {
        entries.filter { it.dayStart in monthStart until monthEnd }
    }
    val total = monthEntries.size
    val monthLabel = remember(monthStart, lang) {
        monthLabelFormatter(lang).format(Date(monthStart))
    }

    val unifiedStats = remember(entries, customs, customMoodDefs, lang) {
        val list = mutableListOf<UnifiedStat>()

        Mood.values().forEach { mood ->
            val count = monthEntries.count { it.customMoodId == null && it.mood == mood }
            list.add(
                UnifiedStat(
                    emoji = emojiOf(mood, customs),
                    label = labelOf(mood, customs, lang),
                    color = mood.color(),
                    count = count,
                    percent = if (total > 0) count.toFloat() / total else 0f,
                    key = "preset_${mood.name}"
                )
            )
        }

        customMoodDefs.forEach { def ->
            val count = monthEntries.count { it.customMoodId == def.id }
            list.add(
                UnifiedStat(
                    emoji = def.emoji,
                    label = def.label,
                    color = Color(0xFF007AFF),
                    count = count,
                    percent = if (total > 0) count.toFloat() / total else 0f,
                    key = "custom_${def.id}"
                )
            )
        }

        list.sortedByDescending { it.count }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = if (isIos) Color.White.copy(alpha = 0.97f)
        else MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(if (isIos) IosRadius.largeButton else 28.dp),
        title = {
            Text(
                L.t("stats_title", lang) + " · " + monthLabel,
                fontSize = 19.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isIos) IosColor.textPrimary
                else MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        String.format(L.t("stats_month_count", lang), total),
                        fontSize = 15.sp,
                        color = if (isIos) IosColor.textSecondary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.weight(1f))
                    if (total > 0) {
                        Text(
                            "${monthEntries.size} / ${unifiedStats.count { it.count > 0 }} 种",
                            fontSize = 12.sp,
                            color = if (isIos) IosColor.textTertiary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (total == 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            L.t("stats_empty", lang),
                            color = if (isIos) IosColor.textSecondary
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    Spacer(Modifier.height(4.dp))
                    unifiedStats.forEachIndexed { i, stat ->
                        var grew by remember { mutableStateOf(false) }
                        LaunchedEffect(stat.key) {
                            delay(i * 80L)
                            grew = true
                        }
                        UnifiedStatBar(stat, isIos, grew)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    L.t("close", lang),
                    fontSize = 17.sp,
                    color = if (isIos) IosColor.primary
                    else MaterialTheme.colorScheme.primary
                )
            }
        }
    )
}


@Composable
internal fun UnifiedStatBar(
    stat: UnifiedStat, isIos: Boolean, grew: Boolean
) {
    val barFraction by animateFloatAsState(
        targetValue = if (grew) stat.percent.coerceIn(0f, 1f) else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "bf_${stat.key}"
    )
    val rowAlpha by animateFloatAsState(
        targetValue = if (grew) 1f else 0f,
        animationSpec = tween(300),
        label = "rowA_${stat.key}"
    )
    val rowX by animateFloatAsState(
        targetValue = if (grew) 0f else -12f,
        animationSpec = spring(
            dampingRatio = 0.6f,
            stiffness = Spring.StiffnessLow
        ),
        label = "rowX_${stat.key}"
    )

    Column(
        modifier = Modifier.graphicsLayer {
            alpha = rowAlpha
            translationX = rowX
        }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stat.emoji, fontSize = 18.sp)
            Spacer(Modifier.width(6.dp))
            Text(
                stat.label,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = if (isIos) IosColor.textPrimary
                else MaterialTheme.colorScheme.onSurface
            )
            if (stat.count == 0) {
                Spacer(Modifier.width(6.dp))
                Text(
                    "·",
                    fontSize = 12.sp,
                    color = if (isIos) IosColor.textTertiary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.weight(1f))
            Text(
                "${stat.count}  ·  ${(stat.percent * 100).toInt()}%",
                fontSize = 13.sp,
                fontWeight = if (stat.count > 0) FontWeight.SemiBold
                else FontWeight.Normal,
                color = if (stat.count > 0)
                    (if (isIos) IosColor.textSecondary
                    else MaterialTheme.colorScheme.onSurfaceVariant)
                else if (isIos) IosColor.textTertiary
                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(
                    if (isIos) IosColor.bg
                    else MaterialTheme.colorScheme.surfaceVariant
                )
        ) {
            if (stat.count > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(barFraction)
                        .clip(RoundedCornerShape(4.dp))
                        .background(stat.color)
                )
            }
        }
    }
}

/* ================= 来自 SettingsDialog.kt ================= */

@Composable
internal fun SettingsDialog(
    entries: List<MoodEntry>, isIos: Boolean, lang: String,
    onToggleStyle: () -> Unit, onPickLanguage: (String) -> Unit,
    onExport: () -> Unit, onClearAll: () -> Unit, onDismiss: () -> Unit
) {
    var showClearConfirm by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = if (isIos) Color.White.copy(alpha = 0.97f) else MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(if (isIos) IosRadius.largeButton else 28.dp),
        title = {
            Text(L.t("settings", lang), fontSize = 19.sp, fontWeight = FontWeight.SemiBold,
                color = if (isIos) IosColor.textPrimary else MaterialTheme.colorScheme.onSurface)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                LanguageRow(isIos, lang, onPickLanguage)
                DividerLine(isIos)
                SettingRow("📤", L.t("export_csv", lang),
                    String.format(L.t("export_desc", lang), entries.size),
                    isIos, null, onExport)
                DividerLine(isIos)
                SettingRow("🗑️", L.t("clear_all", lang), L.t("clear_desc", lang), isIos,
                    if (isIos) IosColor.danger else MaterialTheme.colorScheme.error,
                    { showClearConfirm = true })
                DividerLine(isIos)
                Column(modifier = Modifier.padding(horizontal = 4.dp, vertical = 12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("ℹ️", fontSize = 20.sp)
                        Spacer(Modifier.width(10.dp))
                        Text(L.t("about", lang), fontSize = 16.sp, fontWeight = FontWeight.SemiBold,
                            color = if (isIos) IosColor.textPrimary else MaterialTheme.colorScheme.onSurface)
                    }
                    Spacer(Modifier.height(6.dp))
                    val sc = if (isIos) IosColor.textSecondary else MaterialTheme.colorScheme.onSurfaceVariant
                    Text(L.t("version", lang), fontSize = 13.sp, color = sc)
                    Text(L.t("about_desc", lang), fontSize = 13.sp, color = sc)
                    Text(L.t("author", lang), fontSize = 13.sp, color = sc)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(L.t("close", lang), fontSize = 17.sp,
                    color = if (isIos) IosColor.primary else MaterialTheme.colorScheme.primary)
            }
        }
    )

    AnimatedDialog(visible = showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            containerColor = if (isIos) Color.White.copy(alpha = 0.97f) else MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(if (isIos) IosRadius.largeButton else 28.dp),
            title = {
                Text(L.t("clear_confirm", lang), fontSize = 17.sp, fontWeight = FontWeight.SemiBold,
                    color = if (isIos) IosColor.textPrimary else MaterialTheme.colorScheme.onSurface)
            },
            text = {
                Text(String.format(L.t("clear_confirm_desc", lang), entries.size), fontSize = 15.sp,
                    color = if (isIos) IosColor.textSecondary else MaterialTheme.colorScheme.onSurfaceVariant)
            },
            confirmButton = {
                TextButton(onClick = {
                    onClearAll()
                    showClearConfirm = false
                    onDismiss()
                }) {
                    Text(L.t("clear_yes", lang), fontSize = 17.sp,
                        color = if (isIos) IosColor.danger else MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text(L.t("clear_no", lang), fontSize = 17.sp,
                        color = if (isIos) IosColor.primary else MaterialTheme.colorScheme.primary)
                }
            }
        )
    }
}


@Composable
internal fun DividerLine(isIos: Boolean) {
    Box(Modifier.fillMaxWidth().padding(start = 34.dp).height(0.5.dp)
        .background(if (isIos) IosColor.separator else MaterialTheme.colorScheme.outlineVariant))
}


@Composable
internal fun SettingRow(
    emoji: String, title: String, subtitle: String,
    isIos: Boolean, titleColor: Color?, onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(IosRadius.smallButton))
            .clickable(onClick = onClick).padding(horizontal = 4.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(emoji, fontSize = 20.sp)
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold,
                color = titleColor ?: if (isIos) IosColor.textPrimary
                else MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(2.dp))
            Text(subtitle, fontSize = 13.sp,
                color = if (isIos) IosColor.textSecondary else MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null,
            tint = if (isIos) IosColor.textTertiary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp))
    }
}


@Composable
internal fun LanguageRow(isIos: Boolean, lang: String, onPick: (String) -> Unit) {
    Column(Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("🌐", fontSize = 20.sp)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(L.t("language", lang), fontSize = 16.sp, fontWeight = FontWeight.SemiBold,
                    color = if (isIos) IosColor.textPrimary else MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(2.dp))
                Text(L.languages.firstOrNull { it.first == lang }?.second ?: "中文",
                    fontSize = 13.sp,
                    color = if (isIos) IosColor.textSecondary else MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            L.languages.forEach { (code, name) ->
                val selected = code == lang
                Box(modifier = Modifier.weight(1f).clip(RoundedCornerShape(10.dp))
                    .background(
                        if (selected) (if (isIos) IosColor.primary else MaterialTheme.colorScheme.primary)
                        else if (isIos) IosColor.bg
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
                    .clickable { onPick(code) }.padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center) {
                    Text(name, fontSize = 11.sp,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (selected) Color.White
                        else if (isIos) IosColor.textPrimary
                        else MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}

/* ================= 来自 ShareDialog.kt ================= */

@Composable
fun ShareDialog(
    entry: MoodEntry,
    settings: ShareSettings,
    cardAppearance: CardAppearance,
    lang: String,
    customs: Map<Mood, CustomMoodStyle>,
    customMoodDefs: List<CustomMoodDef> = emptyList(),
    onSettingsChange: (ShareSettings) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    val template = CardTemplate.entries.firstOrNull {
        it.name.lowercase() == settings.template
    } ?: CardTemplate.NATIONAL_DAY

    val photoUri: String? = entry.imageUri
        ?: cardAppearance.imageUri?.takeIf { settings.useCustomPhoto }

    val bitmap by produceState<android.graphics.Bitmap?>(
        initialValue = null,
        entry.id, entry.imageUri, entry.mood, entry.intensity, entry.note,
        settings.template, settings.showWatermark, settings.showDate,
        settings.useCustomPhoto, cardAppearance.imageUri,
        lang, customs, customMoodDefs
    ) {
        value = withContext(Dispatchers.Default) {
            val photo = photoUri?.let { uri ->
                decodeImageDownsampled(context, uri.toUri(), maxEdge = 1080)
            }

            if (photo != null) {
                ShareCardGenerator.generateWithPhoto(
                    entry = entry,
                    photo = photo,
                    showWatermark = settings.showWatermark,
                    showDate = settings.showDate,
                    lang = lang,
                    customs = customs,
                    customMoodDefs = customMoodDefs
                )
            } else {
                ShareCardGenerator.generate(
                    entry = entry,
                    template = template,
                    showWatermark = settings.showWatermark,
                    showDate = settings.showDate,
                    lang = lang,
                    customs = customs,
                    customMoodDefs = customMoodDefs
                )
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(L.t("share_title", lang)) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (photoUri == null) {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(CardTemplate.entries.toList()) { t ->
                            val selected = t.name.lowercase() == settings.template
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(
                                        if (selected) MaterialTheme.colorScheme.primaryContainer
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .clickable {
                                        onSettingsChange(
                                            settings.copy(template = t.name.lowercase())
                                        )
                                    }
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    L.t(t.key, lang),
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = if (selected) FontWeight.Bold
                                    else FontWeight.Normal
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                }

                val preview = bitmap
                if (preview != null) {
                    Image(
                        bitmap = preview.asImageBitmap(),
                        contentDescription = "preview",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth(0.72f)
                            .aspectRatio(1080f / 1920f)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.72f)
                            .aspectRatio(1080f / 1920f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = bitmap != null,
                onClick = {
                    val bmp = bitmap
                    if (bmp != null) {
                        val uri = ImageSaver.saveToGallery(context, bmp)
                        val msg = if (uri != null) L.t("saved_to_album", lang)
                        else L.t("save_failed", lang)
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        if (uri != null) onDismiss()
                    }
                }
            ) {
                Text(L.t("save_album", lang))
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onDismiss) { Text(L.t("cancel", lang)) }
                TextButton(
                    enabled = bitmap != null,
                    onClick = {
                        val bmp = bitmap
                        if (bmp != null) {
                            ShareHelper.shareImage(context, bmp, L.t("share_title", lang))
                        }
                    }
                ) {
                    Text(L.t("share", lang))
                }
            }
        }
    )
}

/* ================= 日记编辑器 ================= */

/**
 * 写 / 改一篇日记。
 *
 * [initial] 为 null 表示新建。删除只在编辑已有日记时提供，并且要先确认。
 *
 * 支持：日期选择、情绪标签（可选）、照片（可选）、纯文本正文、字数统计。
 * 正文不关联情绪——情绪标签只是给日记加一个视觉标记，日记可以独立存在。
 */
@Composable
internal fun DiaryEditorDialog(
    initial: DiaryEntry?,
    isIos: Boolean,
    lang: String,
    customs: Map<Mood, CustomMoodStyle>,
    onSave: (content: String, dayStart: Long, imageUri: String?, mood: Mood?) -> Unit,
    onDelete: (() -> Unit)?,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    var content by remember(initial?.id) { mutableStateOf(initial?.content.orEmpty()) }
    var dayStart by remember(initial?.id) {
        mutableLongStateOf(initial?.dayStart ?: Storage.todayStart())
    }
    var imageUri by remember(initial?.id) { mutableStateOf(initial?.imageUri) }
    var mood by remember(initial?.id) { mutableStateOf(initial?.mood) }
    var confirmDelete by remember { mutableStateOf(false) }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {}
            imageUri = uri.toString()
        }
    }

    val tc = if (isIos) IosColor.textPrimary else MaterialTheme.colorScheme.onSurface
    val stc = if (isIos) IosColor.textSecondary else MaterialTheme.colorScheme.onSurfaceVariant
    val primary = if (isIos) IosColor.primary else MaterialTheme.colorScheme.primary

    val showDatePicker = {
        val cal = JavaCal.getInstance().apply { timeInMillis = dayStart }
        AndroidDatePicker(
            context,
            { _, year, month, day ->
                val picked = JavaCal.getInstance().apply {
                    set(year, month, day, 0, 0, 0)
                    set(JavaCal.MILLISECOND, 0)
                }
                dayStart = picked.timeInMillis
            },
            cal.get(JavaCal.YEAR),
            cal.get(JavaCal.MONTH),
            cal.get(JavaCal.DAY_OF_MONTH)
        ).show()
        Unit
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = if (isIos) Color.White.copy(alpha = 0.97f)
        else MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(if (isIos) IosRadius.largeButton else 28.dp),
        title = {
            Text(
                if (initial == null) L.t("diary_new", lang) else L.t("diary_edit", lang),
                fontSize = 19.sp,
                fontWeight = FontWeight.SemiBold,
                color = tc
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 560.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                // ============ 日期行 ============
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isIos) Color(0xFFF5F5F7)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        )
                        .clickable { showDatePicker() }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("📅", fontSize = 16.sp)
                    Spacer(Modifier.width(8.dp))
                    Text(fullDateText(dayStart, lang), fontSize = 14.sp, color = tc)
                }

                // ============ 情绪标签行 ============
                DiaryMoodRow(
                    selected = mood,
                    customs = customs,
                    lang = lang,
                    isIos = isIos,
                    onSelect = { mood = it }
                )

                // ============ 照片行 ============
                DiaryPhotoRow(
                    imageUri = imageUri,
                    isIos = isIos,
                    lang = lang,
                    onPick = {
                        photoPicker.launch(
                            PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageOnly
                            )
                        )
                    },
                    onClear = { imageUri = null }
                )

                // ============ 正文输入 ============
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 160.dp),
                    placeholder = {
                        Text(L.t("diary_hint", lang), fontSize = 14.sp, color = stc)
                    },
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 15.sp,
                        lineHeight = 22.sp
                    ),
                    minLines = 5
                )

                // ============ 字数统计 ============
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    if (content.isNotEmpty()) {
                        Text(
                            text = if (lang == "zh") "${content.length} 字"
                            else "${content.length} chars",
                            fontSize = 11.sp,
                            color = stc.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = content.isNotBlank(),
                onClick = { onSave(content.trim(), dayStart, imageUri, mood) }
            ) {
                Text(L.t("save", lang), fontSize = 17.sp, color = primary)
            }
        },
        dismissButton = {
            Row {
                if (onDelete != null) {
                    TextButton(onClick = { confirmDelete = true }) {
                        Text(
                            L.t("delete", lang),
                            fontSize = 17.sp,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text(L.t("cancel", lang), fontSize = 17.sp, color = stc)
                }
            }
        }
    )

    if (confirmDelete && onDelete != null) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            containerColor = if (isIos) Color.White.copy(alpha = 0.97f)
            else MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(if (isIos) IosRadius.largeButton else 28.dp),
            title = {
                Text(
                    L.t("diary_delete_confirm", lang),
                    fontSize = 17.sp,
                    color = tc
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    onDelete()
                }) {
                    Text(L.t("delete", lang), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) {
                    Text(L.t("cancel", lang))
                }
            }
        )
    }
}


/**
 * 情绪标签：5 个 emoji 横排。
 *
 * 再次点击已选中的 emoji 会取消选择（把 mood 设回 null）。
 * 情绪标签纯属视觉标记，不影响日记本身的独立性。
 */
@Composable
private fun DiaryMoodRow(
    selected: Mood?,
    customs: Map<Mood, CustomMoodStyle>,
    lang: String,
    isIos: Boolean,
    onSelect: (Mood?) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = if (lang == "zh") "心情（可选）" else "Mood (optional)",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isIos) IosColor.textSecondary
            else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Mood.values().forEach { m ->
                val isSelected = m == selected
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) m.softColor()
                            else if (isIos) Color(0xFFF5F5F7)
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        )
                        .border(
                            width = if (isSelected) 2.dp else 0.dp,
                            color = if (isSelected) m.color() else Color.Transparent,
                            shape = CircleShape
                        )
                        .clickable {
                            onSelect(if (isSelected) null else m)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(emojiOf(m, customs), fontSize = 24.sp)
                }
            }
        }
    }
}


/**
 * 照片行。
 *
 * - 未选照片：显示一枚小图标 + "添加照片"提示
 * - 已选照片：左侧显示缩略图，右侧显示"更换"文字，末尾一个 × 清除
 *
 * 整行点击 = 选择 / 更换；× 点击 = 清除。
 */
@Composable
private fun DiaryPhotoRow(
    imageUri: String?,
    isIos: Boolean,
    lang: String,
    onPick: () -> Unit,
    onClear: () -> Unit
) {
    val tc = if (isIos) IosColor.textPrimary else MaterialTheme.colorScheme.onSurface
    val stc = if (isIos) IosColor.textSecondary else MaterialTheme.colorScheme.onSurfaceVariant
    val primary = if (isIos) IosColor.primary else MaterialTheme.colorScheme.primary

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isIos) Color(0xFFF5F5F7)
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            )
            .clickable { onPick() }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (imageUri != null) {
            val bitmap = rememberPhotoBitmap(imageUri, maxEdge = 240)
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(primary.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Text("🖼️", fontSize = 20.sp)
                }
            }
            Spacer(Modifier.width(12.dp))
        } else {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(primary.copy(alpha = 0.10f)),
                contentAlignment = Alignment.Center
            ) {
                Text("🖼️", fontSize = 17.sp)
            }
            Spacer(Modifier.width(10.dp))
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (imageUri == null) {
                    if (lang == "zh") "添加照片" else "Add photo"
                } else {
                    if (lang == "zh") "更换照片" else "Change photo"
                },
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = if (imageUri == null) tc else primary
            )
            if (imageUri != null) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = if (lang == "zh") "已添加" else "Attached",
                    fontSize = 11.sp,
                    color = stc
                )
            }
        }

        if (imageUri != null) {
            Text(
                text = "×",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = if (isIos) IosColor.danger
                else MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .clickable { onClear() }
                    .padding(horizontal = 6.dp)
            )
        }
    }
}
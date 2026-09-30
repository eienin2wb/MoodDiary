package com.example.mooddiary.ui

import android.app.DatePickerDialog as AndroidDatePicker
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.mooddiary.data.CustomMoodDef
import com.example.mooddiary.data.CustomMoodStyle
import com.example.mooddiary.data.Mood
import com.example.mooddiary.data.MoodEntry
import com.example.mooddiary.util.L
import com.example.mooddiary.util.cachedDateFormatter
import java.util.Calendar as JavaCal
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch

@Composable
internal fun MoodChip(
    mood: Mood,
    selected: Boolean,
    emoji: String,
    label: String,
    isIos: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val cardScale by animateFloatAsState(
        targetValue = when {
            selected -> 1.04f
            isPressed -> 0.96f
            else -> 1f
        },
        animationSpec = spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessMediumLow),
        label = "chipScale"
    )
    val cardBg by animateColorAsState(
        targetValue = when {
            selected -> mood.softColor()
            isPressed -> (if (isIos) Color(0xFFEAEAEC) else MaterialTheme.colorScheme.surfaceVariant).copy(alpha = 0.8f)
            else -> if (isIos) Color(0xFFF5F5F7) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        },
        animationSpec = tween(220), label = "chipBg"
    )
    val borderColor by animateColorAsState(
        targetValue = if (selected) mood.color() else Color.Transparent,
        animationSpec = tween(220), label = "chipBorder"
    )
    val emojiScale by animateFloatAsState(
        targetValue = when {
            selected -> 1.18f
            isPressed -> 0.92f
            else -> 1f
        },
        animationSpec = spring(dampingRatio = 0.4f, stiffness = Spring.StiffnessMediumLow),
        label = "emojiScale"
    )
    val labelColor by animateColorAsState(
        targetValue = if (selected) mood.color()
        else if (isIos) IosColor.textSecondary
        else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(220), label = "chipText"
    )
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .graphicsLayer { scaleX = cardScale; scaleY = cardScale }
            .clip(RoundedCornerShape(14.dp)).background(cardBg)
            .border(width = if (selected) 1.5.dp else 0.dp, color = borderColor,
                shape = RoundedCornerShape(14.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() }
            .padding(vertical = 10.dp)
    ) {
        Text(emoji, fontSize = 28.sp,
            modifier = Modifier.graphicsLayer { scaleX = emojiScale; scaleY = emojiScale })
        Spacer(Modifier.height(4.dp))
        Text(label, fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = labelColor, maxLines = 1)
    }
}


@Composable
internal fun CustomMoodChip(
    def: CustomMoodDef,
    selected: Boolean,
    isIos: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onLongPress: () -> Unit
) {
    val themeColor = Color(0xFF007AFF)
    val themeSoft = Color(0xFFE3F2FD)

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val cardScale by animateFloatAsState(
        targetValue = when {
            selected -> 1.04f
            isPressed -> 0.96f
            else -> 1f
        },
        animationSpec = spring(dampingRatio = 0.5f, stiffness = Spring.StiffnessMediumLow),
        label = "cmChipScale"
    )
    val cardBg by animateColorAsState(
        targetValue = when {
            selected -> themeSoft
            isPressed -> (if (isIos) Color(0xFFEAEAEC) else MaterialTheme.colorScheme.surfaceVariant).copy(alpha = 0.8f)
            else -> if (isIos) Color(0xFFF5F5F7) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        },
        animationSpec = tween(220), label = "cmChipBg"
    )
    val borderColor by animateColorAsState(
        targetValue = if (selected) themeColor else Color.Transparent,
        animationSpec = tween(220), label = "cmChipBorder"
    )
    val emojiScale by animateFloatAsState(
        targetValue = when {
            selected -> 1.18f
            isPressed -> 0.92f
            else -> 1f
        },
        animationSpec = spring(dampingRatio = 0.4f, stiffness = Spring.StiffnessMediumLow),
        label = "cmEmojiScale"
    )
    val labelColor by animateColorAsState(
        targetValue = if (selected) themeColor
        else if (isIos) IosColor.textSecondary
        else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(220), label = "cmChipText"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .graphicsLayer { scaleX = cardScale; scaleY = cardScale }
            .clip(RoundedCornerShape(14.dp)).background(cardBg)
            .border(width = if (selected) 1.5.dp else 0.dp, color = borderColor,
                shape = RoundedCornerShape(14.dp))
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
                onLongClick = onLongPress
            )
            .padding(vertical = 10.dp, horizontal = 2.dp)
    ) {
        Text(def.emoji, fontSize = 28.sp,
            modifier = Modifier.graphicsLayer { scaleX = emojiScale; scaleY = emojiScale },
            maxLines = 1)
        Spacer(Modifier.height(4.dp))
        Text(def.label, fontSize = 11.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = labelColor, maxLines = 1)
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun EditDialog(
    initialDay: Long, isIos: Boolean, lang: String,
    customs: Map<Mood, CustomMoodStyle>,
    customMoodDefs: List<CustomMoodDef>,
    findExisting: (Long) -> MoodEntry?,
    onCustomSave: (Mood, CustomMoodStyle) -> Unit,
    onAddCustomMood: (String, String) -> String,
    onDeleteCustomMood: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: (Mood, Int, String, Long, String?, String?) -> Unit
) {
    val context = LocalContext.current
    var selectedDay by remember { mutableLongStateOf(initialDay) }
    val existing = findExisting(selectedDay)

    var mood by remember(selectedDay) { mutableStateOf(existing?.mood ?: Mood.HAPPY) }
    var selectedCustomMoodId by remember(selectedDay) {
        mutableStateOf(existing?.customMoodId)
    }
    var intensity by remember(selectedDay) {
        mutableFloatStateOf((existing?.intensity ?: 3).toFloat())
    }
    var note by remember(selectedDay) { mutableStateOf(existing?.note ?: "") }
    var photoUri by remember(selectedDay) { mutableStateOf(existing?.imageUri) }
    var customEmoji by remember(selectedDay) { mutableStateOf("") }
    var customLabel by remember(selectedDay) { mutableStateOf("") }
    var showCustom by remember(selectedDay) { mutableStateOf(false) }

    var showNewMoodDialog by remember { mutableStateOf(false) }
    var deleteMoodDef by remember { mutableStateOf<CustomMoodDef?>(null) }

    LaunchedEffect(mood, selectedDay) {
        if (selectedCustomMoodId == null) {
            val c = customs[mood]
            customEmoji = c?.emoji ?: ""
            customLabel = c?.label ?: ""
            showCustom = c != null
        }
    }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: android.net.Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (_: Exception) {}
            photoUri = uri.toString()
        }
    }

    fun showDatePicker() {
        val c = JavaCal.getInstance().apply { timeInMillis = selectedDay }
        AndroidDatePicker(context,
            { _, y, m, d ->
                val nc = JavaCal.getInstance().apply {
                    set(JavaCal.YEAR, y); set(JavaCal.MONTH, m)
                    set(JavaCal.DAY_OF_MONTH, d)
                    set(JavaCal.HOUR_OF_DAY, 0); set(JavaCal.MINUTE, 0)
                    set(JavaCal.SECOND, 0); set(JavaCal.MILLISECOND, 0)
                }
                selectedDay = nc.timeInMillis
            },
            c.get(JavaCal.YEAR), c.get(JavaCal.MONTH), c.get(JavaCal.DAY_OF_MONTH)).show()
    }

    val isToday = selectedDay == MainViewModel.todayStart()
    val dateLabel = if (isToday) L.t("today", lang)
    else cachedDateFormatter("yyyy年M月d日", Locale.CHINA).format(Date(selectedDay))

    val currentEmoji: String = if (selectedCustomMoodId != null) {
        customMoodDefs.firstOrNull { it.id == selectedCustomMoodId }?.emoji
            ?: emojiOf(mood, customs)
    } else emojiOf(mood, customs)
    val currentLabel: String = if (selectedCustomMoodId != null) {
        customMoodDefs.firstOrNull { it.id == selectedCustomMoodId }?.label
            ?: labelOf(mood, customs, lang)
    } else labelOf(mood, customs, lang)
    val currentColor: Color = if (selectedCustomMoodId != null) Color(0xFF007AFF)
    else mood.color()

    val primary = if (isIos) IosColor.primary else MaterialTheme.colorScheme.primary
    val tc = if (isIos) IosColor.textPrimary else MaterialTheme.colorScheme.onSurface
    val stc = if (isIos) IosColor.textSecondary else MaterialTheme.colorScheme.onSurfaceVariant
    val divider = if (isIos) IosColor.separator.copy(alpha = 0.4f)
    else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
    val pageBg = if (isIos) Color(0xFFF5F5FA) else MaterialTheme.colorScheme.background
    val sectionBg = if (isIos) Color.White else MaterialTheme.colorScheme.surface

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(pageBg)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(sectionBg)
                        .statusBarsPadding()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = L.t("cancel", lang),
                            tint = tc
                        )
                    }
                    Spacer(Modifier.width(4.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            if (existing == null) L.t("record_mood", lang)
                            else L.t("edit_record", lang),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = tc
                        )
                        Text(
                            dateLabel,
                            fontSize = 12.sp,
                            color = stc
                        )
                    }
                    Button(
                        onClick = {
                            onSave(mood, intensity.toInt(), note.trim(),
                                selectedDay, photoUri, selectedCustomMoodId)
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = primary),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp)
                    ) {
                        Text(
                            L.t("save", lang),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Box(Modifier.fillMaxWidth().height(0.5.dp).background(divider))

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .imePadding()
                        .padding(horizontal = 16.dp)
                        .padding(top = 16.dp, bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(
                                if (isIos) currentColor.copy(alpha = 0.08f)
                                else currentColor.copy(alpha = 0.1f)
                            )
                            .padding(horizontal = 18.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(currentEmoji, fontSize = 52.sp)
                        Spacer(Modifier.width(16.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                currentLabel,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = currentColor
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                starString(intensity.toInt()),
                                fontSize = 16.sp,
                                color = currentColor.copy(alpha = 0.85f)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(sectionBg)
                                .clickable { showDatePicker() }
                                .padding(horizontal = 14.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("📅", fontSize = 18.sp)
                            Spacer(Modifier.width(10.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    L.t("date_label", lang),
                                    fontSize = 11.sp,
                                    color = stc
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    dateLabel,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = tc,
                                    maxLines = 1
                                )
                            }
                        }

                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(sectionBg)
                                .clickable {
                                    photoPicker.launch(PickVisualMediaRequest(
                                        ActivityResultContracts.PickVisualMedia.ImageOnly))
                                }
                                .padding(horizontal = 14.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(if (photoUri == null) "🖼️" else "✅", fontSize = 18.sp)
                            Spacer(Modifier.width(10.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    if (lang == "zh") "背景照片" else "Photo",
                                    fontSize = 11.sp,
                                    color = stc
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    if (photoUri == null) "未选" else "已选",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (photoUri == null) tc else primary,
                                    maxLines = 1
                                )
                            }
                            if (photoUri != null) {
                                Text(
                                    "×",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isIos) IosColor.danger
                                    else MaterialTheme.colorScheme.error,
                                    modifier = Modifier
                                        .clickable { photoUri = null }
                                        .padding(horizontal = 4.dp)
                                )
                            }
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(sectionBg)
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(L.t("mood_prompt", lang), fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold, color = stc)
                            Spacer(Modifier.weight(1f))
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(primary.copy(alpha = 0.1f))
                                    .clickable { showNewMoodDialog = true }
                                    .padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("➕", fontSize = 11.sp)
                                Spacer(Modifier.width(4.dp))
                                Text(L.t("custom_mood_new", lang), fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold, color = primary)
                            }
                        }
                        Spacer(Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Mood.values().forEach { m ->
                                MoodChip(
                                    mood = m,
                                    selected = selectedCustomMoodId == null && m == mood,
                                    emoji = emojiOf(m, customs),
                                    label = labelOf(m, customs, lang),
                                    isIos = isIos,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    mood = m
                                    selectedCustomMoodId = null
                                }
                            }
                        }

                        if (customMoodDefs.isNotEmpty()) {
                            Spacer(Modifier.height(12.dp))
                            Text("✨ " + if (lang == "zh") "我的情绪" else "My moods",
                                fontSize = 12.sp, color = stc)
                            Spacer(Modifier.height(6.dp))
                            customMoodDefs.chunked(5).forEach { rowDefs ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    rowDefs.forEach { def ->
                                        CustomMoodChip(
                                            def = def,
                                            selected = selectedCustomMoodId == def.id,
                                            isIos = isIos,
                                            modifier = Modifier.weight(1f),
                                            onClick = { selectedCustomMoodId = def.id },
                                            onLongPress = { deleteMoodDef = def }
                                        )
                                    }
                                    repeat(5 - rowDefs.size) { Spacer(Modifier.weight(1f)) }
                                }
                                Spacer(Modifier.height(6.dp))
                            }
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(sectionBg)
                            .padding(14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("💪", fontSize = 14.sp)
                            Spacer(Modifier.width(8.dp))
                            Text(L.t("intensity_label", lang), fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold, color = stc)
                            Spacer(Modifier.weight(1f))
                            Text(
                                starString(intensity.toInt()),
                                fontSize = 16.sp,
                                color = currentColor
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Slider(
                            value = intensity,
                            onValueChange = { intensity = it },
                            valueRange = 1f..5f,
                            steps = 3,
                            colors = SliderDefaults.colors(
                                thumbColor = currentColor,
                                activeTrackColor = currentColor
                            )
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(sectionBg)
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("📓", fontSize = 14.sp)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                if (lang == "zh") "日记" else "Diary",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = stc,
                                letterSpacing = 0.6.sp
                            )
                            Spacer(Modifier.weight(1f))
                            if (note.isNotBlank()) {
                                Text(
                                    "${note.length} " + if (lang == "zh") "字" else "chars",
                                    fontSize = 11.sp,
                                    color = stc.copy(alpha = 0.6f)
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))

                        TextField(
                            value = note,
                            onValueChange = { note = it },
                            placeholder = {
                                Text(
                                    if (lang == "zh")
                                        "今天发生了什么？随便写点什么..."
                                    else
                                        "What happened today? Write anything...",
                                    fontSize = 15.sp,
                                    lineHeight = 24.sp,
                                    color = stc.copy(alpha = 0.5f)
                                )
                            },
                            textStyle = TextStyle(
                                fontSize = 15.sp,
                                lineHeight = 24.sp,
                                color = tc
                            ),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent,
                                cursorColor = primary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 100.dp, max = 320.dp)
                        )

                        Spacer(Modifier.height(6.dp))
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(0.5.dp)
                                .background(divider)
                        )
                    }

                    if (selectedCustomMoodId == null) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(sectionBg)
                                .padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showCustom = !showCustom },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("✏️", fontSize = 14.sp)
                                Spacer(Modifier.width(8.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        if (showCustom) L.t("custom_toggle_collapse", lang)
                                        else L.t("custom_toggle", lang),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = tc
                                    )
                                    Text(
                                        String.format(L.t("custom_scope_hint", lang),
                                            L.t(mood.key, lang)),
                                        fontSize = 11.sp,
                                        color = stc
                                    )
                                }
                                Icon(
                                    if (showCustom) Icons.Default.KeyboardArrowUp
                                    else Icons.Default.KeyboardArrowDown, null,
                                    tint = stc,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            AnimatedVisibility(
                                visible = showCustom,
                                enter = fadeIn(tween(200)) + scaleIn(
                                    initialScale = 0.92f,
                                    animationSpec = spring(dampingRatio = 0.7f,
                                        stiffness = Spring.StiffnessMediumLow)),
                                exit = fadeOut(tween(150)) + scaleOut(
                                    targetScale = 0.92f, animationSpec = tween(150))
                            ) {
                                Column(
                                    modifier = Modifier.padding(top = 12.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        OutlinedTextField(
                                            value = customEmoji,
                                            onValueChange = { customEmoji = it },
                                            label = { Text(L.t("custom_icon_label", lang), fontSize = 11.sp) },
                                            placeholder = { Text(mood.emoji, fontSize = 16.sp) },
                                            singleLine = true,
                                            shape = RoundedCornerShape(12.dp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = primary,
                                                unfocusedBorderColor = if (isIos) IosColor.separator
                                                else MaterialTheme.colorScheme.outline,
                                                focusedContainerColor = pageBg,
                                                unfocusedContainerColor = pageBg
                                            ),
                                            modifier = Modifier.width(100.dp)
                                        )
                                        OutlinedTextField(
                                            value = customLabel,
                                            onValueChange = { customLabel = it },
                                            label = { Text(L.t("custom_name_label", lang), fontSize = 11.sp) },
                                            placeholder = { Text(L.t(mood.key, lang), fontSize = 12.sp) },
                                            singleLine = true,
                                            shape = RoundedCornerShape(12.dp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = primary,
                                                unfocusedBorderColor = if (isIos) IosColor.separator
                                                else MaterialTheme.colorScheme.outline,
                                                focusedContainerColor = pageBg,
                                                unfocusedContainerColor = pageBg
                                            ),
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    Row(modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Button(
                                            onClick = {
                                                onCustomSave(mood, CustomMoodStyle(
                                                    customEmoji.takeIf { it.isNotBlank() },
                                                    customLabel.takeIf { it.isNotBlank() }
                                                ))
                                            },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = primary)
                                        ) { Text(L.t("save_global", lang), fontSize = 13.sp) }
                                        OutlinedButton(
                                            onClick = {
                                                customEmoji = ""
                                                customLabel = ""
                                                onCustomSave(mood, CustomMoodStyle())
                                            },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(12.dp)
                                        ) { Text(L.t("clear", lang), fontSize = 13.sp) }
                                    }
                                }
                            }
                        }
                    }

                    if (!isToday && existing == null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(primary.copy(alpha = 0.08f))
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("💡", fontSize = 14.sp)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                String.format(L.t("backfill_hint", lang), dateLabel),
                                fontSize = 13.sp,
                                color = primary
                            )
                        }
                    }

                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }

    if (showNewMoodDialog) {
        NewMoodDialog(
            lang = lang, isIos = isIos,
            onDismiss = { showNewMoodDialog = false },
            onAdd = { emoji, label ->
                val newId = onAddCustomMood(emoji, label)
                selectedCustomMoodId = newId
                showNewMoodDialog = false
            }
        )
    }

    deleteMoodDef?.let { def ->
        AlertDialog(
            onDismissRequest = { deleteMoodDef = null },
            containerColor = if (isIos) Color.White.copy(alpha = 0.97f) else MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(if (isIos) IosRadius.largeButton else 28.dp),
            title = {
                Text(L.t("custom_mood_delete_confirm", lang), fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isIos) IosColor.textPrimary else MaterialTheme.colorScheme.onSurface)
            },
            text = {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(def.emoji, fontSize = 24.sp)
                        Spacer(Modifier.width(8.dp))
                        Text(def.label, fontSize = 16.sp, fontWeight = FontWeight.SemiBold,
                            color = if (isIos) IosColor.textPrimary else MaterialTheme.colorScheme.onSurface)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(L.t("custom_mood_delete_desc", lang), fontSize = 13.sp,
                        color = if (isIos) IosColor.textSecondary else MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteCustomMood(def.id)
                    if (selectedCustomMoodId == def.id) selectedCustomMoodId = null
                    deleteMoodDef = null
                }) {
                    Text(L.t("delete", lang), fontSize = 17.sp,
                        color = if (isIos) IosColor.danger else MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteMoodDef = null }) {
                    Text(L.t("cancel", lang), fontSize = 17.sp,
                        color = if (isIos) IosColor.primary else MaterialTheme.colorScheme.primary)
                }
            }
        )
    }
}


@Composable
internal fun NewMoodDialog(
    lang: String, isIos: Boolean,
    onDismiss: () -> Unit,
    onAdd: (String, String) -> Unit
) {
    var emoji by remember { mutableStateOf("") }
    var label by remember { mutableStateOf("") }
    val valid = emoji.isNotBlank() && label.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = if (isIos) Color.White.copy(alpha = 0.97f) else MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(if (isIos) IosRadius.largeButton else 28.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("✨", fontSize = 20.sp)
                Spacer(Modifier.width(8.dp))
                Text(L.t("custom_mood_new", lang), fontSize = 19.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isIos) IosColor.textPrimary else MaterialTheme.colorScheme.onSurface)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(L.t("custom_mood_hint", lang), fontSize = 13.sp,
                    color = if (isIos) IosColor.textSecondary else MaterialTheme.colorScheme.onSurfaceVariant)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = emoji,
                        onValueChange = { emoji = it.take(2) },
                        label = { Text(L.t("custom_mood_emoji", lang), fontSize = 12.sp) },
                        placeholder = { Text("😊", fontSize = 18.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(IosRadius.button),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (isIos) IosColor.primary else MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = if (isIos) IosColor.separator else MaterialTheme.colorScheme.outline,
                            focusedContainerColor = if (isIos) IosColor.bg else MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = if (isIos) IosColor.bg else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.width(100.dp)
                    )
                    OutlinedTextField(
                        value = label,
                        onValueChange = { label = it },
                        label = { Text(L.t("custom_mood_label", lang), fontSize = 12.sp) },
                        placeholder = { Text(if (lang == "zh") "如：期待" else "e.g. Excited", fontSize = 13.sp) },
                        singleLine = true,
                        shape = RoundedCornerShape(IosRadius.button),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (isIos) IosColor.primary else MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = if (isIos) IosColor.separator else MaterialTheme.colorScheme.outline,
                            focusedContainerColor = if (isIos) IosColor.bg else MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = if (isIos) IosColor.bg else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
                if (emoji.isNotBlank() || label.isNotBlank()) {
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .clip(RoundedCornerShape(IosRadius.button))
                            .background(if (isIos) IosColor.bg else MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(L.t("custom_preview", lang), fontSize = 12.sp,
                            color = if (isIos) IosColor.textSecondary else MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.weight(1f))
                        Text(emoji.ifBlank { "?" }, fontSize = 24.sp)
                        Spacer(Modifier.width(8.dp))
                        Text(label.ifBlank { "..." }, fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isIos) IosColor.textPrimary else MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onAdd(emoji.trim(), label.trim()) },
                enabled = valid
            ) {
                Text(L.t("custom_mood_add", lang), fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isIos) IosColor.primary else MaterialTheme.colorScheme.primary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(L.t("cancel", lang), fontSize = 17.sp,
                    color = if (isIos) IosColor.primary else MaterialTheme.colorScheme.primary)
            }
        }
    )
}



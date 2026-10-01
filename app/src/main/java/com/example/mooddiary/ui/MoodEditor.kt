package com.example.mooddiary.ui

import android.app.DatePickerDialog as AndroidDatePicker
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mooddiary.data.CustomMoodDef
import com.example.mooddiary.data.CustomMoodStyle
import com.example.mooddiary.data.Mood
import com.example.mooddiary.data.MoodEntry
import com.example.mooddiary.util.L
import java.util.Calendar as JavaCal

/* ============================================================

* MoodChip
* ============================================================ */

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
    val interactionSource = remember {
        MutableInteractionSource()
    }

    val pressed by interactionSource
        .collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = when {
            selected -> 1.025f
            pressed -> 0.965f
            else -> 1f
        },
        animationSpec = spring(
            dampingRatio = 0.72f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "moodChipScale"
    )

    val selectedBackground =
        mood.color().copy(alpha = 0.11f)

    val normalBackground =
        if (isIos) {
            Color(0xFFF7F7F9)
        } else {
            MaterialTheme.colorScheme.surfaceVariant
                .copy(alpha = 0.50f)
        }

    val pressedBackground =
        if (isIos) {
            Color(0xFFECECEF)
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        }

    val background by animateColorAsState(
        targetValue = when {
            selected -> selectedBackground
            pressed -> pressedBackground
            else -> normalBackground
        },
        animationSpec = tween(150),
        label = "moodChipBackground"
    )

    val textColor by animateColorAsState(
        targetValue = if (selected) {
            mood.color()
        } else if (isIos) {
            IosColor.textSecondary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = tween(150),
        label = "moodChipTextColor"
    )

    Column(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(16.dp))
            .background(background)
            .border(
                width = if (selected) 1.5.dp else 0.dp,
                color = if (selected) {
                    mood.color().copy(alpha = 0.80f)
                } else {
                    Color.Transparent
                },
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(
                horizontal = 4.dp,
                vertical = 13.dp
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = emoji,
            fontSize = 26.sp
        )

        Spacer(
            Modifier.height(6.dp)
        )

        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (selected) {
                FontWeight.SemiBold
            } else {
                FontWeight.Medium
            },
            color = textColor,
            maxLines = 1
        )
    }
}

/* ============================================================

* CustomMoodChip
* ============================================================ */

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun CustomMoodChip(
    def: CustomMoodDef,
    selected: Boolean,
    isIos: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onLongPress: () -> Unit
) {
    val primary =
        if (isIos) {
            IosColor.primary
        } else {
            MaterialTheme.colorScheme.primary
        }

    val background =
        if (selected) {
            primary.copy(alpha = 0.10f)
        } else if (isIos) {
            Color(0xFFF7F7F9)
        } else {
            MaterialTheme.colorScheme.surfaceVariant
                .copy(alpha = 0.50f)
        }

    val interactionSource = remember {
        MutableInteractionSource()
    }

    val pressed by interactionSource
        .collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = when {
            selected -> 1.025f
            pressed -> 0.965f
            else -> 1f
        },
        animationSpec = spring(
            dampingRatio = 0.72f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "customMoodChipScale"
    )

    Column(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (pressed) {
                    primary.copy(alpha = 0.16f)
                } else {
                    background
                }
            )
            .border(
                width = if (selected) 1.5.dp else 0.dp,
                color = if (selected) {
                    primary.copy(alpha = 0.80f)
                } else {
                    Color.Transparent
                },
                shape = RoundedCornerShape(16.dp)
            )
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
                onLongClick = onLongPress
            )
            .padding(
                horizontal = 4.dp,
                vertical = 13.dp
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = def.emoji,
            fontSize = 26.sp,
            maxLines = 1
        )

        Spacer(
            Modifier.height(6.dp)
        )

        Text(
            text = def.label,
            fontSize = 11.sp,
            fontWeight = if (selected) {
                FontWeight.SemiBold
            } else {
                FontWeight.Medium
            },
            color = if (selected) {
                primary
            } else if (isIos) {
                IosColor.textSecondary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            maxLines = 1
        )
    }
}

/* ============================================================

* EditDialog （ModalBottomSheet 版）
* ============================================================ */

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
internal fun EditDialog(
    initialDay: Long,
    isIos: Boolean,
    lang: String,
    customs: Map<Mood, CustomMoodStyle>,
    customMoodDefs: List<CustomMoodDef>,
    findExisting: (Long) -> MoodEntry?,
    onCustomSave: (Mood, CustomMoodStyle) -> Unit,
    onAddCustomMood: (String, String) -> String,
    onDeleteCustomMood: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: (
        Mood,
        Int,
        String,
        Long,
        String?,
        String?
    ) -> Unit,
    onDelete: ((Long) -> Unit)? = null
) {
    val context = LocalContext.current

    var selectedDay by remember { mutableLongStateOf(initialDay) }
    val existing = findExisting(selectedDay)

    var mood by remember(selectedDay) { mutableStateOf(existing?.mood ?: Mood.HAPPY) }
    var selectedCustomMoodId by remember(selectedDay) { mutableStateOf(existing?.customMoodId) }
    var intensity by remember(selectedDay) { mutableFloatStateOf((existing?.intensity ?: 3).toFloat()) }
    var note by remember(selectedDay) { mutableStateOf(existing?.note ?: "") }
    var photoUri by remember(selectedDay) { mutableStateOf(existing?.imageUri) }

    var customEmoji by remember(selectedDay) { mutableStateOf("") }
    var customLabel by remember(selectedDay) { mutableStateOf("") }
    var showCustom by remember(selectedDay) { mutableStateOf(false) }

    var showNewMoodDialog by remember { mutableStateOf(false) }
    var deleteMoodDef by remember { mutableStateOf<CustomMoodDef?>(null) }
    var confirmDelete by remember { mutableStateOf(false) }

    LaunchedEffect(mood, selectedDay) {
        if (selectedCustomMoodId == null) {
            val custom = customs[mood]
            customEmoji = custom?.emoji ?: ""
            customLabel = custom?.label ?: ""
            showCustom = custom != null
        }
    }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: android.net.Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {}
            photoUri = uri.toString()
        }
    }

    fun showDatePicker() {
        val calendar = JavaCal.getInstance().apply { timeInMillis = selectedDay }
        AndroidDatePicker(
            context,
            { _, year, month, day ->
                val newCalendar = JavaCal.getInstance().apply {
                    set(JavaCal.YEAR, year)
                    set(JavaCal.MONTH, month)
                    set(JavaCal.DAY_OF_MONTH, day)
                    set(JavaCal.HOUR_OF_DAY, 0)
                    set(JavaCal.MINUTE, 0)
                    set(JavaCal.SECOND, 0)
                    set(JavaCal.MILLISECOND, 0)
                }
                selectedDay = newCalendar.timeInMillis
            },
            calendar.get(JavaCal.YEAR),
            calendar.get(JavaCal.MONTH),
            calendar.get(JavaCal.DAY_OF_MONTH)
        ).show()
    }

    val isToday = selectedDay == MainViewModel.todayStart()

    val dateLabel =
        if (isToday) L.t("today", lang)
        else fullDateText(selectedDay, lang)

    val currentCustom = selectedCustomMoodId?.let { id ->
        customMoodDefs.firstOrNull { it.id == id }
    }
    val currentEmoji = currentCustom?.emoji ?: emojiOf(mood, customs)
    val currentLabel = currentCustom?.label ?: labelOf(mood, customs, lang)

    val primary = if (isIos) IosColor.primary else MaterialTheme.colorScheme.primary
    val tc = if (isIos) IosColor.textPrimary else MaterialTheme.colorScheme.onSurface
    val stc = if (isIos) IosColor.textSecondary else MaterialTheme.colorScheme.onSurfaceVariant
    val pageBg = if (isIos) Color(0xFFF5F5FA) else MaterialTheme.colorScheme.background
    val surface = if (isIos) Color.White else MaterialTheme.colorScheme.surface
    val subtleSurface = if (isIos) Color(0xFFF7F7F9)
    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
    val divider = if (isIos) IosColor.separator.copy(alpha = 0.38f)
    else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
    val cardShape = RoundedCornerShape(20.dp)

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = pageBg,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        sheetMaxWidth = 640.dp,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 8.dp)
                    .size(width = 36.dp, height = 4.dp)
                    .clip(CircleShape)
                    .background(stc.copy(alpha = 0.30f))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
        ) {

            /* ====================================================
             * Navigation
             * ==================================================== */

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = if (existing == null) {
                            L.t("record_mood", lang)
                        } else {
                            L.t("edit_record", lang)
                        },
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = tc
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = dateLabel,
                        fontSize = 12.sp,
                        color = stc
                    )
                }

                if (existing != null && onDelete != null) {
                    IconButton(onClick = { confirmDelete = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = L.t("delete", lang),
                            tint = if (isIos) IosColor.danger
                            else MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                TextButton(
                    onClick = {
                        onSave(
                            mood,
                            intensity.toInt(),
                            note.trim(),
                            selectedDay,
                            photoUri,
                            selectedCustomMoodId
                        )
                    }
                ) {
                    Text(
                        text = L.t("save", lang),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = primary
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.5.dp)
                    .background(divider)
            )

            /* ====================================================
             * Main content
             * ==================================================== */

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(22.dp)
            ) {

                /* ============ HERO ============ */

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = if (lang == "zh") "此刻" else "Right now",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = stc
                    )
                    Spacer(Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(
                                currentColorFor(mood, selectedCustomMoodId != null, primary)
                                    .copy(alpha = 0.11f)
                            )
                            .border(
                                width = 1.dp,
                                color = currentColorFor(mood, selectedCustomMoodId != null, primary)
                                    .copy(alpha = 0.16f),
                                shape = RoundedCornerShape(24.dp)
                            )
                            .padding(horizontal = 20.dp, vertical = 20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(68.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .background(Color.White.copy(alpha = 0.72f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = currentEmoji, fontSize = 42.sp)
                        }
                        Spacer(Modifier.width(16.dp))
                        Column(modifier = Modifier.widthIn(min = 0.dp)) {
                            Text(
                                text = currentLabel,
                                fontSize = 23.sp,
                                fontWeight = FontWeight.Bold,
                                color = currentColorFor(mood, selectedCustomMoodId != null, primary)
                            )
                            Spacer(Modifier.height(5.dp))
                            Text(
                                text = starString(intensity.toInt()),
                                fontSize = 17.sp,
                                color = currentColorFor(mood, selectedCustomMoodId != null, primary)
                                    .copy(alpha = 0.82f)
                            )
                        }
                    }
                }

                /* ============ MOOD ============ */

                Column(modifier = Modifier.fillMaxWidth()) {
                    MoodSectionTitle(
                        title = L.t("mood_prompt", lang),
                        subtitle = if (lang == "zh") "选择最接近现在的情绪"
                        else "Choose how you feel",
                        color = tc,
                        secondaryColor = stc
                    )
                    Spacer(Modifier.height(10.dp))

                    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                        val gap = 7.dp
                        val itemWidth = (maxWidth - gap * 4) / 5

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(gap)
                        ) {
                            Mood.values().forEach { item ->
                                MoodChip(
                                    mood = item,
                                    selected = selectedCustomMoodId == null && item == mood,
                                    emoji = emojiOf(item, customs),
                                    label = labelOf(item, customs, lang),
                                    isIos = isIos,
                                    modifier = Modifier.width(itemWidth),
                                    onClick = {
                                        mood = item
                                        selectedCustomMoodId = null
                                    }
                                )
                            }
                        }
                    }

                    if (customMoodDefs.isNotEmpty()) {
                        Spacer(Modifier.height(18.dp))
                        Text(
                            text = if (lang == "zh") "我的情绪" else "My moods",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = stc
                        )
                        Spacer(Modifier.height(8.dp))

                        customMoodDefs.chunked(5).forEach { rowDefs ->
                            BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                                val gap = 7.dp
                                val itemWidth = (maxWidth - gap * 4) / 5

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(gap)
                                ) {
                                    rowDefs.forEach { def ->
                                        CustomMoodChip(
                                            def = def,
                                            selected = selectedCustomMoodId == def.id,
                                            isIos = isIos,
                                            modifier = Modifier.width(itemWidth),
                                            onClick = { selectedCustomMoodId = def.id },
                                            onLongPress = { deleteMoodDef = def }
                                        )
                                    }
                                    repeat(5 - rowDefs.size) {
                                        Spacer(Modifier.width(itemWidth))
                                    }
                                }
                            }
                            Spacer(Modifier.height(7.dp))
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    CompactAddButton(
                        text = L.t("custom_mood_new", lang),
                        primary = primary,
                        onClick = { showNewMoodDialog = true }
                    )
                }

                /* ============ INTENSITY ============ */

                MoodSectionCard(background = surface, shape = cardShape) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.widthIn(min = 0.dp)) {
                            Text(
                                text = L.t("intensity_label", lang),
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = tc
                            )
                            Spacer(Modifier.height(3.dp))
                            Text(
                                text = if (lang == "zh") "这份情绪有多强烈" else "How intense is it?",
                                fontSize = 11.sp,
                                color = stc
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = intensity.toInt().toString(),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = currentColorFor(mood, selectedCustomMoodId != null, primary)
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Slider(
                        value = intensity,
                        onValueChange = { intensity = it },
                        valueRange = 1f..5f,
                        steps = 3,
                        colors = SliderDefaults.colors(
                            thumbColor = currentColorFor(mood, selectedCustomMoodId != null, primary),
                            activeTrackColor = currentColorFor(mood, selectedCustomMoodId != null, primary)
                        )
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (lang == "zh") "较弱" else "Low",
                            fontSize = 10.sp, color = stc
                        )
                        Text(
                            text = if (lang == "zh") "较强" else "High",
                            fontSize = 10.sp, color = stc
                        )
                    }
                }

                /* ============ DETAILS ============ */

                Column(modifier = Modifier.fillMaxWidth()) {
                    MoodSectionTitle(
                        title = if (lang == "zh") "记录信息" else "Details",
                        subtitle = null,
                        color = tc,
                        secondaryColor = stc
                    )
                    Spacer(Modifier.height(10.dp))

                    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                        val gap = 9.dp
                        val itemWidth = (maxWidth - gap) / 2

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(gap)
                        ) {
                            MoodInfoCard(
                                modifier = Modifier.width(itemWidth),
                                background = surface,
                                shape = cardShape,
                                icon = "📅",
                                title = L.t("date_label", lang),
                                value = dateLabel,
                                valueColor = tc,
                                onClick = ::showDatePicker
                            )

                            MoodInfoCard(
                                modifier = Modifier.width(itemWidth),
                                background = surface,
                                shape = cardShape,
                                icon = if (photoUri == null) "🖼️" else "✓",
                                title = if (lang == "zh") "照片" else "Photo",
                                value = if (photoUri == null) {
                                    if (lang == "zh") "添加照片" else "Add photo"
                                } else {
                                    if (lang == "zh") "已添加" else "Added"
                                },
                                valueColor = if (photoUri == null) tc else primary,
                                onClick = {
                                    photoPicker.launch(
                                        PickVisualMediaRequest(
                                            ActivityResultContracts.PickVisualMedia.ImageOnly
                                        )
                                    )
                                },
                                trailing = if (photoUri != null) {
                                    {
                                        Text(
                                            text = "×",
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isIos) IosColor.danger
                                            else MaterialTheme.colorScheme.error,
                                            modifier = Modifier
                                                .clickable { photoUri = null }
                                                .padding(horizontal = 3.dp)
                                        )
                                    }
                                } else null
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 96.dp),
                        label = {
                            Text(
                                if (lang == "zh") "备注" else "Note",
                                fontSize = 11.sp
                            )
                        },
                        placeholder = {
                            Text(
                                if (lang == "zh") "记下此刻的想法…" else "Write your thoughts…",
                                fontSize = 13.sp
                            )
                        },
                        shape = RoundedCornerShape(13.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primary,
                            unfocusedBorderColor =
                                if (isIos) IosColor.separator
                                else MaterialTheme.colorScheme.outline,
                            focusedContainerColor = subtleSurface,
                            unfocusedContainerColor = subtleSurface
                        )
                    )
                }

                /* ============ CUSTOM STYLE ============ */

                if (selectedCustomMoodId == null) {
                    MoodSectionCard(background = surface, shape = cardShape) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showCustom = !showCustom },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(primary.copy(alpha = 0.10f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "✏️", fontSize = 15.sp)
                            }
                            Spacer(Modifier.width(10.dp))
                            Column(modifier = Modifier.widthIn(min = 0.dp)) {
                                Text(
                                    text = if (showCustom) {
                                        L.t("custom_toggle_collapse", lang)
                                    } else {
                                        L.t("custom_toggle", lang)
                                    },
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = tc
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = String.format(
                                        L.t("custom_scope_hint", lang),
                                        L.t(mood.key, lang)
                                    ),
                                    fontSize = 11.sp,
                                    color = stc
                                )
                            }
                            Spacer(Modifier.width(10.dp))
                            Icon(
                                imageVector = if (showCustom) {
                                    Icons.Default.KeyboardArrowUp
                                } else {
                                    Icons.Default.KeyboardArrowDown
                                },
                                contentDescription = null,
                                tint = stc,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        AnimatedVisibility(
                            visible = showCustom,
                            enter = fadeIn(tween(180)) + scaleIn(
                                initialScale = 0.97f,
                                animationSpec = spring(
                                    dampingRatio = 0.82f,
                                    stiffness = Spring.StiffnessMediumLow
                                )
                            ),
                            exit = fadeOut(tween(130)) + scaleOut(
                                targetScale = 0.97f,
                                animationSpec = tween(130)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(top = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                                    val gap = 8.dp
                                    val emojiWidth = 96.dp
                                    val labelWidth = (maxWidth - emojiWidth - gap)
                                        .coerceAtLeast(80.dp)

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(gap)
                                    ) {
                                        MoodInputField(
                                            value = customEmoji,
                                            onValueChange = { customEmoji = it.take(2) },
                                            label = L.t("custom_icon_label", lang),
                                            placeholder = mood.emoji,
                                            primary = primary,
                                            borderColor = if (isIos) IosColor.separator
                                            else MaterialTheme.colorScheme.outline,
                                            containerColor = subtleSurface,
                                            modifier = Modifier.width(emojiWidth)
                                        )
                                        MoodInputField(
                                            value = customLabel,
                                            onValueChange = { customLabel = it },
                                            label = L.t("custom_name_label", lang),
                                            placeholder = L.t(mood.key, lang),
                                            primary = primary,
                                            borderColor = if (isIos) IosColor.separator
                                            else MaterialTheme.colorScheme.outline,
                                            containerColor = subtleSurface,
                                            modifier = Modifier.width(labelWidth)
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    ActionButton(
                                        text = L.t("save_global", lang),
                                        primary = primary,
                                        filled = true,
                                        onClick = {
                                            onCustomSave(
                                                mood,
                                                CustomMoodStyle(
                                                    customEmoji.takeIf { it.isNotBlank() },
                                                    customLabel.takeIf { it.isNotBlank() }
                                                )
                                            )
                                        }
                                    )
                                    ActionButton(
                                        text = L.t("clear", lang),
                                        primary = primary,
                                        filled = false,
                                        onClick = {
                                            customEmoji = ""
                                            customLabel = ""
                                            onCustomSave(mood, CustomMoodStyle())
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                /* ============ BACKFILL HINT ============ */

                if (!isToday && existing == null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(primary.copy(alpha = 0.075f))
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "💡", fontSize = 15.sp)
                        Spacer(Modifier.width(9.dp))
                        Text(
                            text = String.format(
                                L.t("backfill_hint", lang),
                                dateLabel
                            ),
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            color = primary
                        )
                    }
                }

                Spacer(
                    Modifier
                        .navigationBarsPadding()
                        .height(16.dp)
                )
            }
        }
    }

    /* ============================================================
     * New custom mood
     * ============================================================ */

    if (showNewMoodDialog) {
        NewMoodDialog(
            lang = lang,
            isIos = isIos,
            onDismiss = { showNewMoodDialog = false },
            onAdd = { emoji, label ->
                val newId = onAddCustomMood(emoji, label)
                selectedCustomMoodId = newId
                showNewMoodDialog = false
            }
        )
    }

    /* ============================================================
     * Delete custom mood
     * ============================================================ */

    deleteMoodDef?.let { def ->
        val danger = if (isIos) IosColor.danger else MaterialTheme.colorScheme.error

        AlertDialog(
            onDismissRequest = { deleteMoodDef = null },
            containerColor = if (isIos) Color.White.copy(alpha = 0.98f)
            else MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(
                if (isIos) IosRadius.largeButton else 24.dp
            ),
            title = {
                Text(
                    text = L.t("custom_mood_delete_confirm", lang),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = tc
                )
            },
            text = {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(13.dp))
                                .background(primary.copy(alpha = 0.08f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = def.emoji, fontSize = 25.sp)
                        }
                        Spacer(Modifier.width(11.dp))
                        Text(
                            text = def.label,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = tc
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = L.t("custom_mood_delete_desc", lang),
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                        color = stc
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteCustomMood(def.id)
                        if (selectedCustomMoodId == def.id) {
                            selectedCustomMoodId = null
                        }
                        deleteMoodDef = null
                    }
                ) {
                    Text(
                        text = L.t("delete", lang),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = danger
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteMoodDef = null }) {
                    Text(
                        text = L.t("cancel", lang),
                        fontSize = 16.sp,
                        color = primary
                    )
                }
            }
        )
    }

    /* ============================================================
     * Delete current entry
     * ============================================================ */

    if (confirmDelete && onDelete != null) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            containerColor = if (isIos) Color.White.copy(alpha = 0.98f)
            else MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(
                if (isIos) IosRadius.largeButton else 24.dp
            ),
            title = {
                Text(
                    text = L.t("delete_confirm_title", lang),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = tc
                )
            },
            text = {
                Text(
                    text = L.t("delete_confirm_desc", lang),
                    fontSize = 14.sp,
                    lineHeight = 19.sp,
                    color = stc
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    onDelete.invoke(selectedDay)
                }) {
                    Text(
                        text = L.t("delete", lang),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isIos) IosColor.danger
                        else MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) {
                    Text(
                        text = L.t("cancel", lang),
                        fontSize = 16.sp,
                        color = primary
                    )
                }
            }
        )
    }
}

/* ============================================================

* NewMoodDialog
* ============================================================ */

@Composable
internal fun NewMoodDialog(
    lang: String,
    isIos: Boolean,
    onDismiss: () -> Unit,
    onAdd: (String, String) -> Unit
) {
    var emoji by remember { mutableStateOf("") }
    var label by remember { mutableStateOf("") }

    val valid = emoji.isNotBlank() && label.isNotBlank()

    val primary = if (isIos) IosColor.primary else MaterialTheme.colorScheme.primary
    val tc = if (isIos) IosColor.textPrimary else MaterialTheme.colorScheme.onSurface
    val stc = if (isIos) IosColor.textSecondary else MaterialTheme.colorScheme.onSurfaceVariant
    val background = if (isIos) IosColor.bg else MaterialTheme.colorScheme.surfaceVariant

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = if (isIos) Color.White.copy(alpha = 0.98f)
        else MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(
            if (isIos) IosRadius.largeButton else 24.dp
        ),
        title = {
            Column {
                Text(text = "✨", fontSize = 26.sp)
                Spacer(Modifier.height(6.dp))
                Text(
                    text = L.t("custom_mood_new", lang),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = tc
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = L.t("custom_mood_hint", lang),
                    fontSize = 13.sp,
                    lineHeight = 19.sp,
                    color = stc
                )

                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    val gap = 8.dp
                    val emojiWidth = 96.dp
                    val labelWidth = (maxWidth - emojiWidth - gap).coerceAtLeast(80.dp)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(gap)
                    ) {
                        MoodInputField(
                            value = emoji,
                            onValueChange = { emoji = it.take(2) },
                            label = L.t("custom_mood_emoji", lang),
                            placeholder = "😊",
                            primary = primary,
                            borderColor = if (isIos) IosColor.separator
                            else MaterialTheme.colorScheme.outline,
                            containerColor = background,
                            modifier = Modifier.width(emojiWidth)
                        )
                        MoodInputField(
                            value = label,
                            onValueChange = { label = it },
                            label = L.t("custom_mood_label", lang),
                            placeholder = if (lang == "zh") "如：期待" else "e.g. Excited",
                            primary = primary,
                            borderColor = if (isIos) IosColor.separator
                            else MaterialTheme.colorScheme.outline,
                            containerColor = background,
                            modifier = Modifier.width(labelWidth)
                        )
                    }
                }

                if (emoji.isNotBlank() || label.isNotBlank()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(background)
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = L.t("custom_preview", lang),
                            fontSize = 12.sp,
                            color = stc
                        )
                        Spacer(Modifier.width(14.dp))
                        Text(
                            text = emoji.ifBlank { "?" },
                            fontSize = 25.sp
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = label.ifBlank { "..." },
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = tc
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onAdd(emoji.trim(), label.trim()) },
                enabled = valid
            ) {
                Text(
                    text = L.t("custom_mood_add", lang),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (valid) primary else stc
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = L.t("cancel", lang),
                    fontSize = 16.sp,
                    color = stc
                )
            }
        }
    )
}

/* ============================================================

* UI Components
* ============================================================ */

@Composable
private fun MoodSectionTitle(
    title: String,
    subtitle: String?,
    color: Color,
    secondaryColor: Color
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
        if (!subtitle.isNullOrBlank()) {
            Spacer(Modifier.height(3.dp))
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = secondaryColor
            )
        }
    }
}

@Composable
private fun MoodSectionCard(
    background: Color,
    shape: RoundedCornerShape,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(background)
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        content()
    }
}

@Composable
private fun MoodInfoCard(
    modifier: Modifier,
    background: Color,
    shape: RoundedCornerShape,
    icon: String,
    title: String,
    value: String,
    valueColor: Color,
    onClick: () -> Unit,
    trailing: (@Composable (() -> Unit))? = null
) {
    Row(
        modifier = modifier
            .clip(shape)
            .background(background)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(valueColor.copy(alpha = 0.09f)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = icon, fontSize = 17.sp)
        }
        Spacer(Modifier.width(9.dp))
        Column(modifier = Modifier.widthIn(min = 0.dp)) {
            Text(
                text = title,
                fontSize = 10.sp,
                color = valueColor.copy(alpha = 0.58f)
            )
            Spacer(Modifier.height(3.dp))
            Text(
                text = value,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = valueColor,
                maxLines = 1
            )
        }
        if (trailing != null) {
            Spacer(Modifier.width(4.dp))
            trailing()
        }
    }
}

@Composable
private fun CompactAddButton(
    text: String,
    primary: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(primary.copy(alpha = 0.08f))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "+",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = primary
        )
        Spacer(Modifier.width(5.dp))
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = primary
        )
    }
}

@Composable
private fun ActionButton(
    text: String,
    primary: Color,
    filled: Boolean,
    onClick: () -> Unit
) {
    if (filled) {
        Button(
            onClick = onClick,
            modifier = Modifier.height(44.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = primary)
        ) {
            Text(
                text = text,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    } else {
        OutlinedButton(
            onClick = onClick,
            modifier = Modifier.height(44.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = text,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun MoodInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    primary: Color,
    borderColor: Color,
    containerColor: Color,
    modifier: Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(text = label, fontSize = 11.sp)
        },
        placeholder = {
            Text(text = placeholder, fontSize = 13.sp)
        },
        singleLine = true,
        shape = RoundedCornerShape(13.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = primary,
            unfocusedBorderColor = borderColor,
            focusedContainerColor = containerColor,
            unfocusedContainerColor = containerColor
        ),
        modifier = modifier
    )
}

private fun currentColorFor(
    mood: Mood,
    customSelected: Boolean,
    primary: Color
): Color {
    return if (customSelected) primary else mood.color()
}
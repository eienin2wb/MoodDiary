package com.example.mooddiary.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mooddiary.data.CardAppearance
import com.example.mooddiary.data.Mood
import com.example.mooddiary.data.ShareSettings
import com.example.mooddiary.util.L

@Composable
fun CardStyleSheet(
    appearance: CardAppearance,
    shareSettings: ShareSettings,
    isIos: Boolean,
    lang: String,
    onAppearanceChange: (CardAppearance) -> Unit,
    onShareSettingsChange: (ShareSettings) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } catch (_: Exception) {}
            onAppearanceChange(appearance.copy(imageUri = uri.toString(), background = "photo"))
        }
    }

    val sheetBg = if (isIos) Color.White.copy(alpha = 0.98f) else MaterialTheme.colorScheme.surface
    val primary = if (isIos) IosColor.primary else MaterialTheme.colorScheme.primary
    val tc = if (isIos) IosColor.textPrimary else MaterialTheme.colorScheme.onSurface
    val stc = if (isIos) IosColor.textSecondary else MaterialTheme.colorScheme.onSurfaceVariant
    val divider = if (isIos) IosColor.separator.copy(alpha = 0.4f)
    else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
    val sectionBg = if (isIos) Color(0xFFF7F7FA)
    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = sheetBg,
        shape = RoundedCornerShape(if (isIos) 24.dp else 28.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🎨", fontSize = 20.sp)
                Spacer(Modifier.width(8.dp))
                Text(
                    L.t("card_style_title", lang),
                    fontSize = 19.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = tc
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 540.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // ============ 实时预览 ============
                LivePreviewCard(appearance = appearance, isIos = isIos, lang = lang)

                // ============ 卡片外观 ============
                SectionCard(
                    title = L.t("section_appearance", lang),
                    icon = "🃏",
                    tc = tc,
                    sectionBg = sectionBg
                ) {
                    // 背景
                    Text(
                        L.t("label_background", lang),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = stc,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        BackgroundTile(
                            label = L.t("bg_glass", lang),
                            icon = "💎",
                            type = "glass",
                            selected = appearance.background == "glass",
                            isIos = isIos, primary = primary, tc = tc,
                            modifier = Modifier.weight(1f)
                        ) { onAppearanceChange(appearance.copy(background = "glass")) }

                        BackgroundTile(
                            label = L.t("bg_plain", lang),
                            icon = "◻️",
                            type = "plain",
                            selected = appearance.background == "plain",
                            isIos = isIos, primary = primary, tc = tc,
                            modifier = Modifier.weight(1f)
                        ) { onAppearanceChange(appearance.copy(background = "plain")) }

                        BackgroundTile(
                            label = L.t("bg_gradient", lang),
                            icon = "🌈",
                            type = "gradient",
                            selected = appearance.background == "gradient",
                            isIos = isIos, primary = primary, tc = tc,
                            modifier = Modifier.weight(1f)
                        ) { onAppearanceChange(appearance.copy(background = "gradient")) }

                        BackgroundTile(
                            label = L.t("bg_photo", lang),
                            icon = "🖼️",
                            type = "photo",
                            selected = appearance.background == "photo",
                            isIos = isIos, primary = primary, tc = tc,
                            modifier = Modifier.weight(1f)
                        ) {
                            if (appearance.imageUri == null) {
                                picker.launch(PickVisualMediaRequest(
                                    ActivityResultContracts.PickVisualMedia.ImageOnly))
                            } else {
                                onAppearanceChange(appearance.copy(background = "photo"))
                            }
                        }
                    }

                    // 照片操作行（已选照片时）
                    if (appearance.imageUri != null) {
                        Spacer(Modifier.height(10.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.White.copy(alpha = 0.8f))
                                .clickable {
                                    picker.launch(PickVisualMediaRequest(
                                        ActivityResultContracts.PickVisualMedia.ImageOnly))
                                }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Image, null, tint = primary,
                                modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                L.t("change_photo", lang),
                                fontSize = 13.sp,
                                color = primary,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                L.t("clear_photo", lang),
                                fontSize = 12.sp,
                                color = if (isIos) IosColor.danger
                                else MaterialTheme.colorScheme.error,
                                modifier = Modifier
                                    .clickable {
                                        onAppearanceChange(
                                            appearance.copy(imageUri = null, background = "glass"))
                                    }
                                    .padding(horizontal = 6.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(14.dp))
                    Box(Modifier.fillMaxWidth().height(0.5.dp).background(divider))
                    Spacer(Modifier.height(12.dp))

                    // 显示项（双列）
                    Text(
                        if (lang == "zh") "显示内容" else "Display",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = stc,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DisplayToggle(
                            icon = "😊",
                            label = L.t("show_emoji", lang),
                            checked = appearance.showEmoji,
                            primary = primary, tc = tc, sectionBg = sectionBg,
                            modifier = Modifier.weight(1f)
                        ) { onAppearanceChange(appearance.copy(showEmoji = it)) }

                        DisplayToggle(
                            icon = "📅",
                            label = L.t("show_date", lang),
                            checked = appearance.showDate,
                            primary = primary, tc = tc, sectionBg = sectionBg,
                            modifier = Modifier.weight(1f)
                        ) { onAppearanceChange(appearance.copy(showDate = it)) }

                        DisplayToggle(
                            icon = "⭐",
                            label = L.t("show_stars", lang),
                            checked = appearance.showStars,
                            primary = primary, tc = tc, sectionBg = sectionBg,
                            modifier = Modifier.weight(1f)
                        ) { onAppearanceChange(appearance.copy(showStars = it)) }
                    }

                    Spacer(Modifier.height(14.dp))
                    Box(Modifier.fillMaxWidth().height(0.5.dp).background(divider))
                    Spacer(Modifier.height(12.dp))

                    // 圆角
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("◻️", fontSize = 14.sp)
                        Spacer(Modifier.width(6.dp))
                        Text(
                            L.t("label_radius", lang),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = stc,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            "${appearance.cornerRadius} dp",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = primary
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Slider(
                        value = appearance.cornerRadius.toFloat(),
                        onValueChange = {
                            onAppearanceChange(appearance.copy(cornerRadius = it.toInt()))
                        },
                        valueRange = 8f..28f,
                        steps = 4,
                        colors = SliderDefaults.colors(
                            thumbColor = primary,
                            activeTrackColor = primary
                        )
                    )

                    // 快捷圆角
                    Spacer(Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(8, 12, 16, 20, 24).forEach { r ->
                            val selected = appearance.cornerRadius == r
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (selected) primary.copy(alpha = 0.15f)
                                        else Color.White.copy(alpha = 0.6f)
                                    )
                                    .border(
                                        width = if (selected) 1.5.dp else 0.dp,
                                        color = if (selected) primary else Color.Transparent,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        onAppearanceChange(appearance.copy(cornerRadius = r))
                                    }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "$r",
                                    fontSize = 12.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selected) primary else stc
                                )
                            }
                        }
                    }
                }

                // ============ 分享卡片 ============
                SectionCard(
                    title = L.t("section_share_card", lang),
                    icon = "📤",
                    tc = tc,
                    sectionBg = sectionBg
                ) {
                    // 使用自定义照片
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.7f))
                            .clickable {
                                onShareSettingsChange(
                                    shareSettings.copy(useCustomPhoto = !shareSettings.useCustomPhoto))
                            }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🖼️", fontSize = 18.sp)
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                L.t("use_custom_photo", lang),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                color = tc
                            )
                            Text(
                                if (lang == "zh") "记录自带照片优先"
                                else "Entry photo takes priority",
                                fontSize = 11.sp,
                                color = stc
                            )
                        }
                        Switch(
                            checked = shareSettings.useCustomPhoto,
                            onCheckedChange = {
                                onShareSettingsChange(shareSettings.copy(useCustomPhoto = it))
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = primary
                            )
                        )
                    }

                    Spacer(Modifier.height(14.dp))
                    Box(Modifier.fillMaxWidth().height(0.5.dp).background(divider))
                    Spacer(Modifier.height(12.dp))

                    // 模板
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🎭", fontSize = 14.sp)
                        Spacer(Modifier.width(6.dp))
                        Text(
                            L.t("label_template", lang),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = stc
                        )
                    }
                    Spacer(Modifier.height(8.dp))

                    val templates = listOf(
                        Triple("tpl_national", "national_day", "national"),
                        Triple("tpl_classic", "classic", "classic"),
                        Triple("tpl_minimal", "minimal", "minimal"),
                        Triple("tpl_paper", "paper", "paper"),
                        Triple("tpl_dark", "dark", "dark"),
                        Triple("tpl_note", "note", "note")
                    )
                    templates.chunked(3).forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowItems.forEach { (labelKey, value, previewKey) ->
                                TemplateTile(
                                    label = L.t(labelKey, lang),
                                    selected = shareSettings.template == value,
                                    previewType = previewKey,
                                    primary = primary, tc = tc,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    onShareSettingsChange(shareSettings.copy(template = value))
                                }
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                    }

                    Spacer(Modifier.height(6.dp))
                    Box(Modifier.fillMaxWidth().height(0.5.dp).background(divider))
                    Spacer(Modifier.height(12.dp))

                    // 显示项
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DisplayToggle(
                            icon = "💧",
                            label = L.t("watermark", lang),
                            checked = shareSettings.showWatermark,
                            primary = primary, tc = tc, sectionBg = sectionBg,
                            modifier = Modifier.weight(1f)
                        ) { onShareSettingsChange(shareSettings.copy(showWatermark = it)) }

                        DisplayToggle(
                            icon = "📅",
                            label = L.t("show_date", lang),
                            checked = shareSettings.showDate,
                            primary = primary, tc = tc, sectionBg = sectionBg,
                            modifier = Modifier.weight(1f)
                        ) { onShareSettingsChange(shareSettings.copy(showDate = it)) }

                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = primary),
                modifier = Modifier.padding(end = 4.dp)
            ) {
                Text(
                    L.t("done", lang),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    )
}

/* ================= 实时预览卡 ================= */

@Composable
private fun LivePreviewCard(appearance: CardAppearance, isIos: Boolean, lang: String) {
    val radius = RoundedCornerShape(appearance.cornerRadius.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.55f)
                .height(120.dp)
                .clip(radius)
                .then(
                    when (appearance.background) {
                        "photo" -> {
                            if (appearance.imageUri != null) {
                                Modifier.background(Color(0xFFE8E8E8))
                            } else {
                                Modifier.background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFFEEEEEE), Color(0xFFDDDDDD))))
                            }
                        }
                        "gradient" -> Modifier.background(
                            Brush.verticalGradient(
                                listOf(Color(0xFFFFE4A1), Color(0xFFFFC06C))))
                        "glass" -> Modifier.background(
                            Brush.linearGradient(
                                listOf(Color(0xFFE8F0FF), Color(0xFFF4E8FF))))
                        else -> Modifier.background(Color(0xFFF5F5FA))
                    }
                )
                .then(
                    if (appearance.background == "glass" || appearance.background == "plain")
                        Modifier.border(1.dp, Color.White, radius)
                    else Modifier
                )
        ) {
            // 预览内容
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp),
                verticalArrangement = Arrangement.Center
            ) {
                if (appearance.showDate) {
                    Text(
                        L.t("today", lang),
                        fontSize = 9.sp,
                        color = Color(0xFF666666)
                    )
                    Spacer(Modifier.height(2.dp))
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (appearance.showEmoji) {
                        Text("😊", fontSize = 22.sp)
                        Spacer(Modifier.width(6.dp))
                    }
                    Column {
                        Text(
                            L.t(Mood.HAPPY.key, lang),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF333333)
                        )
                        if (appearance.showStars) {
                            Text(
                                "★★★★☆",
                                fontSize = 8.sp,
                                color = Color(0xFFF5A623)
                            )
                        }
                    }
                }
            }

            // 照片模式的实图预览
            if (appearance.background == "photo" && appearance.imageUri != null) {
                val bitmap = rememberPhotoBitmap(appearance.imageUri)
                bitmap?.let {
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)))
                    Column(
                        modifier = Modifier.fillMaxSize().padding(10.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        if (appearance.showDate) {
                            Text(L.t("today", lang), fontSize = 9.sp, color = Color.White.copy(alpha = 0.7f))
                            Spacer(Modifier.height(2.dp))
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (appearance.showEmoji) {
                                Text("😊", fontSize = 22.sp)
                                Spacer(Modifier.width(6.dp))
                            }
                            Column {
                                Text(L.t(Mood.HAPPY.key, lang), fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold, color = Color.White)
                                if (appearance.showStars) {
                                    Text("★★★★☆", fontSize = 8.sp,
                                        color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/* ================= 分区卡片 ================= */

@Composable
private fun SectionCard(
    title: String,
    icon: String,
    tc: Color,
    sectionBg: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(icon, fontSize = 13.sp)
            Spacer(Modifier.width(6.dp))
            Text(
                title.uppercase(),
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = tc.copy(alpha = 0.55f),
                letterSpacing = 0.8.sp
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(sectionBg)
                .padding(12.dp),
            content = content
        )
    }
}

/* ================= 背景方块 ================= */

@Composable
private fun BackgroundTile(
    label: String,
    icon: String,
    type: String,
    selected: Boolean,
    isIos: Boolean,
    primary: Color,
    tc: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .then(
                    if (selected) Modifier.border(2.dp, primary, RoundedCornerShape(12.dp))
                    else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(icon, fontSize = 22.sp)
            if (selected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Check, null, tint = Color.White,
                        modifier = Modifier.size(10.dp))
                }
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            label,
            fontSize = 10.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) primary else tc,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

/* ================= 显示开关 ================= */

@Composable
private fun DisplayToggle(
    icon: String,
    label: String,
    checked: Boolean,
    primary: Color,
    tc: Color,
    sectionBg: Color,
    modifier: Modifier = Modifier,
    onChange: (Boolean) -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.7f))
            .clickable { onChange(!checked) }
            .padding(vertical = 10.dp, horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(icon, fontSize = 18.sp)
        Spacer(Modifier.height(4.dp))
        Text(
            label,
            fontSize = 10.sp,
            fontWeight = if (checked) FontWeight.SemiBold else FontWeight.Normal,
            color = if (checked) primary else tc,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
        Spacer(Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .size(width = 24.dp, height = 4.dp)
                .clip(CircleShape)
                .background(if (checked) primary else Color.LightGray.copy(alpha = 0.5f))
        )
    }
}

/* ================= 模板方块 ================= */

@Composable
private fun TemplateTile(
    label: String,
    selected: Boolean,
    previewType: String,
    primary: Color,
    tc: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color.White)
                .then(
                    if (selected) Modifier.border(2.dp, primary, RoundedCornerShape(10.dp))
                    else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(
                        when (previewType) {
                            "national" -> Brush.verticalGradient(
                                listOf(Color(0xFFC8102E), Color(0xFF8B0000)))
                            "classic" -> Brush.verticalGradient(
                                listOf(Color(0xFFFFE9B0), Color(0xFFFFB86B)))
                            "minimal" -> Brush.verticalGradient(
                                listOf(Color(0xFFFFFFFF), Color(0xFFFFFFFF)))
                            "paper" -> Brush.verticalGradient(
                                listOf(Color(0xFFFAF6EE), Color(0xFFFAF6EE)))
                            "dark" -> Brush.verticalGradient(
                                listOf(Color(0xFF0D0D0F), Color(0xFF0D0D0F)))
                            "note" -> Brush.verticalGradient(
                                listOf(Color(0xFFFFF7D6), Color(0xFFFFF7D6)))
                            else -> Brush.linearGradient(
                                listOf(Color(0xFFEEEEEE), Color(0xFFEEEEEE)))
                        }
                    )
            ) {
                if (previewType == "minimal") {
                    Box(Modifier.fillMaxWidth().height(3.dp).background(Color(0xFFF5A623)))
                }
                if (previewType == "note") {
                    Box(Modifier.fillMaxSize().border(2.dp, Color(0xFFE0D6A8),
                        RoundedCornerShape(5.dp)))
                }
                if (previewType == "national") {
                    Text("★", color = Color(0xFFFFD700), fontSize = 12.sp,
                        modifier = Modifier.align(Alignment.TopStart).padding(2.dp))
                }
            }
            if (selected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(2.dp)
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Check, null, tint = Color.White,
                        modifier = Modifier.size(9.dp))
                }
            }
        }
        Spacer(Modifier.height(3.dp))
        Text(
            label,
            fontSize = 10.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) primary else tc,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}
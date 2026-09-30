package com.example.mooddiary.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mooddiary.data.CardAppearance
import com.example.mooddiary.data.ShareSettings

@Composable
fun CardStyleSheet(
    appearance: CardAppearance,
    shareSettings: ShareSettings,
    isIos: Boolean,
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
                    uri,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {}
            onAppearanceChange(
                appearance.copy(
                    imageUri = uri.toString(),
                    background = "photo"
                )
            )
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = if (isIos) Color.White.copy(alpha = 0.97f)
        else MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(if (isIos) IosRadius.largeButton else 28.dp),
        title = {
            Text(
                "卡片样式",
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
                    .heightIn(max = 520.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                SectionLabel("卡片外观", isIos)

                OptionLabel("背景", isIos)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    BackgroundChip("玻璃", appearance.background == "glass", isIos) {
                        onAppearanceChange(appearance.copy(background = "glass"))
                    }
                    BackgroundChip("纯色", appearance.background == "plain", isIos) {
                        onAppearanceChange(appearance.copy(background = "plain"))
                    }
                    BackgroundChip("渐变", appearance.background == "gradient", isIos) {
                        onAppearanceChange(appearance.copy(background = "gradient"))
                    }
                }
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    BackgroundChip("照片", appearance.background == "photo", isIos) {
                        if (appearance.imageUri == null) {
                            picker.launch(
                                PickVisualMediaRequest(
                                    ActivityResultContracts.PickVisualMedia.ImageOnly
                                )
                            )
                        } else {
                            onAppearanceChange(appearance.copy(background = "photo"))
                        }
                    }
                }

                Spacer(Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isIos) IosColor.bg
                        else MaterialTheme.colorScheme.surfaceVariant)
                        .clickable {
                            picker.launch(
                                PickVisualMediaRequest(
                                    ActivityResultContracts.PickVisualMedia.ImageOnly
                                )
                            )
                        }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Image,
                        contentDescription = null,
                        tint = if (isIos) IosColor.primary
                        else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            if (appearance.imageUri == null) "选择自定义照片" else "更换照片",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = if (isIos) IosColor.primary
                            else MaterialTheme.colorScheme.primary
                        )
                        if (appearance.imageUri != null) {
                            Text(
                                "已选择 · 点击更换",
                                fontSize = 12.sp,
                                color = if (isIos) IosColor.textSecondary
                                else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    if (appearance.imageUri != null) {
                        Text(
                            "清除",
                            fontSize = 13.sp,
                            color = if (isIos) IosColor.danger
                            else MaterialTheme.colorScheme.error,
                            modifier = Modifier
                                .clickable {
                                    onAppearanceChange(
                                        appearance.copy(
                                            imageUri = null,
                                            background = "glass"
                                        )
                                    )
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))

                SwitchRow("显示 emoji", appearance.showEmoji, isIos) {
                    onAppearanceChange(appearance.copy(showEmoji = it))
                }
                SwitchRow("显示日期", appearance.showDate, isIos) {
                    onAppearanceChange(appearance.copy(showDate = it))
                }
                SwitchRow("显示星星", appearance.showStars, isIos) {
                    onAppearanceChange(appearance.copy(showStars = it))
                }

                Spacer(Modifier.height(10.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    OptionLabel("圆角", isIos, Modifier.weight(1f))
                    Text(
                        "${appearance.cornerRadius} dp",
                        fontSize = 13.sp,
                        color = if (isIos) IosColor.textSecondary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Slider(
                    value = appearance.cornerRadius.toFloat(),
                    onValueChange = {
                        onAppearanceChange(appearance.copy(cornerRadius = it.toInt()))
                    },
                    valueRange = 8f..28f,
                    steps = 4,
                    colors = SliderDefaults.colors(
                        thumbColor = if (isIos) IosColor.primary
                        else MaterialTheme.colorScheme.primary,
                        activeTrackColor = if (isIos) IosColor.primary
                        else MaterialTheme.colorScheme.primary
                    )
                )

                DividerLine(isIos)

                SectionLabel("分享卡片", isIos)

                SwitchRow(
                    "使用自定义照片作为背景",
                    shareSettings.useCustomPhoto,
                    isIos
                ) { onShareSettingsChange(shareSettings.copy(useCustomPhoto = it)) }

                Spacer(Modifier.height(6.dp))

                OptionLabel("模板", isIos)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ShareTemplateChip("经典", "classic", shareSettings, isIos, onShareSettingsChange)
                    ShareTemplateChip("简约", "minimal", shareSettings, isIos, onShareSettingsChange)
                    ShareTemplateChip("杂志", "paper", shareSettings, isIos, onShareSettingsChange)
                }
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ShareTemplateChip("暗夜", "dark", shareSettings, isIos, onShareSettingsChange)
                    ShareTemplateChip("便签", "note", shareSettings, isIos, onShareSettingsChange)
                }

                Spacer(Modifier.height(12.dp))

                SwitchRow("显示底部水印", shareSettings.showWatermark, isIos) {
                    onShareSettingsChange(shareSettings.copy(showWatermark = it))
                }
                SwitchRow("显示日期", shareSettings.showDate, isIos) {
                    onShareSettingsChange(shareSettings.copy(showDate = it))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    "完成",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isIos) IosColor.primary
                    else MaterialTheme.colorScheme.primary
                )
            }
        }
    )
}

@Composable
private fun SectionLabel(text: String, isIos: Boolean) {
    Text(
        text,
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        color = if (isIos) IosColor.textSecondary
        else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
private fun OptionLabel(text: String, isIos: Boolean, modifier: Modifier = Modifier) {
    Text(
        text,
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        color = if (isIos) IosColor.textPrimary
        else MaterialTheme.colorScheme.onSurface,
        modifier = modifier.padding(bottom = 6.dp)
    )
}

@Composable
private fun RowScope.BackgroundChip(
    label: String,
    selected: Boolean,
    isIos: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (selected) (if (isIos) IosColor.primary
                else MaterialTheme.colorScheme.primary)
                else if (isIos) IosColor.bg
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) Color.White
            else if (isIos) IosColor.textPrimary
            else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun RowScope.ShareTemplateChip(
    label: String,
    value: String,
    settings: ShareSettings,
    isIos: Boolean,
    onChange: (ShareSettings) -> Unit
) {
    val selected = settings.template == value
    Box(
        modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (selected) (if (isIos) IosColor.primary
                else MaterialTheme.colorScheme.primary)
                else if (isIos) IosColor.bg
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .clickable { onChange(settings.copy(template = value)) }
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) Color.White
            else if (isIos) IosColor.textPrimary
            else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun SwitchRow(
    label: String,
    checked: Boolean,
    isIos: Boolean,
    onChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            fontSize = 14.sp,
            color = if (isIos) IosColor.textPrimary
            else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = if (isIos) IosColor.primary
                else MaterialTheme.colorScheme.primary
            )
        )
    }
}

@Composable
private fun DividerLine(isIos: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .height(0.5.dp)
            .background(
                if (isIos) IosColor.separator
                else MaterialTheme.colorScheme.outlineVariant
            )
    )
}
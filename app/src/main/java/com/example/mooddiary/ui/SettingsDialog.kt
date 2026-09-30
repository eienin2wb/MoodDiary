package com.example.mooddiary.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mooddiary.data.MoodEntry
import com.example.mooddiary.util.L

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



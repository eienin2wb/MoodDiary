package com.example.mooddiary.ui

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.example.mooddiary.data.CardAppearance
import com.example.mooddiary.data.CustomMoodDef
import com.example.mooddiary.data.CustomMoodStyle
import com.example.mooddiary.data.Mood
import com.example.mooddiary.data.MoodEntry
import com.example.mooddiary.data.ShareSettings
import com.example.mooddiary.util.CardTemplate
import com.example.mooddiary.util.ImageSaver
import com.example.mooddiary.util.L
import com.example.mooddiary.util.ShareCardGenerator
import com.example.mooddiary.util.ShareHelper
import com.example.mooddiary.util.decodeImageDownsampled
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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

    // 优先级：记录自带照片 > 全局照片（当 useCustomPhoto 开启时）
    val photoUri: String? = entry.imageUri
        ?: cardAppearance.imageUri?.takeIf { settings.useCustomPhoto }

    // 生成分享图：解码原图 + 绘制 1080×1920 位图都不便宜，
    // 放到后台线程去做，否则每次打开对话框都会明显卡一下。
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
                // 只有纯模板模式才显示模板选择器
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
                    // 生成中占位，保持对话框高度不跳动
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
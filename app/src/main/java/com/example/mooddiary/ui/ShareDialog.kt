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
import com.example.mooddiary.data.CardAppearance
import com.example.mooddiary.data.MoodEntry
import com.example.mooddiary.data.ShareSettings
import com.example.mooddiary.util.CardTemplate
import com.example.mooddiary.util.ImageSaver
import com.example.mooddiary.util.ShareCardGenerator
import com.example.mooddiary.util.ShareHelper

@Composable
fun ShareDialog(
    entry: MoodEntry,
    settings: ShareSettings,
    cardAppearance: CardAppearance,
    onSettingsChange: (ShareSettings) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    val template = CardTemplate.entries.firstOrNull {
        it.name.lowercase() == settings.template
    } ?: CardTemplate.CLASSIC

    val usePhoto = settings.useCustomPhoto && cardAppearance.imageUri != null

    val bitmap = remember(
        entry.id,
        settings.template,
        settings.showWatermark,
        settings.showDate,
        settings.useCustomPhoto,
        cardAppearance.imageUri
    ) {
        if (usePhoto) {
            val photo = try {
                val input = context.contentResolver.openInputStream(
                    android.net.Uri.parse(cardAppearance.imageUri)
                )
                android.graphics.BitmapFactory.decodeStream(input)
            } catch (e: Exception) { null }

            if (photo != null) {
                ShareCardGenerator.generateWithPhoto(
                    entry = entry,
                    photo = photo,
                    showWatermark = settings.showWatermark,
                    showDate = settings.showDate
                )
            } else {
                ShareCardGenerator.generate(
                    entry = entry,
                    template = template,
                    showWatermark = settings.showWatermark,
                    showDate = settings.showDate
                )
            }
        } else {
            ShareCardGenerator.generate(
                entry = entry,
                template = template,
                showWatermark = settings.showWatermark,
                showDate = settings.showDate
            )
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("分享心情") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
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
                                    onSettingsChange(settings.copy(template = t.name.lowercase()))
                                }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                t.label,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "预览",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxWidth(0.72f)
                        .aspectRatio(1080f / 1920f)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val uri = ImageSaver.saveToGallery(context, bitmap)
                val msg = if (uri != null) "已保存到相册" else "保存失败"
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                if (uri != null) onDismiss()
            }) {
                Text("保存相册")
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onDismiss) { Text("取消") }
                TextButton(onClick = {
                    ShareHelper.shareImage(context, bitmap)
                }) {
                    Text("分享")
                }
            }
        }
    )
}
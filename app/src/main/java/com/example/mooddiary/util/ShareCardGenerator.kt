package com.example.mooddiary.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import com.example.mooddiary.data.Mood
import com.example.mooddiary.data.MoodEntry
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class CardTemplate(val label: String) {
    CLASSIC("经典"),
    MINIMAL("简约"),
    PAPER("杂志"),
    DARK("暗夜"),
    NOTE("便签")
}

object ShareCardGenerator {

    private const val W = 1080
    private const val H = 1920

    fun generate(entry: MoodEntry, template: CardTemplate = CardTemplate.CLASSIC): Bitmap {
        val bmp = Bitmap.createBitmap(W, H, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)

        when (template) {
            CardTemplate.CLASSIC -> drawClassic(canvas, entry)
            CardTemplate.MINIMAL -> drawMinimal(canvas, entry)
            CardTemplate.PAPER   -> drawPaper(canvas, entry)
            CardTemplate.DARK    -> drawDark(canvas, entry)
            CardTemplate.NOTE    -> drawNoteTemplate(canvas, entry)
        }

        return bmp
    }

    // ========== 模板 1：经典渐变 ==========
    private fun drawClassic(canvas: Canvas, entry: MoodEntry) {
        val (c1, c2) = bgColors(entry.mood)
        val bgPaint = Paint().apply {
            shader = LinearGradient(0f, 0f, 0f, H.toFloat(), c1, c2, Shader.TileMode.CLAMP)
        }
        canvas.drawRect(0f, 0f, W.toFloat(), H.toFloat(), bgPaint)

        val textColor = darkTextColor(entry.mood)

        drawEmoji(canvas, entry, 660f, 260f)
        drawLabel(canvas, entry, 830f, 88f, textColor)
        drawStars(canvas, entry, 950f, 64f, textColor)

        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textColor
            alpha = 60
            strokeWidth = 3f
        }
        canvas.drawLine(W * 0.2f, 1030f, W * 0.8f, 1030f, linePaint)

        drawNote(canvas, entry, 1140f, 50f, textColor, (W * 0.78f).toInt())
        drawDate(canvas, entry, H - 250f, 42f, textColor)
        drawWatermark(canvas, H - 170f, 34f, textColor)
    }

    // ========== 模板 2：纯白简约 ==========
    private fun drawMinimal(canvas: Canvas, entry: MoodEntry) {
        val moodColor = moodMainColor(entry.mood)
        canvas.drawColor(Color.WHITE)

        val topPaint = Paint().apply { color = moodColor }
        canvas.drawRect(0f, 0f, W.toFloat(), 20f, topPaint)

        val tagPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#999999")
            textSize = 32f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText("M O O D   D I A R Y", W / 2f, 200f, tagPaint)

        drawEmoji(canvas, entry, 720f, 240f)
        drawLabel(canvas, entry, 900f, 90f, Color.parseColor("#1A1A1A"))
        drawStars(canvas, entry, 1010f, 66f, moodColor)

        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#EEEEEE")
            strokeWidth = 2f
        }
        canvas.drawLine(W * 0.3f, 1100f, W * 0.7f, 1100f, linePaint)

        drawNote(canvas, entry, 1200f, 48f, Color.parseColor("#444444"), (W * 0.7).toInt())
        drawDate(canvas, entry, H - 260f, 40f, Color.parseColor("#999999"))
        drawWatermark(canvas, H - 180f, 32f, Color.parseColor("#CCCCCC"))
    }

    // ========== 模板 3：杂志纸质 ==========
    private fun drawPaper(canvas: Canvas, entry: MoodEntry) {
        val moodColor = moodMainColor(entry.mood)
        canvas.drawColor(Color.parseColor("#FAF6EE"))

        val topLine = Paint().apply { color = Color.parseColor("#DDD5C5") }
        canvas.drawRect(0f, 0f, W.toFloat(), 8f, topLine)

        val dateSdf = SimpleDateFormat("yyyy.MM.dd", Locale.CHINA)
        val dateText = dateSdf.format(Date(entry.dayStart))

        val metaPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#8A7F6B")
            textSize = 36f
        }
        canvas.drawText("ISSUE", 80f, 140f, metaPaint)
        canvas.drawText(dateText, W - 80f - metaPaint.measureText(dateText), 140f, metaPaint)

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#2A2419")
            textSize = 140f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        }
        canvas.drawText(entry.mood.label, 80f, 380f, titlePaint)

        val blockPaint = Paint().apply { color = moodColor }
        canvas.drawRect(80f, 440f, 280f, 456f, blockPaint)

        val emojiPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 360f
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText(entry.mood.emoji, W - 100f, 700f, emojiPaint)

        val starPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = moodColor
            textSize = 56f
        }
        canvas.drawText(starString(entry.intensity), 80f, 560f, starPaint)

        val hrPaint = Paint().apply {
            color = Color.parseColor("#DDD5C5")
            strokeWidth = 2f
        }
        canvas.drawLine(80f, 850f, W - 80f, 850f, hrPaint)

        if (entry.note.isNotBlank()) {
            val notePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#3A3226")
                textSize = 56f
                typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
            }
            val lines = wrapText(entry.note, notePaint, (W - 160))
            var y = 940f
            lines.take(8).forEach { line ->
                canvas.drawText(line, 80f, y, notePaint)
                y += 88f
            }
        }

        val footer = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#8A7F6B")
            textSize = 34f
        }
        canvas.drawText("— 每日情绪账本 —", 80f, H - 100f, footer)
    }

    // ========== 模板 4：暗夜 ==========
    private fun drawDark(canvas: Canvas, entry: MoodEntry) {
        val moodColor = moodMainColor(entry.mood)
        canvas.drawColor(Color.parseColor("#0D0D0F"))

        val glowPaint = Paint().apply {
            shader = LinearGradient(
                0f, 0f, 0f, 800f,
                intArrayOf(moodColor, Color.TRANSPARENT),
                floatArrayOf(0f, 1f),
                Shader.TileMode.CLAMP
            )
            alpha = 60
        }
        canvas.drawRect(0f, 0f, W.toFloat(), 800f, glowPaint)

        drawEmoji(canvas, entry, 700f, 260f)
        drawLabel(canvas, entry, 900f, 92f, Color.WHITE)
        drawStars(canvas, entry, 1010f, 68f, moodColor)

        drawNote(canvas, entry, 1200f, 50f, Color.parseColor("#CCCCCC"), (W * 0.75).toInt())
        drawDate(canvas, entry, H - 260f, 42f, Color.parseColor("#888888"))
        drawWatermark(canvas, H - 180f, 34f, Color.parseColor("#555555"))
    }

    // ========== 模板 5：便签纸 ==========
    private fun drawNoteTemplate(canvas: Canvas, entry: MoodEntry) {
        val moodColor = moodMainColor(entry.mood)
        canvas.drawColor(Color.parseColor("#F0F0F2"))

        val padLeft = 80f
        val padTop = 300f
        val padRight = W - 80f
        val padBottom = H - 300f

        val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#20000000")
        }
        canvas.drawRoundRect(
            RectF(padLeft + 10f, padTop + 15f, padRight + 10f, padBottom + 15f),
            40f, 40f, shadowPaint
        )

        val paperPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#FFF7D6")
        }
        canvas.drawRoundRect(
            RectF(padLeft, padTop, padRight, padBottom),
            40f, 40f, paperPaint
        )

        val clipPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = moodColor }
        val clipPath = Path().apply {
            addRoundRect(
                RectF(padLeft, padTop, padRight, padTop + 30f),
                40f, 40f, Path.Direction.CW
            )
        }
        canvas.save()
        canvas.clipRect(padLeft, padTop, padRight, padTop + 40f)
        canvas.drawPath(clipPath, clipPaint)
        canvas.restore()

        val centerX = (padLeft + padRight) / 2f

        val emojiPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 200f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(entry.mood.emoji, centerX, padTop + 320f, emojiPaint)

        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#3A3226")
            textSize = 80f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText(entry.mood.label, centerX, padTop + 460f, labelPaint)

        val starPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = moodColor
            textSize = 56f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(starString(entry.intensity), centerX, padTop + 560f, starPaint)

        if (entry.note.isNotBlank()) {
            val notePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#5A5040")
                textSize = 46f
                textAlign = Paint.Align.CENTER
            }
            val lines = wrapText(entry.note, notePaint, ((padRight - padLeft - 120)).toInt())
            var y = padTop + 680f
            lines.take(6).forEach { line ->
                canvas.drawText(line, centerX, y, notePaint)
                y += 68f
            }
        }

        val dateSdf = SimpleDateFormat("yyyy.MM.dd  EEEE", Locale.CHINA)
        val dateText = dateSdf.format(Date(entry.dayStart))
        val datePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#8A7F6B")
            textSize = 36f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(dateText, centerX, padBottom - 120f, datePaint)

        val markPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#AA9F88")
            textSize = 32f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("— 每日情绪账本 —", centerX, padBottom - 60f, markPaint)
    }

    // ========== 通用绘制 ==========

    private fun drawEmoji(canvas: Canvas, entry: MoodEntry, y: Float, size: Float) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = size
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(entry.mood.emoji, W / 2f, y, paint)
    }

    private fun drawLabel(canvas: Canvas, entry: MoodEntry, y: Float, size: Float, color: Int) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            textSize = size
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText(entry.mood.label, W / 2f, y, paint)
    }

    private fun drawStars(canvas: Canvas, entry: MoodEntry, y: Float, size: Float, color: Int) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            textSize = size
            textAlign = Paint.Align.CENTER
            alpha = 220
        }
        canvas.drawText(starString(entry.intensity), W / 2f, y, paint)
    }

    private fun drawNote(
        canvas: Canvas,
        entry: MoodEntry,
        startY: Float,
        size: Float,
        color: Int,
        maxWidth: Int
    ) {
        if (entry.note.isBlank()) return
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            textSize = size
            textAlign = Paint.Align.CENTER
        }
        val lines = wrapText(entry.note, paint, maxWidth)
        var y = startY
        lines.take(5).forEach { line ->
            canvas.drawText(line, W / 2f, y, paint)
            y += size * 1.5f
        }
    }

    private fun drawDate(canvas: Canvas, entry: MoodEntry, y: Float, size: Float, color: Int) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            textSize = size
            textAlign = Paint.Align.CENTER
            alpha = 200
        }
        val dateStr = SimpleDateFormat("yyyy.MM.dd  EEEE", Locale.CHINA)
            .format(Date(entry.dayStart))
        canvas.drawText(dateStr, W / 2f, y, paint)
    }

    private fun drawWatermark(canvas: Canvas, y: Float, size: Float, color: Int) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color
            textSize = size
            textAlign = Paint.Align.CENTER
            alpha = 140
        }
        canvas.drawText("— 每日情绪账本 —", W / 2f, y, paint)
    }

    // ========== 工具 ==========

    private fun bgColors(mood: Mood): Pair<Int, Int> = when (mood) {
        Mood.HAPPY   -> Color.parseColor("#FFE9B0") to Color.parseColor("#FFB86B")
        Mood.CALM    -> Color.parseColor("#D6F1EF") to Color.parseColor("#9CC5C0")
        Mood.SAD     -> Color.parseColor("#C6D2E8") to Color.parseColor("#7B93B8")
        Mood.ANGRY   -> Color.parseColor("#FFD1CF") to Color.parseColor("#E07070")
        Mood.ANXIOUS -> Color.parseColor("#E8D1F0") to Color.parseColor("#B589C9")
    }

    private fun darkTextColor(mood: Mood): Int = when (mood) {
        Mood.HAPPY   -> Color.parseColor("#7A4A00")
        Mood.CALM    -> Color.parseColor("#1E4A47")
        Mood.SAD     -> Color.parseColor("#1F2F4A")
        Mood.ANGRY   -> Color.parseColor("#7A1010")
        Mood.ANXIOUS -> Color.parseColor("#3F1E4A")
    }

    private fun moodMainColor(mood: Mood): Int = when (mood) {
        Mood.HAPPY   -> Color.parseColor("#F5A623")
        Mood.CALM    -> Color.parseColor("#4CAF93")
        Mood.SAD     -> Color.parseColor("#5B7DB1")
        Mood.ANGRY   -> Color.parseColor("#E05353")
        Mood.ANXIOUS -> Color.parseColor("#9C6BC7")
    }

    private fun starString(n: Int): String {
        val c = n.coerceIn(0, 5)
        return "★".repeat(c) + "☆".repeat(5 - c)
    }

    private fun wrapText(text: String, paint: Paint, maxWidth: Int): List<String> {
        val result = mutableListOf<String>()
        var current = StringBuilder()
        for (ch in text) {
            val test = current.toString() + ch
            if (paint.measureText(test) > maxWidth && current.isNotEmpty()) {
                result.add(current.toString())
                current = StringBuilder().append(ch)
            } else {
                current.append(ch)
            }
        }
        if (current.isNotEmpty()) result.add(current.toString())
        return result
    }
}
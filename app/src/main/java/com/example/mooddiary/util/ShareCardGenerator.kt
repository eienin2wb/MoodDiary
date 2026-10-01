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
import androidx.core.graphics.createBitmap
import androidx.core.graphics.toColorInt
import com.example.mooddiary.data.CustomMoodDef
import com.example.mooddiary.data.CustomMoodStyle
import com.example.mooddiary.data.Mood
import com.example.mooddiary.data.MoodEntry
import com.example.mooddiary.ui.entryEmoji
import com.example.mooddiary.ui.entryLabel
import com.example.mooddiary.ui.localeFor
import com.example.mooddiary.ui.starString
import java.util.Calendar
import java.util.Date

enum class CardTemplate(val key: String) {
    NATIONAL_DAY("tpl_national"),
    CLASSIC("tpl_classic"),
    MINIMAL("tpl_minimal"),
    PAPER("tpl_paper"),
    DARK("tpl_dark"),
    NOTE("tpl_note")
}

object ShareCardGenerator {

    private const val W = 1080
    private const val H = 1920

    /**
     * 所有模板绘制函数共用的上下文。
     *
     * 每个 drawXxx 原本要传 7 个参数（canvas + entry + wm + date + lang +
     * customs + customMoodDefs），现在收敛为 `(canvas, ctx)`，签名更短也更
     * 容易扩展新字段。
     */
    private data class CardCtx(
        val entry: MoodEntry,
        val wm: Boolean,
        val date: Boolean,
        val lang: String,
        val customs: Map<Mood, CustomMoodStyle>,
        val customMoodDefs: List<CustomMoodDef>
    )

    /* ================= 对外入口 ================= */

    fun generate(
        entry: MoodEntry,
        template: CardTemplate = CardTemplate.NATIONAL_DAY,
        showWatermark: Boolean = true,
        showDate: Boolean = true,
        lang: String = "zh",
        customs: Map<Mood, CustomMoodStyle> = emptyMap(),
        customMoodDefs: List<CustomMoodDef> = emptyList()
    ): Bitmap {
        val bmp = createBitmap(W, H, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)
        val ctx = CardCtx(entry, showWatermark, showDate, lang, customs, customMoodDefs)
        when (template) {
            CardTemplate.NATIONAL_DAY -> drawNationalDay(canvas, ctx)
            CardTemplate.CLASSIC      -> drawClassic(canvas, ctx)
            CardTemplate.MINIMAL      -> drawMinimal(canvas, ctx)
            CardTemplate.PAPER        -> drawPaper(canvas, ctx)
            CardTemplate.DARK         -> drawDark(canvas, ctx)
            CardTemplate.NOTE         -> drawNoteTemplate(canvas, ctx)
        }
        return bmp
    }

    fun generateWithPhoto(
        entry: MoodEntry,
        photo: Bitmap,
        showWatermark: Boolean = true,
        showDate: Boolean = true,
        lang: String = "zh",
        customs: Map<Mood, CustomMoodStyle> = emptyMap(),
        customMoodDefs: List<CustomMoodDef> = emptyList()
    ): Bitmap {
        val bmp = createBitmap(W, H, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bmp)

        val src = cropCenter(photo, W, H)
        canvas.drawBitmap(src, 0f, 0f, null)
        // cropCenter 在目标尺寸恰好等于原图尺寸时会返回同一个引用，
        // 只有真正产生了新 bitmap 才回收，否则会误伤调用方的 photo。
        if (src !== photo) src.recycle()

        val topMask = Paint().apply {
            shader = LinearGradient(
                0f, 0f, 0f, H * 0.5f,
                intArrayOf(0x80000000.toInt(), 0x00000000),
                floatArrayOf(0f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, W.toFloat(), H * 0.5f, topMask)

        val bottomMask = Paint().apply {
            shader = LinearGradient(
                0f, H * 0.5f, 0f, H.toFloat(),
                intArrayOf(0x00000000, 0xCC000000.toInt()),
                floatArrayOf(0f, 1f),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, H * 0.5f, W.toFloat(), H.toFloat(), bottomMask)

        val white = Color.WHITE
        drawEmoji(canvas, entry, 660f, 260f, customs, customMoodDefs)
        drawLabel(canvas, entry, 830f, 88f, white, lang, customs, customMoodDefs)
        drawStars(canvas, entry, 950f, 64f, white)

        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = white; alpha = 80; strokeWidth = 3f
        }
        canvas.drawLine(W * 0.2f, 1030f, W * 0.8f, 1030f, linePaint)

        drawNote(canvas, entry, 1140f, 50f, white, (W * 0.78f).toInt())
        if (showDate) drawDate(canvas, entry, H - 250f, 42f, white, lang)
        if (showWatermark) drawWatermark(canvas, H - 170f, 34f, white, lang)

        return bmp
    }

    private fun cropCenter(src: Bitmap, targetW: Int, targetH: Int): Bitmap {
        val srcW = src.width
        val srcH = src.height
        val srcRatio = srcW.toFloat() / srcH
        val targetRatio = targetW.toFloat() / targetH
        return if (srcRatio > targetRatio) {
            val newW = (srcH * targetRatio).toInt()
            val x = (srcW - newW) / 2
            Bitmap.createBitmap(src, x, 0, newW, srcH)
        } else {
            val newH = (srcW / targetRatio).toInt()
            val y = (srcH - newH) / 2
            Bitmap.createBitmap(src, 0, y, srcW, newH)
        }
    }

    /* ================= 国庆 ================= */

    private fun drawNationalDay(canvas: Canvas, ctx: CardCtx) {
        val entry = ctx.entry
        val lang = ctx.lang

        val red1 = "#C8102E".toColorInt()
        val red2 = "#8B0000".toColorInt()
        val gold = "#FFD700".toColorInt()

        val bgPaint = Paint().apply {
            shader = LinearGradient(0f, 0f, 0f, H.toFloat(), red1, red2, Shader.TileMode.CLAMP)
        }
        canvas.drawRect(0f, 0f, W.toFloat(), H.toFloat(), bgPaint)

        val decoPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = gold; alpha = 90 }
        val deco = listOf(
            Triple(80f, 700f, 8f), Triple(1000f, 500f, 10f),
            Triple(120f, 1400f, 6f), Triple(950f, 1250f, 8f),
            Triple(180f, 500f, 5f), Triple(920f, 800f, 6f),
            Triple(500f, 1650f, 5f), Triple(850f, 1550f, 7f),
            Triple(200f, 1000f, 6f), Triple(880f, 1050f, 5f)
        )
        deco.forEach { (x, y, r) -> drawStar5(canvas, x, y, r, decoPaint) }

        val flagStar = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = gold }
        drawStar5(canvas, 130f, 150f, 38f, flagStar)
        drawStar5(canvas, 210f, 100f, 16f, flagStar)
        drawStar5(canvas, 245f, 150f, 16f, flagStar)
        drawStar5(canvas, 245f, 205f, 16f, flagStar)
        drawStar5(canvas, 210f, 255f, 16f, flagStar)

        val lanternPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = gold }
        canvas.drawCircle(W - 120f, 120f, 22f, lanternPaint)
        canvas.drawRect(W - 130f, 100f, W - 110f, 108f, lanternPaint)
        canvas.drawRect(W - 130f, 142f, W - 110f, 150f, lanternPaint)
        canvas.drawLine(W - 120f, 78f, W - 120f, 98f, lanternPaint)

        val yearPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = gold; textSize = 44f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            letterSpacing = 0.15f
        }
        // 用系统当前年份，避免硬编码 "2026" 在明年过期
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        canvas.drawText("1949 · $currentYear", W / 2f, 380f, yearPaint)

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = gold; textSize = 130f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText(L.t("national_title", lang), W / 2f, 540f, titlePaint)

        val linePaint = Paint().apply {
            color = gold; strokeWidth = 3f; alpha = 200
        }
        canvas.drawLine(W * 0.28f, 610f, W * 0.72f, 610f, linePaint)
        val sideStar = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = gold }
        drawStar5(canvas, W * 0.24f, 610f, 10f, sideStar)
        drawStar5(canvas, W * 0.76f, 610f, 10f, sideStar)

        drawEmoji(canvas, entry, 1020f, 260f, ctx.customs, ctx.customMoodDefs)
        drawLabel(canvas, entry, 1180f, 92f, Color.WHITE, lang, ctx.customs, ctx.customMoodDefs)
        drawStars(canvas, entry, 1300f, 68f, gold)

        val divider = Paint().apply {
            color = gold; strokeWidth = 3f; alpha = 150
        }
        canvas.drawLine(W * 0.22f, 1400f, W * 0.78f, 1400f, divider)

        drawNote(canvas, entry, 1500f, 50f, Color.WHITE, (W * 0.78f).toInt())
        if (ctx.date) drawDate(canvas, entry, H - 280f, 42f, gold, lang)

        if (ctx.wm) {
            val markPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = gold; textSize = 34f
                textAlign = Paint.Align.CENTER
                alpha = 220
            }
            canvas.drawText(L.t("watermark_national", lang), W / 2f, H - 180f, markPaint)
        }
    }

    private fun drawStar5(canvas: Canvas, cx: Float, cy: Float, radius: Float, paint: Paint) {
        val path = Path()
        val outer = radius
        val inner = radius * 0.42f
        val step = (Math.PI / 5).toFloat()
        var angle = -Math.PI.toFloat() / 2
        path.moveTo(cx + outer * kotlin.math.cos(angle), cy + outer * kotlin.math.sin(angle))
        for (i in 1 until 10) {
            val r = if (i % 2 == 0) outer else inner
            angle += step
            path.lineTo(cx + r * kotlin.math.cos(angle), cy + r * kotlin.math.sin(angle))
        }
        path.close()
        canvas.drawPath(path, paint)
    }

    /* ================= 经典 ================= */

    private fun drawClassic(canvas: Canvas, ctx: CardCtx) {
        val entry = ctx.entry
        val lang = ctx.lang

        val (c1, c2) = bgColors(entry.mood)
        val bgPaint = Paint().apply {
            shader = LinearGradient(0f, 0f, 0f, H.toFloat(), c1, c2, Shader.TileMode.CLAMP)
        }
        canvas.drawRect(0f, 0f, W.toFloat(), H.toFloat(), bgPaint)

        val textColor = darkTextColor(entry.mood)
        drawEmoji(canvas, entry, 660f, 260f, ctx.customs, ctx.customMoodDefs)
        drawLabel(canvas, entry, 830f, 88f, textColor, lang, ctx.customs, ctx.customMoodDefs)
        drawStars(canvas, entry, 950f, 64f, textColor)

        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = textColor; alpha = 60; strokeWidth = 3f
        }
        canvas.drawLine(W * 0.2f, 1030f, W * 0.8f, 1030f, linePaint)

        drawNote(canvas, entry, 1140f, 50f, textColor, (W * 0.78f).toInt())
        if (ctx.date) drawDate(canvas, entry, H - 250f, 42f, textColor, lang)
        if (ctx.wm) drawWatermark(canvas, H - 170f, 34f, textColor, lang)
    }

    /* ================= 简约 ================= */

    private fun drawMinimal(canvas: Canvas, ctx: CardCtx) {
        val entry = ctx.entry
        val lang = ctx.lang

        val moodColor = moodMainColor(entry.mood)
        canvas.drawColor(Color.WHITE)

        val topPaint = Paint().apply { color = moodColor }
        canvas.drawRect(0f, 0f, W.toFloat(), 20f, topPaint)

        val tagPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = "#999999".toColorInt(); textSize = 32f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText("M O O D   D I A R Y", W / 2f, 200f, tagPaint)

        drawEmoji(canvas, entry, 720f, 240f, ctx.customs, ctx.customMoodDefs)
        drawLabel(canvas, entry, 900f, 90f, "#1A1A1A".toColorInt(), lang, ctx.customs, ctx.customMoodDefs)
        drawStars(canvas, entry, 1010f, 66f, moodColor)

        val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = "#EEEEEE".toColorInt(); strokeWidth = 2f
        }
        canvas.drawLine(W * 0.3f, 1100f, W * 0.7f, 1100f, linePaint)

        drawNote(canvas, entry, 1200f, 48f, "#444444".toColorInt(), (W * 0.7).toInt())
        if (ctx.date) drawDate(canvas, entry, H - 260f, 40f, "#999999".toColorInt(), lang)
        if (ctx.wm) drawWatermark(canvas, H - 180f, 32f, "#CCCCCC".toColorInt(), lang)
    }

    /* ================= 杂志 ================= */

    private fun drawPaper(canvas: Canvas, ctx: CardCtx) {
        val entry = ctx.entry
        val lang = ctx.lang

        val moodColor = moodMainColor(entry.mood)
        canvas.drawColor("#FAF6EE".toColorInt())

        val topLine = Paint().apply { color = "#DDD5C5".toColorInt() }
        canvas.drawRect(0f, 0f, W.toFloat(), 8f, topLine)

        // 日期跟随卡片语言，不再硬编码 Locale.CHINA
        val dateText = cachedDateFormatter("yyyy.MM.dd", localeFor(lang))
            .format(Date(entry.dayStart))

        val metaPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = "#8A7F6B".toColorInt(); textSize = 36f
        }
        // ISSUE 是杂志风格的英文点缀，多语言下保持视觉一致
        canvas.drawText("ISSUE", 80f, 140f, metaPaint)
        if (ctx.date) {
            canvas.drawText(
                dateText,
                W - 80f - metaPaint.measureText(dateText),
                140f,
                metaPaint
            )
        }

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = "#2A2419".toColorInt(); textSize = 140f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        }
        canvas.drawText(entryLabel(entry, ctx.customs, ctx.customMoodDefs, lang), 80f, 380f, titlePaint)

        val blockPaint = Paint().apply { color = moodColor }
        canvas.drawRect(80f, 440f, 280f, 456f, blockPaint)

        val emojiPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 360f; textAlign = Paint.Align.RIGHT
        }
        canvas.drawText(entryEmoji(entry, ctx.customs, ctx.customMoodDefs), W - 100f, 700f, emojiPaint)

        val starPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = moodColor; textSize = 56f
        }
        canvas.drawText(starString(entry.intensity), 80f, 560f, starPaint)

        val hrPaint = Paint().apply {
            color = "#DDD5C5".toColorInt(); strokeWidth = 2f
        }
        canvas.drawLine(80f, 850f, W - 80f, 850f, hrPaint)

        if (entry.note.isNotBlank()) {
            val notePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = "#3A3226".toColorInt(); textSize = 56f
                typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
            }
            val lines = wrapText(entry.note, notePaint, W - 160)
            var y = 940f
            lines.take(8).forEach { line ->
                canvas.drawText(line, 80f, y, notePaint); y += 88f
            }
        }

        if (ctx.wm) {
            val footer = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = "#8A7F6B".toColorInt(); textSize = 34f
            }
            canvas.drawText(L.t("watermark_text", lang), 80f, H - 100f, footer)
        }
    }

    /* ================= 暗夜 ================= */

    private fun drawDark(canvas: Canvas, ctx: CardCtx) {
        val entry = ctx.entry
        val lang = ctx.lang

        val moodColor = moodMainColor(entry.mood)
        canvas.drawColor("#0D0D0F".toColorInt())

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

        drawEmoji(canvas, entry, 700f, 260f, ctx.customs, ctx.customMoodDefs)
        drawLabel(canvas, entry, 900f, 92f, Color.WHITE, lang, ctx.customs, ctx.customMoodDefs)
        drawStars(canvas, entry, 1010f, 68f, moodColor)

        drawNote(canvas, entry, 1200f, 50f, "#CCCCCC".toColorInt(), (W * 0.75).toInt())
        if (ctx.date) drawDate(canvas, entry, H - 260f, 42f, "#888888".toColorInt(), lang)
        if (ctx.wm) drawWatermark(canvas, H - 180f, 34f, "#555555".toColorInt(), lang)
    }

    /* ================= 便签 ================= */

    private fun drawNoteTemplate(canvas: Canvas, ctx: CardCtx) {
        val entry = ctx.entry
        val lang = ctx.lang

        val moodColor = moodMainColor(entry.mood)
        canvas.drawColor("#F0F0F2".toColorInt())

        val padLeft = 80f
        val padTop = 300f
        val padRight = W - 80f
        val padBottom = H - 300f

        val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = "#20000000".toColorInt()
        }
        canvas.drawRoundRect(
            RectF(padLeft + 10f, padTop + 15f, padRight + 10f, padBottom + 15f),
            40f, 40f, shadowPaint
        )

        val paperPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = "#FFF7D6".toColorInt()
        }
        canvas.drawRoundRect(
            RectF(padLeft, padTop, padRight, padBottom), 40f, 40f, paperPaint
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
            textSize = 200f; textAlign = Paint.Align.CENTER
        }
        canvas.drawText(entryEmoji(entry, ctx.customs, ctx.customMoodDefs), centerX, padTop + 320f, emojiPaint)

        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = "#3A3226".toColorInt(); textSize = 80f
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText(entryLabel(entry, ctx.customs, ctx.customMoodDefs, lang), centerX, padTop + 460f, labelPaint)

        val starPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = moodColor; textSize = 56f; textAlign = Paint.Align.CENTER
        }
        canvas.drawText(starString(entry.intensity), centerX, padTop + 560f, starPaint)

        if (entry.note.isNotBlank()) {
            val notePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = "#5A5040".toColorInt(); textSize = 46f
                textAlign = Paint.Align.CENTER
            }
            val lines = wrapText(entry.note, notePaint, (padRight - padLeft - 120).toInt())
            var y = padTop + 680f
            lines.take(6).forEach { line ->
                canvas.drawText(line, centerX, y, notePaint); y += 68f
            }
        }

        if (ctx.date) {
            val dateText = cachedDateFormatter("yyyy.MM.dd  EEEE", localeFor(lang))
                .format(Date(entry.dayStart))
            val datePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = "#8A7F6B".toColorInt(); textSize = 36f
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(dateText, centerX, padBottom - 120f, datePaint)
        }

        if (ctx.wm) {
            val markPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = "#AA9F88".toColorInt(); textSize = 32f
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(L.t("watermark_text", lang), centerX, padBottom - 60f, markPaint)
        }
    }

    /* ================= 通用绘制 ================= */

    private fun drawEmoji(
        canvas: Canvas, entry: MoodEntry, y: Float, size: Float,
        customs: Map<Mood, CustomMoodStyle>,
        customMoodDefs: List<CustomMoodDef>
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = size; textAlign = Paint.Align.CENTER
        }
        canvas.drawText(entryEmoji(entry, customs, customMoodDefs), W / 2f, y, paint)
    }

    private fun drawLabel(
        canvas: Canvas, entry: MoodEntry, y: Float, size: Float,
        color: Int, lang: String,
        customs: Map<Mood, CustomMoodStyle>,
        customMoodDefs: List<CustomMoodDef>
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color; textSize = size
            textAlign = Paint.Align.CENTER
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText(entryLabel(entry, customs, customMoodDefs, lang), W / 2f, y, paint)
    }

    private fun drawStars(canvas: Canvas, entry: MoodEntry, y: Float, size: Float, color: Int) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color; textSize = size
            textAlign = Paint.Align.CENTER; alpha = 220
        }
        canvas.drawText(starString(entry.intensity), W / 2f, y, paint)
    }

    private fun drawNote(
        canvas: Canvas, entry: MoodEntry,
        startY: Float, size: Float, color: Int, maxWidth: Int
    ) {
        if (entry.note.isBlank()) return
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color; textSize = size; textAlign = Paint.Align.CENTER
        }
        val lines = wrapText(entry.note, paint, maxWidth)
        var y = startY
        lines.take(5).forEach { line ->
            canvas.drawText(line, W / 2f, y, paint); y += size * 1.5f
        }
    }

    private fun drawDate(
        canvas: Canvas, entry: MoodEntry, y: Float, size: Float,
        color: Int, lang: String
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color; textSize = size
            textAlign = Paint.Align.CENTER; alpha = 200
        }
        val dateStr = cachedDateFormatter("yyyy.MM.dd  EEEE", localeFor(lang))
            .format(Date(entry.dayStart))
        canvas.drawText(dateStr, W / 2f, y, paint)
    }

    private fun drawWatermark(
        canvas: Canvas, y: Float, size: Float, color: Int, lang: String
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = color; textSize = size
            textAlign = Paint.Align.CENTER; alpha = 140
        }
        canvas.drawText(L.t("watermark_text", lang), W / 2f, y, paint)
    }

    private fun bgColors(mood: Mood): Pair<Int, Int> = when (mood) {
        Mood.HAPPY   -> "#FFE9B0".toColorInt() to "#FFB86B".toColorInt()
        Mood.CALM    -> "#D6F1EF".toColorInt() to "#9CC5C0".toColorInt()
        Mood.SAD     -> "#C6D2E8".toColorInt() to "#7B93B8".toColorInt()
        Mood.ANGRY   -> "#FFD1CF".toColorInt() to "#E07070".toColorInt()
        Mood.ANXIOUS -> "#E8D1F0".toColorInt() to "#B589C9".toColorInt()
    }

    private fun darkTextColor(mood: Mood): Int = when (mood) {
        Mood.HAPPY   -> "#7A4A00".toColorInt()
        Mood.CALM    -> "#1E4A47".toColorInt()
        Mood.SAD     -> "#1F2F4A".toColorInt()
        Mood.ANGRY   -> "#7A1010".toColorInt()
        Mood.ANXIOUS -> "#3F1E4A".toColorInt()
    }

    private fun moodMainColor(mood: Mood): Int = when (mood) {
        Mood.HAPPY   -> "#F5A623".toColorInt()
        Mood.CALM    -> "#4CAF93".toColorInt()
        Mood.SAD     -> "#5B7DB1".toColorInt()
        Mood.ANGRY   -> "#E05353".toColorInt()
        Mood.ANXIOUS -> "#9C6BC7".toColorInt()
    }

    /**
     * 按最大宽度折行。
     *
     * 用 [Paint.breakText] 代替逐字符累加 + `measureText` 的写法：
     * - 前者在 native 层扫描，O(n)；
     * - 后者每轮都要拼接字符串再 measure，O(n²) 且产生大量临时对象。
     */
    private fun wrapText(text: String, paint: Paint, maxWidth: Int): List<String> {
        if (text.isEmpty()) return emptyList()
        val result = mutableListOf<String>()
        var start = 0
        val len = text.length
        val maxW = maxWidth.toFloat()
        while (start < len) {
            val count = paint.breakText(text, start, len, true, maxW, null)
            if (count <= 0) break
            result.add(text.substring(start, start + count))
            start += count
        }
        return result
    }
}

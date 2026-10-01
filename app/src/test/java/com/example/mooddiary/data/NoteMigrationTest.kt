package com.example.mooddiary.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * 迁移逻辑的单元测试。
 *
 * 这是唯一会动用户已有文字的地方，所以把逻辑抽成纯函数 [planNoteMigration] 并在这里覆盖：
 * 空文字不动、有文字才搬、id 不撞车、重复执行不会二次搬运。
 */
class NoteMigrationTest {

    private fun moodEntry(
        id: Long,
        note: String,
        day: Long,
        mood: Mood = Mood.HAPPY,
        intensity: Int = 4
    ) = MoodEntry(
        id = id,
        mood = mood,
        intensity = intensity,
        note = note,
        dayStart = day
    )

    @Test
    fun blankNotesAreLeftAloneAndCreateNoDiary() {
        val entries = listOf(
            moodEntry(1, "", 1_000L),
            moodEntry(2, "   ", 2_000L)
        )

        val (updated, created) = planNoteMigration(entries, emptyList())

        assertEquals(entries, updated)
        assertTrue(created.isEmpty())
    }

    @Test
    fun noteMovesIntoDiaryAndIsClearedFromTheMoodRecord() {
        val day = 86_400_000L
        val entries = listOf(moodEntry(7, "今天很开心", day, Mood.CALM, intensity = 5))

        val (updated, created) = planNoteMigration(entries, emptyList())

        assertEquals(1, created.size)
        val diary = created.first()
        assertEquals("今天很开心", diary.content)
        assertEquals(day, diary.dayStart)
        assertEquals(Mood.CALM, diary.mood)

        val mood = updated.single()
        assertEquals("", mood.note)
        // 除了文字，其它字段必须原样保留
        assertEquals(7L, mood.id)
        assertEquals(Mood.CALM, mood.mood)
        assertEquals(5, mood.intensity)
        assertEquals(day, mood.dayStart)
    }

    @Test
    fun newDiaryIdsContinueAfterExistingOnes() {
        val existing = listOf(DiaryEntry(id = 5L, content = "旧的", dayStart = 1L))
        val entries = listOf(
            moodEntry(1, "a", 10L),
            moodEntry(2, "b", 20L)
        )

        val (_, created) = planNoteMigration(entries, existing)

        assertEquals(listOf(6L, 7L), created.map { it.id })
    }

    @Test
    fun onlyEntriesWithTextProduceDiaries() {
        val entries = listOf(
            moodEntry(1, "", 10L),
            moodEntry(2, "有内容", 20L),
            moodEntry(3, "  ", 30L),
            moodEntry(4, "也有内容", 40L)
        )

        val (updated, created) = planNoteMigration(entries, emptyList())

        assertEquals(2, created.size)
        assertEquals(listOf("有内容", "也有内容"), created.map { it.content })
        // 只有真正有文字的两条被清空；纯空白的原样留着（本来也没东西可搬）
        assertEquals(listOf("", "", "  ", ""), updated.map { it.note })
    }

    @Test
    fun runningAgainOnMigratedRecordsCreatesNothing() {
        val entries = listOf(moodEntry(1, "写过了", 10L))

        val (migratedOnce, createdFirst) = planNoteMigration(entries, emptyList())
        assertEquals(1, createdFirst.size)

        val (migratedTwice, createdSecond) =
            planNoteMigration(migratedOnce, createdFirst)

        assertTrue(createdSecond.isEmpty())
        assertEquals(migratedOnce, migratedTwice)
    }
}

package app.oubliettes

import app.oubliettes.game.Language
import app.oubliettes.game.lessons
import app.oubliettes.game.nudge
import app.oubliettes.game.reason
import app.oubliettes.game.steps
import app.oubliettes.game.tutorialPuzzle
import app.oubliettes.game.tutorialSteps
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SinhalaTest {
    private fun hasSinhala(text: String) = text.any { it in '඀'..'෿' }

    @Test
    fun wholeTextsAreTranslated() {
        assertEquals("පහසු", Sinhala.translate("Easy"))
        assertEquals("මෙනුව", Sinhala.translate("Menu"))
        for (step in tutorialSteps) assertTrue(step.text, hasSinhala(Sinhala.translate(step.text)))
        for (lesson in lessons) {
            assertTrue(lesson.title, hasSinhala(Sinhala.translate(lesson.title)))
            assertTrue(lesson.about, hasSinhala(Sinhala.translate(lesson.about)))
            // The introduction of the lesson is its first step.
            assertTrue(lesson.title, hasSinhala(Sinhala.translate(lesson.steps.first().text)))
        }
    }

    @Test
    fun textsWithNumbersAreTranslated() {
        assertEquals("පහසු · මට්ටම 3", Sinhala.translate("Easy · Level 3"))
        assertEquals("12 / 250 විසඳා ඇත", Sinhala.translate("12 / 250 solved"))
        assertEquals("6×6 · මෘදු · විසඳූ 4ක්", Sinhala.translate("6×6 · Gentle · 4 solved"))
        assertEquals("පියවර 2 / 9", Sinhala.translate("Step 2 / 9"))
        assertEquals("🎓 පුහුණුව", Sinhala.translate("🎓 Tutorial"))
        assertEquals("🏅 නවක · මට්ටම 2", Sinhala.translate("🏅 Rookie · Level 2"))
        assertEquals("පේළිය 3, තීරුව 2: රාක්ෂයා, සලකුණු කළ යුතුය", Sinhala.translate("Row 3, column 2: monster, to mark"))
    }

    @Test
    fun whatIsNotTranslatedStaysAsItIs() {
        assertEquals("Mathra", Sinhala.translate("Mathra"))
        assertEquals("6×6", Sinhala.translate("6×6"))
        assertEquals("2026-10-18 · සූදානම්", Sinhala.translate("2026-10-18 · Ready"))
        assertEquals("", Sinhala.translate(""))
    }

    @Test
    fun hintsFollowTheLanguage() {
        val before = Language.sinhala
        try {
            val all = steps(tutorialPuzzle)!!
            assertTrue(all.isNotEmpty())
            for (step in all) {
                Language.sinhala = false
                val english = step.nudge(tutorialPuzzle) to step.reason(tutorialPuzzle)
                Language.sinhala = true
                val sinhala = step.nudge(tutorialPuzzle) to step.reason(tutorialPuzzle)
                assertNotEquals(english.first, sinhala.first)
                assertNotEquals(english.second, sinhala.second)
                assertTrue(sinhala.first, hasSinhala(sinhala.first))
                assertTrue(sinhala.second, hasSinhala(sinhala.second))
                assertFalse(sinhala.second, '$' in sinhala.first + sinhala.second)
            }
            // A lesson tells its steps again in the language of the day.
            val lesson = lessons.first()
            Language.sinhala = false
            val english = lesson.steps.map { it.text }
            Language.sinhala = true
            val sinhala = lesson.steps.map { it.text }
            assertEquals(english.size, sinhala.size)
            assertNotEquals(english[1], sinhala[1])
        } finally {
            Language.sinhala = before
        }
    }
}

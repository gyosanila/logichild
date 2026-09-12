package com.gyosanila.logichild

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PatternPuzzleFactoryTest {
    @Test
    fun allFirst500LevelsHaveValidAnswers() {
        for (level in 1..500) {
            val puzzle = PatternPuzzleFactory.create(level)
            PatternPuzzleFactory.validate(puzzle)
            assertTrue("level $level answer missing", puzzle.answer in puzzle.choices)
            assertEquals("level $level has duplicate choices", puzzle.choices.size, puzzle.choices.distinct().size)
        }
    }

    @Test
    fun levelsUpTo1000NeverThrowOrCreateInvalidPattern() {
        for (level in 1..1000) {
            val puzzle = PatternPuzzleFactory.create(level)
            assertEquals("level $level must have one blank", 1, puzzle.sequence.count { it == "?" })
            assertTrue("level $level answer missing", puzzle.answer in puzzle.choices)
            PatternPuzzleFactory.validate(puzzle)
        }
    }

    @Test
    fun knownReportedLevelsUseExpectedAnswers() {
        listOf(6, 9, 12, 15).forEach { level ->
            val puzzle = PatternPuzzleFactory.create(level)
            assertEquals("level $level answer", puzzle.sequence[0], puzzle.answer)
            assertTrue("level $level choices", puzzle.answer in puzzle.choices)
        }
        val level18 = PatternPuzzleFactory.create(18)
        assertEquals("level 18 answer follows ABC cycle", level18.sequence[1], level18.answer)
    }

    @Test
    fun difficultyUsesDifferentPatternFamilies() {
        assertEquals(PatternRule.AB, PatternPuzzleFactory.create(1).rule)
        assertEquals(PatternRule.ABAB, PatternPuzzleFactory.create(4).rule)
        assertEquals(PatternRule.ABC, PatternPuzzleFactory.create(7).rule)
        assertEquals(PatternRule.ABCABC, PatternPuzzleFactory.create(10).rule)
        assertEquals(PatternRule.AABB, PatternPuzzleFactory.create(13).rule)
        assertEquals(PatternRule.ABCD, PatternPuzzleFactory.create(19).rule)
    }
}

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
            if (puzzle.sequence.size == 5) {
                assertEquals(puzzle.sequence[0], puzzle.sequence[2])
                assertEquals(puzzle.sequence[1], puzzle.sequence[3])
                assertEquals(puzzle.sequence[0], puzzle.answer)
            } else {
                assertEquals(puzzle.sequence[0], puzzle.sequence[2])
                assertEquals(puzzle.sequence[1], puzzle.answer)
            }
        }
    }

    @Test
    fun knownAlternatingLevelsUseFirstIconAsAnswer() {
        listOf(3, 6, 9, 12, 15, 18).forEach { level ->
            val puzzle = PatternPuzzleFactory.create(level)
            assertEquals("level $level answer", puzzle.sequence[0], puzzle.answer)
            assertTrue("level $level choices", puzzle.answer in puzzle.choices)
        }
    }
}

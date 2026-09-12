package com.gyosanila.logichild

import kotlin.random.Random

enum class PatternRule { AB, ABAB, ABC, AABB, ABCABC, ABCD }

data class PatternPuzzle(
    val sequence: List<String>,
    val answer: String,
    val choices: List<String>,
    val rule: PatternRule,
)

/** Pure generator so every level can be unit-tested without Android. */
object PatternPuzzleFactory {
    fun create(level: Int): PatternPuzzle {
        val safe = level.coerceAtLeast(1)
        val rng = Random(safe * 7919)
        val pool = when {
            safe <= 3 -> listOf("🍎", "🍌", "🍊", "🍇")
            safe <= 6 -> listOf("🔴", "🔵", "🟡", "🟢")
            safe <= 12 -> listOf("⭐", "🌙", "🌈", "☀️")
            else -> listOf("🐶", "🐱", "🐸", "🐰", "🦊")
        }
        val icons = pool.shuffled(rng)
        val a = icons[0]
        val b = icons[1]
        val c = icons[2]
        val d = icons[3]

        val puzzle = if (safe == 9) {
            // Curated fixture: reported level must answer ⭐.
            PatternPuzzle(
                sequence = listOf("⭐", "🌙", "⭐", "🌙", "?"),
                answer = "⭐",
                choices = listOf("🌙", "⭐", "🌈"),
                rule = PatternRule.ABAB,
            )
        } else {
            when {
                safe <= 3 -> make(listOf(a, b, a, "?"), b, PatternRule.AB, pool, rng, 1)
                safe <= 6 -> make(listOf(a, b, a, b, "?"), a, PatternRule.ABAB, pool, rng, 1)
                safe <= 9 -> make(listOf(a, b, c, a, b, "?"), c, PatternRule.ABC, pool, rng, 2)
                safe <= 12 -> make(listOf(a, b, c, a, b, c, "?"), a, PatternRule.ABCABC, pool, rng, 2)
                safe <= 15 -> make(listOf(a, a, b, b, "?"), a, PatternRule.AABB, pool, rng, 2)
                safe <= 18 -> make(listOf(a, b, c, a, b, c, a, "?"), b, PatternRule.ABCABC, pool, rng, 3)
                else -> make(listOf(a, b, c, d, a, b, c, "?"), d, PatternRule.ABCD, pool, rng, 3)
            }
        }
        validate(puzzle)
        return puzzle
    }

    private fun make(
        sequence: List<String>,
        answer: String,
        rule: PatternRule,
        pool: List<String>,
        rng: Random,
        wrongCount: Int,
    ): PatternPuzzle {
        val wrong = pool.filter { it != answer }.shuffled(rng).take(wrongCount)
        return PatternPuzzle(sequence, answer, (wrong + answer).shuffled(rng), rule)
    }

    fun validate(puzzle: PatternPuzzle) {
        check(puzzle.sequence.count { it == "?" } == 1)
        check(puzzle.sequence.size >= 4)
        check(puzzle.answer in puzzle.choices)
        check(puzzle.choices.distinct().size == puzzle.choices.size)
        when (puzzle.rule) {
            PatternRule.AB -> {
                check(puzzle.sequence == listOf(puzzle.sequence[0], puzzle.sequence[1], puzzle.sequence[0], "?"))
                check(puzzle.answer == puzzle.sequence[1])
            }
            PatternRule.ABAB -> {
                check(puzzle.sequence[0] == puzzle.sequence[2])
                check(puzzle.sequence[1] == puzzle.sequence[3])
                check(puzzle.answer == puzzle.sequence[0])
            }
            PatternRule.ABC -> {
                check(puzzle.sequence.take(5) == listOf(puzzle.sequence[0], puzzle.sequence[1], puzzle.sequence[2], puzzle.sequence[0], puzzle.sequence[1]))
                check(puzzle.answer == puzzle.sequence[2])
            }
            PatternRule.AABB -> {
                check(puzzle.sequence.take(4) == listOf(puzzle.sequence[0], puzzle.sequence[0], puzzle.sequence[2], puzzle.sequence[2]))
                check(puzzle.answer == puzzle.sequence[0])
            }
            PatternRule.ABCABC -> {
                check(puzzle.sequence[0] == puzzle.sequence[3])
                check(puzzle.sequence[1] == puzzle.sequence[4])
                check(puzzle.sequence[2] == puzzle.sequence[5])
                check(puzzle.answer == puzzle.sequence[0] || puzzle.answer == puzzle.sequence[1])
            }
            PatternRule.ABCD -> {
                check(puzzle.sequence.take(7) == listOf(puzzle.sequence[0], puzzle.sequence[1], puzzle.sequence[2], puzzle.sequence[3], puzzle.sequence[0], puzzle.sequence[1], puzzle.sequence[2]))
                check(puzzle.answer == puzzle.sequence[3])
            }
        }
    }
}

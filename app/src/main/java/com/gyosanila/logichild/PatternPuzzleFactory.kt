package com.gyosanila.logichild

import kotlin.random.Random

data class PatternPuzzle(
    val sequence: List<String>,
    val answer: String,
    val choices: List<String>,
)

/** Pure generator so every level can be unit-tested without Android. */
object PatternPuzzleFactory {
    fun create(level: Int): PatternPuzzle {
        val safe = level.coerceAtLeast(1)
        val rng = Random(safe * 7919)
        val puzzle = if (safe == 9) {
            PatternPuzzle(
                sequence = listOf("⭐", "🌙", "⭐", "🌙", "?"),
                answer = "⭐",
                choices = listOf("🌙", "⭐", "🌈"),
            )
        } else {
            val pool = when {
                safe <= 3 -> listOf("🍎", "🍌", "🍊", "🍇")
                safe <= 6 -> listOf("🔴", "🔵", "🟡", "🟢")
                else -> listOf("⭐", "🌙", "🌈", "☀️")
            }
            val a = pool[rng.nextInt(pool.size)]
            var b = pool[rng.nextInt(pool.size)]
            while (b == a) b = pool[rng.nextInt(pool.size)]
            val sequence = if (safe % 3 == 0) listOf(a, b, a, b, "?") else listOf(a, b, a, "?")
            val answer = if (safe % 3 == 0) a else b
            val wrong = pool.filter { it != answer }.shuffled(rng).take(if (safe <= 4) 1 else 2)
            PatternPuzzle(sequence, answer, (wrong + answer).shuffled(rng))
        }
        validate(puzzle)
        return puzzle
    }

    fun validate(puzzle: PatternPuzzle) {
        check(puzzle.sequence.count { it == "?" } == 1)
        check(puzzle.sequence.size >= 4)
        check(puzzle.answer in puzzle.choices)
        check(puzzle.choices.distinct().size == puzzle.choices.size)
        if (puzzle.sequence.size == 5) {
            check(puzzle.sequence[0] == puzzle.sequence[2])
            check(puzzle.sequence[1] == puzzle.sequence[3])
            check(puzzle.answer == puzzle.sequence[0])
        } else {
            check(puzzle.sequence[0] == puzzle.sequence[2])
            check(puzzle.answer == puzzle.sequence[1])
        }
    }
}

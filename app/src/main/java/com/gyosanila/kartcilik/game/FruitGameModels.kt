package com.gyosanila.kartcilik.game

import kotlin.random.Random

/** Perintah buat robot pemetik buah. */
enum class FruitCommand { FORWARD, LEFT, RIGHT, PICK }

/** Satu level "Petik Buah": grid persegi + buah + batu + start. */
data class FruitLevel(
    val size: Int,
    val fruits: List<Pos>,
    val rocks: List<Pos>,
    val start: Pos,
    val maxCommands: Int,
)

object FruitLevelGen {
    /**
     * Generator level adaptif.
     * Level makin tinggi: grid makin besar, buah & batu makin banyak, blok makin banyak.
     * Dijamin semua buah bisa dicapai (BFS) — tidak pernah buntu.
     */
    fun generate(level: Int, rng: Random = Random.Default): FruitLevel {
        // Difficulty: makin tinggi level makin sulit, tiap 10 level +5%.
        val tier = level / 10
        val diff = 1.0 + tier * 0.05
        val size = minOf(7, 4 + (level + 2) / 3)      // 5..7
        val fruitCount = minOf(4, 1 + (level + 1) / 2) // 1..4
        val rockCount = minOf(10, (1 + (level + 1) * diff).toInt())

        repeat(80) {
            val start = Pos(0, 0)
            val rocks = mutableSetOf<Pos>()
            while (rocks.size < rockCount) {
                val p = Pos(rng.nextInt(size), rng.nextInt(size))
                if (p != start) rocks.add(p)
            }
            val fruits = mutableSetOf<Pos>()
            while (fruits.size < fruitCount) {
                val p = Pos(rng.nextInt(size), rng.nextInt(size))
                if (p != start && p !in rocks) fruits.add(p)
            }
            if (allReachable(size, start, fruits, rocks)) {
                return FruitLevel(
                    size = size,
                    fruits = fruits.toList(),
                    rocks = rocks.toList(),
                    start = start,
                    maxCommands = minOf(16, 5 + level * 2),
                )
            }
        }
        // Fallback: level paling sederhana (nyaris mustahil tercapai)
        return FruitLevel(5, listOf(Pos(0, 1)), emptyList(), Pos(0, 0), 6)
    }

    /** BFS: pastikan semua buah bisa dicapai dari start tanpa lewat batu. */
    fun allReachable(size: Int, start: Pos, fruits: Set<Pos>, rocks: Set<Pos>): Boolean {
        val seen = mutableSetOf(start)
        val queue = ArrayDeque<Pos>().apply { add(start) }
        while (queue.isNotEmpty()) {
            val c = queue.removeLast()
            for (d in Dir.entries) {
                val n = Pos(c.x + d.dx, c.y + d.dy)
                if (n.x < 0 || n.y < 0 || n.x >= size || n.y >= size) continue
                if (n in rocks || !seen.add(n)) continue
                queue.add(n)
            }
        }
        return fruits.all { it in seen }
    }
}

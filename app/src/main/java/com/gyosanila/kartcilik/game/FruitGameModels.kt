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
                    maxCommands = minOf(1000, 5 + level * 2),
                )
            }
        }
        // Fallback: level paling sederhana (nyaris mustahil tercapai)
        return FruitLevel(5, listOf(Pos(0, 1)), emptyList(), Pos(0, 0), 1000)
    }

    /** BFS: pastikan semua buah bisa dicapai dari start tanpa lewat batu. */
    private fun allReachable(size: Int, start: Pos, fruits: Set<Pos>, rocks: Set<Pos>): Boolean {
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

    /**
     * Langkah MINIMUM (blok perintah) untuk memetik semua buah — dipakai rating 5★.
     * TSP kecil: jarak antar titik via BFS (posisi+arah, belok = 1 langkah),
     * lalu coba semua urutan petik (maks 4 buah = 24 permutasi).
     */
    fun minStepsToCollect(level: FruitLevel): Int {
        val rocks = level.rocks.toSet()
        val pts = listOf(level.start) + level.fruits
        val n = pts.size
        val dist = Array(n) { i -> IntArray(n) { j ->
            if (i == j) 0 else bfsDist(level.size, rocks, pts[i], pts[j])
        } }
        val mid = (1 until n).toList()
        var best = Int.MAX_VALUE
        for (perm in permutations(mid)) {
            var d = dist[0][perm[0]]
            for (k in 0 until perm.size - 1) d += dist[perm[k]][perm[k + 1]]
            if (d < best) best = d
        }
        return best
    }

    /** Jarak langkah terpendek dari → ke (boleh hadap arah mana pun di awal). */
    private fun bfsDist(size: Int, rocks: Set<Pos>, from: Pos, to: Pos): Int {
        var best = Int.MAX_VALUE
        for (sd in Dir.entries) {
            val seen = mutableSetOf<Pair<Pos, Dir>>()
            val queue = ArrayDeque<Triple<Pos, Dir, Int>>().apply { add(Triple(from, sd, 0)) }
            seen.add(from to sd)
            while (queue.isNotEmpty()) {
                val (p, d, c) = queue.removeFirst()
                if (p == to) {
                    if (c < best) best = c
                    continue
                }
                val nx = p.x + d.dx
                val ny = p.y + d.dy
                if (nx in 0 until size && ny in 0 until size && Pos(nx, ny) !in rocks) {
                    val np = Pos(nx, ny)
                    if (seen.add(np to d)) queue.add(Triple(np, d, c + 1))
                }
                for (nd in listOf(d.left(), d.right())) {
                    if (seen.add(p to nd)) queue.add(Triple(p, nd, c + 1))
                }
            }
        }
        return best
    }

    private fun <T> permutations(list: List<T>): List<List<T>> =
        if (list.size <= 1) listOf(list)
        else list.flatMap { e -> permutations(list - e).map { listOf(e) + it } }
}

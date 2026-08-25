package com.gyosanila.kartcilik.game

import kotlin.random.Random

/**
 * Generator level dinamis untuk Main Mobil — level TANPA BATAS.
 *
 * - Deterministik: level index yang sama selalu menghasilkan layout yang sama
 *   (anak bisa mengulang level yang sama). Seed = index * 7919 + 13.
 * - Sulit bertahap: grid makin besar, cone makin banyak, rute minimum makin panjang.
 * - Dijamin solvable: BFS cek finish terjangkau & jarak rute >= minPath.
 */
object LevelGen {

    fun generate(index: Int): Level {
        val rng = Random(index * 7919L + 13L)
        // Difficulty: makin tinggi level makin sulit, tiap 10 level +5%.
        val tier = index / 10
        val diff = 1.0 + tier * 0.05
        val w = minOf(7, 4 + index / 4)      // 4..7
        val h = minOf(6, 3 + index / 5)      // 3..6
        val coneCount = minOf(12, (1 + index / 2.0 * diff).toInt())
        val minPath = minOf(14, (3 + index / 2.0 * diff).toInt())

        repeat(300) {
            val start = Pos(0, rng.nextInt(h))      // tepi kiri
            val finish = Pos(w - 1, rng.nextInt(h)) // tepi kanan
            if (start == finish) return@repeat
            val cones = mutableSetOf<Pos>()
            while (cones.size < coneCount) {
                val p = Pos(rng.nextInt(w), rng.nextInt(h))
                if (p != start && p != finish) cones.add(p)
            }
            val dist = bfs(start, finish, w, h, cones)
            if (dist != null && dist >= minPath) {
                return Level(index, w, h, start, Dir.E, finish, cones)
            }
        }
        // Fallback (nyaris mustahil): level termudah
        return Level(index, 4, 3, Pos(0, 1), Dir.E, Pos(3, 1), emptySet())
    }

    /** Jarak terpendek start→finish (BFS), null kalau tidak terjangkau. */
    fun bfs(start: Pos, finish: Pos, w: Int, h: Int, cones: Set<Pos>): Int? {
        val seen = mutableSetOf(start)
        val queue = ArrayDeque<Pair<Pos, Int>>().apply { add(start to 0) }
        while (queue.isNotEmpty()) {
            val (c, d) = queue.removeLast()
            if (c == finish) return d
            for (dir in Dir.entries) {
                val n = Pos(c.x + dir.dx, c.y + dir.dy)
                if (n.x < 0 || n.y < 0 || n.x >= w || n.y >= h) continue
                if (n in cones || !seen.add(n)) continue
                queue.add(n to d + 1)
            }
        }
        return null
    }
}

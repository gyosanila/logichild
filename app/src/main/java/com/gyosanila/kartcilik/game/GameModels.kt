package com.gyosanila.kartcilik.game

data class Pos(val x: Int, val y: Int)

enum class Dir(val dx: Int, val dy: Int) {
    N(0, -1), E(1, 0), S(0, 1), W(-1, 0);

    fun left(): Dir = entries[(ordinal + 3) % 4]
    fun right(): Dir = entries[(ordinal + 1) % 4]

    /** Sudut rotasi untuk gambar kart (derajat, searah jarum jam, 0 = atas). */
    val angleDeg: Float
        get() = when (this) {
            N -> 0f; E -> 90f; S -> 180f; W -> 270f
        }
}

enum class Instruction { FORWARD, LEFT, RIGHT }

/** Reward saat menang: level kelipatan 10 = besar, kelipatan 5 = kecil. */
enum class Reward { NONE, SMALL, BIG }

data class KartState(val pos: Pos, val dir: Dir)

data class Level(
    val index: Int,
    val width: Int,
    val height: Int,
    val start: Pos,
    val startDir: Dir,
    val finish: Pos,
    val cones: Set<Pos>,
) {
    val cells: Set<Pos> get() = buildSet {
        for (x in 0 until width) for (y in 0 until height) add(Pos(x, y))
    }
}

object Levels {
    // Semua solusi sudah diverifikasi dengan simulator Python
    // (lurus = F, belok kiri = L, belok kanan = R).
    val all: List<Level> = listOf(
        Level(0, 4, 3, Pos(0, 1), Dir.E, Pos(3, 1), emptySet()),                                  // FFF
        Level(1, 4, 3, Pos(0, 0), Dir.E, Pos(2, 2), emptySet()),                                  // FFRFF
        Level(2, 4, 3, Pos(3, 0), Dir.W, Pos(1, 2), emptySet()),                                  // FFLFF
        Level(3, 5, 4, Pos(0, 1), Dir.E, Pos(4, 1), setOf(Pos(2, 0), Pos(2, 2))),                 // FFFF
        Level(4, 5, 4, Pos(0, 0), Dir.E, Pos(4, 3), setOf(Pos(2, 0), Pos(2, 1), Pos(2, 2))),      // FRFFFLFFF
        Level(5, 5, 5, Pos(0, 2), Dir.E, Pos(4, 2), setOf(Pos(1, 0), Pos(1, 1), Pos(1, 3), Pos(1, 4))), // FFFF
        Level(6, 6, 4, Pos(0, 0), Dir.E, Pos(5, 3), setOf(Pos(3, 1), Pos(3, 2))),                 // FFFFFRFFF
        Level(7, 6, 4, Pos(0, 3), Dir.E, Pos(5, 0),
            setOf(Pos(2, 0), Pos(2, 1), Pos(2, 2), Pos(4, 1), Pos(4, 2), Pos(4, 3))),              // LFFFRFRFFFLFFLFFFRFF
    )
}

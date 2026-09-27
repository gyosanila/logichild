package com.gyosanila.logichild

/** Pure rules for the 24-stop adventure map; intentionally independent of Android and Compose. */
class AdventureBoard {
    /** Satu tap JALAN = maju 1 kotak; kotak Bonus menambah 2 kotak tanpa berantai. */
    fun step(position: Int): AdventureMove {
        val landing = (position.coerceIn(1, MAP_LENGTH) + 1).coerceAtMost(MAP_LENGTH)
        val effect = tileEffect(landing)
        if (effect != AdventureTileEffect.Bonus) return AdventureMove(landing, effect)
        val bonus = (landing + BONUS_STEPS).coerceAtMost(MAP_LENGTH)
        return AdventureMove(bonus, tileEffect(bonus), bonusApplied = true)
    }

    companion object {
        const val MAP_LENGTH = 24
        const val BONUS_STEPS = 2

        fun tileEffect(position: Int): AdventureTileEffect = when (position) {
            1 -> AdventureTileEffect.Start
            3, 9, 15, 19, 23 -> AdventureTileEffect.Star
            4, 8, 12, 17, 21 -> AdventureTileEffect.MiniGame
            6, 18 -> AdventureTileEffect.Bonus
            7, 14, 20 -> AdventureTileEffect.Collect
            11, 22 -> AdventureTileEffect.Gift
            MAP_LENGTH -> AdventureTileEffect.Finish
            else -> AdventureTileEffect.None
        }
    }
}

/** Jeda layar reward mini-game sebelum otomatis balik ke papan. */
const val ADVENTURE_REWARD_MS = 2200L

data class AdventureMove(
    val position: Int,
    val effect: AdventureTileEffect,
    val bonusApplied: Boolean = false,
)

enum class AdventureTileEffect { None, Start, Star, MiniGame, Bonus, Collect, Gift, Finish }

data class AdventureState(
    val position: Int = 1,
    val stars: Int = 0,
    val fruits: Int = 0,
    val mapComplete: Boolean = false,
    val adventureNumber: Int = 1,
    /** Jumlah petak mini-game yang pernah dipicu; dipakai untuk pilihan round-robin persisten. */
    val miniGameIndex: Int = 0,
) {
    /** Tiap 5 apel = 1 baju beruang. Diturunkan dari apel, jadi tidak perlu disimpan & tidak bisa hilang. */
    val outfits: Int get() = fruits / APPLES_PER_OUTFIT

    /** Efek kotak tempat mendarat. MiniGame tidak menambah apa pun di sini: bintangnya dari [withGameRating]. */
    fun land(move: AdventureMove): AdventureState {
        val moved = copy(position = move.position)
        return when (move.effect) {
            AdventureTileEffect.MiniGame -> moved.copy(miniGameIndex = miniGameIndex + 1)
            AdventureTileEffect.Star, AdventureTileEffect.Gift -> moved.copy(stars = stars + 1)
            AdventureTileEffect.Collect -> moved.copy(fruits = fruits + 1)
            AdventureTileEffect.Finish -> moved.copy(mapComplete = true)
            else -> moved
        }
    }

    /** Bintang petualangan += rating game (1–5). Contoh: dapat 4 bintang → +4. */
    fun withGameRating(rating: Int): AdventureState = copy(stars = stars + rating.coerceIn(1, 5))

    /** Main ulang peta yang sudah tamat; bintang & apel tetap. */
    fun restartMap(): AdventureState = copy(position = 1, mapComplete = false)

    companion object {
        const val APPLES_PER_OUTFIT = 5

        fun fromStored(position: Int, stars: Int, fruits: Int, mapComplete: Boolean, adventureNumber: Int = 1, miniGameIndex: Int = 0) = AdventureState(
            position = position.coerceIn(1, AdventureBoard.MAP_LENGTH),
            stars = stars.coerceAtLeast(0),
            fruits = fruits.coerceAtLeast(0),
            mapComplete = mapComplete,
            adventureNumber = adventureNumber.coerceAtLeast(1),
            miniGameIndex = miniGameIndex.coerceAtLeast(0),
        )
    }

    fun nextAdventure(): AdventureState = copy(position = 1, mapComplete = false, adventureNumber = adventureNumber + 1)
}

/** Level petualangan berurutan ganjil: petualangan 1=level 1, 2=3, 3=5, ... */
fun adventureLevel(adventureNumber: Int): Int = ((adventureNumber.coerceAtLeast(1) - 1) * 2) + 1

/** Siklus stabil: urutan peta mini-game berputar tanpa mengulang game sebelumnya. */
fun adventureMiniGame(index: Int): String = listOf("kart", "fruit", "pattern", "color")[index.mod(4)]

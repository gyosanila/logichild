package com.gyosanila.logichild

import kotlin.random.Random

/** Pure rules for the 24-stop adventure map; intentionally independent of Android and Compose. */
class AdventureBoard(private val random: Random = Random.Default) {
    fun roll(): Int = random.nextInt(from = 1, until = 4)

    fun move(position: Int, steps: Int): AdventureMove {
        val start = position.coerceIn(1, MAP_LENGTH)
        val landing = (start + steps.coerceAtLeast(0)).coerceAtMost(MAP_LENGTH)
        return AdventureMove(landing, tileEffect(landing))
    }

    /** Resolves one landing only; a bonus's extra movement cannot chain another tile effect. */
    fun moveAndResolve(position: Int, steps: Int): AdventureMove {
        val initial = move(position, steps)
        if (initial.effect != AdventureTileEffect.Bonus) return initial
        val bonusDestination = (initial.position + BONUS_STEPS).coerceAtMost(MAP_LENGTH)
        return AdventureMove(bonusDestination, tileEffect(bonusDestination), bonusApplied = true)
    }

    private fun tileEffect(position: Int): AdventureTileEffect = when (position) {
        3, 9, 15, 19, 23 -> AdventureTileEffect.Star
        4, 8, 12, 17, 21 -> AdventureTileEffect.MiniGame
        6, 18 -> AdventureTileEffect.Bonus
        7, 14, 20 -> AdventureTileEffect.Collect
        11, 22 -> AdventureTileEffect.Gift
        MAP_LENGTH -> AdventureTileEffect.Finish
        else -> AdventureTileEffect.None
    }

    companion object {
        const val MAP_LENGTH = 24
        const val BONUS_STEPS = 2
    }
}

data class AdventureMove(
    val position: Int,
    val effect: AdventureTileEffect,
    val bonusApplied: Boolean = false,
)

enum class AdventureTileEffect { None, Star, MiniGame, Bonus, Collect, Gift, Finish }

data class AdventureState(
    val position: Int = 1,
    val stars: Int = 0,
    val fruits: Int = 0,
    val mapComplete: Boolean = false,
) {
    fun toStored(): Map<String, Any> = mapOf(
        "adv_position" to position,
        "adv_stars" to stars,
        "adv_fruits" to fruits,
        "adv_maps_done" to if (mapComplete) 1 else 0,
    )

    companion object {
        fun fromStored(position: Int, stars: Int, fruits: Int, mapComplete: Boolean) = AdventureState(
            position = position.coerceIn(1, AdventureBoard.MAP_LENGTH),
            stars = stars.coerceAtLeast(0),
            fruits = fruits.coerceAtLeast(0),
            mapComplete = mapComplete,
        )
    }
}

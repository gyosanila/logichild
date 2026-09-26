package com.gyosanila.logichild

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class AdventureBoardTest {
    @Test fun rollIsAlwaysOneThroughThree() {
        val board = AdventureBoard(Random(42))
        repeat(10_000) { assertTrue(board.roll() in 1..3) }
    }

    @Test fun moveClampsAtFinish() {
        val board = AdventureBoard(Random(1))
        assertEquals(24, board.move(23, 3).position)
        assertEquals(24, board.move(24, 2).position)
    }

    @Test fun bonusMovesTwoButDoesNotChain() {
        val board = AdventureBoard(Random(1))
        // Tile 6 is a bonus, tile 18 is also a bonus: only the landing tile resolves.
        val result = board.moveAndResolve(5, 1)
        assertEquals(8, result.position)
        assertEquals(AdventureTileEffect.MiniGame, result.effect)
        assertTrue(result.bonusApplied)
    }

    @Test fun bonusOnFinalTwoStopsDoesNotChainAgain() {
        val result = AdventureBoard(Random(1)).moveAndResolve(17, 1)
        assertEquals(20, result.position)
        assertEquals(AdventureTileEffect.Collect, result.effect)
        assertTrue(result.bonusApplied)
    }

    @Test fun landingOnFinishReturnsFinishEffect() {
        val result = AdventureBoard(Random(1)).moveAndResolve(22, 3)
        assertEquals(24, result.position)
        assertEquals(AdventureTileEffect.Finish, result.effect)
    }

    @Test fun minigameTileLeavesPositionAtLandingTile() {
        val board = AdventureBoard(Random(1))
        val result = board.moveAndResolve(3, 1)
        assertEquals(4, result.position)
        assertEquals(AdventureTileEffect.MiniGame, result.effect)
    }

    @Test fun savingAndRestoringClampsState() {
        val state = AdventureState(position = 15, stars = 7, fruits = 2, mapComplete = false)
        assertEquals(state, AdventureState.fromStored(15, 7, 2, false))
        assertEquals(AdventureState(24, 0, 0, false), AdventureState.fromStored(99, -1, -3, false))
    }
}

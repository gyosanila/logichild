package com.gyosanila.logichild

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AdventureBoardTest {
    private val board = AdventureBoard()

    @Test fun jalanMovesExactlyOneTile() {
        assertEquals(2, board.step(1).position)
        assertEquals(4, board.step(3).position)
    }

    @Test fun stepClampsAtFinish() {
        assertEquals(24, board.step(23).position)
        assertEquals(24, board.step(24).position)
    }

    @Test fun bonusMovesTwoMoreButDoesNotChain() {
        val result = board.step(5) // 6 = bonus -> 8
        assertEquals(8, result.position)
        assertEquals(AdventureTileEffect.MiniGame, result.effect)
        assertTrue(result.bonusApplied)
        assertEquals(20, board.step(17).position) // 18 bonus -> 20, no chain
    }

    @Test fun miniGameStarsEqualGameRating() {
        // Contoh user: kotak 5, game mobil, dapat 4 bintang -> +4.
        val s = AdventureState(position = 4, stars = 10)
        assertEquals(14, s.withGameRating(4).stars)
        assertEquals(11, s.withGameRating(0).stars) // minimal 1
        assertEquals(15, s.withGameRating(9).stars) // maksimal 5
    }

    @Test fun landingEffects() {
        val s = AdventureState(position = 2)
        assertEquals(1, s.land(board.step(2)).stars)           // 3 bintang
        assertEquals(1, AdventureState(6).land(board.step(6)).fruits) // 7 apel
        val mg = AdventureState(3, stars = 5).land(board.step(3))
        assertEquals(5, mg.stars)                              // minigame: nunggu rating
        assertTrue(AdventureState(23).land(board.step(23)).mapComplete)
    }

    @Test fun restartKeepsRewards() {
        val done = AdventureState(24, stars = 42, fruits = 3, mapComplete = true).restartMap()
        assertEquals(AdventureState(1, 42, 3, false), done)
    }

    @Test fun adventureLevelsAdvanceByTwoStartingAtOne() {
        assertEquals(1, adventureLevel(1))
        assertEquals(3, adventureLevel(2))
        assertEquals(5, adventureLevel(3))
        assertEquals(1999, adventureLevel(1000))
    }

    @Test fun nextAdventureStartsAtTileOneAndKeepsRewards() {
        val done = AdventureState(24, stars = 12, fruits = 6, mapComplete = true, adventureNumber = 1)
        assertEquals(AdventureState(1, 12, 6, false, 2), done.nextAdventure())
    }

    @Test fun everyFiveApplesUnlocksOutfit() {
        assertEquals(0, AdventureState(fruits = 4).outfits)
        assertEquals(1, AdventureState(fruits = 4).land(board.step(6)).outfits) // apel ke-5
        assertEquals(2, AdventureState(fruits = 11).outfits)
    }

    @Test fun savingAndRestoringClampsState() {
        val state = AdventureState(position = 15, stars = 7, fruits = 2, mapComplete = false)
        assertEquals(state, AdventureState.fromStored(15, 7, 2, false))
        assertEquals(AdventureState(24, 0, 0, false), AdventureState.fromStored(99, -1, -3, false))
    }
}

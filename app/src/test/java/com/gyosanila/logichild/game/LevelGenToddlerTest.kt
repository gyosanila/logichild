package com.gyosanila.logichild.game

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LevelGenToddlerTest {
    @Test fun firstThreeLevelsAreStraightAndConeFree() {
        for (index in 0..2) {
            val level = LevelGen.generate(index)
            assertTrue("level $index has no cones", level.cones.isEmpty())
            assertEquals("level $index needs no turns", level.startDir, Dir.E)
            assertEquals("level $index is a straight row", level.start.y, level.finish.y)
            assertEquals("level $index solution is only forward", level.finish.x - level.start.x, index + 1)
            var kart = KartState(level.start, level.startDir)
            repeat(index + 1) { kart = GameEngine.apply(kart, Instruction.FORWARD, level).first }
            assertEquals("level $index reaches finish", level.finish, kart.pos)
        }
    }

    @Test fun earlyLevelsGrowFromOneToThreeForwardSteps() {
        val steps = (0..2).map { LevelGen.generate(it).finish.x - LevelGen.generate(it).start.x }
        assertEquals(listOf(1, 2, 3), steps)
    }

    @Test fun generatedLevelsRemainReachableAfterTheTutorial() {
        for (index in 3..60) {
            val level = LevelGen.generate(index)
            assertTrue("level $index remains solvable", LevelGen.bestInstructions(
                level.start, level.startDir, level.finish, level.width, level.height, level.cones,
            ) != null)
            assertTrue(level.start !in level.cones)
            assertTrue(level.finish !in level.cones)
        }
    }
}

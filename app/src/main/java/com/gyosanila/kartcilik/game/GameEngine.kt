package com.gyosanila.kartcilik.game

sealed interface StepResult {
    data class Moved(val from: Pos, val to: Pos, val dir: Dir) : StepResult
    data class Turned(val dir: Dir) : StepResult
    data class Crashed(val at: Pos, val dir: Dir) : StepResult
    data class Won(val at: Pos) : StepResult
}

object GameEngine {

    /** Terapkan satu instruksi. Kembalikan state baru + hasil. */
    fun apply(state: KartState, instr: Instruction, level: Level): Pair<KartState, StepResult> {
        return when (instr) {
            Instruction.LEFT -> {
                val d = state.dir.left()
                KartState(state.pos, d) to StepResult.Turned(d)
            }
            Instruction.RIGHT -> {
                val d = state.dir.right()
                KartState(state.pos, d) to StepResult.Turned(d)
            }
            Instruction.FORWARD -> {
                val next = Pos(state.pos.x + state.dir.dx, state.pos.y + state.dir.dy)
                if (next.x !in 0 until level.width || next.y !in 0 until level.height ||
                    next in level.cones
                ) {
                    state to StepResult.Crashed(state.pos, state.dir)
                } else if (next == level.finish) {
                    KartState(next, state.dir) to StepResult.Won(next)
                } else {
                    KartState(next, state.dir) to StepResult.Moved(state.pos, next, state.dir)
                }
            }
        }
    }
}

package dev.robocode.tankroyale.server.model

/** Mutable state of a round in a battle. */
data class MutableRound(
    override var roundNumber: Int,

    override val turns: MutableList<ITurn> = mutableListOf(),

    /** Number of turns since round ended */
    private var turnsSinceRoundEnded: Int = -1

) : IRound {

    override var roundEnded: Boolean
        get() = turnsSinceRoundEnded >= 0
        set(ended) {
            turnsSinceRoundEnded = if (ended) 0 else -1
        }

    override val roundEndedThisTurn: Boolean
        get() = turnsSinceRoundEnded == 0

    override val canStartNewRound: Boolean
        get() = turnsSinceRoundEnded >= NUMBER_OF_TURNS_AFTER_ROUND_ENDED || isFirstTurn()

    fun incrementTurnSinceRoundEndedIfRoundHasEnded() {
        if (turnsSinceRoundEnded >= 0) {
            turnsSinceRoundEnded++
        }
    }

    private fun isFirstTurn() = (roundNumber == 0 && (lastTurn?.turnNumber ?: 0) == 0)
}
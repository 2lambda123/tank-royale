package dev.robocode.tankroyale.server.model

/** Mutable state of a round in a battle. */
data class MutableRound(
    override var roundNumber: Int,

    override val turns: MutableList<ITurn> = mutableListOf(),

    /** Number of turns since round ended */
    private var turnsSinceRoundEnded: Int = -1

) : IRound {

    override fun setRoundEnded(ended: Boolean) {
        turnsSinceRoundEnded = if (ended) {
            maxOf(turnsSinceRoundEnded, 0)
        } else {
            -1
        }
    }

    override fun isRoundEnded() = turnsSinceRoundEnded >= 0

    override fun hasRoundEndedThisTurn() = turnsSinceRoundEnded == 0

    override fun canStartNewRound() = isFirstTurn() || isTurnsSinceRoundEndedReached()

    fun incrementTurnSinceRoundEndedIfRoundHasEnded() {
        if (turnsSinceRoundEnded >= 0) {
            turnsSinceRoundEnded++
        }
    }

    private fun isTurnsSinceRoundEndedReached() = turnsSinceRoundEnded >= NUMBER_OF_TURNS_AFTER_ROUND_ENDED

    private fun isFirstTurn() = (roundNumber == 0 && (lastTurn?.turnNumber ?: 0) == 0)
}
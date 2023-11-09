package dev.robocode.tankroyale.server.model

const val NUMBER_OF_TURNS_AFTER_ROUND_ENDED = 50

/** Round interface */
interface IRound {
    /** Round number */
    val roundNumber: Int

    /** List of turns */
    val turns: List<ITurn>

    /** Set round to ended */
    fun setRoundEnded(ended: Boolean)

    /** Check if round is still running */
    fun isRoundRunning(): Boolean

    /** Check if round has ended */
    fun isRoundEnded(): Boolean

    /** Check if round has ended at this last turn in the round */
    fun hasRoundEndedThisTurn(): Boolean

    /** Check if a new round can be started.
     * (when round has ended and a certain number of turns have passed as well).
     */
    fun canStartNewRound(): Boolean

    /** Last turn */
    val lastTurn: ITurn? get() = if (turns.isNotEmpty()) turns[turns.size - 1] else null
}

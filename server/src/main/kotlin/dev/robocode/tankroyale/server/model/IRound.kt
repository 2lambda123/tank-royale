package dev.robocode.tankroyale.server.model

const val NUMBER_OF_TURNS_AFTER_ROUND_ENDED = 50

/** Round interface */
interface IRound {
    /** Round number */
    val roundNumber: Int

    /** List of turns */
    val turns: List<ITurn>

    /** Flag specifying if round has ended this turn or some turns ago */
    val roundEnded: Boolean

    /** Flag specifying if round has ended this exact turn */
    val roundEndedThisTurn: Boolean

    /** Flag specifying if a new round can be started (some turns after round has ended) */
    val canStartNewRound: Boolean

    /** Last turn */
    val lastTurn: ITurn? get() = if (turns.isNotEmpty()) turns[turns.size - 1] else null
}

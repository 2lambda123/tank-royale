package dev.robocode.tankroyale.gui.client

import dev.robocode.tankroyale.gui.model.*
import dev.robocode.tankroyale.gui.server.ServerProcess
import dev.robocode.tankroyale.gui.settings.ServerSettings
import dev.robocode.tankroyale.gui.ui.tps.TpsEventChannel
import dev.robocode.tankroyale.gui.util.Event
import dev.robocode.tankroyale.gui.util.Version
import kotlinx.serialization.PolymorphicSerializer
import java.io.Closeable
import java.net.URI
import java.util.*

object Client : AutoCloseable {

    init {
        TpsEventChannel.onTpsChanged.subscribe(this) { changeTps(it.tps) }
    }

    // public events
    val onConnected = Event<Unit>()
    val onDisconnected = Event<Unit>()
    val onError = Event<Throwable>()

    val onBotListUpdate = Event<BotListUpdate>()

    val onGameStarted = Event<GameStartedEvent>()
    val onGameEnded = Event<GameEndedEvent>()
    val onGameAborted = Event<GameAbortedEvent>()
    val onGamePaused = Event<GamePausedEvent>()
    val onGameResumed = Event<GameResumedEvent>()

    private val onRoundStarted = Event<RoundStartedEvent>()
    private val onRoundEnded = Event<RoundEndedEvent>()

    val onTickEvent = Event<TickEvent>()

    var currentGameSetup: GameSetup? = null

    private var isGameRunning: Boolean = false

    var isGamePaused: Boolean = false
        private set

    private val isConnected: Boolean get() = websocket.isOpen()

    private var participants = listOf<Participant>()
    private var bots = setOf<BotInfo>()

    val joinedBots: Set<BotInfo>
        get() {
            return bots
        }

    private var websocket: WebSocketClient = WebSocketClient(URI(ServerSettings.serverUrl))

    private val json = MessageConstants.json

    private var gameTypes = setOf<String>()

    private val disposables = mutableListOf<Closeable>()

    private var lastStartGame: StartGame? = null

    private var tps: Int? = null

    override fun close() {
        stopGame()

        if (isConnected) websocket.close()

        onDisconnected.fire(Unit)
    }

    fun connect(url: String) {
        disposables.forEach { it.close() }

        websocket = WebSocketClient(URI(url)).apply {
            onOpen.subscribe(websocket) { onConnected.fire(Unit) }
            onClose.subscribe(websocket) { onDisconnected.fire(Unit) }
            onMessage.subscribe(websocket) { onMessage(it) }
            onError.subscribe(websocket) { onError.fire(it) }

            open() // must be called after onOpen.subscribe()
        }
    }

    fun startGame(gameSetup: IGameSetup, botAddresses: Set<BotAddress>) {
        if (isGameRunning) {
            stopGame()
        }
        if (isConnected) {
            lastStartGame = StartGame(gameSetup.toGameSetup(), botAddresses)
            val startGame = lastStartGame

            websocket.send(startGame!!)
        }
    }

    fun stopGame() {
        if (isGameRunning && websocket.isOpen()) {
            websocket.send(StopGame())
        }
        isGamePaused = false
    }

    fun restartGame() {
        resumeGame()
        stopGame()
        websocket.send(lastStartGame!!)
    }

    fun pauseGame() {
        if (isGameRunning && !isGamePaused) {
            websocket.send(PauseGame())
        }
    }

    fun resumeGame() {
        if (isGameRunning && isGamePaused) {
            websocket.send(ResumeGame())
        }
    }

    fun getParticipant(id: Int): Participant = participants.first { participant -> participant.id == id }

    private fun changeTps(tps: Int) {
        if (isGameRunning && tps != this.tps) {
            this.tps = tps
            websocket.send(ChangeTps(tps))
        }
    }

    private fun onMessage(msg: String) {
        when (val type = json.decodeFromString(PolymorphicSerializer(Message::class), msg)) {
            is TickEvent -> handleTickEvent(type)
            is ServerHandshake -> handleServerHandshake(type)
            is BotListUpdate -> handleBotListUpdate(type)
            is GameStartedEvent -> handleGameStarted(type)
            is GameEndedEvent -> handleGameEnded(type)
            is GameAbortedEvent -> handleGameAborted(type)
            is GamePausedEvent -> handleGamePaused(type)
            is GameResumedEvent -> handleGameResumed(type)
            is RoundStartedEvent -> handleRoundStarted(type)
            is RoundEndedEvent -> handleRoundEnded(type)
            is TpsChangedEvent -> handleTpsChanged(type)
            else -> throw IllegalArgumentException("Unknown content type: $type")
        }
    }

    private fun handleServerHandshake(serverHandshake: ServerHandshake) {
        gameTypes = serverHandshake.gameTypes

        val handshake = ControllerHandshake(
            name = "Robocode Tank Royale UI",
            version = "${Version.getVersion()}",
            author = "Flemming N. Larsen",
            secret = ServerProcess.secret
        )
        websocket.send(handshake)
    }

    private fun handleBotListUpdate(botListUpdate: BotListUpdate) {
        bots = Collections.unmodifiableSet(botListUpdate.bots)
        onBotListUpdate.fire(botListUpdate)
    }

    private fun handleGameStarted(gameStartedEvent: GameStartedEvent) {
        isGameRunning = true
        currentGameSetup = gameStartedEvent.gameSetup
        participants = gameStartedEvent.participants

        onGameStarted.fire(gameStartedEvent)
    }

    private fun handleGameEnded(gameEndedEvent: GameEndedEvent) {
        isGameRunning = false
        isGamePaused = false
        onGameEnded.fire(gameEndedEvent)
    }

    private fun handleGameAborted(gameAbortedEvent: GameAbortedEvent) {
        isGameRunning = false
        isGamePaused = false
        onGameAborted.fire(gameAbortedEvent)
    }

    private fun handleGamePaused(gamePausedEvent: GamePausedEvent) {
        isGamePaused = true
        onGamePaused.fire(gamePausedEvent)
    }

    private fun handleGameResumed(gameResumedEvent: GameResumedEvent) {
        isGamePaused = false
        onGameResumed.fire(gameResumedEvent)
    }

    private fun handleRoundStarted(roundStartedEvent: RoundStartedEvent) {
        onRoundStarted.fire(roundStartedEvent)
    }

    private fun handleRoundEnded(roundEndedEvent: RoundEndedEvent) {
        onRoundEnded.fire(roundEndedEvent)
    }

    private fun handleTickEvent(tickEvent: TickEvent) {
        onTickEvent.fire(tickEvent)
    }

    private fun handleTpsChanged(tpsChangedEvent: TpsChangedEvent) {
        TpsEventChannel.onTpsChanged.fire(tpsChangedEvent)
    }
}
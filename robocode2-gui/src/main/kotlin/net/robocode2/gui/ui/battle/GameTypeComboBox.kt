package net.robocode2.gui.ui.battle

import net.robocode2.gui.settings.GameSetup
import net.robocode2.gui.settings.GamesSettings
import net.robocode2.gui.utils.Observable
import javax.swing.JComboBox

class GameTypeComboBox : JComboBox<String>(GamesSettings.games.keys.toTypedArray()) {

    val onGameTypeChanged = Observable<String>()

    val gameSetup: GameSetup
        get() = GamesSettings.games[selectedGameType] as GameSetup

    private val selectedGameType: String
        get() = selectedItem as String

    init {
        addActionListener { notifyGameTypeChanged() }
    }

    fun resetGameType() {
        val default: GameSetup? = GamesSettings.defaultGameSetup[selectedGameType]
        if (default != null) {
            GamesSettings.games[selectedGameType] = default.copy()
            notifyGameTypeChanged()
        }
    }

    fun notifyGameTypeChanged() {
        onGameTypeChanged.notify(selectedGameType)
    }
}
package dev.robocode.tankroyale.gui.settings

import dev.robocode.tankroyale.gui.ui.server.ServerEventTriggers
import dev.robocode.tankroyale.gui.util.RegisterWsProtocol
import dev.robocode.tankroyale.gui.util.WsUrl
import java.net.URI
import java.util.*
import javax.crypto.KeyGenerator

object ServerSettings : PropertiesStore("Robocode Server Settings", "server.properties") {

    const val DEFAULT_SCHEME = "ws"
    const val DEFAULT_PORT = 7654
    const val DEFAULT_URL = "$DEFAULT_SCHEME://localhost"

    private const val CURRENT_SERVER_URL = "current-server-url"
    private const val SERVER_URLS = "server-urls"
    private const val CONTROLLER_SECRETS = "controller-secrets"
    private const val BOT_SECRETS = "bots-secrets"
    private const val INITIAL_POSITION_ENABLED = "initial-position-enabled"

    init {
        RegisterWsProtocol // work-around for ws:// with URI class
        load()

        onSaved.subscribe(this) { ServerEventTriggers.onRebootServer.fire(true /* setting changed */) }
    }

    var currentServerUrl: String
        get() {
            val url = properties.getProperty(CURRENT_SERVER_URL, DEFAULT_URL)
            return WsUrl(url).origin
        }
        set(value) {
            properties.setProperty(CURRENT_SERVER_URL, value)
        }

    val serverPort: Int get() = URI(currentServerUrl).port

    var serverUrls: List<String>
        get() {
            val urls = properties.getProperty(SERVER_URLS, "")
            return if (urls.isBlank()) {
                listOf(currentServerUrl)
            } else {
                urls.split(",")
            }
        }
        set(value) {
            val list = ArrayList(value)
            list.remove(DEFAULT_URL)
            properties.setProperty(SERVER_URLS, list.joinToString(","))
        }

    var controllerSecrets: Set<String>
        get() = getPropertyAsSet(CONTROLLER_SECRETS).ifEmpty {
            controllerSecrets = setOf(generateSecret())
            controllerSecrets
        }
        set(value) {
            setPropertyBySet(CONTROLLER_SECRETS, value)
            save()
        }

    var botSecrets: Set<String>
        get() = getPropertyAsSet(BOT_SECRETS).ifEmpty {
            botSecrets = setOf(generateSecret())
            botSecrets
        }
        set(value) {
            setPropertyBySet(BOT_SECRETS, value)
            save()
        }

    private fun generateSecret(): String {
        val secretKey = KeyGenerator.getInstance("AES").generateKey()
        val encodedKey = Base64.getEncoder().encodeToString(secretKey.encoded)
        // Remove trailing '=='
        return encodedKey.substring(0, encodedKey.length - 2)
    }

    var initialPositionsEnabled: Boolean
        get() {
            load()
            return properties.getProperty(INITIAL_POSITION_ENABLED, "false").toBoolean()
        }
        set(value) {
            properties.setProperty(INITIAL_POSITION_ENABLED, value.toString())
            save()
        }
}
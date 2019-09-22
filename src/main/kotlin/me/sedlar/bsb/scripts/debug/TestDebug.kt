package me.sedlar.bsb.scripts.debug

import me.sedlar.bsb.api.core.Script
import me.sedlar.bsb.api.game.GameScreen
import me.sedlar.bsb.api.game.osrs.OSRSGameState
import me.sedlar.bsb.api.game.osrs.OSRSInventory
import me.sedlar.bsb.api.game.osrs.core.GameState
import java.awt.Color
import java.awt.Graphics2D
import kotlin.system.measureTimeMillis

class TestDebug : Script(
    author = "Static",
    name = "Test Debug"
) {

    private var atLogin = false
    private var atLobby = false
    private var slots: ArrayList<GameScreen.PRect>? = null
    private var invCount = 0
    private var invOpen = false
    private var settingsOpen = false
    private var dropMode = false

    private var ms = 0L

    override fun drawDebug(g: Graphics2D) {
        g.color = Color.green

        g.drawString(invCount.toString(), GameScreen.px(92.44), GameScreen.py(37.27))

        slots?.toMutableList()?.forEach {
            g.draw(it.toScreen())
        }

        g.draw(GameState.SETTINGS_TAB_BOUNDS.toScreen())

        var y = 50

        g.drawString("At Login: $atLogin", 50, y.apply { y += 20 })
        g.drawString("At Lobby: $atLobby", 50, y.apply { y += 20 })
        g.drawString("Inv Open: $invOpen", 50, y.apply { y += 20 })
        g.drawString("Settings Open: $settingsOpen", 50, y.apply { y += 20 })
        g.drawString("Drop Mode: $dropMode", 50, y.apply { y += 20 })
        g.drawString("Time: $ms", 50, y.apply { y += 20 })
    }

    override fun loop(): Int {
        atLogin = OSRSGameState.isAtLogin()
        atLobby = OSRSGameState.isAtLobby()
        slots = OSRSInventory.slots()
        invCount = OSRSInventory.count()
        invOpen = OSRSInventory.viewing()
        settingsOpen = OSRSGameState.isSettingsOpen()
        dropMode = OSRSGameState.isDropMode()

        measureTimeMillis {
            OSRSGameState.isInGame()
        }.let {
            ms = it
        }
        return 100
    }
}
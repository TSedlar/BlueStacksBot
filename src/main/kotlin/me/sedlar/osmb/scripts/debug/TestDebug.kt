package me.sedlar.osmb.scripts.debug

import me.sedlar.osmb.api.core.Keyboard
import me.sedlar.osmb.api.core.Script
import me.sedlar.osmb.api.game.GameScreen
import me.sedlar.osmb.api.game.GameState
import me.sedlar.osmb.api.game.Inventory
import java.awt.Color
import java.awt.Graphics2D
import kotlin.system.measureTimeMillis

class TestDebug : Script() {

    private var atLogin = false
    private var atLobby = false
    private var slots: ArrayList<GameScreen.PRect>? = null
    private var invCount = 0
    private var invOpen = false
    private var dropMode = false

    private var ms = 0L

    override fun drawDebug(g: Graphics2D) {
        g.color = Color.green

        g.drawString(invCount.toString(), GameScreen.px(95.44), GameScreen.py(37.27))

        slots?.toMutableList()?.forEach {
            g.draw(it.toScreen())
        }

        var y = 50

        g.drawString("At Login: $atLogin", 50, y.apply { y += 20 })
        g.drawString("At Lobby: $atLobby", 50, y.apply { y += 20 })
        g.drawString("Inv Open: $invOpen", 50, y.apply { y += 20 })
        g.drawString("Drop Mode: $dropMode", 50, y.apply { y += 20 })
        g.drawString("Time: $ms", 50, y.apply { y += 20 })
    }

    override fun loop(): Int {
        atLogin = GameState.isAtLogin()
        atLobby = GameState.isAtLobby()
        slots = Inventory.slots()
        invCount = Inventory.count()
        invOpen = Inventory.viewing()
        dropMode = GameState.isDropMode()

        measureTimeMillis {
            GameState.isInGame()
        }.let {
            ms = it
        }
        return 100
    }

    override fun toString(): String {
        return "Test Debug"
    }
}
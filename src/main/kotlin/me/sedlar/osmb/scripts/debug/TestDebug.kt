package me.sedlar.osmb.scripts.debug

import me.sedlar.osmb.api.core.Script
import me.sedlar.osmb.api.game.GameScreen
import me.sedlar.osmb.api.game.Inventory
import java.awt.Color
import java.awt.Graphics2D
import java.awt.Rectangle

class TestDebug : Script() {

    private var slots: ArrayList<GameScreen.PRect>? = null
    private var invCount = 0

    override fun drawDebug(g: Graphics2D) {
        g.color = Color.green

        g.drawString(invCount.toString(), GameScreen.px(95.44), GameScreen.py(37.27))

        slots?.toMutableList()?.forEach {
            g.draw(it.toScreen())
        }
    }

    override fun loop(): Int {
        slots = Inventory.slots()
        invCount = Inventory.count()
        return 100
    }

    override fun toString(): String {
        return "Test Debug"
    }
}
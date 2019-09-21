package me.sedlar.bsb.scripts

import javafx.scene.layout.Pane
import me.sedlar.bsb.api.game.osrs.core.Inventory
import me.sedlar.bsb.api.game.osrs.core.OSRSScript
import java.awt.Graphics2D

class EmptyScript : OSRSScript(
    author = "",
    name = "Empty Script"
) {

    override fun createUI(parent: Pane) {
    }

    override fun onStart() {
    }

    override fun onStop() {
    }

    override fun drawDebug(g: Graphics2D) {
    }

    override fun loop(): Int {
        println("${Inventory.isFull()} - ${Inventory.viewing()}")
        return 1000
    }
}
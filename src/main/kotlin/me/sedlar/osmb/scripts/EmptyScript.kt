package me.sedlar.osmb.scripts

import javafx.scene.layout.Pane
import me.sedlar.osmb.api.core.Script

class EmptyScript : Script() {

    override fun createUI(parent: Pane) {
    }

    override fun onStart() {
    }

    override fun onStop() {
    }

    override fun loop(): Int {
        return 1000
    }

    override fun toString(): String {
        return "Empty Script"
    }
}
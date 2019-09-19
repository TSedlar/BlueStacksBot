package me.sedlar.bsb.scripts

import javafx.scene.layout.Pane
import me.sedlar.bsb.api.core.Script

class EmptyScript : Script(
    author = "",
    name = "Empty Script"
) {

    override fun createUI(parent: Pane) {
    }

    override fun onStart() {
    }

    override fun onStop() {
    }

    override fun loop(): Int {
        return 1000
    }
}
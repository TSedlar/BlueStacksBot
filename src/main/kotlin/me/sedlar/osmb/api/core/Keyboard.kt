package me.sedlar.osmb.api.core

import me.sedlar.osmb.BlueStacks

object Keyboard {

    fun arrowUp() {
        BlueStacks.sendKey(0x26, 0x48, 1250)
    }

    fun arrowDown() {
        BlueStacks.sendKey(0x28, 0x50, 1250)
    }
}
package me.sedlar.bsb.api.game.osrs.core

import me.sedlar.bsb.api.core.Script
import me.sedlar.bsb.api.game.osrs.OSRSGameState
import me.sedlar.bsb.api.game.osrs.OSRSInventory

typealias GameState = OSRSGameState
typealias Inventory = OSRSInventory

abstract class OSRSScript(author: String, name: String) : Script(author, name) {

    override fun setup(): Boolean {
        if (!OSRSGameState.setCamZoom(3)) {
            return false
        }
        if (!OSRSGameState.setBrightness(2)) {
            return false
        }
        OSRSGameState.resetPerspective()
        return true
    }
}
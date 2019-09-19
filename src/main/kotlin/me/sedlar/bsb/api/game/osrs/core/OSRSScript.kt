package me.sedlar.bsb.api.game.osrs.core

import me.sedlar.bsb.api.core.Script
import me.sedlar.bsb.api.game.osrs.OSRSGameState
import me.sedlar.bsb.api.game.osrs.OSRSInventory

typealias GameState = OSRSGameState
typealias Inventory = OSRSInventory

abstract class OSRSScript(author: String, name: String) : Script(author, name)
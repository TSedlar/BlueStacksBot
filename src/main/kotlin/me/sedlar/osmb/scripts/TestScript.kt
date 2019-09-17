package me.sedlar.osmb.scripts

import javafx.scene.layout.Pane
import me.sedlar.osmb.api.core.BotConsole
import me.sedlar.osmb.api.core.Script
import me.sedlar.osmb.api.game.GameScreen
import me.sedlar.osmb.api.game.Inventory
import me.sedlar.osmb.api.game.closestToPlayer
import me.sedlar.osmb.api.util.FX
import java.awt.Color
import java.awt.Graphics2D
import java.awt.Point
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList
import kotlin.random.Random.Default.nextInt
import kotlin.random.Random.Default.nextLong

class TestScript : Script() {

    private val COPPER_ORE = Color(209, 110, 42)
    private val COPPER_ROCK = Color(129, 98, 71)
    private val INVALID_ROCK = Color(84, 78, 78)

    private val COPPER_ORE_TOLERANCE = 5
    private val COPPER_ROCK_TOLERANCE = 1
    private val INVALID_ROCK_TOLERANCE = 8

    private val VICINITY = GameScreen.PRect(5.78, 26.73, 65.33, 52.55)

    private val matches = ArrayList<Point>()

    override fun createUI(parent: Pane) {
    }

    override fun onStart() {
    }

    override fun onStop() {
    }

    override fun loop(): Int {

        if (Inventory.isFull()) {
            BotConsole.println("Inventory full...")

            val copperSlots = Inventory.findAllSlots(COPPER_ORE, COPPER_ORE_TOLERANCE)

            BotConsole.println("Dropping ${copperSlots.size} copper")

            copperSlots.forEach {
                val center = it.center()
                GameScreen.click(center.x + nextInt(-10, 10), center.y + nextInt(-10, 10))
                Thread.sleep(nextLong(180, 300))
            }

            return nextInt(50, 75)
        }

        val localMatches = VICINITY.find(COPPER_ROCK, COPPER_ROCK_TOLERANCE)

        matches.clear()
        matches.addAll(localMatches)

        localMatches.closestToPlayer(55.0, true)?.let {
            GameScreen.click(it.x, it.y)
            val start = TimeUnit.NANOSECONDS.toMillis(System.nanoTime())
            while (now() - start < 5500) {
                Thread.sleep(50, 75)
                if (FX.isTolerable(INVALID_ROCK, GameScreen.colorAt(it.x, it.y), INVALID_ROCK_TOLERANCE)) {
                    break
                }
            }
        }

        return nextInt(50, 75)
    }

    override fun drawDebug(g: Graphics2D) {
        g.color = Color.green

        matches.toTypedArray().forEach {
            g.drawRect(it.x, it.y, 1, 1)
        }
    }

    override fun toString(): String {
        return "Test Script"
    }
}
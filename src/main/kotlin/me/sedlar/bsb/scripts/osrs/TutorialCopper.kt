package me.sedlar.bsb.scripts.osrs

import javafx.scene.layout.Pane
import me.sedlar.bsb.api.core.BotConsole
import me.sedlar.bsb.api.game.GameScreen
import me.sedlar.bsb.api.game.center
import me.sedlar.bsb.api.game.closestToPlayer
import me.sedlar.bsb.api.game.find
import me.sedlar.bsb.api.game.osrs.core.GameState
import me.sedlar.bsb.api.game.osrs.core.Inventory
import me.sedlar.bsb.api.game.osrs.core.OSRSScript
import me.sedlar.bsb.api.util.*
import me.sedlar.bsb.api.util.Timing.waitFor
import org.opencv.core.Mat
import java.awt.Color
import java.awt.Graphics2D
import java.awt.Rectangle
import kotlin.random.Random.Default.nextInt
import kotlin.random.Random.Default.nextLong

class TutorialCopper : OSRSScript(
    author = "Static",
    name = "Tutorial Copper"
) {

    private var COPPER_ROCK: List<Pair<Mat, Double>> = ArrayList()
    private var COPPER_ORE: Pair<Mat, Double>? = null

    private val INVALID_ROCK = Color(78, 71, 71)
    private val INVALID_ROCK_TOLERANCE = 2

    private val VIEWPORT = GameScreen.PRect(6.0, 5.64, 65.44, 83.27)

    private val matches = ArrayList<Pair<Rectangle, Boolean>>()

    override fun createUI(parent: Pane) {
    }

    override fun onStart() {
        COPPER_ROCK = OpenCV.normModelRange("scripts/tutorial_miner/copper", 1..8, 0.75)
        COPPER_ORE = OpenCV.mat("scripts/tutorial_miner/copper_ore/1.png") to 0.85
    }

    override fun onStop() {
        matches.clear()
    }

    override fun setup(): Boolean {
        if (!GameState.setCamZoom(3)) {
            return false
        }
        if (!GameState.setBrightness(2)) {
            return false
        }
        return true
    }

    override fun loop(): Int {
        return when {
            !Inventory.viewing() -> {
                Inventory.open()
                nextInt(500, 750)
            }
            Inventory.isFull() -> {
                dropCopper()
                nextInt(50, 75)
            }
            else -> {
                mineCopper()
                nextInt(50, 75)
            }
        }
    }

    private fun dropCopper() {
        if (!GameState.isDropMode()) {
            GameState.setDropMode(true)
            return
        }

        val copperSlots = Inventory.findAllSlots(COPPER_ORE!!)

        BotConsole.println("Dropping ${copperSlots.size} copper")

        matches.clear()

        copperSlots.forEach {
            matches.add(it.toScreen() to true)
        }

        copperSlots.forEach {
            GameScreen.click(it.center.offset(10))
            Thread.sleep(nextLong(180, 300))
        }
    }

    private fun mineCopper() {
        val localMatches = VIEWPORT.toMat().findAllTemplates(*COPPER_ROCK.toTypedArray()).shift(VIEWPORT)
        val validMatches = localMatches.filter { it.find(INVALID_ROCK, INVALID_ROCK_TOLERANCE).size < 5 }

        matches.clear()

        localMatches.let {
            it.forEach { rect ->
                matches.add(rect to validMatches.contains(rect))
            }
        }

        validMatches.closestToPlayer(50.0)?.let { nearest ->
            GameScreen.click(nearest.center.offset(10))
            waitFor(5000) {
                return@waitFor nearest.find(INVALID_ROCK, INVALID_ROCK_TOLERANCE).size >= 5
            }.pass {
                Thread.sleep(nextLong(50, 100))
            }
        }
    }

    override fun drawDebug(g: Graphics2D) {
        matches.toTypedArray().forEach {
            g.color = if (it.second) Color.green else Color.red
            g.draw(it.first)
        }
    }
}
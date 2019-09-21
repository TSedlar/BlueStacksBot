package me.sedlar.bsb.scripts.osrs

import javafx.scene.layout.Pane
import me.sedlar.bsb.api.core.BotConsole
import me.sedlar.bsb.api.game.GameScreen
import me.sedlar.bsb.api.game.click
import me.sedlar.bsb.api.game.closestToPlayer
import me.sedlar.bsb.api.game.matrix
import me.sedlar.bsb.api.game.osrs.OSRSGameState.setDropMode
import me.sedlar.bsb.api.game.osrs.core.GameState
import me.sedlar.bsb.api.game.osrs.core.Inventory
import me.sedlar.bsb.api.game.osrs.core.OSRSScript
import me.sedlar.bsb.api.util.OpenCV
import me.sedlar.bsb.api.util.Timing
import me.sedlar.bsb.api.util.Timing.now
import me.sedlar.bsb.api.util.findAllTemplates
import me.sedlar.bsb.api.util.findFirstTemplate
import org.opencv.core.Mat
import java.awt.Color
import java.awt.Graphics2D
import java.awt.Rectangle
import kotlin.random.Random.Default.nextInt
import kotlin.random.Random.Default.nextLong

class MapleShield : OSRSScript(
    author = "Static",
    name = "Maple Shield"
) {

    private var knife: Mat? = null
    private var log: Mat? = null
    private var shield: Mat? = null
    private var iface: Mat? = null
    private var tree: Array<Pair<Mat, Double>>? = null

    private var knifeSlots: ArrayList<GameScreen.PRect> = ArrayList()
    private var logSlots: ArrayList<GameScreen.PRect> = ArrayList()
    private var shieldSlots: ArrayList<GameScreen.PRect> = ArrayList()
    private var treeRects: ArrayList<Rectangle> = ArrayList()
    private var ifaceRect: Rectangle? = null

    private val viewport = GameScreen.PRect(5.67, 6.38, 62.99, 84.88)
    private val player = GameScreen.PRect(44.81, 37.34, 6.63, 14.57)
    private val maker = GameScreen.PRect(44.92, 8.01, 11.23, 16.21)

    override fun createUI(parent: Pane) {
    }

    override fun onStart() {
        knife = OpenCV.mat("scripts/maple_shield/knife.png")
        log = OpenCV.mat("scripts/maple_shield/log.png")
        shield = OpenCV.mat("scripts/maple_shield/shield.png")
        iface = OpenCV.mat("scripts/maple_shield/shield_iface.png")
        tree = OpenCV.normModelRange("scripts/maple_shield/tree", 1..2, 0.95).toTypedArray()
    }

    override fun onStop() {
        knifeSlots.clear()
        logSlots.clear()
        shieldSlots.clear()
        treeRects.clear()
        ifaceRect = null
    }

    override fun drawDebug(g: Graphics2D) {
        g.color = Color(40, 40, 40, 150)
        Inventory.slots().forEach { g.draw(it.toScreen()) }

        g.color = Color.lightGray.brighter()
        knifeSlots.toTypedArray().forEach { g.draw(it.toScreen()) }

        g.color = Color.magenta
        logSlots.toTypedArray().forEach { g.draw(it.toScreen()) }

        g.color = Color.red
        shieldSlots.toTypedArray().forEach { g.draw(it.toScreen()) }

        g.color = Color.cyan
        ifaceRect?.let { g.draw(it) }

        g.color = Color.ORANGE.darker()
        treeRects.toTypedArray().forEach { g.draw(it) }
    }

    override fun loop(): Int {
        val localKnife = Inventory.findAllSlots(knife!! to 0.7)
        val localLog = Inventory.findAllSlots(log!! to 0.55)
        val localShield = Inventory.findAllSlots(shield!! to 0.8)
        val localTree = viewport.toMat().findAllTemplates(*tree!!)

        knifeSlots.clear()
        knifeSlots.addAll(localKnife)

        logSlots.clear()
        logSlots.addAll(localLog)

        shieldSlots.clear()
        shieldSlots.addAll(localShield)

        localTree.forEach { it.translate(viewport.toScreenX(), viewport.toScreenY()) }

        treeRects.clear()
        treeRects.addAll(localTree)

        when {
            !Inventory.viewing() -> {
                println("Opening inventory")
                Inventory.open()
                return nextInt(1500, 2000)
            }
            localShield.isNotEmpty() -> {
                println("Dropping shields")
                dropShields(localShield)
                return nextInt(500, 750)
            }
            Inventory.isFull() -> {
                println("Fletching shields")
                fletchShields(localKnife, localLog)
            }
            !Inventory.isFull() -> {
                println("Chopping trees")
                chopTree(localTree)
            }
        }

        return nextInt(50, 75)
    }

    private fun chopTree(trees: List<Rectangle>) {
        if (GameState.setDropMode(false)) {
            trees.closestToPlayer(120.0)?.let {
                it.click(10)
                // Check if chopped tree
                if (player.checkForChanges(100, 50, 0.35) >= 0.5) {
                    Timing.waitFor(nextLong(15000, 17500)) {
                        Inventory.isFull()
                    }
                }
            }
        }
    }

    private fun dropShields(slots: ArrayList<GameScreen.PRect>) {
        if (setDropMode(true)) {
            slots.forEach {
                it.click(10)
                Thread.sleep(nextLong(150, 200))
            }
            GameState.setDropMode(false)
        }
    }

    private fun fletchShields(knives: List<GameScreen.PRect>, logs: List<GameScreen.PRect>) {
        if (GameState.setDropMode(false)) {
            if (maker.toMat().findFirstTemplate(iface!! to 0.85) != null) {
                maker.click(10)
                Thread.sleep(2000)
                Timing.waitFor(45000L) {
                    Inventory.findAllSlots(log!! to 0.55).count() < 2
                }
            } else if (knives.isNotEmpty() && logs.isNotEmpty()) {
                knives.first().click(10)
                Thread.sleep(nextLong(500, 750))
                logs.first().click(10)
                Thread.sleep(nextLong(2250, 2500))
            }
        }
    }
}
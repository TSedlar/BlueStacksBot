package me.sedlar.bsb.scripts.osrs

import javafx.scene.layout.Pane
import me.sedlar.bsb.api.game.GameScreen
import me.sedlar.bsb.api.game.osrs.OSRSGameState.setDropMode
import me.sedlar.bsb.api.game.osrs.core.GameState
import me.sedlar.bsb.api.game.osrs.core.Inventory
import me.sedlar.bsb.api.game.osrs.core.OSRSScript
import me.sedlar.bsb.api.util.OpenCV
import me.sedlar.bsb.api.util.Timing
import me.sedlar.bsb.api.util.findFirstTemplate
import org.opencv.core.Mat
import java.awt.Color
import java.awt.Graphics2D
import java.awt.Rectangle
import kotlin.random.Random.Default.nextInt
import kotlin.random.Random.Default.nextLong

class MapleShort : OSRSScript(
    author = "Static",
    name = "Maple Short"
) {

    private var knife: Mat? = null
    private var log: Mat? = null
    private var item: Mat? = null
    private var iface: Mat? = null
    private var maple: Color? = null
    private var mapleTolerance = 3

    private var knifeSlots: ArrayList<GameScreen.PRect> = ArrayList()
    private var logSlots: ArrayList<GameScreen.PRect> = ArrayList()
    private var itemSlots: ArrayList<GameScreen.PRect> = ArrayList()
    private var ifaceRect: Rectangle? = null

    private val player = GameScreen.PRect(44.81, 37.34, 6.63, 14.57)
    private val maker = GameScreen.PRect(12.3, 8.01, 11.02, 16.03)

    private val trees = arrayOf(
        GameScreen.PRect(53.69, 42.44, 3.96, 7.29),
        GameScreen.PRect(38.93, 35.15, 4.17, 7.29)
    )

    override fun createUI(parent: Pane) {
    }

    override fun onStart() {
        knife = OpenCV.mat("scripts/maple_shield/knife.png")
        log = OpenCV.mat("scripts/maple_shield/log.png")
        item = OpenCV.mat("scripts/maple_shield/short.png")
        iface = OpenCV.mat("scripts/maple_shield/short_iface.png")
        maple = Color(82, 32, 0)
    }

    override fun onStop() {
        knifeSlots.clear()
        logSlots.clear()
        itemSlots.clear()
        ifaceRect = null
        maple = null
    }

    override fun drawDebug(g: Graphics2D) {
        g.color = Color(40, 40, 40, 150)
        Inventory.slots().forEach { g.draw(it.toScreen()) }

        g.color = Color.lightGray.brighter()
        knifeSlots.toTypedArray().forEach { g.draw(it.toScreen()) }

        g.color = Color.magenta
        logSlots.toTypedArray().forEach { g.draw(it.toScreen()) }

        g.color = Color.red
        itemSlots.toTypedArray().forEach { g.draw(it.toScreen()) }

        g.color = Color.cyan
        ifaceRect?.let { g.draw(it) }
    }

    override fun loop(): Int {
        val localKnife = Inventory.findAllSlots(knife!! to 0.55)
        val localLog = Inventory.findAllSlots(log!! to 0.55)
        val localItem = Inventory.findAllSlots(item!! to 0.65)

        knifeSlots.clear()
        knifeSlots.addAll(localKnife)

        logSlots.clear()
        logSlots.addAll(localLog)

        itemSlots.clear()
        itemSlots.addAll(localItem)

        when {
            !Inventory.viewing() -> {
                println("Opening inventory")
                Inventory.open()
                return nextInt(1500, 2000)
            }
            localItem.isNotEmpty() -> {
                println("Dropping shields")
                dropItems(localItem)
                return nextInt(500, 750)
            }
            Inventory.isFull() -> {
                println("Fletching shields")
                fletchItems(localKnife, localLog)
            }
            !Inventory.isFull() -> {
                println("Chopping trees")
                chopTree()
            }
        }

        return nextInt(50, 75)
    }

    private fun chopTree() {
        if (GameState.setDropMode(false)) {
            for (tree in trees) {
                if (tree.find(maple!!, mapleTolerance).isNotEmpty()) {
                    tree.click(10)
                    val change = player.checkForChanges(100, 50, 0.35)
                    if (change >= 0.1) {
                        Timing.waitFor(nextLong(15000, 17500)) {
                            Inventory.isFull()
                        }
                    } else {
                        println(change)
                    }
                    break
                }
            }
        }
    }

    private fun dropItems(slots: ArrayList<GameScreen.PRect>) {
        if (setDropMode(true)) {
            slots.forEach {
                it.click(10)
                Thread.sleep(nextLong(150, 200))
            }
            GameState.setDropMode(false)
        }
    }

    private fun fletchItems(knives: List<GameScreen.PRect>, logs: List<GameScreen.PRect>) {
        if (GameState.setDropMode(false)) {
            if (maker.toMat().findFirstTemplate(iface!! to 0.85) != null) {
                maker.click(10)
                Thread.sleep(2000)
                Timing.waitFor(45000L) {
                    Inventory.findAllSlots(log!! to 0.55).count() == 0
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
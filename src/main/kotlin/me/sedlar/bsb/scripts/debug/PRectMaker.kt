package me.sedlar.bsb.scripts.debug

import me.sedlar.bsb.api.core.BotConsole
import me.sedlar.bsb.api.core.Script
import me.sedlar.bsb.api.game.GameScreen
import java.awt.Color
import java.awt.Graphics2D
import java.awt.Point
import java.awt.Rectangle
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent

class PRectMaker : Script(
    author = "Static",
    name = "PRect Maker"
) {

    private var rect: Rectangle? = null
    private var translated: Rectangle? = null
    private var clicks: Int = 0

    private val clickListener = object : MouseAdapter() {
        override fun mouseReleased(e: MouseEvent) {
            when (clicks) {
                0 -> {
                    rect = Rectangle(Point(e.x, e.y))
                    clicks++;
                }
                1 -> {
                    rect!!.add(Point(e.x, e.y))
                    val px = GameScreen.x2p(rect!!.x)
                    val py = GameScreen.y2p(rect!!.y)
                    val pw = GameScreen.x2p(rect!!.width)
                    val ph = GameScreen.y2p(rect!!.height)
                    BotConsole.println("GameScreen.PRect($px, $py, $pw, $ph)")
                    translated = GameScreen.PRect(px, py, pw, ph).toScreen()
                    clicks++
                }
                else -> {
                    rect = null
                    translated = null
                    clicks = 0
                }
            }
        }
    }

    override fun drawDebug(g: Graphics2D) {
        g.color = Color.green
        if (clicks == 1) {
            g.fillRect(rect!!.x, rect!!.y, 1, 1)
        } else if (translated != null) {
            g.draw(translated)
        }
    }

    override fun onStart() {
        canvas.addMouseListener(clickListener)
    }

    override fun onStop() {
        rect = null
        translated = null
        clicks = 0
        canvas.removeMouseListener(clickListener)
    }

    override fun loop(): Int {
        return 1000
    }
}
package me.sedlar.osmb.api.game

import me.sedlar.osmb.BlueStacks
import me.sedlar.osmb.OSMobileBot
import me.sedlar.osmb.api.util.FX
import me.sedlar.osmb.api.util.round
import java.awt.Color
import java.awt.Point
import java.awt.Rectangle

class GameScreen {

    companion object {
        val WIDTH: Int
            get() = BlueStacks.GAME_WIDTH

        val HEIGHT: Int
            get() = BlueStacks.GAME_HEIGHT

        val PIXEL_WIDTH: Int
            get() = BlueStacks.CANVAS_BOUNDS.width

        val PIXEL_HEIGHT: Int
            get() = BlueStacks.CANVAS_BOUNDS.width

        val player = PRect(47.78, 41.45, 4.67, 10.0)

        fun click(x: Int, y: Int, hold: Int = 0) {
            BlueStacks.click(x, y, hold)
        }

        fun px(percent: Double): Int {
            return (WIDTH * (percent / 100.0)).toInt()
        }

        fun py(percent: Double): Int {
            return (HEIGHT * (percent / 100.0)).toInt()
        }

        fun x2p(x: Int): Double {
            return (x.toDouble() / WIDTH * 100.0).round(2)
        }

        fun y2p(y: Int): Double {
            return (y.toDouble() / HEIGHT * 100.0).round(2)
        }

        fun rgbAt(x: Int, y: Int): Int {
            return try {
                OSMobileBot.pixels[x + PIXEL_WIDTH * y]
            } catch (err: ArrayIndexOutOfBoundsException) {
                0 // black
            }
        }

        fun colorAt(x: Int, y: Int): Color {
            return Color(rgbAt(x, y))
        }

        fun find(
            color: Color,
            tolerance: Int,
            rx: Int = 0,
            ry: Int = 0,
            rw: Int = WIDTH,
            rh: Int = HEIGHT
        ): List<Point> {
            val matches = ArrayList<Point>()
            for (x in rx..rx + rw) {
                for (y in ry..ry + rh) {
                    val localColor = colorAt(x, y)
                    if (FX.isTolerable(color, localColor, tolerance)) {
                        matches.add(Point(x, y))
                    }
                }
            }
            return matches
        }

        fun find(color: Color, tolerance: Int, bounds: Rectangle): List<Point> {
            return find(color, tolerance, bounds.x, bounds.y, bounds.width, bounds.height)
        }
    }

    class PRect(val px: Double, val py: Double, val pw: Double, val ph: Double) {

        fun toScreen(): Rectangle {
            return Rectangle(
                px(px),
                py(py),
                px(pw),
                py(ph)
            )
        }

        fun center(): Point {
            val rect = toScreen()
            return Point(rect.x + (rect.width / 2), rect.y + (rect.height / 2))
        }

        fun find(color: Color, tolerance: Int): List<Point> {
            return find(color, tolerance, toScreen())
        }
    }

    class PRegion(val px: Double, val py: Double, val w: Int, val h: Int) {

        fun toScreen(): Rectangle {
            return Rectangle(
                px(px),
                py(py), w, h
            )
        }

        fun center(): Point {
            val rect = toScreen();
            return Point(rect.x + (rect.width / 2), rect.y + (rect.height / 2));
        }

        fun find(color: Color, tolerance: Int): List<Point> {
            return find(color, tolerance, toScreen())
        }
    }
}

fun List<Point>.closestToPlayer(maxDist: Double = Double.MAX_VALUE, exclude: Boolean = false) : Point? {
    val player = GameScreen.player.toScreen()
    val center = GameScreen.player.center()
    val copy = this.toMutableList()

    // Remove candidates
    copy.removeIf { it.distance(center) > maxDist }

    // Remove points on player
    if (exclude) {
        copy.removeIf { player.contains(it) }
    }

    // Sort by distance
    copy.sortBy { it.distance(center) }

    return copy.firstOrNull()
}
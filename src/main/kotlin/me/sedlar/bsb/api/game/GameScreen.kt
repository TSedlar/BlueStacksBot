package me.sedlar.bsb.api.game

import me.sedlar.bsb.BlueStacks
import me.sedlar.bsb.BlueStacksBot
import me.sedlar.bsb.api.util.FX
import me.sedlar.bsb.api.util.offset
import me.sedlar.bsb.api.util.round
import org.opencv.core.Mat
import org.opencv.core.Rect
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

        val matrix: Mat
            get() = BlueStacksBot.matrix

        val player = PRect(47.78, 41.45, 4.67, 10.0)

        fun center(): Point {
            return Point(WIDTH / 2, HEIGHT / 2)
        }

        fun click(x: Int, y: Int) {
            BlueStacks.click(x, y)
        }

        fun click(point: Point) {
            click(point.x, point.y)
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
                BlueStacksBot.pixels[x + PIXEL_WIDTH * y]
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

    abstract class ScreenRegion(protected val px: Double, protected val py: Double) {

        val center: Point
            get() {
                val rect = toScreen()
                return Point(rect.x + (rect.width / 2), rect.y + (rect.height / 2))
            }

        abstract fun toScreen(): Rectangle

        fun toScreenX(): Int {
            return px(px)
        }

        fun toScreenY(): Int {
            return py(py)
        }

        fun toScreenCV(): Rect {
            val rect = toScreen()
            return Rect(rect.x, rect.y, rect.width, rect.height)
        }

        fun toMat(): Mat {
            return matrix.submat(toScreenCV())
        }

        fun find(color: Color, tolerance: Int): List<Point> {
            return find(color, tolerance, toScreen())
        }

        fun click(offset: Int) {
            click(center.offset(offset))
        }
    }

    class PRect(px: Double, py: Double, private val pw: Double, private val ph: Double) : ScreenRegion(px, py) {

        override fun toScreen(): Rectangle {
            return Rectangle(
                toScreenX(),
                toScreenY(),
                px(pw),
                py(ph)
            )
        }
    }

    class PRegion(px: Double, py: Double, val w: Int, val h: Int) : ScreenRegion(px, py) {

        override fun toScreen(): Rectangle {
            return Rectangle(
                toScreenX(),
                toScreenY(),
                w,
                h
            )
        }
    }

    class PPoint(private val px: Double, private val py: Double) {

        fun toScreen(): Point {
            return Point(px(px), py(py))
        }

        fun click() {
            click(toScreen())
        }
    }
}

fun Rectangle.find(color: Color, tolerance: Int): List<Point> {
    return GameScreen.find(color, tolerance, this)
}

val Rectangle.center: Point
    get() = Point(this.centerX.toInt(), this.centerY.toInt())

fun List<Rectangle>.closestToPlayer(maxDist: Double = Double.MAX_VALUE, exclude: Boolean = false): Rectangle? {
    val player = GameScreen.player.toScreen()
    val center = GameScreen.player.center
    val copy = this.toMutableList()

    // Remove candidates
    copy.removeIf { it.center.distance(center) > maxDist }

    // Remove points on player
    if (exclude) {
        copy.removeIf { player.contains(it) }
    }

    // Sort by distance
    copy.sortBy { it.center.distance(center) }

    return copy.firstOrNull()
}

fun List<Point>.closestToPlayer(maxDist: Double = Double.MAX_VALUE, exclude: Boolean = false): Point? {
    val player = GameScreen.player.toScreen()
    val center = GameScreen.player.center
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
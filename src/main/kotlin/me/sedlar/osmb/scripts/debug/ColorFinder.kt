package me.sedlar.osmb.scripts.debug

import javafx.application.Platform
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.ComboBox
import javafx.scene.control.Label
import javafx.scene.layout.Background
import javafx.scene.layout.BackgroundFill
import javafx.scene.layout.CornerRadii
import javafx.scene.layout.Pane
import me.sedlar.osmb.api.core.Script
import me.sedlar.osmb.api.game.GameScreen
import me.sedlar.osmb.api.util.FX
import java.awt.Color
import java.awt.Graphics2D
import java.awt.Point
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import kotlin.system.measureTimeMillis

class ColorFinder : Script() {

    private var clickLabel: Label? = null
    private var hoverLabel: Label? = null
    private var matchLabel: Label? = null
    private var toleranceLabel: Label? = null
    private var toleranceCombo: ComboBox<Int>? = null

    private var chosenColor: Color? = null
    private var matches = ArrayList<Point>()

    private val mouseListener = object: MouseAdapter() {
        override fun mouseMoved(e: MouseEvent) {
            Platform.runLater {
                hoverLabel?.let {
                    val color = GameScreen.colorAt(e.x, e.y)

                    it.background = jcol2bg(color)
                    it.textFill = jcol2fx(FX.contrast(color))
                    it.text = "${color.red}, ${color.green}, ${color.blue}"
                }
            }
        }

        override fun mouseClicked(e: MouseEvent) {
            Platform.runLater {
                clickLabel?.let {
                    val color = GameScreen.colorAt(e.x, e.y)

                    it.background = jcol2bg(color)
                    it.textFill = jcol2fx(FX.contrast(color))
                    it.text = "${color.red}, ${color.green}, ${color.blue}"

                    chosenColor = color
                }
            }
        }
    }

    override fun createUI(parent: Pane) {
        var y = 2.0

        clickLabel = Label("No Color Clicked").apply {
            background = jcol2bg(Color(16, 16, 16))
            textFill = jcol2fx(Color(204, 204, 204))
            prefHeight = 26.0
            prefWidth = parent.width - 4.0
            layoutX = 2.0
            layoutY = y.apply { y += 28 }
            alignment = Pos.CENTER
        }

        hoverLabel = Label("No Color Hovered").apply {
            background = jcol2bg(Color(16, 16, 16))
            textFill = jcol2fx(Color(204, 204, 204))
            prefHeight = 26.0
            prefWidth = parent.width - 4.0
            layoutX = 2.0
            layoutY = y.apply { y += 56 }
            alignment = Pos.CENTER
        }

        toleranceLabel = Label("Tolerance:").apply {
            textFill = jcol2fx(Color(204, 204, 204))
            prefHeight = 26.0
            prefWidth = parent.width - 4.0
            layoutX = 2.0
            layoutY = y.apply { y += 28 }
            alignment = Pos.CENTER
        }

        toleranceCombo = ComboBox<Int>().apply {
            items.addAll(0..20)
            selectionModel.clearAndSelect(5)
            background = jcol2bg(Color(16, 16, 16))
            prefHeight = 26.0
            prefWidth = parent.width - 4.0
            layoutX = 2.0
            layoutY = y.apply { y += 56 }
        }

        matchLabel = Label("0 Matches").apply {
            background = jcol2bg(Color(16, 16, 16))
            textFill = jcol2fx(Color(204, 204, 204))
            prefHeight = 26.0
            prefWidth = parent.width - 4.0
            layoutX = 2.0
            layoutY = y.apply { y += 28 }
            alignment = Pos.CENTER
        }

        parent.children.add(clickLabel)
        parent.children.add(hoverLabel)
        parent.children.add(toleranceLabel)
        parent.children.add(toleranceCombo)
        parent.children.add(matchLabel)
    }

    override fun onStart() {
        canvas.addMouseMotionListener(mouseListener)
        canvas.addMouseListener(mouseListener)
    }

    override fun onStop() {
        clickLabel = null
        hoverLabel = null
        toleranceLabel = null
        toleranceCombo = null
        matchLabel = null
        chosenColor = null
        matches.clear()
        canvas.removeMouseMotionListener(mouseListener)
        canvas.removeMouseListener(mouseListener)
    }

    override fun loop(): Int {
        val localMatches = ArrayList<Point>()

        var ms = 0L

        chosenColor?.let { color ->
            measureTimeMillis {
                localMatches.addAll(GameScreen.find(color, toleranceCombo!!.selectionModel.selectedItem))
            }.let {
                ms = it
            }
        }

        matches.clear()
        matches.addAll(localMatches)

        Platform.runLater {
            matchLabel?.text = "${matches.size} Matches (${ms}ms)"
        }
        return 100
    }

    override fun drawDebug(g: Graphics2D) {
        g.color = Color.green
        matches.toTypedArray().forEach {
            g.fillRect(it.x, it.y, 1, 1)
        }
    }

    private fun jcol2bg(color: Color): Background {
        return Background(BackgroundFill(jcol2fx(color), CornerRadii(4.0), Insets.EMPTY))
    }

    private fun jcol2fx(color: Color): javafx.scene.paint.Color {
        val r = color.red
        val g = color.green
        val b = color.blue
        val a = color.alpha
        val opacity = a / 255.0
        return javafx.scene.paint.Color.rgb(r, g, b, opacity)
    }

    override fun toString(): String {
        return "Color Finder"
    }
}
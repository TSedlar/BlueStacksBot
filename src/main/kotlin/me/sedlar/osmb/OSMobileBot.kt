package me.sedlar.osmb

import javafx.application.Application
import javafx.embed.swing.SwingNode
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.Pane
import javafx.stage.Stage
import me.sedlar.osmb.api.core.Script
import me.sedlar.osmb.scripts.EmptyScript
import me.sedlar.osmb.scripts.TestScript
import me.sedlar.osmb.scripts.debug.ColorFinder
import me.sedlar.osmb.scripts.debug.PRectMaker
import me.sedlar.osmb.scripts.debug.TestDebug
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.image.BufferedImage
import java.util.*
import javax.swing.JPanel
import javax.swing.SwingUtilities
import kotlin.system.exitProcess

class OSMobileBot : Application() {

    companion object {

        private val ALL_SCRIPTS = arrayOf(
            EmptyScript(),
            TestScript()
        )

        private val ALL_DEBUGS = arrayOf(
            TestDebug(),
            PRectMaker(),
            ColorFinder()
        )

        private var scene: Scene? = null

        private var debugging = false

        private val gameBtn: Button
            get() = scene!!.lookup("#game-btn") as Button

        private val fpsLow: RadioButton
            get() = scene!!.lookup("#fps-low") as RadioButton

        private val fpsMed: RadioButton
            get() = scene!!.lookup("#fps-med") as RadioButton

        private val fpsHigh: RadioButton
            get() = scene!!.lookup("#fps-high") as RadioButton

        private val tabs: TabPane
            get() = scene!!.lookup("#tabs") as TabPane

        @Suppress("UNCHECKED_CAST")
        private val scriptList: ListView<Script>
            get() = scene!!.lookup("#list-scripts") as ListView<Script>

        @Suppress("UNCHECKED_CAST")
        private val debugList: ListView<Script>
            get() = scene!!.lookup("#list-debugs") as ListView<Script>

        private val scriptBtn: Button
            get() = scene!!.lookup("#btn-script") as Button

        private val debugBtn: Button
            get() = scene!!.lookup("#btn-debug") as Button

        private val infoBtn = Button("Stop")

        public val infoPane: AnchorPane
            get() = scene!!.lookup("#info-pane") as AnchorPane

        public val console: TextArea
            get() = scene!!.lookup("#console") as TextArea

        private var _script: Script? = null

        var script: Script?
            get() = _script
            set(value) {
                _script?.interrupt()
                _script = value

                if (value == null) {
                    scriptBtn.text = "Start Script"
                    debugBtn.text = "Start Debug"
                    scriptBtn.isDisable = false
                    debugBtn.isDisable = false
                    infoPane.children.clear()
                    tabs.selectionModel.select(if (debugging) 1 else 0)
                } else {
                    if (debugging) {
                        debugBtn.text = "Stop Debug"
                        scriptBtn.isDisable = true
                    } else {
                        scriptBtn.text = "Stop Script"
                        debugBtn.isDisable = true
                    }
                    _script!!.createUI(infoPane)
                    addInfoStop()
                    tabs.selectionModel.select(2)
                }
            }

        private var showing: Boolean = false

        private var drawnImage: BufferedImage? = null
        private var drawnPixels: IntArray = IntArray(0)

        val pixels: IntArray
            get() = drawnPixels

        val canvas = object : JPanel() {
            override fun paintComponent(g: Graphics) {
                super.paintComponent(g)

                if (showing && drawnImage != null) {
                    g.drawImage(drawnImage, 0, 0, null)
                    _script?.drawDebug(g as Graphics2D)
                } else {
                    g.clearRect(0, 0, width, height)
                }
            }
        }

        private fun addInfoStop() {
            infoBtn.prefWidth = 161.0
            infoBtn.prefHeight = 25.0
            infoBtn.resize(161.0, 25.0)
            infoBtn.background = scriptBtn.background
            infoBtn.textFill = scriptBtn.textFill
            infoBtn.layoutX = scriptBtn.layoutX + 2
            infoBtn.layoutY = scriptBtn.layoutY
            infoPane.children.add(infoBtn)
            infoBtn.setOnAction {
                script = null
            }
        }
    }

    private var repaintTimer: Timer? = null
    private var pixelTimer: Timer = Timer()

    @Throws(Exception::class)
    override fun start(stage: Stage) {
        val root = FXMLLoader.load<Parent>(javaClass.getResource("/ui.fxml"))

        scene = Scene(root)

        scene!!.stylesheets.add(this.javaClass.getResource("/ui.css").toExternalForm())

        stage.title = "OSMobileBot"
        stage.scene = scene
        stage.show()

        createOverlay()

        SwingUtilities.invokeLater {
            setupUI()
            startPixelTimer()
            startRepaintTimer()
        }
    }

    override fun stop() {
        repaintTimer?.cancel()
        pixelTimer.cancel()
        exitProcess(0)
    }

    private fun setupUI() {
        gameBtn.setOnAction {
            showing = !showing
            gameBtn.text = if (showing) "Hide Game" else "Show Game"
        }

        fpsLow.isSelected = true
        fpsMed.isSelected = false
        fpsHigh.isSelected = false

        fpsLow.setOnAction {
            if (fpsLow.isSelected) {
                fpsMed.isSelected = false
                fpsHigh.isSelected = false
                startRepaintTimer()
            }
        }

        fpsMed.setOnAction {
            if (fpsMed.isSelected) {
                fpsLow.isSelected = false
                fpsHigh.isSelected = false
                startRepaintTimer()
            }
        }

        fpsHigh.setOnAction {
            if (fpsHigh.isSelected) {
                fpsLow.isSelected = false
                fpsMed.isSelected = false
                startRepaintTimer()
            }
        }

        // Add scripts
        scriptList.items.addAll(ALL_SCRIPTS)

        // Add debugs
        debugList.items.addAll(ALL_DEBUGS)

        // Add button controls
        scriptBtn.setOnAction {
            if (script != null) {
                script = null
            } else {
                scriptList.selectionModel.selectedItem?.let {
                    debugging = false
                    script = it
                    Thread(it).start()
                }
            }
        }

        debugBtn.setOnAction {
            if (script != null) {
                script = null
            } else {
                debugList.selectionModel.selectedItem?.let {
                    debugging = true
                    script = it
                    Thread(it).start()
                }
            }
        }
    }

    private fun createOverlay() {
        val pane = scene!!.lookup("#image-pane") as Pane
        val node = scene!!.lookup("#canvas") as SwingNode

        SwingUtilities.invokeLater {
            node.prefWidth(pane.width)
            node.prefHeight(pane.height)

            node.resize(pane.width, pane.height)

            canvas.size = Dimension(pane.width.toInt(), pane.height.toInt())
            canvas.preferredSize = canvas.size
            canvas.background = Color.BLACK

            node.content = canvas
        }
    }

    private fun startPixelTimer() {
        pixelTimer.cancel()
        pixelTimer = Timer()

        val pixelTask = object : TimerTask() {
            override fun run() {
                drawnPixels = BlueStacks.pixels()
            }
        }

        // fetch pixels every 100ms (10fps)
        pixelTimer.scheduleAtFixedRate(pixelTask, 0L, 20L)
    }

    private fun startRepaintTimer() {
        repaintTimer?.cancel()
        repaintTimer = Timer()

        val interval = if (fpsLow.isSelected) 100L else if (fpsMed.isSelected) 40L else 20L

        val repaintTask = object : TimerTask() {
            override fun run() {
                if (drawnImage == null) {
                    drawnImage = BlueStacks.snapshot()
                } else {
                    if (showing) {
                        drawnImage!!.setRGB(
                            0,
                            0,
                            drawnImage!!.width,
                            drawnImage!!.height,
                            drawnPixels,
                            0,
                            drawnImage!!.width
                        )
                    }
                }

                if (showing) {
                    canvas.repaint()
                }
            }
        }

        repaintTimer!!.scheduleAtFixedRate(repaintTask, 0L, interval)

    }
}

fun main() {
    object : Thread() {
        override fun run() {
            Application.launch(OSMobileBot::class.java)
        }
    }.start()
}
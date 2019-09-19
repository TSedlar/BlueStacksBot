package me.sedlar.osmb.api.core

import javafx.scene.layout.Pane
import me.sedlar.osmb.OSMobileBot
import me.sedlar.osmb.api.game.GameState
import java.awt.Graphics2D
import javax.swing.JPanel

abstract class Script : Runnable {

    private var isInterrupted = false

    private var didSetup = false

    internal val canvas: JPanel
        get() = OSMobileBot.canvas

    abstract fun loop(): Int

    open fun onStart() {}
    open fun onStop() {}

    open fun setup(): Boolean {
        return doDefaultSetup()
    }

    open fun doDefaultSetup(): Boolean {
        if (!GameState.setCamZoom(3)) {
            return false
        }
        if (!GameState.setBrightness(2)) {
            return false
        }
        GameState.resetPerspective()
        return true
    }

    override fun run() {
        isInterrupted = false
        didSetup = false
        BotConsole.println("Starting script: $this")
        onStart()
        while (!isInterrupted) {
            if (!didSetup) {
                BotConsole.println("Running setup...")
                didSetup = setup()
                if (didSetup) {
                    BotConsole.println("Setup ran successfully")
                }
                continue
            }
            try {
                val delay = loop()
                if (delay < 0) {
                    break
                } else {
                    Thread.sleep(delay.toLong())
                }
            } catch (_: InterruptedException) {
                // ignore..
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        BotConsole.println("Stopping script: $this")
        onStop()
    }

    open fun drawDebug(g: Graphics2D) {}

    fun interrupt() {
        isInterrupted = true
    }

    open fun createUI(parent: Pane) {}

    companion object {

        fun doSafeLoop(condition: () -> Boolean, action: () -> Unit) {
            while (OSMobileBot.script != null && condition()) {
                action()
            }
        }
    }
}
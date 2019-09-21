package me.sedlar.bsb.api.core

import javafx.scene.layout.Pane
import me.sedlar.bsb.BlueStacksBot
import me.sedlar.bsb.api.game.osrs.OSRSGameState
import java.awt.Graphics2D
import javax.swing.JPanel

abstract class Script(author: String, name: String) : Runnable {

    val info = ScriptInfo(author, name)

    private var isInterrupted = false

    private var didSetup = false

    internal val canvas: JPanel
        get() = BlueStacksBot.canvas

    abstract fun loop(): Int

    open fun onStart() {}
    open fun onStop() {}

    open fun setup(): Boolean {
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

    override fun toString(): String {
        return info.name
    }

    companion object {

        fun doSafeLoop(condition: () -> Boolean, action: () -> Unit) {
            while (BlueStacksBot.script != null && condition()) {
                action()
            }
        }
    }
}
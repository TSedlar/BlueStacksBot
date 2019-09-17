package me.sedlar.osmb.api.core

import javafx.scene.layout.Pane
import me.sedlar.osmb.OSMobileBot
import java.awt.Graphics2D
import java.util.concurrent.TimeUnit
import javax.swing.JPanel

abstract class Script : Runnable {

    private var isInterrupted = false
    internal val canvas: JPanel
        get() = OSMobileBot.canvas

    abstract fun loop(): Int

    open fun onStart() {}
    open fun onStop() {}

    override fun run() {
        isInterrupted = false
        BotConsole.println("Starting script: $this")
        onStart()
        while (!isInterrupted) {
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

    fun now() : Long {
        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime())
    }

    open fun createUI(parent: Pane) {}
}
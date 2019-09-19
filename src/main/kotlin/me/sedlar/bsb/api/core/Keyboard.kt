package me.sedlar.bsb.api.core

import me.sedlar.bsb.BlueStacks
import java.awt.Robot
import java.awt.event.KeyEvent

object Keyboard {

    fun doAHKCombo(keyCode: Int) {
        val robot = Robot()

        robot.keyPress(KeyEvent.VK_WINDOWS)
        robot.keyPress(KeyEvent.VK_CONTROL)
        robot.keyPress(KeyEvent.VK_ALT)
        robot.keyPress(KeyEvent.VK_SHIFT)
        robot.keyPress(keyCode)

        robot.keyRelease(keyCode)
        robot.keyRelease(KeyEvent.VK_SHIFT)
        robot.keyRelease(KeyEvent.VK_ALT)
        robot.keyRelease(KeyEvent.VK_CONTROL)
        robot.keyRelease(KeyEvent.VK_WINDOWS)
    }

    fun arrowUp() {
        BlueStacks.sendKey(0x26, 0x48, 1250)
    }

    fun arrowDown() {
        BlueStacks.sendKey(0x28, 0x50, 1250)
    }
}
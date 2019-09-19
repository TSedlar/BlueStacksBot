package me.sedlar.osmb

import net.harawata.appdirs.AppDirsFactory
import java.awt.Desktop
import java.awt.Robot
import java.awt.event.KeyEvent
import java.io.File
import java.nio.file.Files

object AHK {

    val FILE_NAME = "OSMobileBot.ahk"

    fun file(): File {
        val dirs = AppDirsFactory.getInstance()
        val site = dirs.getSiteConfigDir("osmb", "shared", "OSMobileBot")
        return File(site, FILE_NAME)
    }

    fun extract() {
        val ahkResource = javaClass.getResourceAsStream("/$FILE_NAME")
        val ahkData = ahkResource.readBytes()
        val ahkFile = file()

        ahkFile.parentFile.mkdirs()
        Files.write(ahkFile.toPath(), ahkData)

        println("Extracted AHK: ${ahkFile.normalizedPath}")
    }

    fun launch() {
        Desktop.getDesktop().open(file())
        Thread.sleep(1500)
    }

    fun doCombo(keyCode: Int) {
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
}
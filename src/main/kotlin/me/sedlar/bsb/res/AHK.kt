package me.sedlar.bsb.res

import net.harawata.appdirs.AppDirsFactory
import java.awt.Desktop
import java.io.File
import java.nio.file.Files

object AHK {

    val FILE_NAMES = arrayOf(
        "BSB_OSRS.ahk"
    )

    fun files(): List<File> {
        val dirs = AppDirsFactory.getInstance()
        val site = dirs.getSiteConfigDir("bsb", "shared", "BlueStacksBot")
        return FILE_NAMES.map { File(site, it) }
    }

    fun extract() {
        val files = files()
        FILE_NAMES.indices.forEach {
            val fileName = FILE_NAMES[it]
            val ahkResource = javaClass.getResourceAsStream("/ahk/$fileName")
            val ahkData = ahkResource.readBytes()
            val ahkFile = files[it]

            ahkFile.parentFile.mkdirs()
            Files.write(ahkFile.toPath(), ahkData)

            println("Extracted AHK: ${ahkFile.normalizedPath}")
        }
    }

    fun launch() {
        files().forEach {
            Desktop.getDesktop().open(it)
        }
        Thread.sleep(1500)
    }
}
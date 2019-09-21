package me.sedlar.bsb.native

import me.sedlar.bsb.res.normalizedPath
import net.harawata.appdirs.AppDirsFactory
import java.io.File
import java.nio.file.Files
import java.util.*

object JavaLibraryPath {

    val X86_LIBS = arrayOf("/lib/x86/AutoHotkey.dll", "/lib/x86/opencv_java341.dll")
    val X64_LIBS = arrayOf("/lib/x64/AutoHotkey.dll", "/lib/x64/opencv_java341.dll")
    val SHARED_LIBS = arrayOf("/lib/adb.exe", "/lib/AdbWinApi.dll")

    val SITE: String
        get() {
            val dirs = AppDirsFactory.getInstance()
            return dirs.getSiteDataDir("bsb", "shared", "BlueStacksBot")
        }

    fun add(pathToAdd: String) {
        val usrPathsField = ClassLoader::class.java.getDeclaredField("usr_paths")
        usrPathsField.isAccessible = true

        val paths = usrPathsField.get(null) as Array<*>

        for (path in paths) {
            if (path == pathToAdd) {
                return
            }
        }

        val newPaths = Arrays.copyOf(paths, paths.size + 1)
        newPaths[newPaths.size - 1] = pathToAdd
        usrPathsField.set(null, newPaths)
    }

    fun extractAndUseResourceLibs() {
        val site = SITE

        (X86_LIBS + X64_LIBS + SHARED_LIBS).forEach {
            val resource = javaClass.getResourceAsStream(it)
            val file = File(site, it)
            val resourceData = resource.readBytes()

            println("Extracting resource: ${file.normalizedPath}")

            if (!file.exists()) {
                file.parentFile.mkdirs()
                Files.write(file.toPath(), resourceData)
            }

            resource.close()
        }

        if (is64System()) {
            X64_LIBS.forEach {
                System.load(File(site, it).absolutePath)
            }
        } else {
            X86_LIBS.forEach {
                System.load(File(site, it).absolutePath)
            }
        }
    }

    private fun is64System(): Boolean {
        return System.getProperty("sun.arch.data.model").contains("64")
    }
}
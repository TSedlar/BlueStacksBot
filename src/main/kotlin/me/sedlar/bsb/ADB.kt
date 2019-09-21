package me.sedlar.bsb

import me.sedlar.bsb.native.JavaLibraryPath
import me.sedlar.bsb.res.normalizedPath
import java.io.File
import java.io.OutputStreamWriter
import java.io.PrintWriter

object ADB {

    private var process: Process? = null
    private var writer: PrintWriter? = null

    private val adbExe: String
        get() {
            val site = JavaLibraryPath.SITE
            return File(site, "/lib/adb.exe").normalizedPath
        }

    fun openConnection(forceRenew: Boolean = false) {
        if (process == null || !process!!.isAlive || forceRenew) {
            val adb = adbExe
            ProcessBuilder(adb, "connect 127.0.0.1:5555").start().waitFor()
            val builder = ProcessBuilder(adb, "-s", "127.0.0.1:5555", "shell")
            process = builder.start()
            writer = PrintWriter(OutputStreamWriter(process!!.outputStream))
        }
    }

    fun send(vararg command: String): Process {
        openConnection()
        return ProcessBuilder(adbExe, "-s", "127.0.0.1:5555", *command).start()
    }

    fun shell(command: String) {
        openConnection()
        try {
            writer?.println(command)
            writer?.flush()
        } catch (e: Exception) {
            e.printStackTrace()
            openConnection(true) // force renew connection
            writer?.write(command)
            writer?.flush()
        }
    }
}
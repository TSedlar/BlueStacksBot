package me.sedlar.bsb.res

import com.sun.jna.Library
import com.sun.jna.Native
import com.sun.jna.win32.W32APIOptions
import me.sedlar.bsb.native.JavaLibraryPath
import com.sun.jna.WString
import com.sun.jna.Pointer

object AHK {

    val INSTANCE = Native.load("AutoHotkey", AutoHotKey::class.java, W32APIOptions.DEFAULT_OPTIONS)
}

interface AutoHotKey : Library {

    fun ahkExec(s: WString)

    fun ahkdll(s: WString, o: WString, p: WString)

    fun addFile(s: WString, a: Int)

    fun ahktextdll(s: WString, o: WString, p: WString)

    fun ahkFunction(
        f: WString,
        p1: WString,
        p2: WString,
        p3: WString,
        p4: WString,
        p5: WString,
        p6: WString,
        p7: WString,
        p8: WString,
        p9: WString,
        p10: WString
    ): Pointer
}
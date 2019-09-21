package me.sedlar.bsb.native

import com.sun.jna.Native
import com.sun.jna.platform.win32.GDI32
import com.sun.jna.platform.win32.WinDef.HDC
import com.sun.jna.platform.win32.WinDef.HWND
import com.sun.jna.win32.W32APIOptions


interface User32Ext : GDI32 {

    fun PrintWindow(hwnd: HWND, hdcBlt: HDC, nFlags: Int): Boolean
    fun GetFocus(): HWND
    fun IsIconic(hwnd: HWND): Boolean

    companion object {
        val INSTANCE = Native.load("user32", User32Ext::class.java, W32APIOptions.DEFAULT_OPTIONS)
    }
}
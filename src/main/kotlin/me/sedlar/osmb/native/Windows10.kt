package me.sedlar.osmb.native

import com.sun.jna.platform.win32.User32
import com.sun.jna.platform.win32.WinDef
import java.awt.Robot
import java.awt.event.KeyEvent

object Windows10 {

    fun moveToNewDesktop(hwnd: WinDef.HWND) {
        if (!System.getProperty("os.name").contains("Windows 10")) {
            return
        }

        User32.INSTANCE.SetForegroundWindow(hwnd)

        // Wait for foreground window
        Thread.sleep(500)

        val r = Robot()
        // Press WIN+TAB (Open virtual desktop manager)
        r.keyPress(KeyEvent.VK_WINDOWS)
        r.keyPress(KeyEvent.VK_TAB)
        r.keyRelease(KeyEvent.VK_WINDOWS)
        r.keyRelease(KeyEvent.VK_TAB)

        // Wait a bit for desktop manager to open
        Thread.sleep(750)

        // Press Shift+F10 (Open menu)
        r.keyPress(KeyEvent.VK_SHIFT)
        r.keyPress(KeyEvent.VK_F10)
        r.keyRelease(KeyEvent.VK_F10)
        r.keyRelease(KeyEvent.VK_SHIFT)

        // Wait for menu to open
        Thread.sleep(750)

        // Go down 2 menu entries (move to)
        r.keyPress(KeyEvent.VK_DOWN)
        r.keyRelease(KeyEvent.VK_DOWN)
        r.keyPress(KeyEvent.VK_DOWN)
        r.keyRelease(KeyEvent.VK_DOWN)

        // Wait for submenu to open
        Thread.sleep(250)

        // Open the move to menu
        r.keyPress(KeyEvent.VK_RIGHT)
        r.keyRelease(KeyEvent.VK_RIGHT)

        // Wait for submenu to open
        Thread.sleep(500)

        // Go to the new desktop entry
        r.keyPress(KeyEvent.VK_UP)
        r.keyRelease(KeyEvent.VK_UP)

        // Submit
        r.keyPress(KeyEvent.VK_ENTER)
        r.keyRelease(KeyEvent.VK_ENTER)

        // Wait for it to move
        Thread.sleep(250)

        // Go back to desktop
        r.keyPress(KeyEvent.VK_ESCAPE)
        r.keyRelease(KeyEvent.VK_ESCAPE)
    }
}
package me.sedlar.osmb

import com.sun.jna.Pointer
import com.sun.jna.platform.win32.GDI32Util
import com.sun.jna.platform.win32.User32
import com.sun.jna.platform.win32.WinDef
import com.sun.jna.platform.win32.WinDef.LPARAM
import com.sun.jna.platform.win32.WinDef.WPARAM
import com.sun.jna.platform.win32.WinUser.SWP_NOMOVE
import com.sun.jna.platform.win32.WinUser.SWP_NOSIZE
import me.sedlar.osmb.native.ExtGDI32
import me.sedlar.osmb.native.Windows10
import me.sedlar.osmb.native.hasPointer
import org.opencv.core.CvType
import org.opencv.core.Mat
import java.awt.Color
import java.awt.Rectangle
import java.awt.Toolkit
import java.awt.image.BufferedImage


private var mainHandle: WinDef.HWND? = null
private var winHandle: WinDef.HWND? = null
private var canvasHandle: WinDef.HWND? = null

private const val MAIN_NAME = "BlueStacks"
private const val WIN_NAME = "WindowsForms10.Window.8.app.0.34f5582_r6_ad1"
private const val CANVAS_NAME = "BlueStacksApp"

const val WM_COMMAND = 0x111
const val WM_LBUTTONDOWN = 0x201
const val WM_LBUTTONUP = 0x202
const val WM_LBUTTONDBLCLK = 0x203
const val WM_RBUTTONDOWN = 0x204
const val WM_RBUTTONUP = 0x205
const val WM_RBUTTONDBLCLK = 0x206
const val WM_KEYDOWN = 0x100
const val WM_KEYUP = 0x101
const val WM_MOUSEWHEEL = 0x020A
const val WM_MOUSEMOVE = 0x0200
const val WM_SETCURSOR = 0x0020
const val WM_CHAR = 0x0102

object BlueStacks {

    const val GAME_WIDTH = 900
    const val GAME_HEIGHT = 550
    private val BLANK_IMAGE = BufferedImage(GAME_WIDTH, GAME_HEIGHT, BufferedImage.TYPE_INT_RGB)

    private var canvasBounds: Rectangle? = null

    val CANVAS_BOUNDS: Rectangle
        get() = canvasBounds!!

    private fun hasValidHandles(): Boolean {
        return mainHandle != null && mainHandle!!.hasPointer() && winHandle != null && winHandle!!.hasPointer() &&
                canvasHandle != null && canvasHandle!!.hasPointer()
    }

    fun findHandles(): Boolean {
        if (hasValidHandles()) {
            return true
        }

        mainHandle = User32.INSTANCE.FindWindow(null, MAIN_NAME)
        winHandle = User32.INSTANCE.FindWindowEx(mainHandle, WinDef.HWND(Pointer.NULL), WIN_NAME, null)
        canvasHandle = User32.INSTANCE.FindWindowEx(winHandle, WinDef.HWND(Pointer.NULL), CANVAS_NAME, null)

        if (!hasValidHandles()) {
            return false
        }

        println("Main Handle: $mainHandle")
        println("Win Handle: $winHandle")
        println("Canvas Handle: $canvasHandle")

        println("Setting window size...")

        val screen = Toolkit.getDefaultToolkit().screenSize
        val midX = (screen.width / 2) - (GAME_WIDTH / 2)
        val midY = (screen.height / 2) - (GAME_HEIGHT / 2)

        User32.INSTANCE.SetWindowPos(mainHandle, WinDef.HWND(Pointer.NULL), midX, midY, GAME_WIDTH, GAME_HEIGHT, 0)

        Thread.sleep(1000)

        val canvasRect = WinDef.RECT()

        User32.INSTANCE.GetWindowRect(canvasHandle, canvasRect)

        canvasBounds = canvasRect.toRectangle()

        return true
    }

    fun focus() {
        User32.INSTANCE.SetForegroundWindow(mainHandle)
    }

    fun sendToVirtualDesktop() {
        Windows10.moveToNewDesktop(mainHandle!!)
    }

    fun doSafeAction(action: () -> Unit) {
        if (findHandles()) {
            action()
        }
    }

    fun click(x: Int, y: Int, hold: Int = 0) {
        doSafeAction {
            val position = (y shl 16) or (x and 0xFFFF)
            val w = WPARAM(0)
            val l = LPARAM(position.toLong())
            User32.INSTANCE.SendMessage(winHandle, WM_LBUTTONDOWN, w, l)
            if (hold > 0) {
                Thread.sleep(hold.toLong())
            }
            User32.INSTANCE.SendMessage(winHandle, WM_LBUTTONUP, w, l)
        }
    }

    fun sendKey(keyCode: Int, scanCode: Int, wait: Int = 0) {
        doSafeAction {
            val activeWin = User32.INSTANCE.GetForegroundWindow()
            User32.INSTANCE.SetForegroundWindow(mainHandle) // sadly has to be focused to send keys..
            Thread.sleep(500)
            val wparam = WPARAM(keyCode.toLong())
            val lParam = 0x00000001 or (scanCode /* scancode */ shl 16) or 0x01000000 /* extended */
            val lparamDown = LPARAM(lParam.toLong())
            val lparamUp = LPARAM((lParam or (1 shl 30) or (1 shl 31)).toLong())
            User32.INSTANCE.PostMessage(winHandle, WM_KEYDOWN, wparam, lparamDown)
            User32.INSTANCE.PostMessage(winHandle, WM_KEYUP, wparam, lparamUp)
            Thread.sleep(250)
            // send to back
            val bottom = WinDef.HWND(Pointer.createConstant(1))
            User32.INSTANCE.SetWindowPos(
                mainHandle, bottom, 0, 0, 0, 0,
                SWP_NOSIZE or SWP_NOMOVE
            )
            Thread.sleep(wait.toLong())
            User32.INSTANCE.SetForegroundWindow(activeWin)
        }
    }

    fun snapshot(): BufferedImage {
        var img = BLANK_IMAGE
        doSafeAction {
            img = GDI32Util.getScreenshot(canvasHandle)
        }
        return img
    }

    fun pixels(): IntArray {
        var pixels = IntArray(0)
        doSafeAction {
            pixels = ExtGDI32.GetAllPixels(canvasHandle!!)
        }
        return pixels
    }

    fun mat(): Mat {
        var mat = Mat()
        doSafeAction {
            mat = Mat(canvasBounds!!.height, canvasBounds!!.width, CvType.CV_8UC3)

            val pixels = OSMobileBot.pixels

            val data = ByteArray(canvasBounds!!.width * canvasBounds!!.height * mat.elemSize().toInt())

            for (i in pixels.indices) {
                data[i * 3] = (pixels[i] shr 0 and 0xFF).toByte() // r
                data[i * 3 + 1] = (pixels[i] shr 8 and 0xFF).toByte() // g
                data[i * 3 + 2] = (pixels[i] shr 16 and 0xFF).toByte() // b
            }

            mat.put(0, 0, data)
        }
        return mat
    }

    fun colorAt(x: Int, y: Int): Color {
        var color = Color.BLACK
        doSafeAction {
            val hdc = User32.INSTANCE.GetDC(canvasHandle)
            val pixel = ExtGDI32.INSTANCE.GetPixel(hdc, x, y)
            User32.INSTANCE.ReleaseDC(canvasHandle, hdc)
            color = Color(
                (pixel and 0x000000FF),
                (pixel and 0x0000FF00) shr 8,
                (pixel and 0x00FF0000) shr 16
            )
        }
        return color
    }
}
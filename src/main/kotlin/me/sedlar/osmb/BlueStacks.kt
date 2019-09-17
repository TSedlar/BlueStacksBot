package me.sedlar.osmb

import com.sun.jna.Pointer
import com.sun.jna.platform.win32.GDI32Util
import com.sun.jna.platform.win32.User32
import com.sun.jna.platform.win32.WinDef
import me.sedlar.osmb.native.ExtGDI32
import me.sedlar.osmb.native.hasPointer
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

class BlueStacks {
    companion object {

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

            val canvasRect = WinDef.RECT()

            User32.INSTANCE.GetWindowRect(canvasHandle, canvasRect)

            canvasBounds = canvasRect.toRectangle()

            return true
        }

        fun doSafeAction(action: () -> Unit) {
            if (findHandles()) {
                action()
            }
        }

        fun click(x: Int, y: Int, hold: Int = 0) {
            doSafeAction {
                val position = (y shl 16) or (x and 0xFFFF)
                val w = WinDef.WPARAM(0)
                val l = WinDef.LPARAM(position.toLong())
                User32.INSTANCE.SendMessage(winHandle, WM_LBUTTONDOWN, w, l)
                if (hold > 0) {
                    Thread.sleep(hold.toLong())
                }
                User32.INSTANCE.SendMessage(winHandle, WM_LBUTTONUP, w, l)
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
}
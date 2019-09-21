package me.sedlar.bsb

import com.sun.jna.Pointer
import com.sun.jna.platform.win32.GDI32Util
import com.sun.jna.platform.win32.User32
import com.sun.jna.platform.win32.WinDef
import com.sun.jna.platform.win32.WinUser.SWP_NOMOVE
import com.sun.jna.platform.win32.WinUser.SWP_NOSIZE
import me.sedlar.bsb.api.game.osrs.core.GameState
import me.sedlar.bsb.native.*
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

const val WM_LBUTTONDOWN = 0x201
const val WM_LBUTTONUP = 0x202

object BlueStacks {

    val handleMain: WinDef.HWND
        get() = mainHandle!!

    val handleWin: WinDef.HWND
        get() = winHandle!!

    val handleCanvas: WinDef.HWND
        get() = canvasHandle!!

    const val GAME_WIDTH = 935 // adb screen is 1900
    const val GAME_HEIGHT = 549 // adb screen is 1075

    const val ADB_X_SCALE = 2.13863636364
    const val ADB_Y_SCALE = 2.11764705882

    private val BLANK_IMAGE = BufferedImage(GAME_WIDTH, GAME_HEIGHT, BufferedImage.TYPE_INT_RGB)

    internal var canvasBounds: Rectangle? = null

    private var mat: Mat? = null

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

        mainHandle?.hasPointer()?.let {
            println("Setting window size...")

            val screen = Toolkit.getDefaultToolkit().screenSize
            val midX = (screen.width / 2) - (GAME_WIDTH / 2)
            val midY = (screen.height / 2) - (GAME_HEIGHT / 2)

            val hwndRect = WinDef.RECT()

            User32.INSTANCE.GetWindowRect(mainHandle, hwndRect)

            val currBounds = hwndRect.toRectangle()

            if (currBounds.width != GAME_WIDTH && currBounds.height != GAME_HEIGHT) {
                User32.INSTANCE.MoveWindow(mainHandle, midX, midY, GAME_WIDTH, GAME_HEIGHT, true)
                Thread.sleep(1000)
                User32.INSTANCE.GetWindowRect(mainHandle, hwndRect)
                println(hwndRect.toRectangle())
            }
        }

        winHandle = User32.INSTANCE.FindWindowEx(mainHandle, WinDef.HWND(Pointer.NULL), WIN_NAME, null)
        canvasHandle = User32.INSTANCE.FindWindowEx(winHandle, WinDef.HWND(Pointer.NULL), CANVAS_NAME, null)

        if (!hasValidHandles()) {
            return false
        }

        println("Main Handle: $mainHandle")
        println("Win Handle: $winHandle")
        println("Canvas Handle: $canvasHandle")

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
            if (User32Ext.INSTANCE.IsIconic(mainHandle!!)) {
                val bottom = WinDef.HWND(Pointer.createConstant(1))
                User32.INSTANCE.SetWindowPos(
                    mainHandle, bottom, 0, 0, 0, 0,
                    SWP_NOSIZE or SWP_NOMOVE
                )
            }
            action()
        }
    }

    fun click(x: Int, y: Int) {
//        doSafeAction {
//            val position = (y shl 16) or (x and 0xFFFF)
//            val w = WinDef.WPARAM(0)
//            val l = WinDef.LPARAM(position.toLong())
//            User32.INSTANCE.SendMessage(winHandle, WM_LBUTTONDOWN, w, l)
//            User32.INSTANCE.SendMessage(winHandle, WM_LBUTTONUP, w, l)
//        }
        adbClick(x, y) // lets experiment only using adb for now
    }

    fun adbClick(x: Int, y: Int) {
        val transX = (x * ADB_X_SCALE).toInt()
        val transY = (y * ADB_Y_SCALE).toInt()
        ADB.shell("input tap $transX $transY")
    }

    fun adbDrag(x1: Int, y1: Int, x2: Int, y2: Int) {
        val t1X = (x1 * ADB_X_SCALE).toInt()
        val t1Y = (y1 * ADB_Y_SCALE).toInt()
        val t2X = (x2 * ADB_X_SCALE).toInt()
        val t2Y = (y2 * ADB_Y_SCALE).toInt()
        ADB.shell("input swipe $t1X $t1Y $t2X $t2Y")
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
        doSafeAction {
            if (mat == null) {
                mat = Mat(canvasBounds!!.height, canvasBounds!!.width, CvType.CV_8UC3)
            }

            val pixels = BlueStacksBot.pixels

            val data = ByteArray(canvasBounds!!.width * canvasBounds!!.height * mat!!.elemSize().toInt())

            for (i in pixels.indices) {
                data[i * 3] = (pixels[i] shr 0 and 0xFF).toByte() // r
                data[i * 3 + 1] = (pixels[i] shr 8 and 0xFF).toByte() // g
                data[i * 3 + 2] = (pixels[i] shr 16 and 0xFF).toByte() // b
            }

            mat!!.put(0, 0, data)
        }
        return mat!!
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
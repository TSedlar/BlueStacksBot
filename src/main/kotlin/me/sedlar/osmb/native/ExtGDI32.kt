package me.sedlar.osmb.native

import com.sun.jna.Memory
import com.sun.jna.Native
import com.sun.jna.platform.win32.*
import com.sun.jna.platform.win32.WinDef.HDC
import com.sun.jna.win32.W32APIOptions

interface ExtGDI32 : GDI32 {

    fun GetPixel(hdc: HDC, x: Int, y: Int): Int

    companion object {
        val INSTANCE = Native.load("gdi32", ExtGDI32::class.java, W32APIOptions.DEFAULT_OPTIONS)

        fun GetAllPixels(target: WinDef.HWND): IntArray {
            val rect = WinDef.RECT()
            if (!User32.INSTANCE.GetWindowRect(target, rect)) {
                throw Win32Exception(Native.getLastError())
            }
            val jRectangle = rect.toRectangle()
            val windowWidth = jRectangle.width
            val windowHeight = jRectangle.height

            check(!(windowWidth == 0 || windowHeight == 0)) { "Window width and/or height were 0 even though GetWindowRect did not appear to fail." }

            val hdcTarget = User32.INSTANCE.GetDC(target) ?: throw Win32Exception(Native.getLastError())

            var we: Win32Exception? = null

            // device context used for drawing
            var hdcTargetMem: HDC? = null

            // handle to the bitmap to be drawn to
            var hBitmap: WinDef.HBITMAP? = null

            // original display surface associated with the device context
            var hOriginal: WinNT.HANDLE? = null

            // final java image structure we're returning.
            var image = IntArray(0)

            try {
                hdcTargetMem = GDI32.INSTANCE.CreateCompatibleDC(hdcTarget)
                if (hdcTargetMem == null) {
                    throw Win32Exception(Native.getLastError())
                }

                hBitmap = GDI32.INSTANCE.CreateCompatibleBitmap(hdcTarget, windowWidth, windowHeight)
                if (hBitmap == null) {
                    throw Win32Exception(Native.getLastError())
                }

                hOriginal = GDI32.INSTANCE.SelectObject(hdcTargetMem, hBitmap)
                if (hOriginal == null) {
                    throw Win32Exception(Native.getLastError())
                }

                // draw to the bitmap
                if (!GDI32.INSTANCE.BitBlt(
                        hdcTargetMem,
                        0,
                        0,
                        windowWidth,
                        windowHeight,
                        hdcTarget,
                        0,
                        0,
                        GDI32.SRCCOPY
                    )
                ) {
                    throw Win32Exception(Native.getLastError())
                }

                val bmi = WinGDI.BITMAPINFO()
                bmi.bmiHeader.biWidth = windowWidth
                bmi.bmiHeader.biHeight = -windowHeight
                bmi.bmiHeader.biPlanes = 1
                bmi.bmiHeader.biBitCount = 32
                bmi.bmiHeader.biCompression = WinGDI.BI_RGB

                val buffer = Memory((windowWidth * windowHeight * 4).toLong())
                val resultOfDrawing = GDI32.INSTANCE.GetDIBits(
                    hdcTarget, hBitmap, 0, windowHeight, buffer, bmi,
                    WinGDI.DIB_RGB_COLORS
                )
                if (resultOfDrawing == 0 || resultOfDrawing == WinError.ERROR_INVALID_PARAMETER) {
                    throw Win32Exception(Native.getLastError())
                }

                val bufferSize = windowWidth * windowHeight
                image = buffer.getIntArray(0, bufferSize)
            } catch (e: Win32Exception) {
                we = e
            } finally {
                if (hOriginal != null) {
                    // per MSDN, set the display surface back when done drawing
                    val result = GDI32.INSTANCE.SelectObject(hdcTargetMem, hOriginal)
                    // failure modes are null or equal to HGDI_ERROR
                    if (result == null || WinGDI.HGDI_ERROR == result) {
                        val ex = Win32Exception(Native.getLastError())
                        if (we != null) {
                            ex.addSuppressed(we)
                        }
                        we = ex
                    }
                }

                if (hBitmap != null) {
                    if (!GDI32.INSTANCE.DeleteObject(hBitmap)) {
                        val ex = Win32Exception(Native.getLastError())
                        if (we != null) {
                            ex.addSuppressed(we)
                        }
                        we = ex
                    }
                }

                if (hdcTargetMem != null) {
                    // get rid of the device context when done
                    if (!GDI32.INSTANCE.DeleteDC(hdcTargetMem)) {
                        val ex = Win32Exception(Native.getLastError())
                        if (we != null) {
                            ex.addSuppressed(we)
                        }
                        we = ex
                    }
                }

                check(
                    0 != User32.INSTANCE.ReleaseDC(
                        target,
                        hdcTarget
                    )
                ) { "Device context did not release properly." }
            }

            if (we != null) {
                throw we
            }
            return image
        }
    }
}
package me.sedlar.osmb.api.util

import java.awt.Color
import kotlin.math.abs

class FX {
    companion object {

        fun contrast(c: Color): Color {
            return Color(if (c.red > 127) 0 else 255, if (c.green > 127) 0 else 255, if (c.blue > 127) 0 else 255)
        }

        fun distance(r1: Int, g1: Int, b1: Int, r2: Int, g2: Int, b2: Int): Int {
            val red = abs(r2 - r1)
            val green = abs(g2 - g1)
            val blue = abs(b2 - b1)
            return (red + green + blue) / 3
        }

        fun distance(c1: Color, c2: Color): Int {
            return distance(c1.red, c1.green, c1.blue, c2.red, c2.green, c2.blue)
        }

        fun isTolerable(c1: Color, c2: Color, tolerance: Int): Boolean {
            return distance(c1, c2) <= tolerance
        }
    }
}
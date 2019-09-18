package me.sedlar.osmb.api.util

import me.sedlar.osmb.api.game.GameScreen
import org.opencv.core.*
import org.opencv.imgcodecs.Imgcodecs
import org.opencv.imgproc.Imgproc
import java.awt.Rectangle
import java.awt.image.BufferedImage
import java.awt.image.PixelGrabber
import java.io.ByteArrayInputStream
import javax.imageio.ImageIO


object OpenCV {

    fun mat(imgPath: String, format: Int): Mat {
        val mat = Imgcodecs.imread(imgPath, format)
        if (format != Imgcodecs.IMREAD_GRAYSCALE) {
            val reducedColor = Mat()
            Imgproc.cvtColor(mat, reducedColor, Imgproc.COLOR_RGBA2RGB)
            return reducedColor
        }
        return mat
    }

    fun mat(imgPath: String) = mat(imgPath, Imgcodecs.IMREAD_UNCHANGED)

    fun modelRange(imgDir: String, range: IntRange, weight: Double, format: Int): List<Pair<Mat, Double>> {
        val list: MutableList<Pair<Mat, Double>> = ArrayList()
        for (i in range) {
            list.add(mat("$imgDir/$i.png", format) to weight)
        }
        return list
    }

    fun cannyModelRange(imgDir: String, range: IntRange, weight: Double): List<Pair<Mat, Double>> {
        val list: MutableList<Pair<Mat, Double>> = ArrayList()
        for (i in range) {
            list.add(mat("$imgDir/$i.png", Imgcodecs.IMREAD_GRAYSCALE).canny() to weight)
        }
        return list
    }

    fun bwModelRange(imgDir: String, range: IntRange, weight: Double): List<Pair<Mat, Double>> {
        return modelRange(imgDir, range, weight, Imgcodecs.IMREAD_GRAYSCALE)
    }

    fun normModelRange(imgDir: String, range: IntRange, weight: Double): List<Pair<Mat, Double>> {
        return modelRange(imgDir, range, weight, Imgcodecs.IMREAD_UNCHANGED)
    }
}

fun Mat.canny(threshold: Double, ratio: Double): Mat {
    val edges = Mat()
    Imgproc.Canny(this, edges, threshold, threshold * ratio)
    return edges
}

fun Mat.canny(): Mat = canny(60.0, 3.0)

fun Mat.toImage(): BufferedImage {
    val mob = MatOfByte()
    Imgcodecs.imencode(".png", this, mob)
    return ImageIO.read(ByteArrayInputStream(mob.toArray()))
}

fun Mat.pixels(): IntArray {
    PixelGrabber(toImage(), 0, 0, width(), height(), true).let {
        it.grabPixels()
        return it.pixels as IntArray
    }
}

private fun Mat.findTemplates(breakFirst: Boolean, vararg templates: Pair<Mat, Double>): List<Rectangle> {
    val results: MutableList<Rectangle> = ArrayList()

    templates.forEach {
        val template = it.first
        val threshold = it.second

        val result = Mat()

        Imgproc.matchTemplate(this, template, result, Imgproc.TM_CCOEFF_NORMED)
        Imgproc.threshold(result, result, 0.1, 1.0, Imgproc.THRESH_TOZERO)

        while (true) {
            val mml = Core.minMaxLoc(result)
            val pos = mml.maxLoc
            if (mml.maxVal >= threshold) {
                Imgproc.rectangle(
                    this, pos, Point(pos.x + template.cols(), pos.y + template.rows()),
                    Scalar(0.0, 255.0, 0.0), 1
                )
                Imgproc.rectangle(
                    result, pos, Point(pos.x + template.cols(), pos.y + template.rows()),
                    Scalar(0.0, 255.0, 0.0), -1
                )
                results.add(Rectangle(pos.x.toInt(), pos.y.toInt(), template.width(), template.height()))
                if (breakFirst) {
                    return results
                }
            } else {
                break
            }
        }
    }

    return results
}

fun Mat.findAllTemplates(vararg templates: Pair<Mat, Double>): List<Rectangle> {
    return this.findTemplates(false, *templates)
}

fun Mat.findFirstTemplate(vararg templates: Pair<Mat, Double>): Rectangle? {
    val result = findTemplates(true, *templates)
    return if (result.isNotEmpty()) result[0] else null
}

fun List<Rectangle>.shift(rect: GameScreen.PRect): List<Rectangle> {
    val xOff = rect.toScreenX()
    val yOff = rect.toScreenY()
    this.forEach {
        it.x += xOff
        it.y += yOff
    }
    return this
}

package me.sedlar.osmb.api.game

import me.sedlar.osmb.api.util.findFirstTemplate
import org.opencv.core.Mat
import java.awt.Color


object Inventory {

    val ITEM_OUTLINE = Color(15, 13, 9)
    val BOUNDS = GameScreen.PRect(73.00, 37.82, 21.33, 48.36)

    const val INSET_X = 1.00
    const val INSET_Y = 1.35
    const val SLOT_MARGIN_X = 0.285
    const val SLOT_MARGIN_Y = 0.5455
    const val SLOT_WIDTH = 4.75
    const val SLOT_HEIGHT = 6.30

    fun slots(): ArrayList<GameScreen.PRect> {
        val slots = ArrayList<GameScreen.PRect>()

        val bounds = BOUNDS.toScreen()

        val marginX = GameScreen.px(SLOT_MARGIN_X)
        val marginY = GameScreen.py(SLOT_MARGIN_Y)

        val startX = bounds.x + GameScreen.px(INSET_X)
        val startY = bounds.y + GameScreen.py(INSET_Y)

        var x = startX
        var y = startY

        val w = GameScreen.px(SLOT_WIDTH)
        val h = GameScreen.py(SLOT_HEIGHT)

        for (i in 0 until 28) {
            slots.add(GameScreen.PRect(GameScreen.x2p(x), GameScreen.y2p(y), SLOT_WIDTH, SLOT_HEIGHT))
            x += w + marginX

            if ((i + 1) % 4 != 0) continue

            x = startX
            y += h + marginY
        }

        return slots
    }

    fun isSlotEmpty(slot: GameScreen.PRect): Boolean {
        return slot.find(ITEM_OUTLINE, 5).isEmpty()
    }

    fun count(): Int {
        return slots().count { !isSlotEmpty(it) }
    }

    fun isFull(): Boolean {
        return count() == 28
    }

    fun findSlot(color: Color, tolerance: Int): GameScreen.PRect? {
        val slots = slots()
        for (slot in slots) {
            if (slot.find(color, tolerance).isNotEmpty()) {
                return slot
            }
        }
        return null
    }

    fun findAllSlots(color: Color, tolerance: Int): ArrayList<GameScreen.PRect> {
        val slots = slots()
        val matches = ArrayList<GameScreen.PRect>()
        for (slot in slots) {
            if (slot.find(color, tolerance).isNotEmpty()) {
                matches.add(slot)
            }
        }
        return matches
    }

    fun findSlot(template: Pair<Mat, Double>): GameScreen.PRect? {
        val slots = slots()
        for (slot in slots) {
            if (GameScreen.matrix.submat(slot.toScreenCV()).findFirstTemplate(template) != null) {
                return slot
            }
        }
        return null
    }

    fun findAllSlots(template: Pair<Mat, Double>): ArrayList<GameScreen.PRect> {
        val slots = slots()
        val matches = ArrayList<GameScreen.PRect>()
        for (slot in slots) {
            if (slot.toMat().findFirstTemplate(template) != null) {
                matches.add(slot)
            }
        }
        return matches
    }
}
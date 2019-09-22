package me.sedlar.bsb.api.game.osrs

import me.sedlar.bsb.BlueStacks
import me.sedlar.bsb.api.core.Script.Companion.doSafeLoop
import me.sedlar.bsb.api.game.GameScreen
import me.sedlar.bsb.api.game.center
import me.sedlar.bsb.api.util.FX
import me.sedlar.bsb.api.util.OpenCV
import me.sedlar.bsb.api.util.findFirstTemplate
import java.awt.Color
import kotlin.random.Random.Default.nextInt
import kotlin.random.Random.Default.nextLong

object OSRSGameState {

    val COMPASS_BOUNDS = GameScreen.PRect(75.51, 0.36, 5.03, 8.74)

    val LOGIN_SCREEN_BOUNDS = GameScreen.PRect(28.02, 30.05, 40.21, 40.26)
    val LOGIN_SCREEN_MATS = OpenCV.normModelRange("api/osrs/login_screen", 1..4, 0.95)

    val LOBBY_PLAY_BOUNDS = GameScreen.PRect(34.76, 51.0, 26.52, 20.22)
    val LOBBY_PLAY_MAT = OpenCV.mat("api/osrs/lobby_play.png")

    val DROP_MODE_BOUNDS = GameScreen.PRect(0.75, 30.42, 4.28, 5.83)
    val DROP_MODE_MAT = OpenCV.mat("api/osrs/drop_mode.png")

    val DROP_MODE_ARROW = GameScreen.PRect(1.5, 32.06, 1.28, 3.1)
    val DROP_MODE_COLOR = Color(180, 38, 38)
    val DROP_MODE_TOLERANCE = 10

    val INV_TAB_BOUNDS = GameScreen.PRect(91.02, 36.07, 4.49, 8.56)
    val INV_OPEN_MAT = OpenCV.mat("api/osrs/inv_open.png")

    val SETTINGS_TAB_BOUNDS = GameScreen.PRect(90.59, 72.13, 5.03, 8.74)
    val SETTINGS_OPEN_MAT = OpenCV.mat("api/osrs/settings_open.png")

    val PHONE_SUB_BOUNDS = GameScreen.PRect(70.27, 37.16, 5.24, 9.65)
    val PHONE_SUB_OPEN_MAT = OpenCV.mat("api/osrs/phone_sub_open.png")

    val CAM_ZOOM_BOUNDS = GameScreen.PRect(74.65, 47.91, 14.65, 5.46)
    val BRIGHTNESS_LEVEL_BOUNDS = GameScreen.PRect(74.65, 54.28, 14.65, 5.46)

    val TOGGLE_GREEN_MAT = OpenCV.mat("api/osrs/toggle_green.png")
    val TOGGLE_BLUE_MAT = OpenCV.mat("api/osrs/toggle_blue.png")

    val CAM_1 = GameScreen.PPoint(76.35, 50.27) // zoomed out
    val CAM_2 = GameScreen.PPoint(78.92, 50.27)
    val CAM_3 = GameScreen.PPoint(81.91, 50.27)
    val CAM_4 = GameScreen.PPoint(84.59, 50.27)
    val CAM_5 = GameScreen.PPoint(87.47, 50.27) // zoomed in

    val BRIGHTNESS_1 = GameScreen.PPoint(76.79, 56.65) // dim
    val BRIGHTNESS_2 = GameScreen.PPoint(80.11, 56.65)
    val BRIGHTNESS_3 = GameScreen.PPoint(83.42, 56.65)
    val BRIGHTNESS_4 = GameScreen.PPoint(87.06, 56.65) // bright

    fun isAtLogin(): Boolean {
        return LOGIN_SCREEN_BOUNDS.toMat().findFirstTemplate(*LOGIN_SCREEN_MATS.toTypedArray()) != null
    }

    fun isAtLobby(): Boolean {
        return LOBBY_PLAY_BOUNDS.toMat().findFirstTemplate(LOBBY_PLAY_MAT to 0.9) != null
    }

    fun isInGame(): Boolean {
        return !isAtLobby() && !isAtLogin()
    }

    fun isDropMode(): Boolean {
//        return DROP_MODE_BOUNDS.toMat().findFirstTemplate(DROP_MODE_MAT to 1.0) != null
        return DROP_MODE_ARROW.find(DROP_MODE_COLOR, DROP_MODE_TOLERANCE).isNotEmpty()
    }

    fun isInvOpen(): Boolean {
        return INV_TAB_BOUNDS.toMat().findFirstTemplate(INV_OPEN_MAT to 0.75) != null
    }

    fun isSettingsOpen(): Boolean {
        return SETTINGS_TAB_BOUNDS.toMat().findFirstTemplate(SETTINGS_OPEN_MAT to 0.75) != null
    }

    private fun isPhoneSubOpen(): Boolean {
        return PHONE_SUB_BOUNDS.toMat().findFirstTemplate(PHONE_SUB_OPEN_MAT to 0.9) != null
    }

    fun setDropMode(enabled: Boolean): Boolean {
        if (enabled) {
            return if (!isDropMode()) {
                DROP_MODE_BOUNDS.click(5)
                Thread.sleep(nextLong(1250, 1500))
                isDropMode()
            } else {
                true
            }
        } else {
            return if (isDropMode()) {
                DROP_MODE_BOUNDS.click(5)
                Thread.sleep(nextLong(1250, 1500))
                !isDropMode()
            } else {
                true
            }
        }
    }

    fun setCamZoom(stage: GameScreen.PPoint): Boolean {
        var didSet = false
        doSafeLoop({ isInGame() && !didSet }) {
            if (!isSettingsOpen()) {
                println("opening settings...")
                SETTINGS_TAB_BOUNDS.click(10)
                Thread.sleep(nextLong(500, 750))
            } else if (!isPhoneSubOpen()) {
                println("opening phone submenu...")
                PHONE_SUB_BOUNDS.click(10)
                Thread.sleep(nextLong(500, 750))
            } else {
                println("Clicking cam zoom...")
                stage.click()
                Thread.sleep(nextLong(250, 300))
                CAM_ZOOM_BOUNDS.toMat().findFirstTemplate(TOGGLE_GREEN_MAT to 0.85)?.let {
                    val center = it.center
                    center.translate(CAM_ZOOM_BOUNDS.toScreenX(), CAM_ZOOM_BOUNDS.toScreenY())
                    if (center.distance(stage.toScreen()) < 10) {
                        didSet = true
                    }
                }
            }
        }
        return didSet
    }

    fun setCamZoom(level: Int): Boolean {
        require(!(level < 1 || level > 5)) { "Cam zoom level must be between 1-5" }
        return setCamZoom(
            when (level) {
                1 -> CAM_1
                2 -> CAM_2
                3 -> CAM_3
                4 -> CAM_4
                else -> CAM_5
            }
        )
    }

    fun swipeCamUp() {
        val xLoc = nextInt(590, 610)
        BlueStacks.adbDrag(
            xLoc,
            nextInt(45, 65),
            xLoc,
            nextInt(390, 410)
        )
    }

    fun swipeCamDown() {
        val xLoc = nextInt(590, 610)
        BlueStacks.adbDrag(
            xLoc,
            nextInt(390, 410),
            xLoc,
            nextInt(45, 65)
        )
    }

    fun setBrightness(stage: GameScreen.PPoint): Boolean {
        var didSet = false
        doSafeLoop({ isInGame() && !didSet }) {
            if (!isSettingsOpen()) {
                println("opening settings...")
                SETTINGS_TAB_BOUNDS.click(10)
                Thread.sleep(nextLong(500, 750))
            } else if (!isPhoneSubOpen()) {
                println("opening phone submenu...")
                PHONE_SUB_BOUNDS.click(10)
                Thread.sleep(nextLong(500, 750))
            } else {
                println("Clicking brightness...")
                stage.click()
                Thread.sleep(nextLong(250, 300))
                BRIGHTNESS_LEVEL_BOUNDS.toMat().findFirstTemplate(TOGGLE_BLUE_MAT to 0.85)?.let {
                    val center = it.center
                    center.translate(BRIGHTNESS_LEVEL_BOUNDS.toScreenX(), BRIGHTNESS_LEVEL_BOUNDS.toScreenY())
                    if (center.distance(stage.toScreen()) < 10) {
                        didSet = true
                    }
                }
            }
        }
        return didSet
    }

    fun setBrightness(level: Int): Boolean {
        require(!(level < 1 || level > 4)) { "Brightness level must be between 1-4" }
        return setBrightness(
            when (level) {
                1 -> BRIGHTNESS_1
                2 -> BRIGHTNESS_2
                3 -> BRIGHTNESS_3
                else -> BRIGHTNESS_4
            }
        )
    }

    fun resetPerspective() {
        COMPASS_BOUNDS.click(8)
        swipeCamUp()
        Thread.sleep(2500)
    }
}
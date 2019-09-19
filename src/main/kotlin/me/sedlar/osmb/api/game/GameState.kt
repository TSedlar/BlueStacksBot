package me.sedlar.osmb.api.game

import me.sedlar.osmb.api.core.Keyboard
import me.sedlar.osmb.api.core.Script.Companion.doSafeLoop
import me.sedlar.osmb.api.util.OpenCV
import me.sedlar.osmb.api.util.findFirstTemplate
import kotlin.random.Random.Default.nextLong

object GameState {

    val COMPASS_BOUNDS = GameScreen.PRect(78.78, 0.55, 4.78, 7.64)

    val LOGIN_SCREEN_BOUNDS = GameScreen.PRect(29.11, 29.82, 41.11, 39.64)
    val LOGIN_SCREEN_MATS = OpenCV.normModelRange("api/login_screen", 1..4, 0.95)

    val LOBBY_PLAY_BOUNDS = GameScreen.PRect(36.22, 50.91, 27.67, 19.64)
    val LOBBY_PLAY_MAT = OpenCV.mat("api/lobby_play.png")

    val DROP_MODE_BOUNDS = GameScreen.PRect(0.78, 30.91, 4.56, 5.45)
    val DROP_MODE_MAT = OpenCV.mat("api/drop_mode.png")

    val INV_TAB_BOUNDS = GameScreen.PRect(94.78, 36.73, 4.67, 8.18)
    val INV_OPEN_MAT = OpenCV.mat("api/inv_open.png")

    val SETTINGS_TAB_BOUNDS = GameScreen.PRect(94.56, 71.82, 5.0, 8.91)
    val SETTINGS_OPEN_MAT = OpenCV.mat("api/settings_open.png")

    val PHONE_SUB_BOUNDS = GameScreen.PRect(73.33, 37.45, 5.33, 8.91)
    val PHONE_SUB_OPEN_MAT = OpenCV.mat("api/phone_sub_open.png")

    val CAM_ZOOM_BOUNDS = GameScreen.PRect(78.0, 48.18, 14.33, 4.55)
    val BRIGHTNESS_LEVEL_BOUNDS = GameScreen.PRect(78.0, 54.55, 14.33, 4.55)

    val TOGGLE_GREEN_MAT = OpenCV.mat("api/toggle_green.png")
    val TOGGLE_BLUE_MAT = OpenCV.mat("api/toggle_blue.png")

    val CAM_1 = GameScreen.PPoint(79.44, 50.55) // zoomed out
    val CAM_2 = GameScreen.PPoint(82.11, 50.55)
    val CAM_3 = GameScreen.PPoint(85.22, 50.55)
    val CAM_4 = GameScreen.PPoint(88.11, 50.55)
    val CAM_5 = GameScreen.PPoint(91.00, 50.55) // zoomed in

    val BRIGHTNESS_1 = GameScreen.PPoint(80.0, 56.91) // dim
    val BRIGHTNESS_2 = GameScreen.PPoint(83.22, 56.91)
    val BRIGHTNESS_3 = GameScreen.PPoint(86.89, 56.91)
    val BRIGHTNESS_4 = GameScreen.PPoint(90.67, 56.91) // bright

    fun isAtLogin(): Boolean {
        return LOGIN_SCREEN_BOUNDS.toMat().findFirstTemplate(*LOGIN_SCREEN_MATS.toTypedArray()) != null
    }

    fun isAtLobby(): Boolean {
        return LOBBY_PLAY_BOUNDS.toMat().findFirstTemplate(LOBBY_PLAY_MAT to 0.95) != null
    }

    fun isInGame(): Boolean {
        return !isAtLobby() && !isAtLogin()
    }

    fun isDropMode(): Boolean {
        return DROP_MODE_BOUNDS.toMat().findFirstTemplate(DROP_MODE_MAT to 0.95) != null
    }

    fun isInvOpen(): Boolean {
        return INV_TAB_BOUNDS.toMat().findFirstTemplate(INV_OPEN_MAT to 0.95) != null
    }

    fun isSettingsOpen(): Boolean {
        return SETTINGS_TAB_BOUNDS.toMat().findFirstTemplate(SETTINGS_OPEN_MAT to 0.95) != null
    }

    private fun isPhoneSubOpen(): Boolean {
        return PHONE_SUB_BOUNDS.toMat().findFirstTemplate(PHONE_SUB_OPEN_MAT to 0.95) != null
    }

    fun setDropMode(enabled: Boolean) {
        if (enabled) {
            if (!isDropMode()) {
                DROP_MODE_BOUNDS.click(5)
                Thread.sleep(nextLong(750, 1000))
            }
        } else {
            if (isDropMode()) {
                DROP_MODE_BOUNDS.click(5)
                Thread.sleep(nextLong(750, 1000))
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
        return setCamZoom(when(level) {
            1 -> CAM_1
            2 -> CAM_2
            3 -> CAM_3
            4 -> CAM_4
            else -> CAM_5
        })
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
        return setBrightness(when(level) {
            1 -> BRIGHTNESS_1
            2 -> BRIGHTNESS_2
            3 -> BRIGHTNESS_3
            else -> BRIGHTNESS_4
        })
    }

    fun resetPerspective() {
        COMPASS_BOUNDS.click(8)
        Keyboard.arrowUp()
    }
}
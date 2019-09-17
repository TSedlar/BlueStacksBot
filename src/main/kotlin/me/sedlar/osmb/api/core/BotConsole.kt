package me.sedlar.osmb.api.core

import javafx.application.Platform
import me.sedlar.osmb.OSMobileBot
import java.text.SimpleDateFormat
import java.util.*

class BotConsole {

    companion object {

        fun println(msg: String) {
            kotlin.io.println(msg)
            Platform.runLater {
                if (OSMobileBot.console.text.isNotEmpty()) {
                    OSMobileBot.console.text += "\n"
                }
                val stamp = SimpleDateFormat("HH:mm:ss").format(Date())
                OSMobileBot.console.text += "[$stamp] $msg"

                OSMobileBot.console.scrollTop = Double.MAX_VALUE
            }
        }
    }
}
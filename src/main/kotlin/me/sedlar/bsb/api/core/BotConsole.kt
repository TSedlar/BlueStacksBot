package me.sedlar.bsb.api.core

import javafx.application.Platform
import me.sedlar.bsb.BlueStacksBot
import java.text.SimpleDateFormat
import java.util.*

object BotConsole {

    fun println(msg: String) {
        kotlin.io.println(msg)
        Platform.runLater {
            if (BlueStacksBot.console.text.isNotEmpty()) {
                BlueStacksBot.console.text += "\n"
            }
            val stamp = SimpleDateFormat("HH:mm:ss").format(Date())
            BlueStacksBot.console.text += "[$stamp] $msg"

            BlueStacksBot.console.scrollTop = Double.MAX_VALUE
        }
    }
}
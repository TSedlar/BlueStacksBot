package me.sedlar.bsb.api.util

import java.util.concurrent.TimeUnit
import kotlin.random.Random.Default.nextLong

object Timing {

    fun now(): Long {
        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime())
    }

    fun waitFor(timeout: Long, condition: () -> Boolean): Boolean {
        val start = now()
        while (now() - start < timeout) {
            if (condition()) {
                return true
            }
            Thread.sleep(nextLong(50, 75))
        }
        return false
    }
}

fun Boolean.pass(action: () -> Unit) {
    if (this) {
        action()
    }
}
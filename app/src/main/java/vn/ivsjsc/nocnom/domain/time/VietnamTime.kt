package vn.ivsjsc.nocnom.domain.time

import java.time.Instant
import java.time.ZoneId

object VietnamTime {
    private val zone = ZoneId.of("Asia/Ho_Chi_Minh")

    fun dateKey(timestamp: Long = System.currentTimeMillis()): String =
        Instant.ofEpochMilli(timestamp).atZone(zone).toLocalDate().toString()

    fun dayKey(timestamp: Long = System.currentTimeMillis()): String =
        when (Instant.ofEpochMilli(timestamp).atZone(zone).dayOfWeek.value) {
            1 -> "mon"
            2 -> "tue"
            3 -> "wed"
            4 -> "thu"
            5 -> "fri"
            6 -> "sat"
            else -> "sun"
        }

    fun hour(timestamp: Long = System.currentTimeMillis()): Int =
        Instant.ofEpochMilli(timestamp).atZone(zone).hour
}

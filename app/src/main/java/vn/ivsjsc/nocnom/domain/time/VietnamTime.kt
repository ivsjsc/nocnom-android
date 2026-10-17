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

    fun formatTime(timestamp: Long = System.currentTimeMillis()): String {
        val zdt = Instant.ofEpochMilli(timestamp).atZone(zone)
        return "%02d:%02d".format(zdt.hour, zdt.minute)
    }

    fun vietnameseDayFull(dayKey: String): String = when (dayKey) {
        "mon" -> "Thứ Hai"
        "tue" -> "Thứ Ba"
        "wed" -> "Thứ Tư"
        "thu" -> "Thứ Năm"
        "fri" -> "Thứ Sáu"
        "sat" -> "Thứ Bảy"
        "sun" -> "Chủ Nhật"
        else -> ""
    }

    fun isDateEditable(dateKey: String, now: Long = System.currentTimeMillis()): Boolean {
        return try {
            val date = java.time.LocalDate.parse(dateKey)
            val today = Instant.ofEpochMilli(now).atZone(zone).toLocalDate()
            val diff = java.time.temporal.ChronoUnit.DAYS.between(date, today)
            diff in 0..3
        } catch (_: Exception) {
            false
        }
    }

    fun editableDateRange(now: Long = System.currentTimeMillis()): Pair<String, String> {
        val today = Instant.ofEpochMilli(now).atZone(zone).toLocalDate()
        val minDate = today.minusDays(3)
        return minDate.toString() to today.toString()
    }

    fun timestampForDateAndMeal(dateKey: String, mealKey: vn.ivsjsc.nocnom.domain.model.MealKey): Long {
        val time = when (mealKey) {
            vn.ivsjsc.nocnom.domain.model.MealKey.A -> "08:00:00"
            vn.ivsjsc.nocnom.domain.model.MealKey.B -> "12:00:00"
            vn.ivsjsc.nocnom.domain.model.MealKey.C -> "18:00:00"
        }
        val date = java.time.LocalDate.parse(dateKey)
        val localTime = java.time.LocalTime.parse(time)
        return java.time.ZonedDateTime.of(date, localTime, zone).toInstant().toEpochMilli()
    }

    fun formatDayWithDate(dateKey: String): String {
        return try {
            val date = java.time.LocalDate.parse(dateKey)
            val dayOfWeek = when (date.dayOfWeek.value) {
                1 -> "Thứ Hai"
                2 -> "Thứ Ba"
                3 -> "Thứ Tư"
                4 -> "Thứ Năm"
                5 -> "Thứ Sáu"
                6 -> "Thứ Bảy"
                else -> "Chủ Nhật"
            }
            val dtf = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")
            "$dayOfWeek, ${date.format(dtf)}"
        } catch (_: Exception) {
            dateKey
        }
    }

    fun formatDateOnly(dateKey: String): String {
        return try {
            val date = java.time.LocalDate.parse(dateKey)
            val dtf = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")
            date.format(dtf)
        } catch (_: Exception) {
            dateKey
        }
    }
}

package com.habithut.util

import java.time.LocalDate
import java.time.format.DateTimeFormatter

object DateUtils {
    val DATE_KEY_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    fun todayKey(): String = LocalDate.now().format(DATE_KEY_FORMAT)

    fun previousDayKey(key: String): String = LocalDate.parse(key, DATE_KEY_FORMAT)
        .minusDays(1).format(DATE_KEY_FORMAT)

    fun nextDayKey(key: String): String = LocalDate.parse(key, DATE_KEY_FORMAT)
        .plusDays(1).format(DATE_KEY_FORMAT)
}
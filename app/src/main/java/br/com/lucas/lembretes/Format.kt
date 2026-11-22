package br.com.lucas.lembretes

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs

object Format {
    val locale: Locale = Locale.forLanguageTag("pt-BR")

    private val hourFmt = DateTimeFormatter.ofPattern("HH:mm", locale)
    private val dayLongFmt = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", locale)
    private val dayShortFmt = DateTimeFormatter.ofPattern("EEE, dd/MM", locale)
    private val dateButtonFmt = DateTimeFormatter.ofPattern("EEE, dd/MM/yyyy", locale)

    fun zoned(millis: Long): ZonedDateTime =
        Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault())

    fun time(millis: Long): String = zoned(millis).format(hourFmt)

    fun hour(h: Int, m: Int): String = "%02d:%02d".format(h, m)

    fun dateButton(date: LocalDate): String = date.format(dateButtonFmt).capitalized()

    /** "Hoje", "Amanhã", "Ontem" ou "Sábado, 28 de novembro". */
    fun dayHeader(date: LocalDate): String {
        val today = LocalDate.now()
        return when (date) {
            today -> "Hoje"
            today.plusDays(1) -> "Amanhã"
            today.minusDays(1) -> "Ontem"
            else -> date.format(dayLongFmt).capitalized()
        }
    }

    /** "hoje às 14:00", "amanhã às 08:30" ou "sex., 27/11 às 19:00". */
    fun whenText(millis: Long): String {
        val z = zoned(millis)
        val date = z.toLocalDate()
        val today = LocalDate.now()
        val day = when (date) {
            today -> "hoje"
            today.plusDays(1) -> "amanhã"
            else -> date.format(dayShortFmt)
        }
        return "$day às ${z.format(hourFmt)}"
    }

    /** "em 3 dias", "em 45 min", "há 2 h". */
    fun relative(millis: Long): String {
        val diff = millis - System.currentTimeMillis()
        val minutes = abs(diff) / 60_000L
        val amount = when {
            minutes < 1 -> return "agora"
            minutes < 60 -> "$minutes min"
            minutes < 1440 -> "${minutes / 60} h"
            else -> {
                val days = minutes / 1440
                if (days == 1L) "1 dia" else "$days dias"
            }
        }
        return if (diff >= 0) "em $amount" else "há $amount"
    }

    private fun String.capitalized(): String = replaceFirstChar { it.titlecase(locale) }
}

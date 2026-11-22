package br.com.lucas.lembretes

import org.json.JSONArray
import org.json.JSONObject

enum class Category(val label: String, val color: Long) {
    TRABALHO("Trabalho", 0xFF1E88E5),
    PROVA("Prova", 0xFFE53935),
    COBRANCA("Cobrança", 0xFF2E7D32),
    EVENTO("Evento", 0xFF8E24AA),
    OUTRO("Outro", 0xFF607D8B),
}

data class ReminderOption(val minutes: Int, val label: String)

/** Antecedências disponíveis para os lembretes (em minutos antes do evento). */
val REMINDER_OPTIONS = listOf(
    ReminderOption(0, "Na hora"),
    ReminderOption(15, "15 min antes"),
    ReminderOption(60, "1 hora antes"),
    ReminderOption(180, "3 horas antes"),
    ReminderOption(1440, "1 dia antes"),
    ReminderOption(2880, "2 dias antes"),
    ReminderOption(10080, "1 semana antes"),
)

data class Event(
    val id: Long,
    val title: String,
    val category: Category,
    /** Data e hora do evento, em epoch millis. */
    val dateTime: Long,
    /** Minutos de antecedência de cada lembrete. */
    val reminders: List<Int>,
    val notes: String = "",
    val done: Boolean = false,
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("title", title)
        put("category", category.name)
        put("dateTime", dateTime)
        put("reminders", JSONArray().apply { reminders.forEach { put(it) } })
        put("notes", notes)
        put("done", done)
    }

    companion object {
        fun fromJson(o: JSONObject): Event {
            val arr = o.optJSONArray("reminders") ?: JSONArray()
            return Event(
                id = o.getLong("id"),
                title = o.getString("title"),
                category = runCatching { Category.valueOf(o.getString("category")) }
                    .getOrDefault(Category.OUTRO),
                dateTime = o.getLong("dateTime"),
                reminders = List(arr.length()) { arr.getInt(it) },
                notes = o.optString("notes", ""),
                done = o.optBoolean("done", false),
            )
        }
    }
}

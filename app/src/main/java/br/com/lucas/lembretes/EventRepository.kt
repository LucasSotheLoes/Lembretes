package br.com.lucas.lembretes

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONArray
import java.io.File

/**
 * Guarda os eventos num arquivo JSON no armazenamento interno do app.
 * Simples e suficiente para algumas centenas de eventos.
 */
object EventRepository {
    private const val FILE_NAME = "eventos.json"

    private val _events = MutableStateFlow<List<Event>>(emptyList())
    val events: StateFlow<List<Event>> = _events.asStateFlow()

    @Volatile
    private var loaded = false

    @Synchronized
    fun init(context: Context) {
        if (loaded) return
        val file = File(context.applicationContext.filesDir, FILE_NAME)
        _events.value = if (file.exists()) {
            runCatching {
                val arr = JSONArray(file.readText())
                List(arr.length()) { Event.fromJson(arr.getJSONObject(it)) }
            }.getOrDefault(emptyList())
        } else {
            emptyList()
        }
        loaded = true
    }

    fun find(id: Long): Event? = _events.value.firstOrNull { it.id == id }

    @Synchronized
    fun upsert(context: Context, event: Event) {
        init(context)
        _events.value = (_events.value.filterNot { it.id == event.id } + event)
            .sortedBy { it.dateTime }
        persist(context)
        ReminderScheduler.schedule(context, event)
    }

    @Synchronized
    fun delete(context: Context, event: Event) {
        init(context)
        _events.value = _events.value.filterNot { it.id == event.id }
        persist(context)
        ReminderScheduler.cancel(context, event)
    }

    private fun persist(context: Context) {
        val dir = context.applicationContext.filesDir
        val arr = JSONArray()
        _events.value.forEach { arr.put(it.toJson()) }
        val tmp = File(dir, "$FILE_NAME.tmp")
        tmp.writeText(arr.toString())
        tmp.renameTo(File(dir, FILE_NAME))
    }
}

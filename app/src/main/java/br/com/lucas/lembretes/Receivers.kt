package br.com.lucas.lembretes

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/** Disparado pelo AlarmManager na hora de cada lembrete. */
class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getLongExtra(ReminderScheduler.EXTRA_ID, -1L)
        val offset = intent.getIntExtra(ReminderScheduler.EXTRA_OFFSET, 0)
        EventRepository.init(context)
        val event = EventRepository.find(id) ?: return
        if (event.done) return
        Notifications.show(context, event, offset)
    }
}

/** Recria os alarmes após reiniciar o celular, atualizar o app ou mudar o relógio. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        ReminderScheduler.rescheduleAll(context)
    }
}

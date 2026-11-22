package br.com.lucas.lembretes

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build

/** Agenda (e cancela) os alarmes que disparam as notificações. */
object ReminderScheduler {
    const val ACTION_REMINDER = "br.com.lucas.lembretes.REMINDER"
    const val EXTRA_ID = "event_id"
    const val EXTRA_OFFSET = "offset_minutes"

    private fun reminderIntent(context: Context, id: Long, offset: Int): Intent =
        Intent(context, ReminderReceiver::class.java).apply {
            action = ACTION_REMINDER
            // A URI deixa cada PendingIntent único (evento + antecedência).
            data = Uri.parse("lembretes://evento/$id/$offset")
            putExtra(EXTRA_ID, id)
            putExtra(EXTRA_OFFSET, offset)
        }

    fun schedule(context: Context, event: Event) {
        cancel(context, event)
        if (event.done) return

        val alarmManager = context.getSystemService(AlarmManager::class.java)
        val now = System.currentTimeMillis()

        for (offset in event.reminders.distinct()) {
            val triggerAt = event.dateTime - offset * 60_000L
            if (triggerAt <= now) continue

            val pi = PendingIntent.getBroadcast(
                context,
                0,
                reminderIntent(context, event.id, offset),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            val canExact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
                alarmManager.canScheduleExactAlarms()
            try {
                if (canExact) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
                } else {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
                }
            } catch (ex: SecurityException) {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
            }
        }
    }

    fun cancel(context: Context, event: Event) {
        val alarmManager = context.getSystemService(AlarmManager::class.java)
        val offsets = (REMINDER_OPTIONS.map { it.minutes } + event.reminders).distinct()
        for (offset in offsets) {
            val pi = PendingIntent.getBroadcast(
                context,
                0,
                reminderIntent(context, event.id, offset),
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE,
            ) ?: continue
            alarmManager.cancel(pi)
            pi.cancel()
        }
    }

    /** Alarmes somem quando o celular reinicia — este método recria todos. */
    fun rescheduleAll(context: Context) {
        EventRepository.init(context)
        EventRepository.events.value.forEach { schedule(context, it) }
    }
}

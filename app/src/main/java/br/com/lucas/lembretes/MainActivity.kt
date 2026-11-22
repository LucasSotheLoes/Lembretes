package br.com.lucas.lembretes

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import br.com.lucas.lembretes.ui.EditScreen
import br.com.lucas.lembretes.ui.LembretesTheme
import br.com.lucas.lembretes.ui.ListScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        EventRepository.init(this)
        setContent {
            LembretesTheme {
                AppRoot()
            }
        }
    }
}

private const val NEW_EVENT = 0L

@Composable
fun AppRoot() {
    val context = LocalContext.current
    val events by EventRepository.events.collectAsStateWithLifecycle()

    // null = lista; NEW_EVENT = criando; outro valor = editando o evento com esse id
    var editingId by rememberSaveable { mutableStateOf<Long?>(null) }

    var notificationsOk by remember { mutableStateOf(Notifications.canNotify(context)) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { notificationsOk = Notifications.canNotify(context) }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !notificationsOk) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
    LifecycleResumeEffect(Unit) {
        notificationsOk = Notifications.canNotify(context)
        onPauseOrDispose { }
    }

    val id = editingId
    if (id == null) {
        ListScreen(
            events = events,
            notificationsEnabled = notificationsOk,
            onEnableNotifications = { openNotificationSettings(context) },
            onAdd = { editingId = NEW_EVENT },
            onOpen = { editingId = it.id },
            onToggleDone = { EventRepository.upsert(context, it.copy(done = !it.done)) },
            onDelete = { EventRepository.delete(context, it) },
        )
    } else {
        val existing = if (id == NEW_EVENT) null else events.firstOrNull { it.id == id }
        EditScreen(
            initial = existing,
            onSave = {
                EventRepository.upsert(context, it)
                editingId = null
            },
            onCancel = { editingId = null },
            onDelete = existing?.let { event ->
                {
                    EventRepository.delete(context, event)
                    editingId = null
                }
            },
        )
    }
}

private fun openNotificationSettings(context: Context) {
    val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
        .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(intent)
}

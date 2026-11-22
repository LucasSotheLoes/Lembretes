package br.com.lucas.lembretes.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import br.com.lucas.lembretes.Event
import br.com.lucas.lembretes.Format

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListScreen(
    events: List<Event>,
    notificationsEnabled: Boolean,
    onEnableNotifications: () -> Unit,
    onAdd: () -> Unit,
    onOpen: (Event) -> Unit,
    onToggleDone: (Event) -> Unit,
    onDelete: (Event) -> Unit,
) {
    var showDone by rememberSaveable { mutableStateOf(false) }
    var toDelete by remember { mutableStateOf<Event?>(null) }

    val visible = remember(events, showDone) {
        events.filter { showDone || !it.done }.sortedBy { it.dateTime }
    }
    val grouped = remember(visible) {
        visible.groupBy { Format.zoned(it.dateTime).toLocalDate() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lembretes") },
                actions = {
                    TextButton(onClick = { showDone = !showDone }) {
                        Text(if (showDone) "Ocultar concluídos" else "Ver concluídos")
                    }
                },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAdd,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Novo") },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (!notificationsEnabled) {
                item(key = "banner") { NotificationBanner(onEnableNotifications) }
            }
            if (visible.isEmpty()) {
                item(key = "empty") { EmptyState() }
            }
            grouped.forEach { (date, dayEvents) ->
                item(key = "h-$date") {
                    Text(
                        text = Format.dayHeader(date),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 12.dp, bottom = 2.dp),
                    )
                }
                items(dayEvents, key = { it.id }) { event ->
                    EventCard(
                        event = event,
                        onClick = { onOpen(event) },
                        onToggleDone = { onToggleDone(event) },
                        onDelete = { toDelete = event },
                    )
                }
            }
        }
    }

    toDelete?.let { event ->
        AlertDialog(
            onDismissRequest = { toDelete = null },
            title = { Text("Excluir evento?") },
            text = { Text("\"${event.title}\" e os lembretes dele serão removidos.") },
            confirmButton = {
                TextButton(onClick = {
                    onDelete(event)
                    toDelete = null
                }) { Text("Excluir") }
            },
            dismissButton = {
                TextButton(onClick = { toDelete = null }) { Text("Cancelar") }
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EventCard(
    event: Event,
    onClick: () -> Unit,
    onToggleDone: () -> Unit,
    onDelete: () -> Unit,
) {
    val color = Color(event.category.color)
    val overdue = !event.done && event.dateTime < System.currentTimeMillis()
    val muted = MaterialTheme.colorScheme.onSurfaceVariant

    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(color),
            )
            Column(
                Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
            ) {
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleMedium,
                    textDecoration = if (event.done) TextDecoration.LineThrough else null,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = event.category.label,
                        style = MaterialTheme.typography.labelMedium,
                        color = color,
                    )
                    val extra = if (event.done) "" else "  ·  ${Format.relative(event.dateTime)}"
                    Text(
                        text = "  ·  ${Format.time(event.dateTime)}$extra",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (overdue) MaterialTheme.colorScheme.error else muted,
                    )
                }
                if (overdue) {
                    Text(
                        text = "Atrasado",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
                if (event.notes.isNotBlank()) {
                    Text(
                        text = event.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = muted,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            IconButton(onClick = onToggleDone) {
                Icon(
                    imageVector = if (event.done) Icons.Default.CheckCircle else Icons.Default.Check,
                    contentDescription = if (event.done) "Marcar como pendente" else "Concluir",
                    tint = if (event.done) color else muted,
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Excluir", tint = muted)
            }
        }
    }
}

@Composable
private fun NotificationBanner(onEnable: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
        ),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                "As notificações estão desativadas. Sem elas o app não consegue te lembrar de nada.",
                style = MaterialTheme.typography.bodyMedium,
            )
            TextButton(onClick = onEnable) { Text("Ativar notificações") }
        }
    }
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 96.dp, start = 24.dp, end = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Nada pendente", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        Text(
            "Toque em Novo para criar um lembrete de trabalho, prova, cobrança ou evento.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

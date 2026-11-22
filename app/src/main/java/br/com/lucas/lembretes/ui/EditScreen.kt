package br.com.lucas.lembretes.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import br.com.lucas.lembretes.Category
import br.com.lucas.lembretes.Event
import br.com.lucas.lembretes.Format
import br.com.lucas.lembretes.REMINDER_OPTIONS
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditScreen(
    initial: Event?,
    onSave: (Event) -> Unit,
    onCancel: () -> Unit,
    onDelete: (() -> Unit)?,
) {
    val zone = ZoneId.systemDefault()
    val initialDateTime = remember(initial) {
        initial?.let { Instant.ofEpochMilli(it.dateTime).atZone(zone).toLocalDateTime() }
            ?: LocalDate.now().plusDays(1).atTime(9, 0)
    }

    var title by remember { mutableStateOf(initial?.title ?: "") }
    var category by remember { mutableStateOf(initial?.category ?: Category.TRABALHO) }
    var date by remember { mutableStateOf(initialDateTime.toLocalDate()) }
    var time by remember { mutableStateOf(initialDateTime.toLocalTime().withSecond(0).withNano(0)) }
    var reminders by remember { mutableStateOf(initial?.reminders?.toSet() ?: setOf(1440, 60)) }
    var notes by remember { mutableStateOf(initial?.notes ?: "") }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val eventMillis = date.atTime(time).atZone(zone).toInstant().toEpochMilli()
    val now = System.currentTimeMillis()
    val futureReminders = reminders.count { eventMillis - it * 60_000L > now }

    BackHandler(onBack = onCancel)

    fun save() {
        onSave(
            Event(
                id = initial?.id ?: System.currentTimeMillis(),
                title = title.trim(),
                category = category,
                dateTime = eventMillis,
                reminders = reminders.sorted(),
                notes = notes.trim(),
                done = initial?.done ?: false,
            ),
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (initial == null) "Novo lembrete" else "Editar") },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Default.Close, contentDescription = "Cancelar")
                    }
                },
                actions = {
                    TextButton(onClick = { save() }, enabled = title.isNotBlank()) {
                        Text("Salvar")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("O que é?") },
                placeholder = { Text("Ex.: Prova de Sistemas Distribuídos") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                modifier = Modifier.fillMaxWidth(),
            )

            SectionTitle("Tipo")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Category.entries.forEach { c ->
                    val selected = c == category
                    FilterChip(
                        selected = selected,
                        onClick = { category = c },
                        label = { Text(c.label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(c.color).copy(alpha = 0.18f),
                            selectedLabelColor = Color(c.color),
                        ),
                    )
                }
            }

            SectionTitle("Quando")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { showDatePicker = true }, modifier = Modifier.weight(1.6f)) {
                    Icon(Icons.Default.DateRange, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(Format.dateButton(date))
                }
                OutlinedButton(onClick = { showTimePicker = true }, modifier = Modifier.weight(1f)) {
                    Text(Format.hour(time.hour, time.minute))
                }
            }

            SectionTitle("Me avisar")
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                REMINDER_OPTIONS.forEach { option ->
                    val selected = option.minutes in reminders
                    FilterChip(
                        selected = selected,
                        onClick = {
                            reminders = if (selected) reminders - option.minutes else reminders + option.minutes
                        },
                        label = { Text(option.label) },
                        leadingIcon = if (selected) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        } else {
                            null
                        },
                    )
                }
            }
            when {
                reminders.isEmpty() -> Hint("Nenhum aviso selecionado — o evento fica só na lista.", warning = true)
                futureReminders == 0 -> Hint("Todos os avisos escolhidos já passaram; nenhuma notificação será enviada.", warning = true)
                else -> Hint(
                    if (futureReminders == 1) "1 notificação será agendada." else "$futureReminders notificações serão agendadas.",
                    warning = false,
                )
            }

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Observações (opcional)") },
                placeholder = { Text("Ex.: sala 204, levar calculadora, valor R$ 350") },
                minLines = 3,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                modifier = Modifier.fillMaxWidth(),
            )

            if (onDelete != null) {
                TextButton(onClick = onDelete) {
                    Text("Excluir este evento", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }

    if (showDatePicker) {
        val state = rememberDatePickerState(
            // O DatePicker trabalha com meia-noite em UTC
            initialSelectedDateMillis = date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let {
                        date = Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") }
            },
        ) {
            DatePicker(state = state)
        }
    }

    if (showTimePicker) {
        val state = rememberTimePickerState(
            initialHour = time.hour,
            initialMinute = time.minute,
            is24Hour = true,
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    time = LocalTime.of(state.hour, state.minute)
                    showTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Cancelar") }
            },
            text = { TimePicker(state = state) },
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
    )
}

@Composable
private fun Hint(text: String, warning: Boolean) {
    Row {
        Icon(
            Icons.Default.Notifications,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = if (warning) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = if (warning) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

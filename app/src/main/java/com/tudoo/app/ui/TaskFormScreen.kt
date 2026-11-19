package com.tudoo.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tudoo.app.data.Task
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private const val HOUR_MS = 3_600_000L
private const val DAY_MS = 86_400_000L

fun formatDateTime(millis: Long): String {
    val format = SimpleDateFormat("EEE, d MMM yyyy HH:mm", Locale("id", "ID"))
    return format.format(Date(millis))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateTimeField(
    label: String,
    value: Long,
    onValueChange: (Long) -> Unit
) {
    var showDate by remember { mutableStateOf(false) }
    var showTime by remember { mutableStateOf(false) }
    var pickedDate by remember { mutableLongStateOf(0L) }

    Text(
        text = label,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onBackground
    )
    OutlinedButton(
        onClick = { showDate = true },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(formatDateTime(value))
    }

    if (showDate) {
        val dateState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDate = false },
            confirmButton = {
                TextButton(onClick = {
                    val selected = dateState.selectedDateMillis
                    showDate = false
                    if (selected != null) {
                        pickedDate = selected
                        showTime = true
                    }
                }) { Text("Lanjut pilih jam") }
            },
            dismissButton = {
                TextButton(onClick = { showDate = false }) { Text("Batal") }
            }
        ) {
            DatePicker(state = dateState)
        }
    }

    if (showTime) {
        val now = Calendar.getInstance()
        val timeState = rememberTimePickerState(
            initialHour = now.get(Calendar.HOUR_OF_DAY),
            initialMinute = now.get(Calendar.MINUTE),
            is24Hour = true
        )
        AlertDialog(
            onDismissRequest = { showTime = false },
            title = { Text("Pilih jam") },
            text = { TimeInput(state = timeState) },
            confirmButton = {
                TextButton(onClick = {
                    val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                    utc.timeInMillis = pickedDate
                    val local = Calendar.getInstance()
                    local.set(
                        utc.get(Calendar.YEAR),
                        utc.get(Calendar.MONTH),
                        utc.get(Calendar.DAY_OF_MONTH),
                        timeState.hour,
                        timeState.minute,
                        0
                    )
                    local.set(Calendar.MILLISECOND, 0)
                    onValueChange(local.timeInMillis)
                    showTime = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showTime = false }) { Text("Batal") }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TaskFormScreen(
    taskId: Long,
    viewModel: TaskViewModel,
    onFinish: () -> Unit
) {
    var existing by remember { mutableStateOf<Task?>(null) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var teacher by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var deadline by remember { mutableLongStateOf(System.currentTimeMillis() + DAY_MS) }
    var reminderMode by remember { mutableStateOf("none") }
    var manualReminder by remember { mutableLongStateOf(System.currentTimeMillis() + HOUR_MS) }

    LaunchedEffect(taskId) {
        if (taskId != 0L) {
            val t = viewModel.getTask(taskId)
            if (t != null) {
                existing = t
                title = t.title
                description = t.description
                teacher = t.teacher
                notes = t.notes
                deadline = t.deadline
                if (t.reminderTime != null) {
                    reminderMode = "manual"
                    manualReminder = t.reminderTime
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = if (taskId == 0L) "Tambah Tugas" else "Edit Tugas",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Judul / Mata pelajaran") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Materi / deskripsi tugas") },
            minLines = 2,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = teacher,
            onValueChange = { teacher = it },
            label = { Text("Nama guru") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Catatan") },
            minLines = 2,
            modifier = Modifier.fillMaxWidth()
        )

        DateTimeField(
            label = "Deadline",
            value = deadline,
            onValueChange = { deadline = it }
        )

        Text(
            text = "Pengingat",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        val options = listOf(
            "none" to "Tanpa pengingat",
            "1h" to "1 jam sebelum",
            "3h" to "3 jam sebelum",
            "1d" to "1 hari sebelum",
            "manual" to "Atur manual"
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            options.forEach { (mode, text) ->
                FilterChip(
                    selected = reminderMode == mode,
                    onClick = { reminderMode = mode },
                    label = { Text(text) }
                )
            }
        }

        if (reminderMode == "manual") {
            DateTimeField(
                label = "Waktu pengingat",
                value = manualReminder,
                onValueChange = { manualReminder = it }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onFinish,
                modifier = Modifier.weight(1f)
            ) { Text("Batal") }

            Button(
                onClick = {
                    val reminder: Long? = when (reminderMode) {
                        "1h" -> deadline - HOUR_MS
                        "3h" -> deadline - 3 * HOUR_MS
                        "1d" -> deadline - DAY_MS
                        "manual" -> manualReminder
                        else -> null
                    }
                    val base = existing
                    if (base == null) {
                        viewModel.addTask(
                            Task(
                                title = title.trim(),
                                description = description.trim(),
                                teacher = teacher.trim(),
                                notes = notes.trim(),
                                deadline = deadline,
                                reminderTime = reminder
                            )
                        )
                    } else {
                        viewModel.updateTask(
                            base.copy(
                                title = title.trim(),
                                description = description.trim(),
                                teacher = teacher.trim(),
                                notes = notes.trim(),
                                deadline = deadline,
                                reminderTime = reminder
                            )
                        )
                    }
                    onFinish()
                },
                enabled = title.isNotBlank(),
                modifier = Modifier.weight(1f)
            ) { Text("Simpan") }
        }
    }
}

@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

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
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tudoo.app.data.Task
import com.tudoo.app.data.reminderTimes
import com.tudoo.app.data.toReminderString
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private const val HOUR_MS = 3_600_000L
private const val DAY_MS = 86_400_000L

private val localeId = Locale("id", "ID")

fun formatDateTime(millis: Long): String =
    SimpleDateFormat("EEE, d MMM yyyy HH:mm", localeId).format(Date(millis))

private fun formatDate(millis: Long): String =
    SimpleDateFormat("EEE, d MMM yyyy", localeId).format(Date(millis))

private fun formatTime(millis: Long): String =
    SimpleDateFormat("HH:mm", localeId).format(Date(millis))

private fun utcMidnightOf(local: Long): Long {
    val c = Calendar.getInstance().apply { timeInMillis = local }
    val u = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
    u.clear()
    u.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH))
    return u.timeInMillis
}

private fun withDate(base: Long, utcDate: Long): Long {
    val u = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
    u.timeInMillis = utcDate
    val c = Calendar.getInstance()
    c.timeInMillis = base
    c.set(u.get(Calendar.YEAR), u.get(Calendar.MONTH), u.get(Calendar.DAY_OF_MONTH))
    return c.timeInMillis
}

private fun withTime(base: Long, hour: Int, minute: Int): Long {
    val c = Calendar.getInstance()
    c.timeInMillis = base
    c.set(Calendar.HOUR_OF_DAY, hour)
    c.set(Calendar.MINUTE, minute)
    c.set(Calendar.SECOND, 0)
    c.set(Calendar.MILLISECOND, 0)
    return c.timeInMillis
}

@Composable
private fun DateTimeRow(
    label: String,
    value: Long,
    onValueChange: (Long) -> Unit
) {
    var showDate by remember { mutableStateOf(false) }
    var showTime by remember { mutableStateOf(false) }

    Text(
        text = label,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onBackground
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedButton(
            onClick = { showDate = true },
            modifier = Modifier.weight(1.5f)
        ) { Text(formatDate(value)) }
        OutlinedButton(
            onClick = { showTime = true },
            modifier = Modifier.weight(1f)
        ) { Text(formatTime(value)) }
    }

    if (showDate) {
        val dateState = rememberDatePickerState(
            initialSelectedDateMillis = utcMidnightOf(value)
        )
        DatePickerDialog(
            onDismissRequest = { showDate = false },
            confirmButton = {
                TextButton(onClick = {
                    val selected = dateState.selectedDateMillis
                    if (selected != null) {
                        onValueChange(withDate(value, selected))
                    }
                    showDate = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDate = false }) { Text("Batal") }
            }
        ) {
            DatePicker(state = dateState)
        }
    }

    if (showTime) {
        val c = Calendar.getInstance()
        c.timeInMillis = value
        val timeState = rememberTimePickerState(
            initialHour = c.get(Calendar.HOUR_OF_DAY),
            initialMinute = c.get(Calendar.MINUTE),
            is24Hour = true
        )
        AlertDialog(
            onDismissRequest = { showTime = false },
            title = { Text("Pilih jam") },
            text = { TimePicker(state = timeState) },
            confirmButton = {
                TextButton(onClick = {
                    onValueChange(withTime(value, timeState.hour, timeState.minute))
                    showTime = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showTime = false }) { Text("Batal") }
            }
        )
    }
}

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
    var quickSelected by remember { mutableStateOf(setOf<Long>()) }
    var manualList by remember { mutableStateOf(listOf<Long>()) }
    var pendingManual by remember { mutableLongStateOf(System.currentTimeMillis() + HOUR_MS) }

    val quickOptions = listOf(
        HOUR_MS to "1 jam sebelum",
        3 * HOUR_MS to "3 jam sebelum",
        DAY_MS to "1 hari sebelum"
    )

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
                val quick = mutableSetOf<Long>()
                val manual = mutableListOf<Long>()
                t.reminderTimes().forEach { time ->
                    val offset = t.deadline - time
                    if (quickOptions.any { it.first == offset }) {
                        quick.add(offset)
                    } else {
                        manual.add(time)
                    }
                }
                quickSelected = quick
                manualList = manual
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

        DateTimeRow(
            label = "Deadline",
            value = deadline,
            onValueChange = { deadline = it }
        )

        Text(
            text = "Pengingat (boleh lebih dari satu)",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            quickOptions.forEach { (offset, text) ->
                FilterChip(
                    selected = offset in quickSelected,
                    onClick = {
                        quickSelected = if (offset in quickSelected) {
                            quickSelected - offset
                        } else {
                            quickSelected + offset
                        }
                    },
                    label = { Text(text) }
                )
            }
        }

        manualList.forEach { time ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Pengingat: " + formatDateTime(time),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                TextButton(onClick = { manualList = manualList - time }) {
                    Text("Hapus")
                }
            }
        }

        DateTimeRow(
            label = "Tambah pengingat manual",
            value = pendingManual,
            onValueChange = { pendingManual = it }
        )
        OutlinedButton(
            onClick = {
                if (pendingManual !in manualList) {
                    manualList = manualList + pendingManual
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("+ Tambah pengingat ini") }

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
                    val times = quickSelected.map { deadline - it } + manualList
                    val reminderText = times.toReminderString()
                    val base = existing
                    if (base == null) {
                        viewModel.addTask(
                            Task(
                                title = title.trim(),
                                description = description.trim(),
                                teacher = teacher.trim(),
                                notes = notes.trim(),
                                deadline = deadline,
                                reminders = reminderText
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
                                reminders = reminderText
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
}                        description = description.trim(),
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

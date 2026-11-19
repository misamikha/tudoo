package com.tudoo.app.ui

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import com.tudoo.app.R
import com.tudoo.app.data.Task
import com.tudoo.app.reminder.ReminderNotifications
import com.tudoo.app.reminder.ReminderScheduler

/** Alat bantu untuk halaman pengaturan dan diagnosis alarm. */
private object AlarmDiagnostics {

    fun canScheduleExact(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        return am.canScheduleExactAlarms()
    }

    /** Jadwalkan alarm tes (tanpa tugas di database) lewat jalur yang sama. */
    fun scheduleTest(context: Context, delayMs: Long) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val time = System.currentTimeMillis() + delayMs
        val intent = Intent(ReminderScheduler.ACTION_REMINDER).apply {
            setPackage(context.packageName)
            data = Uri.parse("tudoo://reminder/-1/$time")
            putExtra(ReminderScheduler.EXTRA_TASK_ID, -1L)
            putExtra(ReminderScheduler.EXTRA_REMINDER_TIME, time)
        }
        val pending = PendingIntent.getBroadcast(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        try {
            if (canScheduleExact(context)) {
                am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pending)
            } else {
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pending)
            }
        } catch (e: SecurityException) {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pending)
        }
    }
}

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    var refresh by remember { mutableIntStateOf(0) }
    var info by remember { mutableStateOf("") }

    val notifOn = remember(refresh) {
        NotificationManagerCompat.from(context).areNotificationsEnabled()
    }
    val exactOn = remember(refresh) { AlarmDiagnostics.canScheduleExact(context) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.dodo_bangga),
            contentDescription = "Dodo",
            modifier = Modifier.size(120.dp)
        )
        Text(
            text = "Pengaturan",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Status pengingat",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Notifikasi: " + if (notifOn) "AKTIF ✅" else "MATI ❌",
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Alarm presisi: " + if (exactOn) "AKTIF ✅" else "MATI ❌ (alarm bisa telat)",
            color = MaterialTheme.colorScheme.onBackground
        )

        OutlinedButton(
            onClick = { refresh++ },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Segarkan status") }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Button(
                onClick = {
                    try {
                        context.startActivity(
                            Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                data = Uri.parse("package:${context.packageName}")
                            }
                        )
                    } catch (e: Exception) {
                        info = "Gagal membuka pengaturan alarm."
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Buka izin alarm presisi") }
        }

        OutlinedButton(
            onClick = {
                try {
                    context.startActivity(
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.parse("package:${context.packageName}")
                        }
                    )
                } catch (e: Exception) {
                    info = "Gagal membuka pengaturan aplikasi."
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Buka pengaturan aplikasi") }

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Tes",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Button(
            onClick = {
                val now = System.currentTimeMillis()
                ReminderNotifications.show(context, Task(title = "Tes Dodo", deadline = now), now)
                info = "Notifikasi tes dikirim sekarang. Muncul?"
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Tes notifikasi sekarang") }

        Button(
            onClick = {
                AlarmDiagnostics.scheduleTest(context, 60_000L)
                info = "Alarm tes dijadwalkan 1 menit lagi. Keluar dari aplikasi dan tunggu."
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Tes alarm 1 menit lagi") }

        if (info.isNotBlank()) {
            Text(
                text = info,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
        }
    }
}

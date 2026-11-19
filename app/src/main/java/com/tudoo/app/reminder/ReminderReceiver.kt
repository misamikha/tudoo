package com.tudoo.app.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.tudoo.app.data.Task
import com.tudoo.app.data.TudooDatabase
import com.tudoo.app.data.reminderTimes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Menerima alarm pengingat lalu menampilkan notifikasi.
 */
class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ReminderScheduler.ACTION_REMINDER) return

        val taskId = intent.getLongExtra(ReminderScheduler.EXTRA_TASK_ID, -1L)
        val time = intent.getLongExtra(ReminderScheduler.EXTRA_REMINDER_TIME, -1L)
        val appContext = context.applicationContext

        ReminderDebug.record(appContext, "ALARM DITERIMA (tugas=$taskId)")

        // Alarm tes dari halaman Settings (tanpa tugas di database).
        if (taskId == -1L && time > 0) {
            ReminderNotifications.show(
                appContext,
                Task(title = "Tes Dodo", deadline = time),
                time
            )
            return
        }
        if (taskId < 0 || time < 0) return

        val pending = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val task = TudooDatabase.getInstance(appContext)
                    .taskDao()
                    .getTaskById(taskId)

                // Tampilkan hanya jika tugas masih ada, belum selesai,
                // dan waktu pengingat ini memang masih terdaftar.
                if (task != null && !task.isDone && time in task.reminderTimes()) {
                    ReminderNotifications.show(appContext, task, time)
                } else {
                    ReminderDebug.record(
                        appContext,
                        "alarm diterima tapi dilewati (tugas hilang/selesai/waktu tidak terdaftar)"
                    )
                }
            } finally {
                pending.finish()
            }
        }
    }
}

package com.tudoo.app.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.tudoo.app.data.TudooDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Menjadwalkan ulang semua alarm pengingat setelah HP restart
 * atau setelah aplikasi diperbarui (alarm hilang pada kedua kejadian itu).
 */
class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action != Intent.ACTION_BOOT_COMPLETED &&
            action != Intent.ACTION_MY_PACKAGE_REPLACED
        ) return

        val appContext = context.applicationContext
        val pending = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val tasks = TudooDatabase.getInstance(appContext)
                    .taskDao()
                    .getTasksWithPendingReminder()
                ReminderScheduler.scheduleAll(appContext, tasks)
                ReminderDebug.record(
                    appContext,
                    "setelah restart/update: ${tasks.size} tugas dijadwalkan ulang"
                )
            } catch (e: Exception) {
                ReminderDebug.record(appContext, "jadwal ulang GAGAL: ${e.javaClass.simpleName}")
            } finally {
                pending.finish()
            }
        }
    }
}

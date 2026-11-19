package com.tudoo.app.reminder

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import com.tudoo.app.data.Task
import com.tudoo.app.data.reminderTimes

/**
 * Menjadwalkan dan membatalkan alarm pengingat tugas.
 *
 * Setiap pengingat (tugas + waktu) punya alamat unik, sehingga:
 * - satu tugas bisa punya banyak alarm,
 * - menjadwalkan dua kali tidak membuat alarm ganda,
 * - alarm bisa dibatalkan dengan tepat.
 */
object ReminderScheduler {

    const val ACTION_REMINDER = "com.tudoo.app.ACTION_REMINDER"
    const val EXTRA_TASK_ID = "extra_task_id"
    const val EXTRA_REMINDER_TIME = "extra_reminder_time"

    /** Jadwalkan semua pengingat tugas yang waktunya belum lewat. */
    fun schedule(context: Context, task: Task) {
        if (task.isDone) return
        val now = System.currentTimeMillis()
        task.reminderTimes()
            .filter { it > now }
            .forEach { time -> scheduleOne(context, task.id, time) }
    }

    /** Batalkan semua alarm milik tugas ini. */
    fun cancel(context: Context, task: Task) {
        task.reminderTimes().forEach { time -> cancelOne(context, task.id, time) }
    }

    /** Jadwalkan ulang banyak tugas sekaligus (untuk setelah HP restart). */
    fun scheduleAll(context: Context, tasks: List<Task>) {
        tasks.forEach { schedule(context, it) }
    }

    @SuppressLint("ScheduleExactAlarm")
    private fun scheduleOne(context: Context, taskId: Long, time: Long) {
        val alarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = buildPendingIntent(context, taskId, time, create = true)
            ?: return

        val canUseExact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
            alarmManager.canScheduleExactAlarms()

        try {
            if (canUseExact) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, time, pendingIntent
                )
            } else {
                // Izin alarm presisi belum diberikan: pakai alarm biasa
                // (bisa meleset beberapa menit, tapi tetap berbunyi).
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, time, pendingIntent
                )
            }
        } catch (e: SecurityException) {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP, time, pendingIntent
            )
        }
    }

    private fun cancelOne(context: Context, taskId: Long, time: Long) {
        val alarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pendingIntent = buildPendingIntent(context, taskId, time, create = false)
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    private fun buildPendingIntent(
        context: Context,
        taskId: Long,
        time: Long,
        create: Boolean
    ): PendingIntent? {
        val intent = Intent(ACTION_REMINDER).apply {
            setPackage(context.packageName)
            // Alamat unik per pengingat: inilah yang membedakan satu alarm
            // dari alarm lainnya.
            data = Uri.parse("tudoo://reminder/$taskId/$time")
            putExtra(EXTRA_TASK_ID, taskId)
            putExtra(EXTRA_REMINDER_TIME, time)
        }
        val flags = if (create) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        }
        return PendingIntent.getBroadcast(context, 0, intent, flags)
    }
}

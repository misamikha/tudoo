package com.tudoo.app.reminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.tudoo.app.MainActivity
import com.tudoo.app.R
import com.tudoo.app.data.Task

/**
 * Membuat channel dan menampilkan notifikasi pengingat.
 */
object ReminderNotifications {

    const val CHANNEL_ID = "tudoo_reminders"

    /** Aman dipanggil berkali-kali. */
    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE)
            as NotificationManager
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Pengingat tugas",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Pengingat deadline tugas dari Dodo"
        }
        manager.createNotificationChannel(channel)
    }

    /** Mengembalikan teks hasil (untuk diagnosis) dan mencatatnya. */
    fun show(context: Context, task: Task, reminderTime: Long): String {
        val result = try {
            buildAndNotify(context, task, reminderTime)
        } catch (e: Exception) {
            "GAGAL: ${e.javaClass.simpleName}: ${e.message}"
        }
        ReminderDebug.record(context, "hasil: $result")
        return result
    }

    private fun buildAndNotify(context: Context, task: Task, reminderTime: Long): String {
        val manager = NotificationManagerCompat.from(context)
        if (!manager.areNotificationsEnabled()) {
            return "NOTIFIKASI DIMATIKAN di pengaturan HP untuk Tudoo"
        }

        ensureChannel(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager
            val channel = nm.getNotificationChannel(CHANNEL_ID)
            if (channel != null && channel.importance == NotificationManager.IMPORTANCE_NONE) {
                return "CHANNEL 'Pengingat tugas' DIMATIKAN di pengaturan HP"
            }
        }

        // ID unik per pengingat supaya tidak saling menimpa.
        val notificationId = "${task.id}-$reminderTime".hashCode()

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openPending = PendingIntent.getActivity(
            context,
            notificationId,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val message = if (task.teacher.isNotBlank()) {
            "Tugas ${task.title} dari ${task.teacher} sudah mendekati deadline."
        } else {
            "Tugas ${task.title} sudah mendekati deadline."
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setColor(0xFF839557.toInt())
            .setLargeIcon(decodeScaled(context, R.drawable.dodo_mengingatkan, 256))
            .setContentTitle("Waktunya mengerjakan tugas! \uD83D\uDC30")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(openPending)
            .setAutoCancel(true)
            .build()

        manager.notify(notificationId, notification)
        return "TERKIRIM ke sistem Android"
    }

    /** Memuat gambar dengan ukuran dikecilkan supaya hemat memori. */
    private fun decodeScaled(context: Context, resId: Int, maxSize: Int): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeResource(context.resources, resId, bounds)

        var sample = 1
        while (bounds.outWidth / (sample * 2) >= maxSize &&
            bounds.outHeight / (sample * 2) >= maxSize
        ) {
            sample *= 2
        }

        val options = BitmapFactory.Options().apply { inSampleSize = sample }
        return BitmapFactory.decodeResource(context.resources, resId, options)
    }
}

package com.tudoo.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.tudoo.app.reminder.ReminderNotifications
import com.tudoo.app.ui.TudooApp
import com.tudoo.app.ui.theme.TudooTheme

class MainActivity : ComponentActivity() {

    // Dialog izin notifikasi (Android 13+). Hasilnya tidak perlu diproses:
    // kalau ditolak, aplikasi tetap jalan, hanya notifikasi yang tidak tampil.
    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ReminderNotifications.ensureChannel(this)
        requestNotificationPermissionIfNeeded()

        setContent {
            TudooTheme {
                TudooApp()
            }
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

        val granted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED

        if (!granted) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}

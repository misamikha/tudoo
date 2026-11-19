package com.tudoo.app.reminder

import android.content.Context
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Catatan kecil untuk diagnosis: kapan alarm terakhir diterima dan hasilnya.
 */
object ReminderDebug {
    private const val PREFS = "tudoo_debug"
    private const val KEY = "last"

    fun record(context: Context, text: String) {
        val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY, "$time $text")
            .apply()
    }

    fun read(context: Context): String {
        val last = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY, null)
        return "Catatan terakhir: " + (last ?: "belum ada alarm/notifikasi tercatat")
    }
}

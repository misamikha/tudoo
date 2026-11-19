package com.tudoo.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tudoo.app.data.Task
import com.tudoo.app.data.TudooDatabase
import com.tudoo.app.reminder.ReminderScheduler
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TaskViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = TudooDatabase.getInstance(application).taskDao()
    private val appContext = application.applicationContext

    val tasks: StateFlow<List<Task>> = dao.getAllTasks()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addTask(task: Task) {
        viewModelScope.launch {
            val newId = dao.insert(task)
            // Jadwalkan alarm memakai ID tugas yang baru dibuat.
            ReminderScheduler.schedule(appContext, task.copy(id = newId))
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            // Batalkan alarm lama dulu (berdasarkan data lama di database),
            // supaya tidak ada notifikasi ganda atau alarm yang tertinggal.
            val old = dao.getTaskById(task.id)
            if (old != null) {
                ReminderScheduler.cancel(appContext, old)
            }
            dao.update(task)
            ReminderScheduler.schedule(appContext, task)
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            ReminderScheduler.cancel(appContext, task)
            dao.delete(task)
        }
    }

    fun toggleDone(task: Task) {
        viewModelScope.launch {
            val updated = task.copy(isDone = !task.isDone)
            dao.update(updated)
            if (updated.isDone) {
                // Tugas selesai: tidak boleh ada pengingat lagi.
                ReminderScheduler.cancel(appContext, task)
            } else {
                // Centang dilepas: jadwalkan lagi pengingat yang belum lewat.
                ReminderScheduler.schedule(appContext, updated)
            }
        }
    }

    suspend fun getTask(id: Long): Task? = dao.getTaskById(id)
}

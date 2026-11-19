package com.tudoo.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tudoo.app.data.Task
import com.tudoo.app.data.TudooDatabase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TaskViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = TudooDatabase.getInstance(application).taskDao()

    val tasks: StateFlow<List<Task>> = dao.getAllTasks()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addTask(task: Task) {
        viewModelScope.launch { dao.insert(task) }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch { dao.update(task) }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch { dao.delete(task) }
    }

    fun toggleDone(task: Task) {
        viewModelScope.launch { dao.update(task.copy(isDone = !task.isDone)) }
    }

    suspend fun getTask(id: Long): Task? = dao.getTaskById(id)
}

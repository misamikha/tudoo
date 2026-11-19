package com.tudoo.app.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String = "",
    val teacher: String = "",
    val notes: String = "",
    val deadline: Long,
    val reminderTime: Long? = null,
    val isDone: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY isDone ASC, deadline ASC")
    fun getAllTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTaskById(id: Long): Task?

    @Query("SELECT * FROM tasks WHERE isDone = 0 AND reminderTime IS NOT NULL")
    suspend fun getTasksWithPendingReminder(): List<Task>

    @Insert
    suspend fun insert(task: Task): Long

    @Update
    suspend fun update(task: Task)

    @Delete
    suspend fun delete(task: Task)
}

@Database(entities = [Task::class], version = 1, exportSchema = false)
abstract class TudooDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao

    companion object {
        @Volatile
        private var instance: TudooDatabase? = null

        fun getInstance(context: Context): TudooDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    TudooDatabase::class.java,
                    "tudoo.db"
                ).build().also { instance = it }
            }
        }
    }
}

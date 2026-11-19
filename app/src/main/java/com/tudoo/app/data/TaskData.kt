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
    // Tidak dipakai lagi, digantikan oleh "reminders".
    val reminderTime: Long? = null,
    // Daftar waktu pengingat (milidetik), dipisah koma.
    val reminders: String = "",
    val isDone: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

fun Task.reminderTimes(): List<Long> =
    reminders.split(",").mapNotNull { it.trim().toLongOrNull() }.sorted()

fun List<Long>.toReminderString(): String =
    distinct().sorted().joinToString(",")

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY isDone ASC, deadline ASC")
    fun getAllTasks(): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTaskById(id: Long): Task?

    @Query("SELECT * FROM tasks WHERE isDone = 0 AND reminders != ''")
    suspend fun getTasksWithPendingReminder(): List<Task>

    @Insert
    suspend fun insert(task: Task): Long

    @Update
    suspend fun update(task: Task)

    @Delete
    suspend fun delete(task: Task)
}

@Database(entities = [Task::class], version = 2, exportSchema = false)
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
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { instance = it }
            }
        }
    }
}

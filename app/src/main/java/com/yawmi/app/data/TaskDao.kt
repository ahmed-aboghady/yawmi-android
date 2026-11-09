package com.yawmi.app.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE type = 'WEEKLY' AND weekStart = :week ORDER BY id ASC")
    fun weeklyTasks(week: String): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE type = 'DAILY' AND dayKey = :day ORDER BY id ASC")
    fun dailyTasks(day: String): Flow<List<Task>>

    @Query("SELECT * FROM tasks WHERE type = 'DAILY' AND dayKey = :day ORDER BY id ASC")
    suspend fun dailyTasksNow(day: String): List<Task>

    @Query("SELECT * FROM tasks WHERE type = 'WEEKLY' AND weekStart = :week ORDER BY id ASC")
    suspend fun weeklyTasksNow(week: String): List<Task>

    @Insert suspend fun insert(task: Task)
    @Update suspend fun update(task: Task)
    @Query("DELETE FROM tasks WHERE id = :id") suspend fun delete(id: Long)
    @Query("SELECT * FROM tasks WHERE id = :id LIMIT 1") suspend fun byId(id: Long): Task?
}

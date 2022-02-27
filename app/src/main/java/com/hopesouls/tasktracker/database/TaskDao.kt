package com.hopesouls.tasktracker.database

import androidx.room.*

@Dao
interface TaskDao {
    @Query("SELECT * FROM TaskList")
    fun getAllTasks(): List<Task>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAll(vararg task: Task)

    @Delete
    fun deleteTask(task: Task)

    @Update
    suspend  fun updateTask(vararg task: Task)
}


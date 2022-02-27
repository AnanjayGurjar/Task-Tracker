package com.hopesouls.tasktracker.database

import androidx.room.Database
import androidx.room.RoomDatabase
@Database(entities = arrayOf(Task::class), version = 1)
abstract class TaskDataBase: RoomDatabase() {
    abstract fun taskDao(): TaskDao
}
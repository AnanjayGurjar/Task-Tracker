package com.hopesouls.tasktracker.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "TaskList")
data class Task (
    val taskName: String,
    val totalTime: String,
    val completedTime: String,
    val percentage: String,
    val timeStamp: String,
    val days: Int,
    val daysRemain: Int,
    @PrimaryKey(autoGenerate = true)
    var uid: Int = 0
)

/*val sdf = SimpleDateFormat("dd MMM, yyyy - HH:mm")
                    val currentDate: String = sdf.format(Date())*/
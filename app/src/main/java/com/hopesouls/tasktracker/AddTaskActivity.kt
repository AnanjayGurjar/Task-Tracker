package com.hopesouls.tasktracker

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.text.isDigitsOnly
import androidx.room.Room
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.hopesouls.tasktracker.database.Task
import com.hopesouls.tasktracker.database.TaskDataBase
import kotlinx.android.synthetic.main.activity_add_task.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.String
import java.text.SimpleDateFormat
import java.util.*

class AddTaskActivity : AppCompatActivity() {
    var days = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_task)
        et_time.setOnClickListener {
            openTimePicker()
        }
        btn_addTask.setOnClickListener {
            val taskTitle = et_taskName.text.toString()
            val time = et_time.text.toString()





            val sdf = SimpleDateFormat("dd MMM, yyyy - HH:mm")
            val currentDate = sdf.format(Date())
            if(et_taskName.text.isNullOrEmpty()){
                et_taskName.error = "Field is Mandatory"
            }else if(et_time.text.isNullOrEmpty()) {
                et_time.error = "Field is Mandatory"
            }else if(et_days.text?.isDigitsOnly() == false  || et_days.text.isNullOrEmpty()){
                et_days.error = "Enter valid days"
            }else{
                days = et_days.text.toString().toInt()
                val task = Task(taskTitle, time, time, "0", "$currentDate", days, days)
                insertTask(task)
            }

        }
    }
    private fun openTimePicker(){
        val timeFormat = TimeFormat.CLOCK_24H
        val picker = MaterialTimePicker.Builder()
            .setTimeFormat(timeFormat)
            .setHour(12)
            .setMinute(0)
            .setTitleText("Set the time to be alloted")
            .build()
        picker.show(supportFragmentManager, "TAG")
        picker.addOnPositiveButtonClickListener {
            val hour = picker.hour
            val min = picker.minute
            et_time.setText(
                String.format("%02d : %02d : 00",hour, min)
            )
            et_time.isFocusable = false
        }
        picker.addOnNegativeButtonClickListener {
            picker.dismiss()
            et_time.isFocusable = false
        }
    }
    private fun insertTask(task: Task){
        val db = Room.databaseBuilder(this,
            TaskDataBase::class.java,
            "TaskList")
            .build()
        GlobalScope.launch {
            db.taskDao().insertAll(task)
            finish()
        }
    }
}
package com.hopesouls.tasktracker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.room.Room
import com.hopesouls.tasktracker.database.Task
import com.hopesouls.tasktracker.database.TaskDataBase
import ir.samanjafari.easycountdowntimer.CountDownInterface
import kotlinx.android.synthetic.main.activity_run_timer.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates
class RunTimer : AppCompatActivity() {

    var time_passed = 0L
    var isRunning = false
    var hour = 0
    var minute = 0
    var second = 0
    var hour_start = 0
    var min_start = 0
    var sec_start = 0
    var btn_reset_clicked = 0
    val CHANNEL_ID = "task completed"
    var id by Delegates.notNull<Int>()
    lateinit var title: String
    lateinit var date: String
    lateinit var time_start: String
    var days: Int = 0
    var days_remain = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_run_timer)

        var count = 0
        var time_spent = intent.getStringExtra(TIME_SPENT)
        var time_alloted = intent.getStringExtra(TIME_ALLOTED)
        days = intent.getIntExtra("DAYS", 0)
        days_remain = intent.getIntExtra("DAYSREMAIN", 0)
        id = intent.getIntExtra("ID", -1)
        title = intent.getStringExtra("TT").toString()
        date = intent.getStringExtra("DATE").toString()

        if(days != 0){
            easyCountDownTextview.setShowDays(true)
        }


        tv_heading.text = title

        btn_reset.isEnabled = false
        hour = time_spent!!.substring(0, 2).toInt()
        minute = time_spent!!.substring(5, 7).toInt()
        second = time_spent!!.substring(10).toInt()

        hour_start = time_alloted!!.substring(0, 2).toInt()
        min_start = time_alloted!!.substring(5, 7).toInt()
        sec_start = time_alloted!!.substring(10).toInt()
        time_passed = ((hour_start*3600+min_start*60+sec_start)*1000).toLong()




        var hour_string = hour_start.toString()
        var minute_string = min_start.toString()
        var second_string = sec_start.toString()

        if(hour_string.length == 1){
            hour_string = String.format("%02d", hour_start)
        }
        if(minute_string.length == 1){
            minute_string = String.format("%02d", min_start)
        }
        if(second_string.length == 1){
            second_string = String.format("%02d", sec_start)
        }

        time_start = "$hour_string : $minute_string : $second_string"
        easyCountDownTextview.setTime(days_remain, hour, minute, second)

        btn_play_pause.setOnClickListener {
            if(isRunning){
                btn_play_pause.setImageResource(R.drawable.ic_play)
                isRunning = false
                easyCountDownTextview.pause()
            }else{
                btn_play_pause.setImageResource(R.drawable.ic_pause)
                btn_reset_clicked = 0
                isRunning = true
                if(count == 0){
                    easyCountDownTextview.startTimer()
                    count++
                }else{
                    count = 0
                    easyCountDownTextview.resume()
                }
            }
            btn_ok.isEnabled = true
            btn_reset.isEnabled = true
        }

        btn_reset.setOnClickListener {
            btn_play_pause.isClickable = true
            easyCountDownTextview.pause()
            easyCountDownTextview.stopTimer()
            isRunning = false
            btn_reset_clicked = 1
            count = 0
            btn_play_pause.setImageResource(R.drawable.ic_play)
            easyCountDownTextview.setTime(days, hour_start, min_start, sec_start)
            btn_ok.isEnabled = false
            btn_reset.isEnabled = false

        }


        btn_ok.setOnClickListener {
            updateTaskData()
        }

        easyCountDownTextview.setOnTick(object : CountDownInterface {
            override fun onTick(time: Long) {
               time_passed = time

            }
            override fun onFinish() {

                btn_play_pause.setImageResource(R.drawable.ic_play)
                btn_play_pause.isClickable = false
                btn_reset.isEnabled = true
                isRunning = false

                val intent = Intent(applicationContext, MainActivity::class.java)
                var task = Task(title!!, time_start, "00 : 00 : 00", "100", "$date", days, days_remain)
                task.uid = id
                updateTask(task)
                val pendingIntent: PendingIntent = PendingIntent.getActivity(applicationContext, 0, intent, 0)

                //show notification

                var notificationManager = NotificationManagerCompat.from(applicationContext)
                createNotificationChannel()
                var notifcation = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_work)
                    .setContentTitle("Times UP")
                    .setContentText("Your Scheduled time for task $title is up")
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .build()

                notificationManager.notify(1, notifcation)

            }

        })
    }

    private fun updateTaskData() {
        days_remain = TimeUnit.MILLISECONDS.toDays(time_passed).toInt()
        var hour_remain = (TimeUnit.MILLISECONDS.toHours(time_passed)%24).toString()
        var min_remain = ((TimeUnit.MILLISECONDS.toMinutes(time_passed))%60).toString()
        var sec_remain = ((TimeUnit.MILLISECONDS.toSeconds(time_passed))%60).toString()

        var percentage = 100 - (100*(days_remain*3600*24 + hour_remain.toInt()*3600 + min_remain.toInt()*60+sec_remain.toInt())/(days*3600*24 + hour_start*3600 + min_start*60 + sec_start))

        if(hour_remain.length == 1){
            hour_remain = String.format("%02d" , hour_remain.toInt())
        }
        if(min_remain.length == 1){
            min_remain = String.format("%02d" , min_remain.toInt())
        }
        if(sec_remain.length == 1){
            sec_remain = String.format("%02d" , sec_remain.toInt())
        }

        var time_remain = "$hour_remain : $min_remain : $sec_remain"

        var task = Task(title!!, time_start, time_remain, percentage.toString(), "$date",days,  days_remain)

        task.uid = id
        updateTask(task)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Task Tracker" //name of the channel visible to user

            val descriptionText = "This is to notify that the alloted time is completed"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }


    private fun updateTask(task: Task){
        val db = Room.databaseBuilder(this,
            TaskDataBase::class.java,
            "TaskList")
            .build()
        GlobalScope.launch {
            db.taskDao().updateTask(task)
            finish()
        }
    }
    private fun updateTimerText(){
        var days_remain = TimeUnit.MILLISECONDS.toDays(time_passed).toInt()
        var hour_remain = (TimeUnit.MILLISECONDS.toHours(time_passed))%24
        var min_remain = (TimeUnit.MILLISECONDS.toMinutes(time_passed))%60
        var sec_remain = (TimeUnit.MILLISECONDS.toSeconds(time_passed))%60
        easyCountDownTextview.setTime(days_remain, hour_remain.toInt(), min_remain.toInt(), sec_remain.toInt())
    }
    override fun onBackPressed() {
        if(isRunning){
            Toast.makeText(this, "Timer is running, either press save/pause to save or press home button to run in background", Toast.LENGTH_LONG).show()
        }else{
            updateTaskData()
        }
    }
    override fun onStart() {
        super.onStart()
        onResume()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putLong("TIME", time_passed)
        outState.putBoolean("ISRUNNING", isRunning)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        time_passed = savedInstanceState.getLong("TIME")
        isRunning = savedInstanceState.getBoolean("ISRUNNING")
        updateTimerText()
        if(isRunning){
            easyCountDownTextview.startTimer()
            isRunning = false
            btn_play_pause.setImageResource(R.drawable.ic_pause)
        }else{
            btn_play_pause.setImageResource(R.drawable.ic_play)
        }
    }
}
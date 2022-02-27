package com.hopesouls.tasktracker

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.hopesouls.tasktracker.database.Task
import org.w3c.dom.Text
import java.util.*

const val TIME_SPENT = "time spent"
const val TIME_ALLOTED = "time alloted"
class TaskAdapter(private var tasks: List<Task>, var context: Context): RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    class TaskViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        var taskTitle = itemView.findViewById<TextView>(R.id.tv_taskTitle)
        val timeAlloted = itemView.findViewById<TextView>(R.id.tv_timeAlloted)
        val timeSpent = itemView.findViewById<TextView>(R.id.tv_timeSpent)
        val daysAlloted = itemView.findViewById<TextView>(R.id.tv_daysAlloted)
        val dayRemaining = itemView.findViewById<TextView>(R.id.tv_daysRemaining)
        val percentageCompleted = itemView.findViewById<TextView>(R.id.tv_percent)
        val date = itemView.findViewById<TextView>(R.id.tv_date)
        val cardView = itemView.findViewById<CardView>(R.id.cardView)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }
    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]
        val colors = getColors()
        val currentColor: Int = colors[
                if(position<13){
                    position
                }else{
                    position%13
                }
        ]
        holder.cardView.setCardBackgroundColor(currentColor)
        holder.taskTitle.text = task.taskName
        holder.timeAlloted.text = "${task.totalTime}"//8
        holder.timeSpent.text = "${task.completedTime}"//10
        holder.date.text = "added at: ${task.timeStamp}"
        holder.percentageCompleted.text = "Progress: ${task.percentage}%"
        holder.daysAlloted.text = "alloted ${task.days} d "
        holder.dayRemaining.text = "remainig ${task.daysRemain} d "



        holder.itemView.setOnClickListener {
            val intent = Intent(context, RunTimer::class.java )
            intent.putExtra("TT", holder.taskTitle.text)
            intent.putExtra(TIME_SPENT, holder.timeSpent.text)
            intent.putExtra(TIME_ALLOTED, holder.timeAlloted.text)
            intent.putExtra("ID", task.uid)
            intent.putExtra("DATE", task.timeStamp)
            intent.putExtra("DAYS", task.days)
            intent.putExtra("DAYSREMAIN", task.daysRemain)

            context.startActivity(intent)
        }
    }
    override fun getItemCount() = tasks.size

    fun setData(tasks: List<Task>){
        this.tasks = tasks
        notifyDataSetChanged()
    }
}
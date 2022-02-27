package com.hopesouls.tasktracker

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.snackbar.Snackbar
import com.hopesouls.tasktracker.database.Task
import com.hopesouls.tasktracker.database.TaskDataBase
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var deletedTask: Task
    private lateinit var oldTaskList: List<Task>
    lateinit var tasks: List<Task>
    lateinit var adapter: TaskAdapter
    lateinit var layoutManger: LinearLayoutManager
    private lateinit var db: TaskDataBase
    var pieList: ArrayList<PieEntry> = ArrayList()
    var totalLength = 0f
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var isPieChartOpen = false
        tasks = listOf()
        adapter = TaskAdapter(tasks, this)
        layoutManger = LinearLayoutManager(this)
        rv_tasks.adapter = adapter
        rv_tasks.layoutManager = layoutManger


        db = Room.databaseBuilder(this,
            TaskDataBase::class.java,
            "TaskList")
            .build()
        //swipe to delete
        val itemTouchHelper = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT){
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                deleteTask(tasks[viewHolder.adapterPosition])
            }

        }
        val swipeHelper = ItemTouchHelper(itemTouchHelper)
        swipeHelper.attachToRecyclerView(rv_tasks)
        rv_tasks.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0 || dy < 0 && fab.isShown){
                    fab.hide()
                    btn_pie.visibility = View.INVISIBLE
                }
            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE){
                    fab.show()
                    btn_pie.visibility = View.VISIBLE
                }
                super.onScrollStateChanged(recyclerView, newState)
            }
        })

        btn_pie.setOnClickListener{
            if(isPieChartOpen){
                pieChart.visibility = View.GONE
                rv_tasks.visibility = View.VISIBLE
                btn_pie.setText("Show PieChart")
                isPieChartOpen = false
            }else{
                pieChart.visibility = View.VISIBLE
                rv_tasks.visibility = View.GONE
                setUpPieChart()
                loadPieChartData()
                btn_pie.setText("Hide PieChart")
                isPieChartOpen = true
            }

        }
        fab.setOnClickListener {
            pieChart.visibility = View.GONE
            rv_tasks.visibility = View.VISIBLE
            isPieChartOpen = false
            pieList.clear()
            val intent = Intent(this, AddTaskActivity::class.java)
            startActivity(intent)
        }
    }

    private fun deleteTask(task: Task) {
        deletedTask = task
        oldTaskList = tasks

        GlobalScope.launch {
            db.taskDao().deleteTask(task)
            tasks = tasks.filter { it.uid != task.uid }
            runOnUiThread {
                adapter.setData(tasks)
                showSnackBar()
            }
        }
    }
    private fun undoDelete(){
        GlobalScope.launch {
            db.taskDao().insertAll(deletedTask)
            tasks = oldTaskList
            runOnUiThread {
                adapter.setData(tasks)
            }
        }
    }
    private fun showSnackBar() {
        val view = findViewById<View>(R.id.coordinator)
        val snackbar = Snackbar.make(view, "Task deleted", Snackbar.LENGTH_LONG)
        snackbar.setAction("Undo"){
            undoDelete()
        }
            .setActionTextColor(ContextCompat.getColor(applicationContext, R.color.red))
            .setTextColor(ContextCompat.getColor(applicationContext, R.color.white))
            .show()
    }
    override fun onResume() {
        super.onResume()
        fetchAll()
    }
    private fun fetchAll(){
        GlobalScope.launch {
            tasks = db.taskDao().getAllTasks()
            runOnUiThread {
                adapter.setData(tasks)
            }
        }
    }

    //for pie chart View
    private fun setUpPieChart(){
        pieChart.apply {
            isDrawHoleEnabled = true
            setUsePercentValues(true)
            setEntryLabelTextSize(14f)
            setEntryLabelColor(Color.BLACK)
            centerText = "Time Alloted"
            setCenterTextSize(24f)
            description.isEnabled = false

        }
        var legend = pieChart.legend
        legend.verticalAlignment = Legend.LegendVerticalAlignment.TOP
        legend.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
        legend.orientation = Legend.LegendOrientation.VERTICAL
        legend.isEnabled = true
    }
    private fun loadPieChartData(){
        pieList.clear()
        totalLength = 0f
        var pieDataList = ArrayList<PieElement>()

        tasks.forEach {
            var time = it.totalTime
            var days = it.days
            var hour = time.substring(0, 2).toInt()
            var minute = time.substring(5, 7).toInt()
            var second = time.substring(10).toInt()
            var length = (days*24.0f*60.0f*60.0f)/1000 +(hour*60.0f*60.0f)/1000 + (minute*60.0f)/1000 + (second)/1000
            pieDataList.add(PieElement(it.taskName, length))
        }

        pieDataList.forEach {
            totalLength += it.length
        }
        pieDataList.forEach {
            pieList.add(PieEntry(it.length/totalLength, it.name))
        }

        val colors = getColors()
        var dataSet = PieDataSet(pieList, "Tasks")
        dataSet.setColors(colors)

        var pieData = PieData(dataSet)
        pieData.apply {
            setDrawValues(true)
            setValueFormatter(PercentFormatter(pieChart))
            setValueTextSize(14f)
            setValueTextColor(Color.BLACK)
        }
        pieChart.data = pieData
        pieChart.invalidate()
        pieChart.animateY(1000, Easing.EaseOutQuad)
    }
    data class PieElement(
        var name: String,
        var length: Float
    )
}
fun getColors(): ArrayList<Int>{
    var colors: ArrayList<Int> = ArrayList()
    ColorTemplate.MATERIAL_COLORS.forEach { color->
        colors.add(color)
    }

    ColorTemplate.JOYFUL_COLORS.forEach { color->
        colors.add(color)
    }
    ColorTemplate.COLORFUL_COLORS.forEach { color->
        colors.add(color)
    }
    return colors
}
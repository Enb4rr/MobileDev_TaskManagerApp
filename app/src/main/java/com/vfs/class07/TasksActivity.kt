package com.vfs.class07

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class TasksActivity : AppCompatActivity(), TaskListener
{
    lateinit var thisGroup : Group
    lateinit var taskAdapter : TasksAdapter


    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.tasks_layout)

        val index = intent.getIntExtra("index", 0)
        thisGroup = AppData.groups[index]

        val grpTextView = findViewById<TextView>(R.id.grpNameTextView_id)
        grpTextView.text = thisGroup.name

        val tasksRv = findViewById<RecyclerView>(R.id.tasksRv_id)
        tasksRv.layoutManager = LinearLayoutManager(this)

        taskAdapter = TasksAdapter(this, thisGroup)
        tasksRv.adapter = taskAdapter
    }

    override fun taskClicked(index: Int)
    {
        thisGroup.tasks[index].completed = !thisGroup.tasks[index].completed
        taskAdapter.notifyDataSetChanged()
    }

    override fun taskLongClicked(index: Int)
    {
        TODO("Not yet implemented")
    }
}
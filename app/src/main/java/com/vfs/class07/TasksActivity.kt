package com.vfs.class07

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.EditText
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

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.rootLayout)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )
            insets
        }

        val index = intent.getIntExtra("index", 0)
        thisGroup = AppData.groups[index]

        val grpTextView = findViewById<TextView>(R.id.grpNameTextView_id)
        grpTextView.text = thisGroup.name

        val tasksRv = findViewById<RecyclerView>(R.id.tasksRv_id)
        tasksRv.layoutManager = LinearLayoutManager(this)

        taskAdapter = TasksAdapter(this, thisGroup)
        tasksRv.adapter = taskAdapter
    }

    fun goBackToGroups(v: View)
    {

        finish()
    }

    override fun taskClicked(index: Int)
    {
        thisGroup.tasks[index].completed = !thisGroup.tasks[index].completed
        taskAdapter.notifyItemChanged(index)
    }

    override fun taskLongClicked(index: Int)
    {
        val options = arrayOf("Edit", "Delete")

        AlertDialog.Builder(this)
            .setTitle("Task options")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showEditTaskDialog(index)
                    1 -> deleteTask(index)
                }
            }
            .show()
    }

    fun addNewTask(v : View)
    {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("New Task")
        builder.setMessage("Enter the name of the new task")

        val nameEditText = EditText(this)
        builder.setView(nameEditText)

        builder.setPositiveButton("Create") { _, _ ->
            val newTask = Task(nameEditText.text.toString(), false)
            thisGroup.tasks.add(newTask)
            taskAdapter.notifyDataSetChanged()
        }

        builder.setNegativeButton("Cancel") { _, _ -> }

        val dialog = builder.create()
        dialog.show()
    }

    fun deleteTask(index: Int)
    {
        thisGroup.tasks.removeAt(index)
        taskAdapter.notifyDataSetChanged()
    }

    fun showEditTaskDialog(index: Int)
    {
        val task = thisGroup.tasks[index]

        val builder = AlertDialog.Builder(this)
        builder.setTitle("Edit Task")
        builder.setMessage("Update task name")

        val editText = EditText(this)
        editText.setText(task.name)
        editText.setSelection(task.name.length)
        builder.setView(editText)

        builder.setPositiveButton("Save") { _, _ ->
            task.name = editText.text.toString()
            taskAdapter.notifyItemChanged(index)
        }

        builder.setNegativeButton("Cancel", null)

        builder.create().show()
    }
}
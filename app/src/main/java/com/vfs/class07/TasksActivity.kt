package com.vfs.class07

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
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
        // Initialize Activity
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.tasks_layout)

        // Keep Activity in bounds
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

        // Initialize Data
        val index = intent.getIntExtra("index", 0)
        thisGroup = AppData.groups[index]

        // Initialize UI
        val grpTextView = findViewById<TextView>(R.id.grpNameTextView_id)
        grpTextView.text = thisGroup.name

        val tasksRv = findViewById<RecyclerView>(R.id.tasksRv_id)
        tasksRv.layoutManager = LinearLayoutManager(this)

        taskAdapter = TasksAdapter(this, thisGroup)
        tasksRv.adapter = taskAdapter
    }

    // Return to GroupsActivity
    fun goBackToGroups(v: View)
    {
        finish()
    }

    // Toggle task completion
    override fun taskClicked(index: Int)
    {
        thisGroup.tasks[index].completed = !thisGroup.tasks[index].completed
        taskAdapter.notifyItemChanged(index)
    }

    // Show edit/delete dialog
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

    // Add new task to Data and UI
    fun addNewTask(v : View)
    {
        // Create Dialog
        val builder = AlertDialog.Builder(this)
        builder.setTitle("New Task")
        builder.setMessage("Enter the name of the new task")

        val nameEditText = EditText(this)
        builder.setView(nameEditText)

        builder.setPositiveButton("Create") { _, _ ->

            // Get task name
            val taskName = nameEditText.text.toString().normalized()

            // Empty check
            if (taskName.isEmpty()) {
                Toast.makeText(this, "Task name cannot be empty", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }

            // Duplicate check inside the same group
            val exists = thisGroup.tasks.any {
                it.name.equals(taskName, ignoreCase = true)
            }

            if (exists) {
                Toast.makeText(this, "Task already exists in this group", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }

            // Add task to Data and UI
            thisGroup.tasks.add(Task(taskName, false))
            taskAdapter.notifyDataSetChanged()
        }

        // Cancel
        builder.setNegativeButton("Cancel") { _, _ -> }

        // Show Dialog
        val dialog = builder.create()
        dialog.show()
    }

    // Remove task from Data and UI
    fun deleteTask(index: Int)
    {
        thisGroup.tasks.removeAt(index)
        taskAdapter.notifyDataSetChanged()
    }

    // Edit task name
    fun showEditTaskDialog(index: Int)
    {
        // Get task
        val task = thisGroup.tasks[index]

        // Create Dialog
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Edit Task")
        builder.setMessage("Update task name")

        val editText = EditText(this)
        editText.setText(task.name)
        editText.setSelection(task.name.length)
        builder.setView(editText)

        builder.setPositiveButton("Save") { _, _ ->

            // Get new task name
            val newName = editText.text.toString().normalized()

            // Empty check
            if (newName.isEmpty()) {
                Toast.makeText(this, "Task name cannot be empty", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }

            // Duplicate check (exclude current task)
            val exists = thisGroup.tasks.any {
                it !== task && it.name.equals(newName, ignoreCase = true)
            }

            if (exists) {
                Toast.makeText(this, "Task already exists", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }

            // Update task name in Data and UI
            task.name = newName
            taskAdapter.notifyItemChanged(index)
        }

        // Cancel
        builder.setNegativeButton("Cancel", null)

        // Show Dialog
        builder.create().show()
    }

    // Extension function to remove leading/trailing spaces
    fun String.normalized(): String = this.trim()
}
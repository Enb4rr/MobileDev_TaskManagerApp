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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

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
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
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
        
        listenForChanges()
    }

    private fun listenForChanges() {
        Cloud.db.reference.child("groups").child(thisGroup.id)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val updatedGroup = snapshot.getValue(Group::class.java)
                    if (updatedGroup != null) {
                        thisGroup.tasks = updatedGroup.tasks ?: mutableListOf()
                        thisGroup.name = updatedGroup.name
                        findViewById<TextView>(R.id.grpNameTextView_id).text = thisGroup.name
                        taskAdapter.notifyDataSetChanged()
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    fun goBackToGroups(v: View) {
        finish()
    }

    override fun taskClicked(index: Int) {
        thisGroup.tasks[index].completed = !thisGroup.tasks[index].completed
        taskAdapter.notifyItemChanged(index)
        Cloud.saveGroup(thisGroup)
    }

    override fun taskLongClicked(index: Int) {
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

    fun addNewTask(v : View) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("New Task")
        val nameEditText = EditText(this)
        builder.setView(nameEditText)

        builder.setPositiveButton("Create") { _, _ ->
            val taskName = nameEditText.text.toString().trim()
            if (taskName.isEmpty()) return@setPositiveButton

            thisGroup.tasks.add(Task(taskName, false))
            taskAdapter.notifyDataSetChanged()
            Cloud.saveGroup(thisGroup)
        }
        builder.setNegativeButton("Cancel", null)
        builder.show()
    }

    fun deleteTask(index: Int) {
        thisGroup.tasks.removeAt(index)
        taskAdapter.notifyDataSetChanged()
        Cloud.saveGroup(thisGroup)
    }

    fun showEditTaskDialog(index: Int) {
        val task = thisGroup.tasks[index]
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Edit Task")
        val editText = EditText(this)
        editText.setText(task.name)
        builder.setView(editText)

        builder.setPositiveButton("Save") { _, _ ->
            val newName = editText.text.toString().trim()
            if (newName.isEmpty()) return@setPositiveButton
            task.name = newName
            taskAdapter.notifyItemChanged(index)
            Cloud.saveGroup(thisGroup)
        }
        builder.setNegativeButton("Cancel", null)
        builder.show()
    }
}
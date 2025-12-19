package com.vfs.class07

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class GroupsActivity : AppCompatActivity(), GroupListener
{
    lateinit var groupsAdapter: GroupsAdapter

    override fun onCreate(savedInstanceState: Bundle?)
    {
        // Initialize Activity
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.groups_layout)

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
        AppData.initialize()

        // Initialize UI
        val groupsRv = findViewById<RecyclerView>(R.id.groupsRv_id)
        groupsRv.layoutManager = LinearLayoutManager(this)

        groupsAdapter = GroupsAdapter(this)
        groupsRv.adapter = groupsAdapter
    }

    // Load TaskActivity with the clicked Group Data
    override fun groupClicked(index: Int) {
        val intent = Intent(this, TasksActivity::class.java)
        intent.putExtra("index", index)
        startActivity(intent)
    }

    // Remove Group from Data and UI
    override fun groupLongClicked(index: Int)
    {
        AppData.groups.removeAt(index)
        groupsAdapter.notifyDataSetChanged()
    }

    // Add new group to Data and UI
    fun addNewGroup(v : View)
    {
        // Create Dialog
        val builder = AlertDialog.Builder(this)
        builder.setTitle("New Group")
        builder.setMessage("Enter the name of the new group")

        val nameEditText = EditText(this)
        builder.setView(nameEditText)

        builder.setPositiveButton("Create") { _, _ ->

            // Get group name
            val groupName = nameEditText.text.toString().normalized()

            // Empty check
            if (groupName.isEmpty()) {
                Toast.makeText(this, "Group name cannot be empty", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }

            // Duplicate check
            val exists = AppData.groups.any {
                it.name.equals(groupName, ignoreCase = true)
            }

            if (exists) {
                Toast.makeText(this, "Group already exists", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }

            // Add group to Data and UI
            AppData.groups.add(Group(groupName, mutableListOf()))
            groupsAdapter.notifyDataSetChanged()
        }

        // Cancel
        builder.setNegativeButton("Cancel", null)

        // Show Dialog
        val dialog = builder.create()
        dialog.show()
    }

    // Extension function to remove leading/trailing spaces
    fun String.normalized(): String = this.trim()
}
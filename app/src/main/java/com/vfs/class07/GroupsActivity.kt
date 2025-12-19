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
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

// Home page ~ Launcher

    // This should be called something like GroupsActivity
    // List of groups
    // Add groups
    // Delete groups
    // Click on a group

// Group page

    // This should be called something like ItemsActivity
    // List items in the group
    // Delete items
    // Set an item as done
    // Go back to home page

class GroupsActivity : AppCompatActivity(), GroupListener
{
    lateinit var groupsAdapter: GroupsAdapter

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.groups_layout)

        AppData.initialize()

        val groupsRv = findViewById<RecyclerView>(R.id.groupsRv_id)
        groupsRv.layoutManager = LinearLayoutManager(this)

        groupsAdapter = GroupsAdapter(this)
        groupsRv.adapter = groupsAdapter
    }

    override fun groupClicked(index: Int) {
        val intent = Intent(this, TasksActivity::class.java)
        intent.putExtra("index", index)
        startActivity(intent)
    }

    override fun groupLongClicked(index: Int)
    {
        AppData.groups.removeAt(index)
        groupsAdapter.notifyItemRemoved(index)
    }

    fun addNewGroup(v : View)
    {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("New Group")
        builder.setMessage("Enter the name of the new group")

        val nameEditText = EditText(this)
        builder.setView(nameEditText)

        builder.setPositiveButton("Create") { _, _ ->
            val newGroup = Group(nameEditText.text.toString(), mutableListOf())
            AppData.groups.add(newGroup)
            groupsAdapter.notifyItemInserted(AppData.groups.count() - 1)
        }
        builder.setNegativeButton("Cancel") { _, _ -> }

        val dialog = builder.create()
        dialog.show()
    }
}
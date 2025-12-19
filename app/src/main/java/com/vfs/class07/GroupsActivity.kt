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
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class GroupsActivity : AppCompatActivity(), GroupListener
{
    lateinit var groupsAdapter: GroupsAdapter

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.groups_layout)

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
        groupsAdapter.notifyDataSetChanged()
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
            groupsAdapter.notifyDataSetChanged()
        }
        builder.setNegativeButton("Cancel") { _, _ -> }

        val dialog = builder.create()
        dialog.show()
    }
}
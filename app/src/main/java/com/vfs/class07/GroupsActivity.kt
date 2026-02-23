package com.vfs.class07

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth

class GroupsActivity : AppCompatActivity(), GroupListener
{
    lateinit var groupsAdapter: GroupsAdapter

    // Firebase
    lateinit var statusButton: Button

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

        // Firebase
        statusButton = findViewById(R.id.statusButton_id)
        statusButton.setOnClickListener {
            if (Cloud.auth.currentUser != null)
            {
                showLogoutModal()
            }
            else
            {
                showLoginRegisterModal()
            }
        }
        Cloud.auth = FirebaseAuth.getInstance()
        
        // Initialize UI
        val groupsRv = findViewById<RecyclerView>(R.id.groupsRv_id)
        groupsRv.layoutManager = LinearLayoutManager(this)

        groupsAdapter = GroupsAdapter(this)
        groupsRv.adapter = groupsAdapter

        checkOnlineStatus()
    }

    override fun onResume() {
        super.onResume()
        // Refresh UI in case tasks changed in TasksActivity
        groupsAdapter.notifyDataSetChanged()
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
        Cloud.saveGroups()
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
            Cloud.saveGroups()
        }

        // Cancel
        builder.setNegativeButton("Cancel", null)

        // Show Dialog
        val dialog = builder.create()
        dialog.show()
    }

    fun checkOnlineStatus()
    {
        if (Cloud.auth.currentUser != null) {
            statusButton.text = "${Cloud.auth.currentUser?.displayName ?: "User"} is Online"
            AppData.loadFromFirebase {
                groupsAdapter.notifyDataSetChanged()
            }
        } else {
            statusButton.text = "Offline"
            AppData.initialize()
            groupsAdapter.notifyDataSetChanged()
        }
    }

    // Extension function to remove leading/trailing spaces
    fun String.normalized(): String = this.trim()
}

// Function to display a login or register modal when pressed
fun GroupsActivity.showLoginRegisterModal ()
{
    val builder = AlertDialog.Builder(this)

    builder.setTitle("Login or Register")
    builder.setMessage("Do you want to login or register?")

    builder.setPositiveButton("Login") { _, _ ->
        val intent = Intent (this, LoginRegisterActivity::class.java)
        intent.putExtra("type", "login")

        startActivity(intent)
    }

    builder.setNeutralButton("Register") { _, _ ->
        val intent = Intent (this, LoginRegisterActivity::class.java)
        intent.putExtra("type", "register")

        startActivity(intent)
    }

    builder.setNegativeButton("Cancel") { _, _ ->
    }

    val dialog = builder.create()
    dialog.show()

    dialog.window?.setGravity(Gravity.BOTTOM)
}

// Function to display a login or register modal when pressed
fun GroupsActivity.showLogoutModal ()
{
    val builder = AlertDialog.Builder(this)

    builder.setTitle("Log Out")
    builder.setMessage("Are you sure you want to log out?")

    builder.setPositiveButton("Log Out") { _, _ ->
        Cloud.auth.signOut()
        AppData.groups.clear()
        checkOnlineStatus()
    }

    builder.setNegativeButton("Cancel") { _, _ ->
    }

    val dialog = builder.create()
    dialog.show()

    dialog.window?.setGravity(Gravity.BOTTOM)
}
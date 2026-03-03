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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class GroupsActivity : AppCompatActivity(), GroupListener
{
    lateinit var groupsAdapter: GroupsAdapter
    lateinit var statusButton: Button

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.groups_layout)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.rootLayout)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        statusButton = findViewById(R.id.statusButton_id)
        statusButton.setOnClickListener {
            if (Cloud.auth.currentUser != null) showLogoutModal()
            else showLoginRegisterModal()
        }
        
        Cloud.auth = FirebaseAuth.getInstance()
        
        val groupsRv = findViewById<RecyclerView>(R.id.groupsRv_id)
        groupsRv.layoutManager = LinearLayoutManager(this)

        groupsAdapter = GroupsAdapter(this)
        groupsRv.adapter = groupsAdapter

        checkOnlineStatus()
        listenForInvitations()
    }

    override fun onResume() {
        super.onResume()
        checkOnlineStatus()
    }

    override fun groupClicked(index: Int) {
        val intent = Intent(this, TasksActivity::class.java)
        intent.putExtra("index", index)
        startActivity(intent)
    }

    override fun groupLongClicked(index: Int)
    {
        val options = arrayOf("Invite User", "Delete Group")
        AlertDialog.Builder(this)
            .setTitle("Group Options")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showInviteUserDialog(index)
                    1 -> deleteGroup(index)
                }
            }
            .show()
    }

    private fun deleteGroup(index: Int) {
        val group = AppData.groups[index]
        Cloud.deleteGroup(group)
        AppData.groups.removeAt(index)
        groupsAdapter.notifyDataSetChanged()
    }

    private fun showInviteUserDialog(index: Int) {
        val group = AppData.groups[index]
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Invite User")
        builder.setMessage("Enter the email of the user you want to invite to '${group.name}'")

        val emailEditText = EditText(this)
        builder.setView(emailEditText)

        builder.setPositiveButton("Invite") { _, _ ->
            val email = emailEditText.text.toString().trim()
            if (email.isNotEmpty()) {
                sendInvitation(email, group)
            }
        }
        builder.setNegativeButton("Cancel", null)
        builder.show()
    }

    private fun sendInvitation(email: String, group: Group) {
        // Find user by email
        Cloud.db.reference.child("users").orderByChild("email").equalTo(email)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val targetUid = snapshot.children.first().key ?: ""
                        val invitation = Invitation(
                            fromUid = Cloud.auth.currentUser?.uid ?: "",
                            fromEmail = Cloud.auth.currentUser?.email ?: "",
                            groupId = group.id,
                            groupName = group.name
                        )
                        Cloud.db.reference.child("invitations").child(targetUid).child(invitation.id).setValue(invitation)
                        Toast.makeText(this@GroupsActivity, "Invitation sent!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@GroupsActivity, "User not found", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun listenForInvitations() {
        val uid = Cloud.auth.currentUser?.uid ?: return
        Cloud.db.reference.child("invitations").child(uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (invitationSnapshot in snapshot.children) {
                        val invitation = invitationSnapshot.getValue(Invitation::class.java)
                        if (invitation != null && invitation.status == "pending") {
                            showInvitationDialog(invitation)
                        }
                    }
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun showInvitationDialog(invitation: Invitation) {
        AlertDialog.Builder(this)
            .setTitle("New Invitation")
            .setMessage("${invitation.fromEmail} invited you to join the group '${invitation.groupName}'")
            .setPositiveButton("Accept") { _, _ ->
                acceptInvitation(invitation)
            }
            .setNegativeButton("Reject") { _, _ ->
                rejectInvitation(invitation)
            }
            .setCancelable(false)
            .show()
    }

    private fun acceptInvitation(invitation: Invitation) {
        val uid = Cloud.auth.currentUser?.uid ?: return
        Cloud.addGroupToUser(uid, invitation.groupId)
        Cloud.db.reference.child("invitations").child(uid).child(invitation.id).child("status").setValue("accepted")
            .addOnCompleteListener {
                checkOnlineStatus()
            }
    }

    private fun rejectInvitation(invitation: Invitation) {
        val uid = Cloud.auth.currentUser?.uid ?: return
        Cloud.db.reference.child("invitations").child(uid).child(invitation.id).child("status").setValue("rejected")
    }

    fun addNewGroup(v : View)
    {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("New Group")
        val nameEditText = EditText(this)
        builder.setView(nameEditText)

        builder.setPositiveButton("Create") { _, _ ->
            val groupName = nameEditText.text.toString().trim()
            if (groupName.isEmpty()) return@setPositiveButton

            val newGroup = Group(name = groupName)
            val uid = Cloud.auth.currentUser?.uid ?: return@setPositiveButton
            
            Cloud.saveGroup(newGroup)
            Cloud.addGroupToUser(uid, newGroup.id)
            
            AppData.groups.add(newGroup)
            groupsAdapter.notifyDataSetChanged()
        }
        builder.setNegativeButton("Cancel", null)
        builder.show()
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
            AppData.groups.clear()
            groupsAdapter.notifyDataSetChanged()
        }
    }
}

fun GroupsActivity.showLoginRegisterModal () {
    val builder = AlertDialog.Builder(this)
    builder.setTitle("Login or Register")
    builder.setPositiveButton("Login") { _, _ ->
        startActivity(Intent (this, LoginRegisterActivity::class.java))
    }
    builder.setNeutralButton("Register") { _, _ ->
        startActivity(Intent (this, LoginRegisterActivity::class.java))
    }
    builder.setNegativeButton("Cancel", null)
    builder.show().window?.setGravity(Gravity.BOTTOM)
}

fun GroupsActivity.showLogoutModal () {
    val builder = AlertDialog.Builder(this)
    builder.setTitle("Log Out")
    builder.setPositiveButton("Log Out") { _, _ ->
        Cloud.auth.signOut()
        checkOnlineStatus()
    }
    builder.setNegativeButton("Cancel", null)
    builder.show().window?.setGravity(Gravity.BOTTOM)
}
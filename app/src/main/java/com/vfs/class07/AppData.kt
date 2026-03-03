package com.vfs.class07

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import java.util.UUID

data class User(val uid: String = "", val username: String = "", val email: String = "", val groups: MutableMap<String, Boolean> = mutableMapOf())

data class Task (var name: String = "", var completed: Boolean = false)

data class Group (var id: String = UUID.randomUUID().toString(), var name: String = "", var tasks: MutableList<Task> = mutableListOf(), var members: MutableMap<String, Boolean> = mutableMapOf())

data class Invitation(val id: String = UUID.randomUUID().toString(), val fromUid: String = "", val fromEmail: String = "", val groupId: String = "", val groupName: String = "", var status: String = "pending")

class AppData
{
    companion object
    {
        var groups: MutableList<Group> = mutableListOf()
        var user: User? = null

        fun loadFromFirebase(onComplete: () -> Unit)
        {
            val uid = Cloud.auth.currentUser?.uid ?: return

            // First, get the current user's data to find their group memberships
            Cloud.db.reference.child("users").child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    user = snapshot.getValue(User::class.java)
                    if (user == null) {
                        // This case should ideally not happen for a logged-in user
                        onComplete()
                        return
                    }

                    // Now, load the groups based on the user's group list
                    val groupIds = user?.groups?.keys ?: setOf()
                    if (groupIds.isEmpty()) {
                        groups.clear()
                        onComplete()
                        return
                    }

                    Cloud.db.reference.child("groups").addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(groupSnapshot: DataSnapshot) {
                            val loadedGroups = mutableListOf<Group>()
                            for (id in groupIds) {
                                val group = groupSnapshot.child(id).getValue(Group::class.java)
                                if (group != null) {
                                    if (group.tasks == null) group.tasks = mutableListOf()
                                    if (group.members == null) group.members = mutableMapOf()
                                    loadedGroups.add(group)
                                }
                            }
                            groups = loadedGroups
                            onComplete()
                        }

                        override fun onCancelled(error: DatabaseError) {
                            onComplete()
                        }
                    })
                }

                override fun onCancelled(error: DatabaseError) {
                    onComplete()
                }
            })
        }
    }
}
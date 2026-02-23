package com.vfs.class07

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

data class User(val uid: String = "", val username: String = "", val email: String = "")

data class Task (var name: String = "", var completed: Boolean = false)

data class Group (var name: String = "", var tasks: MutableList<Task> = mutableListOf())

class AppData
{
    companion object
    {
        var groups: MutableList<Group> = mutableListOf()

        fun initialize ()
        {
            if (groups.isNotEmpty()) return

            val task_1 = Task("Task 1", false)
            val task_2 = Task("Task 2", false)
            val task_3 = Task("Task 3", false)
            val task_4 = Task("Task 4", false)
            val task_5 = Task("Task 5", false)
            val task_6 = Task("Task 6", false)
            val task_7 = Task("Task 7", false)
            val task_8 = Task("Task 8", false)

            val group_1 = Group("Home", mutableListOf(task_1, task_2))
            val group_2 = Group("Work", mutableListOf(task_3))
            val group_3 = Group("School", mutableListOf(task_4))
            val group_4 = Group("Groceries", mutableListOf(task_5, task_6, task_7))
            val group_5 = Group("Other", mutableListOf(task_8))

            groups = mutableListOf(group_1, group_2, group_3, group_4, group_5)
        }

        fun loadFromFirebase(onComplete: () -> Unit)
        {
            val uid = Cloud.auth.currentUser?.uid ?: return
            Cloud.db.reference.child("users").child(uid).child("groups")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val loadedGroups = mutableListOf<Group>()
                        for (groupSnapshot in snapshot.children) {
                            val group = groupSnapshot.getValue(Group::class.java)
                            if (group != null) {
                                if (group.tasks == null) group.tasks = mutableListOf()
                                loadedGroups.add(group)
                            }
                        }
                        groups = loadedGroups
                        onComplete()
                    }

                    override fun onCancelled(error: DatabaseError) {
                        // Handle error
                    }
                })
        }
    }
}
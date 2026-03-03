package com.vfs.class07

import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.database

class Cloud
{
    companion object
    {
        lateinit var auth : FirebaseAuth
        val db = Firebase.database

        fun saveGroup(group: Group)
        {
            db.reference.child("groups").child(group.id).setValue(group)
        }

        fun deleteGroup(group: Group)
        {
            val uid = auth.currentUser?.uid ?: return
            
            // Remove from groups collection
            // In a real app, we might check if other members still exist
            db.reference.child("groups").child(group.id).removeValue()
            
            // Remove from user's group list
            db.reference.child("users").child(uid).child("groups").child(group.id).removeValue()
        }
        
        fun addGroupToUser(uid: String, groupId: String)
        {
            db.reference.child("users").child(uid).child("groups").child(groupId).setValue(true)
            
            // Also add user to group members
            db.reference.child("groups").child(groupId).child("members").child(uid).setValue(true)
        }
    }
}
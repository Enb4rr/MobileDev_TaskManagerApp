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

        fun saveGroups()
        {
            val uid = auth.currentUser?.uid ?: return
            db.reference.child("users").child(uid).child("groups").setValue(AppData.groups)
        }
    }
}
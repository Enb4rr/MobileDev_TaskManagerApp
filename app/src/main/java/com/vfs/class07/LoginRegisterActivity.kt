package com.vfs.class07

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest

class LoginRegisterActivity : AppCompatActivity()
{
    private lateinit var usernameInputLayout: TextInputLayout
    private lateinit var usernameField: TextInputEditText
    private lateinit var emailField: TextInputEditText
    private lateinit var passwordField: TextInputEditText
    private lateinit var toggleGroup: MaterialButtonToggleGroup
    private lateinit var actionButton: Button

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.login_register_layout)

        // Initialize Firebase Auth in your Cloud helper
        Cloud.auth = FirebaseAuth.getInstance()

        // UI Binding
        usernameInputLayout = findViewById(R.id.usernameInputLayout)
        usernameField = findViewById(R.id.usernameEditText)
        emailField = findViewById(R.id.emailEditText)
        passwordField = findViewById(R.id.passwordEditText)
        toggleGroup = findViewById(R.id.toggleGroup)
        actionButton = findViewById(R.id.actionButton)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Handle Segmented Control (Toggle) changes
        toggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.loginTab -> {
                        actionButton.text = "Login"
                        usernameInputLayout.visibility = View.GONE
                    }
                    R.id.registerTab -> {
                        actionButton.text = "Register"
                        usernameInputLayout.visibility = View.VISIBLE
                    }
                }
            }
        }

        // Action Button Click
        actionButton.setOnClickListener {
            val email = emailField.text.toString().trim()
            val password = passwordField.text.toString().trim()
            val username = usernameField.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (toggleGroup.checkedButtonId == R.id.loginTab) {
                performLogin(email, password)
            } else {
                if (username.isEmpty()) {
                    Toast.makeText(this, "Please enter a username", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                performRegister(email, password, username)
            }
        }
    }

    private fun performLogin(email: String, password: String)
    {
        Cloud.auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    navigateToMain()
                } else {
                    Toast.makeText(this, "Login Failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun performRegister(email: String, password: String, username: String)
    {
        Cloud.auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val firebaseUser = Cloud.auth.currentUser
                    val uid = firebaseUser?.uid ?: ""
                    
                    // Create User object
                    val newUser = User(uid, username, email)
                    
                    // Save to Realtime Database
                    Cloud.db.reference.child("users").child(uid).setValue(newUser)
                        .addOnCompleteListener { dbTask ->
                            if (dbTask.isSuccessful) {
                                // Also update Firebase Auth Profile (for displayName)
                                val profileUpdates = userProfileChangeRequest {
                                    displayName = username
                                }
                                
                                firebaseUser?.updateProfile(profileUpdates)
                                    ?.addOnCompleteListener { profileTask ->
                                        if (profileTask.isSuccessful) {
                                            Toast.makeText(this, "Registration Successful!", Toast.LENGTH_SHORT).show()
                                            navigateToMain()
                                        }
                                    }
                            } else {
                                Toast.makeText(this, "Database Error: ${dbTask.exception?.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    Toast.makeText(this, "Registration Failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun navigateToMain()
    {
        val intent = Intent(this, GroupsActivity::class.java)
        startActivity(intent)
        finish()
    }
}
package com.example.foodsaverapps

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.foodsaverapps.Model.UserModel
import com.example.foodsaverapps.databinding.ActivityChangePasswordBinding
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ChangePassword2Activity : AppCompatActivity() {
    private lateinit var binding: ActivityChangePasswordBinding
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangePasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonChangePassword.setOnClickListener {
            val currentPassword = binding.editTextPassword.text.toString()
            val newPassword = binding.editTextNewPassword.text.toString()
            val confirmPassword = binding.editTextConfirmPassword.text.toString()

            if (newPassword == confirmPassword) {
                changePassword(currentPassword, newPassword)
            } else {
                Toast.makeText(this, "New Password and Confirm Password do not match", Toast.LENGTH_SHORT).show()
            }
        }

        binding.imageButtonBackArticle.setOnClickListener {
            finish()
        }
    }

    private fun changePassword(currentPassword: String, newPassword: String) {
        val user = auth.currentUser
        val userId = user?.uid ?: return
        val userReference = database.getReference("seller").child(userId)

        if (user.email != null) {
            val credential = EmailAuthProvider.getCredential(user.email!!, currentPassword)

            user.reauthenticate(credential).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    user.updatePassword(newPassword).addOnCompleteListener { updateTask ->
                        if (updateTask.isSuccessful) {
                            // Update password in Firebase Realtime Database
                            val updatedUserData = mapOf<String, Any>(
                                "password" to newPassword
                            )

                            userReference.updateChildren(updatedUserData).addOnCompleteListener { dbTask ->
                                if (dbTask.isSuccessful) {
                                    Toast.makeText(this, "Password updated successfully", Toast.LENGTH_SHORT).show()
                                    auth.signOut()
                                    val intent = Intent(this@ChangePassword2Activity, Login2Activity::class.java)
                                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                                    startActivity(intent)
                                    finish()
                                } else {
                                    Toast.makeText(this, "Password update failed in database", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            Toast.makeText(this, "Password update failed in authentication", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this, "Current password is incorrect", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

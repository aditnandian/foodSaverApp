package com.example.foodsaverapps.Fragment

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.example.foodsaverapps.ChangePasswordActivity
import com.example.foodsaverapps.Model.UserModel
import com.example.foodsaverapps.ProfileInformationActivity
import com.example.foodsaverapps.StartActivity
import com.example.foodsaverapps.databinding.FragmentProfileBinding
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage

class ProfileFragment : Fragment() {
    private lateinit var binding: FragmentProfileBinding

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()
    private val storage = FirebaseStorage.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        setUserData()

        binding.imageViewProfilePicture.setOnClickListener {
            pickImage()
        }

        binding.imageButtonProfileInformation.setOnClickListener {
            val intent = Intent(requireContext(), ProfileInformationActivity::class.java)
            startActivityForResult(intent, PROFILE_INFORMATION_REQUEST_CODE)
        }

        binding.imageButtonChangePassword.setOnClickListener {
            val intent = Intent(requireContext(), ChangePasswordActivity::class.java)
            startActivity(intent)
        }

        binding.imageButtonLogout.setOnClickListener {
            logoutUser()
        }

        return binding.root
    }

    private fun pickImage() {
        ImagePicker.with(this)
            .cropSquare()
            .compress(1024)
            .maxResultSize(512, 512)
            .start()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            PROFILE_INFORMATION_REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    // Refresh user data after profile information update
                    setUserData()
                }
            }

            ImagePicker.REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val fileUri = data.data
                    fileUri?.let {
                        uploadImageToFirebase(it)
                    }
                } else if (resultCode == ImagePicker.RESULT_ERROR) {
                    Toast.makeText(requireContext(), ImagePicker.getError(data), Toast.LENGTH_SHORT)
                        .show()
                } else {
                    Toast.makeText(requireContext(), "Task Cancelled", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun uploadImageToFirebase(fileUri: Uri) {
        val userId = auth.currentUser?.uid ?: return
        val storageRef = storage.reference.child("profilePictures/$userId.jpg")

        storageRef.putFile(fileUri).addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                updateProfilePictureUri(uri.toString())
            }
        }.addOnFailureListener { e ->
            Toast.makeText(requireContext(), "Upload failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateProfilePictureUri(uri: String) {
        val userId = auth.currentUser?.uid ?: return
        val userRef = database.getReference("customer").child(userId)

        userRef.child("profilePictureUrl").setValue(uri).addOnSuccessListener {
            Glide.with(this)
                .load(uri)
                .transform(CircleCrop())
                .into(binding.imageViewProfilePicture)
            Toast.makeText(requireContext(), "Profile picture updated", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener { e ->
            Toast.makeText(requireContext(), "Failed to update profile picture: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setUserData() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val userReference = database.getReference("customer").child(userId)

            userReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val userProfile = snapshot.getValue(UserModel::class.java)
                        if (userProfile != null) {
                            binding.name.text = userProfile.name
                            binding.email.text = userProfile.email
                            binding.phone.text = userProfile.phone
                            userProfile.profilePictureUrl?.let { url ->
                                Glide.with(this@ProfileFragment)
                                    .load(url)
                                    .transform(CircleCrop())
                                    .into(binding.imageViewProfilePicture)
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            })
        }
    }

    private fun logoutUser() {
        auth.signOut()
        val intent = Intent(requireContext(), StartActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show()
        activity?.finish() // Close the current fragment/activity
    }

    companion object {
        private const val PROFILE_INFORMATION_REQUEST_CODE = 100
    }
}

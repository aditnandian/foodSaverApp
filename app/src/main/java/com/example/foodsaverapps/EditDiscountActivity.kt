package com.example.foodsaverapps

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import android.widget.CheckBox
import android.widget.SeekBar
import androidx.core.content.res.ResourcesCompat
import com.example.foodsaverapps.databinding.ActivityAddDiscountBinding
import com.example.foodsaverapps.Model.VoucherItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class EditDiscountActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddDiscountBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var userId: String
    private lateinit var itemKey: String
    private lateinit var checkBox: CheckBox
    private lateinit var discName: String
    private lateinit var discAmount: String
    private lateinit var discCondition: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddDiscountBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        val currentUser = auth.currentUser
        if (currentUser == null) {
            // Handle not logged in case
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        userId = currentUser.uid

        itemKey = intent.getStringExtra("voucherItemKey") ?: ""
        setData(itemKey,userId)

        binding.addbutton.background = ResourcesCompat.getDrawable(resources,R.drawable.bg_login_button_green,null)
        binding.addbutton.isEnabled = true

        val seek = findViewById<SeekBar>(R.id.bar)
        val check = findViewById<CheckBox>(R.id.lilbox)

        check.isChecked = true
//        seek.progress = 10

        seek?.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seek: SeekBar, progress: Int, fromuser: Boolean){
                    val str = progress * 5;
                    binding.DiscText.text = str.toString();
                }

                override fun onStartTrackingTouch(seek: SeekBar) {
//                    TODO("No fun")
                }

                override fun onStopTrackingTouch(seek: SeekBar) {
//                    TODO("No fun")
                }
            }
        )
        check.setOnCheckedChangeListener{ buttonView, isChecked ->
            if (isChecked) {
                binding.addbutton.background = ResourcesCompat.getDrawable(resources,R.drawable.bg_login_button_green,null)
                binding.addbutton.isEnabled = true
            } else {
                binding.addbutton.background = ResourcesCompat.getDrawable(resources,R.drawable.bg_login_button_ngreen,null)
                binding.addbutton.isEnabled = false
            }
        }

        binding.addbutton.setOnClickListener {
            if (binding.addbutton.isEnabled) {
                discName = binding.DiscText7.text.toString().trim()
                discAmount = binding.DiscText.text.toString().trim()
                discCondition = binding.DiscText5.text.toString().trim()

                if (!(discAmount.isBlank() || discCondition.isBlank() || discName.isBlank())) {
                    uploadData(itemKey,userId)
//                    Toast.makeText(this, "Discount Added Successfully", Toast.LENGTH_SHORT).show()
//                    finish()
                } else {
                    Toast.makeText(this, "Please fill all the details", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "You have to read and agree to the Terms and Conditions", Toast.LENGTH_SHORT).show()
            }
        }
        binding.BackButton.setOnClickListener {
            val intent = Intent(this@EditDiscountActivity, DiscountsActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun setData(itemKey: String, userId: String) {
//        val databaseReference = FirebaseDatabase.getInstance().reference
        val discRef = database.reference.child("shop/$userId/voucher").child(itemKey)

        discRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Retrieve item details from snapshot
                val discItem = snapshot.getValue(VoucherItem::class.java)
                if (discItem != null) {
                    binding.addbutton.text = "Update Discount"
                    binding.TextView1.text = "Update a Discount"
                    binding.DiscText.text = discItem.discount
                    binding.bar.progress = (discItem.discount?.toIntOrNull() ?: 0) / 5
                    binding.DiscText5.setText(discItem.voucherCondition)
                    binding.DiscText7.setText(discItem.voucherName)
                } else {
                    Toast.makeText(this@EditDiscountActivity, "Error", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@EditDiscountActivity, "Something goes wrong, Please try again later", Toast.LENGTH_SHORT).show()
                finish()
            }
        })
    }
    private fun uploadData(itemKey: String, userId: String) {
//        val userId = auth.currentUser?.uid ?: return
        val vRef = database.reference.child("shop/$userId/voucher")
//        val storageRef = FirebaseStorage.getInstance().reference

        val newItem = VoucherItem(
            itemKey,
            voucherName = discName,
            discount = discAmount,
            voucherCondition = discCondition
        )
        vRef.child(itemKey).setValue(newItem)
            .addOnSuccessListener {
                Toast.makeText(this, "Discount updated successfully", Toast.LENGTH_SHORT).show()
                val intent = Intent(this@EditDiscountActivity, DiscountsActivity::class.java)
                startActivity(intent)
                finish()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Update Failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}

//        uploadTask.addOnSuccessListener {
//            imageRef.downloadUrl.addOnSuccessListener {
//                    uri->
//                //
//                val newItem = AllMenu(
//                    menuItemKey,
//                    foodName = foodName,
//                    foodPrice = foodPrice,
//                    foodCategory = foodCategory,
//                    foodStock = foodStock,
//                    foodDescription = foodDescription,
//                    foodImage = uri.toString()
//                )
//                database.getReference("menu").child(menuItemKey).setValue(newItem)
//                    .addOnSuccessListener {
//                        Toast.makeText(this, "Data updated successfully", Toast.LENGTH_SHORT).show()
//                        finish()
//                    }
//                    .addOnFailureListener {
//                        Toast.makeText(this, "Data update failure", Toast.LENGTH_SHORT).show()
//                    }
//            }
//        }.addOnFailureListener {
//            Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show()
//        }
//    }

//    private fun pickImage() {
//        ImagePicker.with(this)
//            .cropSquare()
//            .compress(1024)
//            .maxResultSize(512, 512)
//            .start()
//    }
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (resultCode == Activity.RESULT_OK && data != null) {
//            val fileUri = data.data
//            fileUri?.let {
//                previewImage(it)
//                foodImageUri = it
//            }
//        } else if (resultCode == ImagePicker.RESULT_ERROR) {
//            Toast.makeText(this@EditDiscountActivity, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
//        } else {
//            Toast.makeText(this@EditDiscountActivity, "Task Cancelled", Toast.LENGTH_SHORT).show()
//        }
//    }
//    private fun previewImage(fileUri: Uri) {
//        Glide.with(this@EditDiscountActivity)
//            .load(fileUri)
//            .transform(CenterCrop(), RoundedCorners(20))
//            .into(binding.ImageView1)
//        binding.ImageView1.setPadding(0)
//        binding.ImageView1.background = ResourcesCompat.getDrawable(resources,R.drawable.nullify,null)
//    }

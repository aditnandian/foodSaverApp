package com.example.foodsaverapps

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import android.os.Bundle
import android.widget.Toast
import android.widget.CheckBox
import android.widget.SeekBar
import com.example.foodsaverapps.Model.VoucherItem
import com.example.foodsaverapps.databinding.ActivityAddDiscountBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class AddDiscountActivity : AppCompatActivity() {

    private lateinit var checkBox: CheckBox
    private lateinit var discName: String
    private lateinit var discAmount: String
    private lateinit var discCondition: String

    // Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase

    private val binding: ActivityAddDiscountBinding by lazy {
        ActivityAddDiscountBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.addbutton.isEnabled = false

        // initialize FireBase & database instance
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()

        val seek = findViewById<SeekBar>(R.id.bar)
        val check = findViewById<CheckBox>(R.id.lilbox)
        val perc = 50
        seek.progress = 10
        binding.DiscText.text = perc.toString()

        seek.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seek: SeekBar, progress: Int, fromuser: Boolean) {
                    val str = progress * 5
                    binding.DiscText.text = str.toString()
                }

                override fun onStartTrackingTouch(seek: SeekBar) {
//                    TODO("No fun")
                }

                override fun onStopTrackingTouch(seek: SeekBar) {
//                    TODO("No fun")
                }
            }
        )
        check.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                binding.addbutton.background = ResourcesCompat.getDrawable(resources, R.drawable.bg_login_button_green, null)
            } else {
                binding.addbutton.background = ResourcesCompat.getDrawable(resources, R.drawable.bg_login_button_ngreen, null)
            }
            binding.addbutton.isEnabled = !binding.addbutton.isEnabled
        }

        binding.addbutton.setOnClickListener {
            if (binding.addbutton.isEnabled) {
                discName = binding.DiscText7.text.toString().trim()
                discAmount = binding.DiscText.text.toString().trim()
                discCondition = binding.DiscText5.text.toString().trim()

                if (discCondition.isNotBlank() && discName.isNotBlank()) {
                    uploadData()
                    val intent = Intent(this@AddDiscountActivity, DiscountsActivity::class.java)
                    startActivity(intent)
                    finish() // Close NextActivity and return to CurrentActivity
                } else {
                    Toast.makeText(this, "Please fill all the details", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "You have to read and agree to the Terms and Conditions", Toast.LENGTH_SHORT).show()
            }
        }
        binding.BackButton.setOnClickListener {
            val intent = Intent(this@AddDiscountActivity, DiscountsActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun uploadData() {
        val userId = auth.currentUser?.uid ?: return
        // get a reference to the "menu" node in the database
        val vRef = database.getReference("shop/$userId/voucher")
        // Generate a unique key for the new menu item
        val newItemKey = vRef.push().key

        val newItem = VoucherItem(
            newItemKey,
            voucherName = discName,
            discount = discAmount,
            voucherCondition = discCondition
        )
        newItemKey?.let { key ->
            vRef.child(key).setValue(newItem).addOnSuccessListener {
                Toast.makeText(this, "Discount Added successfully", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                Toast.makeText(this, "Failure", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

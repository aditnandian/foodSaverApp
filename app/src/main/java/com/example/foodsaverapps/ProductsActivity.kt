package com.example.foodsaverapps

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.appcompat.app.AppCompatActivity
import com.example.foodsaverapps.Adapter.MenuItemAdapter
import com.example.foodsaverapps.Model.AllMenu
import com.example.foodsaverapps.databinding.ActivityProductsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProductsActivity : AppCompatActivity() {
    private lateinit var databaseReference: DatabaseReference
    private lateinit var database: FirebaseDatabase
    private val auth = FirebaseAuth.getInstance()
    private var menuItems: ArrayList<AllMenu> = ArrayList()
    private val binding : ActivityProductsBinding by lazy {
        ActivityProductsBinding.inflate(layoutInflater)
    }

    var meals = false
    var drinks = false
    var snacks = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        databaseReference = FirebaseDatabase.getInstance().reference
        retrieveMenuItem()

        binding.BackButton.setOnClickListener {
            finish()
        }
        binding.addbutton.setOnClickListener{
            clickMenuItems(-1)
        }
    }


    private fun retrieveMenuItem() {
        val userId = auth.currentUser?.uid ?: return
        database = FirebaseDatabase.getInstance()
        val foodRef: DatabaseReference = database.reference.child("shop/$userId/menu")

        meals = false
        drinks = false
        snacks = false

        // fetch data from database
        foodRef.addListenerForSingleValueEvent(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                // Clear existing data before populating
                menuItems.clear()

                // loop for through each food item
                for (foodSnapshot in snapshot.children){
                    val menuItem = foodSnapshot.getValue(AllMenu::class.java)
                    menuItem?.let {
                        menuItems.add(it)
                        when (it.foodCategory) {
                            "Heavy Meal" -> meals = true
                            "Snacks" -> snacks = true
                            "Drinks" -> drinks = true
                        }
                    }
                }
                setAdapter()
                val updateData = hashMapOf<String, Any>(
                    "heavymeals" to meals,
                    "snacks" to snacks,
                    "drinks" to drinks
                )
                database.reference.child("shop/$userId").updateChildren(updateData as Map<String, Any>)
                    .addOnFailureListener { error ->
                        Log.e("DatabaseUpdate", "Failed to update categories: ${error.message}")
                        Toast.makeText(this@ProductsActivity, "Something went wrong. Please retry.", Toast.LENGTH_SHORT).show()
                    }
            }
            override fun onCancelled(error: DatabaseError) {
                Log.d("DatabaseError", "Error: ${error.message}")
            }
        })
    }
    private fun setAdapter() {

        val adapter = MenuItemAdapter(
            this@ProductsActivity,
            menuItems,
            databaseReference,
            onItemClickListener = { position ->
                clickMenuItems(position)
            },
            onDeleteClickListener = { position ->
                deleteMenuItems(position)
            },
            onSwitchChangeListener = { position, isChecked ->
                toggleMenuItems(position,isChecked)
            })
        binding.MenuRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.MenuRecyclerView.adapter= adapter
    }

    private fun clickMenuItems(position: Int) {
        if (position == -1){
            val intent = Intent(this@ProductsActivity, AddProductActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            val menuItemToEdit= menuItems[position]
            val menuItemKey = menuItemToEdit.key

            val intent = Intent(this@ProductsActivity, EditProductActivity::class.java)
            intent.putExtra("menuItemKey", menuItemKey)
            startActivity(intent)
            finish()
        }
        binding.MenuRecyclerView.adapter?.notifyDataSetChanged()
    }
    private fun deleteMenuItems(position: Int) {
        val userId = auth.currentUser?.uid ?: return
        val menuItemToDelete= menuItems[position]
        val menuItemKey = menuItemToDelete.key
        val foodMenuReference = database.reference.child("shop/$userId/menu").child(menuItemKey!!)
        foodMenuReference.removeValue().addOnCompleteListener {task ->
            if (task.isSuccessful){
                menuItems.removeAt(position)
                binding.MenuRecyclerView.adapter?.notifyItemRemoved(position)
            }else {
                Toast.makeText(this, "Item Undeleted", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun toggleMenuItems(position: Int, isChecked: Boolean) {
        val userId = auth.currentUser?.uid ?: return
        val menuItem= menuItems[position]
        val menuItemKey = menuItem.key
        val foodMenuReference = database.reference.child("shop/$userId/menu").child(menuItemKey!!)
        foodMenuReference.updateChildren(mapOf("foodAvailable" to isChecked))
            .addOnSuccessListener {
                if (isChecked){
                    Toast.makeText(this,"The chosen menu is now available.",Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this,"The chosen menu is now unavailable.",Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this,"An error occurred. Please try again later!",Toast.LENGTH_LONG).show()
                Log.e("toogleMenuItems","",exception)
            }
    }
}
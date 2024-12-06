package com.example.foodsaverapp.Adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.foodsaverapps.Fragment.CartFragment
import com.example.foodsaverapps.databinding.CartItemBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class CartAdapter(
    private val context: Context,
    private val cartItems: MutableList<String?>,
    private val cartItemPrices: MutableList<Int>,
    private val cartDescriptions: MutableList<String>,
    private val cartImages: MutableList<String>,
    private val cartQuantities: MutableList<Int>
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    // instance Firebase
    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance()
    private val userId = auth.currentUser?.uid ?: ""
    private val cartItemsReference = database.reference.child("customer").child(userId).child("cartItems")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = CartItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CartViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int = cartItems.size

    // get updated quantity
    fun getUpdatedItemsQuantities(): List<Int> {
        return cartQuantities.toList()
    }

    inner class CartViewHolder(private val binding: CartItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(position: Int) {
            binding.apply {
                val quantity = cartQuantities[position]
                cartFoodName.text = cartItems[position]
                cartItemPrice.text = "Rp" + cartItemPrices[position]
                val uriString = cartImages[position]
                val uri = Uri.parse(uriString)
                Glide.with(context).load(uri).into(cartImage)
                catItemQuantity.text = quantity.toString()

                minusbutton.setOnClickListener {
                    if (quantity > 1) {
                        cartQuantities[position] = cartQuantities[position] - 1
                        catItemQuantity.text = cartQuantities[position].toString()
                        updateQuantityInFirebase(position, cartQuantities[position])
                    }
                }

                plusebutton.setOnClickListener {
                    if (quantity < 10) {
                        cartQuantities[position] = cartQuantities[position] + 1
                        catItemQuantity.text = cartQuantities[position].toString()
                        updateQuantityInFirebase(position, cartQuantities[position])
                    }
                }

                deleteButton.setOnClickListener {
                    deleteItem(position)
                }
            }
        }

        private fun updateQuantityInFirebase(position: Int, newQuantity: Int) {
            val itemName = cartItems[position]
            cartItemsReference.orderByChild("foodName").equalTo(itemName).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (dataSnapshot in snapshot.children) {
                        val key = dataSnapshot.key
                        key?.let {
                            cartItemsReference.child(it).child("foodQuantity").setValue(newQuantity)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, "Failed to update quantity", Toast.LENGTH_SHORT).show()
                }
            })
        }


        private fun deleteItem(position: Int) {
            val itemName = cartItems[position]
            cartItemsReference.orderByChild("foodName").equalTo(itemName).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (dataSnapshot in snapshot.children) {
                        val key = dataSnapshot.key
                        key?.let {
                            cartItemsReference.child(it).removeValue().addOnSuccessListener {
                                // Ensure position is valid before modifying lists
                                if (position >= 0 && position < cartItems.size) {
                                    // Update local data
                                    cartItems.removeAt(position)
                                    cartImages.removeAt(position)
                                    cartDescriptions.removeAt(position)
                                    cartQuantities.removeAt(position)
                                    cartItemPrices.removeAt(position)

                                    // Notify adapter
                                    notifyItemRemoved(position)
                                    notifyItemRangeChanged(position, itemCount)
                                    Toast.makeText(context, "Item Deleted", Toast.LENGTH_SHORT).show()

                                    // Update proceed button visibility
                                    if (cartItems.isEmpty()) {
                                        (context as FragmentActivity).supportFragmentManager.findFragmentByTag("CartFragment")?.let {
                                            (it as CartFragment).updateProceedButtonVisibility()
                                        }
                                    }
                                }
                            }.addOnFailureListener {
                                Toast.makeText(context, "Failed to Delete", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(context, "Failed to delete item", Toast.LENGTH_SHORT).show()
                }
            })
        }


    }
}

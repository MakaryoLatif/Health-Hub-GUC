package com.example.healthhubguc

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.RecyclerView

class CartAdapter(
    private val cartItems: List<Medication>,
    private val deleteItemListener: (Medication) -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_cart_adapter, parent, false)
        return CartViewHolder(view)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        holder.bind(cartItems[position])
    }

    override fun getItemCount() = cartItems.size

    inner class CartViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameText = itemView.findViewById<TextView>(R.id.cartItemName)
        private val quantityText = itemView.findViewById<TextView>(R.id.cartItemQuantity)
        private val priceText = itemView.findViewById<TextView>(R.id.cartItemPrice)
        private val deleteButton = itemView.findViewById<Button>(R.id.deleteButton)

        fun bind(medication: Medication) {
            nameText.text = medication.name
            quantityText.text = "Quantity: ${medication.selectedQuantity}"
            priceText.text = "\"Price: \$${"%.2f".format(medication.price * medication.selectedQuantity)}"
            deleteButton.setOnClickListener {
                deleteItemListener(medication) // Call the listener to delete the item
            }
        }
    }
}

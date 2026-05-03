package com.example.healthhubguc

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MedicationsAdapterD(
    private val medications: List<Medication>,
    private val onEditClick: (Medication) -> Unit
) : RecyclerView.Adapter<MedicationsAdapterD.MedicationViewHolder>() { // Updated class reference

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_medications_adapter_d, parent, false)
        return MedicationViewHolder(view)
    }

    override fun onBindViewHolder(holder: MedicationViewHolder, position: Int) {
        val medication = medications[position]
        holder.bind(medication)
        holder.itemView.setOnClickListener { onEditClick(medication) }
    }

    override fun getItemCount() = medications.size

    inner class MedicationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameText: TextView = itemView.findViewById(R.id.item_medication_name)
        private val quantityText: TextView = itemView.findViewById(R.id.item_medication_quantity)
        private val priceText: TextView = itemView.findViewById(R.id.item_medication_price)
        private val medicationImage: ImageView = itemView.findViewById(R.id.item_medication_image)

        fun bind(medication: Medication) {
            nameText.text = medication.name
            quantityText.text = "Quantity: ${medication.quantity}"
            priceText.text = "Price: ${medication.price}"

            // Load image from base64 if it's not null
            medication.imagePath?.let {
                val decodedByte = Base64.decode(it, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.size)
                medicationImage.setImageBitmap(bitmap)
            } ?: run {
                // Set a default placeholder if no image is available
                medicationImage.setImageResource(R.drawable.ic_placeholder_image)
            }
        }
    }
}

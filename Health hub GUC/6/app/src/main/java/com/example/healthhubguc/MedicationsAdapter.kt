package com.example.healthhubguc

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.util.Base64
import android.widget.ImageView

class MedicationsAdapter(
    private val medicationsList: MutableList<Medication>,
    private val onMedicationSelected: (Medication) -> Unit
) : RecyclerView.Adapter<MedicationsAdapter.MedicationViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MedicationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.activity_medications_adapter, parent, false)
        return MedicationViewHolder(view)
    }

    override fun onBindViewHolder(holder: MedicationViewHolder, position: Int) {
        holder.bind(medicationsList[position])
    }

    override fun getItemCount() = medicationsList.size

    inner class MedicationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameText: TextView = itemView.findViewById(R.id.medication_name)
        private val quantityText: TextView = itemView.findViewById(R.id.medication_quantity)
        private val priceText: TextView = itemView.findViewById(R.id.medication_price)
        private val chosenQuantityText = itemView.findViewById<TextView>(R.id.chosen_quantity)
        private val increaseButton = itemView.findViewById<Button>(R.id.increaseButton)
        private val decreaseButton = itemView.findViewById<Button>(R.id.decreaseButton)
        private val expandView: View = itemView.findViewById(R.id.expandable_view)
        private val medicationImage: ImageView = itemView.findViewById(R.id.item_medication_image)


        fun bind(medication: Medication) {
            // Set medication details
            nameText.text = medication.name
            quantityText.text = "Quantity: ${medication.quantity}"
            priceText.text = "Price: ${medication.price}"
            chosenQuantityText.text = medication.selectedQuantity.toString()
            expandView.visibility = View.GONE

            // Decode the image if available
            medication.imagePath?.let {
                val decodedByte = Base64.decode(it, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(decodedByte, 0, decodedByte.size)
                medicationImage.setImageBitmap(bitmap)
            } ?: run {
                medicationImage.setImageResource(R.drawable.ic_placeholder_image) // Placeholder image
            }

            // Set button visibility based on status

            val isApproved = medication.status == "Approved"

            // Disable buttons and prevent quantity change after approval
            increaseButton.isEnabled = !isApproved
            decreaseButton.isEnabled = !isApproved

            // Ensure quantity text remains fixed after approval
            chosenQuantityText.text = medication.selectedQuantity.toString()


            // Handle quantity changes
            increaseButton.setOnClickListener {
                if (medication.selectedQuantity < medication.quantity) {
                    medication.selectedQuantity += 1
                    chosenQuantityText.text = medication.selectedQuantity.toString()
                }
            }

            decreaseButton.setOnClickListener {
                if (medication.selectedQuantity > 0) {
                    medication.selectedQuantity -= 1
                    chosenQuantityText.text = medication.selectedQuantity.toString()
                }
            }

            // Handle ask for approval button click


            // Handle add to cart button click


            // Expand or collapse details view on item click
            itemView.setOnClickListener {
                expandView.visibility = if (expandView.visibility == View.GONE) View.VISIBLE else View.GONE
                onMedicationSelected(medication)
            }
        }
    }
}


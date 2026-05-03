package com.example.healthhubguc

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.io.Serializable

@Parcelize
data class Medication(
    var name: String? = null,
    var quantity: Int = 0,
    var price: Double = 0.0,
    var id: String? = null,
    var selectedQuantity: Int = 0,
    var quantityInCart: Int = 0,
    var imagePath: String? = null,
    var status: String = "Pending",
    var requestedBy: String? = null,
    var requestedByName: String? = null,
    var requestedQuantity: Int = 0,
    var approvedFor: String? = null
) : Parcelable

package com.example.healthhubguc

data class User(

    var name: String? = null,
    var age: String? = null,
    var email: String? = null,
    var userType: String? = null,
    var gender: String? = null,
    var userId: String? = null,
    var password: String? = null,
    var cart: MutableList<Medication>? = mutableListOf(),
    var consultations: MutableList<Consultation>? = mutableListOf()

)
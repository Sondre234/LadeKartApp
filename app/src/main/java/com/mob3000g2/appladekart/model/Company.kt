package com.mob3000g2.appladekart.model

import kotlinx.serialization.SerialName

// Data class for company objects
data class Company(
    @SerialName("name")
    val name : String
){
    companion object {
        // Function for making company objects from string
        fun fromString(name: String): Company {
            return Company(name)
        }
    }
}
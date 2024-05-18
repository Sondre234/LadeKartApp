package com.mob3000g2.appladekart.model

import kotlinx.serialization.SerialName

// Data class for car model objects
data class CarModel(
    @SerialName("name")
    val name : String
){
    companion object {
        // Function for making car model objects from string
        fun fromString(name: String): CarModel {
            return CarModel(name)
        }
    }
}
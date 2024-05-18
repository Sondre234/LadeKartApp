package com.mob3000g2.appladekart.model

data class UserMarkSelect(
    val id: String,
    val lat: Double,
    val lng: Double,
    val distance: Double,
    val range: Double,
    var maxCharge: Double,
    val addedRange: Double
)

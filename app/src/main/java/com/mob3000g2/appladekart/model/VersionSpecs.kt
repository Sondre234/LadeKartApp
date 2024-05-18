package com.mob3000g2.appladekart.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Data class for specifications of a ModelVersion
@Serializable
data class VersionSpecs(
    @SerialName("Company")
    val company: String,
    @SerialName("Car Model")
    val carModel: String,
    @SerialName("Version")
    val modelVersion: String,
    @SerialName("Details")
    val details: Details

)

// Data class for range, battery capacity and charging speed
@Serializable
data class Details(
    @SerialName("Range (km)")
    val range: Int,
    @SerialName("Battery Capacity (kWh)")
    val batteryCapacity: Int,
    @SerialName("Charging Speed (kW)")
    val chargingSpeed: Int
)

// Helping class for editing existing specifications
// Designed like this due to match the APIs expectations
data class CarDetailsUpdate(
    val range_km: Int,
    val battery_capacity_kWh: Int,
    val charging_speed_kW: Int
)

// Help class for setting specifications on new car
// Designed like this due to match the APIs expectations
@Serializable
data class NewSpecs(
    @SerialName("Company")
    val company: String,
    @SerialName("Car Model")
    val carModel: String,
    @SerialName("Version")
    val modelVersion: String,
    @SerialName("Details")
    val range: Int,
    val batteryCapacity: Int,
    val chargingSpeed: Int
)




package com.mob3000g2.appladekart.model

import com.google.gson.annotations.SerializedName


data class MapStations (
    @SerializedName("id")
    val id: String,

    @SerializedName("geolocation")
    val geolocation: String,

    @SerializedName("latlng")
    val latLng: LatLng,

    @SerializedName("name")
    val name: String,

    @SerializedName("connector")
    val connector: String,

    @SerializedName("maxChargingCapacity")
    val maxChargingCapacity: String,

    @SerializedName("adress")
    val address: String,

    @SerializedName("description")
    val description: String?,

    @SerializedName("alreadyadded")
    val alreadyAdded: Boolean
    ) {
}


data class LatLng(
    @SerializedName("lat")
    val lat: Double,

    @SerializedName("lng")
    val lng: Double
)

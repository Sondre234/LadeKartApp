package com.mob3000g2.appladekart.model

data class ChargerRequestBody(
    val bounds: Bounds
) {
    data class Bounds(
        val ne: String,
        val sw: String
    )
}

package com.mob3000g2.appladekart.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Data class for versions of car models
@Serializable
data class ModelVersion(
    @SerialName(value = "Versions")
    val versions: List<String>
)
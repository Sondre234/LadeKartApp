package com.mob3000g2.appladekart.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Admin account data class used for logon to admin pages
@Serializable
data class AdminAccount(
    @SerialName("email")
    val username : String,
    @SerialName("password")
    val password : String
)

// Data class for creating new admin account
@Serializable
data class CreateAdminAccount(
    @SerialName("email")
    val email : String,
    @SerialName("password")
    val password : String,
    @SerialName("isSuperAdmin")
    val isSuperAdmin : Boolean
)

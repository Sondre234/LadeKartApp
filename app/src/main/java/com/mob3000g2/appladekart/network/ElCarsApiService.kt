package com.mob3000g2.appladekart.network

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.mob3000g2.appladekart.model.AdminAccount
import com.mob3000g2.appladekart.model.CarDetailsUpdate
import com.mob3000g2.appladekart.model.CreateAdminAccount
import com.mob3000g2.appladekart.model.ModelVersion
import com.mob3000g2.appladekart.model.NewSpecs
import com.mob3000g2.appladekart.model.VersionSpecs
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

/*
 * Code built up using code pattern in this guide:
 * https://developer.android.com/codelabs/basic-android-kotlin-training-getting-data-internet#5
 * 11.10.2023 19:15
 */

// Base URL constant
private const val BASE_URL = "https://testgruppe3usnexpress.onrender.com"

private val retrofit = Retrofit.Builder()
    .addConverterFactory(ScalarsConverterFactory.create())
    .addConverterFactory(GsonConverterFactory.create())
    .baseUrl(BASE_URL)
    .build()

// Retrofit builder and convert JSON to String
private val retrofit2 = Retrofit.Builder()
    .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
    .baseUrl(BASE_URL)
    .build()

// Defining how Retrofit talk to web server
interface ElCarsApiService {
    // Get all companies from database
    @GET("ElCars")
    suspend fun getCompanies(): List<String>

    // Get all car models of a company
    @GET("ElCars/{Company}")
    suspend fun getCarModels(@Path("Company") company: String): List<String>

    // Get all versions of a car model
    @GET("ElCars/{Company}/{CarModel}")
    suspend fun getModelVersions(@Path("Company") company: String, @Path("CarModel") carModel: String): List<ModelVersion>

    // Get specifications of a model version
    @GET("ElCars/{Company}/{CarModel}/{ModelVersion}")
    suspend fun getVersionSpecs(
        @Path("Company") company: String,
        @Path("CarModel") carModel: String,
        @Path("ModelVersion") modelVersion: String
    ): List<VersionSpecs>

    // Create new admin account
    @POST("Admin")
    suspend fun createAdminAccount(@Body createAdminAccount: CreateAdminAccount): Response<Boolean>

    // For logon to admin pages
    @POST("Admin/login")
    suspend fun adminLogin(@Body credentials: AdminAccount): Response<Boolean>

    // Insert a company into the database
    @POST("ElCars/{Company}")
    suspend fun setCompany(@Path("Company") company: String) : String

    // Insert model, version and specifications into the database
    @POST("ElCars/{Company}/{CarModel}/{ModelVersion}")
    suspend fun setCar(
        @Path("Company") company: String,
        @Path("CarModel") carModel: String,
        @Path("ModelVersion") version: String,
        @Body newSpecs: NewSpecs
    )

    // Edit an existing company in the database
    @PUT("ElCars/{oldCompany}/{newCompany}")
    suspend fun editCompany(
        @Path("oldCompany") oldCompany: String,
        @Path("newCompany") newCompany: String
    ): String

    // Edit existing model, version and specs in the database
    @PUT("ElCars/{company}/{oldCarModel}/{newCarModel}/{oldVersion}/{newVersion}")
    suspend fun editCarDetails(
        @Path("company") company: String,
        @Path("oldCarModel") oldCarModel: String,
        @Path("newCarModel") newCarModel: String,
        @Path("oldVersion") oldVersion: String,
        @Path("newVersion") newVersion: String,
        @Body updatedDetails: CarDetailsUpdate
    )

    // Delete a company from the database
    @DELETE("ElCars/{Company}")
    suspend fun deleteCompany(@Path("Company") company: String) : String

    // Delete a car model from the database
    @DELETE("ElCars/{Company}/{CarModel}")
    suspend fun deleteCarModel(
        @Path("Company") company: String,
        @Path("CarModel") carModel: String
    ) : String
}

// Initializing Retrofit service
object ElCarsApi {
    val retrofitService : ElCarsApiService by lazy {
        retrofit.create(ElCarsApiService::class.java)
    }
}

object ElCarsApi2 {
    val retrofitService2 : ElCarsApiService by lazy {
        retrofit2.create(ElCarsApiService::class.java)
    }
}

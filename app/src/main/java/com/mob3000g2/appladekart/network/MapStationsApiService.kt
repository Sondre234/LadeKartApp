package com.mob3000g2.appladekart.network

import com.mob3000g2.appladekart.model.ChargerRequestBody
import com.mob3000g2.appladekart.model.MapStations
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

/*
 * Code built up using code pattern in this guide:
 * https://developer.android.com/codelabs/basic-android-kotlin-training-getting-data-internet#5
 * * 11.10.2023 19:15
 */

// Base URL constant
private const val BASE_URL = "https://testgruppe3usnexpress.onrender.com"

private val retrofit = Retrofit.Builder()
    .addConverterFactory(ScalarsConverterFactory.create())
    .addConverterFactory(GsonConverterFactory.create())
    .baseUrl(BASE_URL)
    .build()

/*
 * Defining how Retrofit talk to web server
 * This version will only work for /charger-stations api calls,
 * as the ElCarsApiService used Json.asConverterFactory("application/json".toMediaType())
 * and that will not work for this.
 * So I use GsonConverterFactory.create() to get /charger-stations api calls
 * to work, even when both xApiService.kt calls on the same REST API
 */
interface MapStationsApiService {
    @POST("/charger-stations")
    suspend fun getChargerJson(
        @Body requestBody: ChargerRequestBody
    ): List<MapStations>
}

object MapStationsApi {
    val retrofitService : MapStationsApiService by lazy {
        retrofit.create(MapStationsApiService::class.java)
    }
}
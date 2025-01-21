package com.example.gdp2025

import com.example.gdp2025.WeatherResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApi {
    @GET("weather")
    suspend fun getWeather(
        @Query("q") city: String,
        @Query("appid") apiKey: String = "324bd9f23ac0d46f5f23582be6b51c5c",
        @Query("units") units: String = "metric"
    ): WeatherResponse
}

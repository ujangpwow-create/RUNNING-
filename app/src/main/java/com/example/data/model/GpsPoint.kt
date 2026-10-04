package com.example.data.model

data class GpsPoint(
    val latitude: Double,
    val longitude: Double,
    val altitude: Double = 0.0,
    val speedMps: Float = 0f,
    val timestamp: Long = System.currentTimeMillis()
)

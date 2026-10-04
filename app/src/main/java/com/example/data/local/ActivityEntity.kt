package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "activities")
data class ActivityEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val sportType: String, // "RUN" or "RIDE"
    val timestamp: Long,
    val durationSeconds: Long,
    val distanceMeters: Double,
    val elevationGainMeters: Double,
    val avgSpeedKmh: Double,
    val maxSpeedKmh: Double,
    val calories: Int,
    val avgPaceSecondsPerKm: Int,
    val routeJson: String,
    val splitsJson: String,
    val kudosCount: Int = 0,
    val userNotes: String = ""
)

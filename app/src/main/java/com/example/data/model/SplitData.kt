package com.example.data.model

data class SplitData(
    val kilometer: Int,
    val durationSeconds: Long,
    val paceSecondsPerKm: Int,
    val avgSpeedKmh: Double,
    val elevationChangeMeters: Double
) {
    fun formatPace(): String {
        val minutes = paceSecondsPerKm / 60
        val seconds = paceSecondsPerKm % 60
        return String.format("%d:%02d", minutes, seconds)
    }

    fun formatDuration(): String {
        val minutes = durationSeconds / 60
        val seconds = durationSeconds % 60
        return String.format("%d:%02d", minutes, seconds)
    }
}

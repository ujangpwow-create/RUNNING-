package com.example.data.model

enum class SportType(
    val title: String,
    val paceLabel: String,
    val speedLabel: String,
    val isPacePrimary: Boolean
) {
    RUN("Lari", "min/km", "km/j", true),
    RIDE("Bersepeda", "min/km", "km/j", false);

    companion object {
        fun fromString(value: String): SportType {
            return try {
                valueOf(value.uppercase())
            } catch (e: Exception) {
                RUN
            }
        }
    }
}

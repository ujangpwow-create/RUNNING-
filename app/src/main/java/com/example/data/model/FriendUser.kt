package com.example.data.model

data class FriendUser(
    val id: String,
    val name: String,
    val username: String,
    val avatarColorHex: Long,
    val weeklyRunKm: Double,
    val weeklyRideKm: Double,
    val weeklyActivityCount: Int,
    val isCurrentUser: Boolean = false,
    val kudosGiven: Boolean = false,
    val kudosCount: Int = 0,
    val streakWeeks: Int = 1,
    val rankBadge: String = "",
    val motto: String = ""
) {
    val totalWeeklyKm: Double
        get() = weeklyRunKm + weeklyRideKm

    val initials: String
        get() = name.split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .map { it.first().uppercase() }
            .joinToString("")
            .ifEmpty { "U" }
}

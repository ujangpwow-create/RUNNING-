package com.example.data.repository

import com.example.data.local.ActivityDao
import com.example.data.local.ActivityEntity
import com.example.data.model.FriendUser
import com.example.data.model.SportType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Calendar

class ActivityRepository(private val activityDao: ActivityDao) {

    val allActivities: Flow<List<ActivityEntity>> = activityDao.getAllActivities()

    // Friends list for weekly community leaderboard
    private val _friends = MutableStateFlow<List<FriendUser>>(createInitialFriends())
    val friends: StateFlow<List<FriendUser>> = _friends.asStateFlow()

    suspend fun saveActivity(activity: ActivityEntity): Long {
        val id = activityDao.insertActivity(activity)
        // Update user stats in leaderboard
        updateCurrentUserInLeaderboard(activity)
        return id
    }

    suspend fun deleteActivity(activity: ActivityEntity) {
        activityDao.deleteActivity(activity)
    }

    suspend fun giveKudosToActivity(id: Long) {
        activityDao.incrementKudos(id)
    }

    fun giveKudosToFriend(friendId: String) {
        val currentList = _friends.value
        val updated = currentList.map { friend ->
            if (friend.id == friendId) {
                val newKudosState = !friend.kudosGiven
                val newCount = if (newKudosState) friend.kudosCount + 1 else friend.kudosCount - 1
                friend.copy(kudosGiven = newKudosState, kudosCount = maxOf(0, newCount))
            } else {
                friend
            }
        }
        _friends.value = updated
    }

    fun addFriend(name: String, motto: String) {
        val initials = name.split(" ").filter { it.isNotBlank() }.take(2).map { it.first().uppercase() }.joinToString("")
        val colors = listOf(0xFFFC5200, 0xFF00D2D3, 0xFF7C4DFF, 0xFF00E676, 0xFFFFB300, 0xFFE91E63)
        val randomColor = colors.random()
        val newFriend = FriendUser(
            id = "friend_${System.currentTimeMillis()}",
            name = name,
            username = "@${name.lowercase().replace(" ", "")}",
            avatarColorHex = randomColor,
            weeklyRunKm = (5..22).random().toDouble() + (0..9).random() / 10.0,
            weeklyRideKm = (15..55).random().toDouble() + (0..9).random() / 10.0,
            weeklyActivityCount = (2..5).random(),
            isCurrentUser = false,
            kudosCount = (3..18).random(),
            streakWeeks = (1..6).random(),
            motto = motto.ifEmpty { "Tetap konsisten berlatih setiap minggu!" }
        )
        _friends.value = _friends.value + newFriend
    }

    private fun updateCurrentUserInLeaderboard(activity: ActivityEntity) {
        val addedKm = activity.distanceMeters / 1000.0
        val isRun = activity.sportType == SportType.RUN.name

        _friends.value = _friends.value.map { friend ->
            if (friend.isCurrentUser) {
                friend.copy(
                    weeklyRunKm = if (isRun) friend.weeklyRunKm + addedKm else friend.weeklyRunKm,
                    weeklyRideKm = if (!isRun) friend.weeklyRideKm + addedKm else friend.weeklyRideKm,
                    weeklyActivityCount = friend.weeklyActivityCount + 1
                )
            } else {
                friend
            }
        }
    }

    fun syncUserActivitiesToLeaderboard(activities: List<ActivityEntity>) {
        val startOfWeek = getStartOfWeekTimestamp()
        val thisWeekActivities = activities.filter { it.timestamp >= startOfWeek }

        val weeklyRun = thisWeekActivities.filter { it.sportType == SportType.RUN.name }
            .sumOf { it.distanceMeters } / 1000.0
        val weeklyRide = thisWeekActivities.filter { it.sportType == SportType.RIDE.name }
            .sumOf { it.distanceMeters } / 1000.0

        _friends.value = _friends.value.map { friend ->
            if (friend.isCurrentUser) {
                friend.copy(
                    weeklyRunKm = weeklyRun,
                    weeklyRideKm = weeklyRide,
                    weeklyActivityCount = thisWeekActivities.size
                )
            } else {
                friend
            }
        }
    }

    private fun getStartOfWeekTimestamp(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    private fun createInitialFriends(): List<FriendUser> {
        return listOf(
            FriendUser(
                id = "me",
                name = "Saya (Anda)",
                username = "@athlete_saya",
                avatarColorHex = 0xFFFC5200,
                weeklyRunKm = 5.2,
                weeklyRideKm = 24.5,
                weeklyActivityCount = 2,
                isCurrentUser = true,
                kudosCount = 28,
                streakWeeks = 4,
                motto = "Siap taklukkan target mingguan!"
            ),
            FriendUser(
                id = "f_1",
                name = "Budi Santoso",
                username = "@budi_run",
                avatarColorHex = 0xFF00D2D3,
                weeklyRunKm = 28.4,
                weeklyRideKm = 0.0,
                weeklyActivityCount = 4,
                kudosCount = 42,
                streakWeeks = 8,
                motto = "Target 100km bulan ini! Semangat tanpa batas!"
            ),
            FriendUser(
                id = "f_2",
                name = "Reza Pratama",
                username = "@rezavelo",
                avatarColorHex = 0xFF7C4DFF,
                weeklyRunKm = 0.0,
                weeklyRideKm = 82.6,
                weeklyActivityCount = 3,
                kudosCount = 56,
                streakWeeks = 12,
                motto = "Pagi tanjakan, sore gowes santai."
            ),
            FriendUser(
                id = "f_3",
                name = "Siti Rahma",
                username = "@siti_runner",
                avatarColorHex = 0xFF00E676,
                weeklyRunKm = 21.0,
                weeklyRideKm = 15.2,
                weeklyActivityCount = 4,
                kudosCount = 35,
                streakWeeks = 5,
                motto = "Lari demi kesehatan dan pikiran jernih."
            ),
            FriendUser(
                id = "f_4",
                name = "Dimas Anggara",
                username = "@dimas_tri",
                avatarColorHex = 0xFFFFB300,
                weeklyRunKm = 15.5,
                weeklyRideKm = 45.0,
                weeklyActivityCount = 3,
                kudosCount = 29,
                streakWeeks = 3,
                motto = "Kombinasi lari dan sepedaan tiap akhir pekan."
            ),
            FriendUser(
                id = "f_5",
                name = "Maya Kusuma",
                username = "@mayapride",
                avatarColorHex = 0xFFE91E63,
                weeklyRunKm = 18.2,
                weeklyRideKm = 0.0,
                weeklyActivityCount = 3,
                kudosCount = 24,
                streakWeeks = 2,
                motto = "Start slow, finish strong!"
            ),
            FriendUser(
                id = "f_6",
                name = "Kevin Wijaya",
                username = "@kevin_spoke",
                avatarColorHex = 0xFF3F51B5,
                weeklyRunKm = 0.0,
                weeklyRideKm = 64.0,
                weeklyActivityCount = 2,
                kudosCount = 31,
                streakWeeks = 6,
                motto = "Weekend Gran Fondo adalah hobi wajib."
            )
        )
    }
}

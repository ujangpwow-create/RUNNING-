package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityDao {
    @Query("SELECT * FROM activities ORDER BY timestamp DESC")
    fun getAllActivities(): Flow<List<ActivityEntity>>

    @Query("SELECT * FROM activities WHERE id = :id LIMIT 1")
    fun getActivityById(id: Long): Flow<ActivityEntity?>

    @Query("SELECT * FROM activities WHERE sportType = :sportType ORDER BY timestamp DESC")
    fun getActivitiesBySport(sportType: String): Flow<List<ActivityEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivity(activity: ActivityEntity): Long

    @Delete
    suspend fun deleteActivity(activity: ActivityEntity)

    @Query("UPDATE activities SET kudosCount = kudosCount + 1 WHERE id = :id")
    suspend fun incrementKudos(id: Long)

    @Query("SELECT SUM(distanceMeters) FROM activities WHERE timestamp >= :sinceTimestamp")
    suspend fun getTotalDistanceSince(sinceTimestamp: Long): Double?

    @Query("SELECT COUNT(*) FROM activities")
    suspend fun getTotalCount(): Int
}

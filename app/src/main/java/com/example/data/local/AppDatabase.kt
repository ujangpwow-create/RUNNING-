package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.GpsPoint
import com.example.data.model.SplitData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [ActivityEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun activityDao(): ActivityDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "velorun_database"
                )
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialActivities(database.activityDao())
                    }
                }
            }
        }

        private suspend fun populateInitialActivities(dao: ActivityDao) {
            val now = System.currentTimeMillis()
            val dayMillis = 86_400_000L

            // Sample Run 1 (Kemarin)
            val run1Points = generateSampleRoute(
                startLat = -6.2146,
                startLng = 106.8451,
                pointCount = 45,
                distanceTotalMeters = 5200.0,
                baseElevation = 18.0
            )
            val run1Splits = listOf(
                SplitData(1, 315, 315, 11.4, 2.5),
                SplitData(2, 320, 320, 11.2, 4.0),
                SplitData(3, 310, 310, 11.6, -1.0),
                SplitData(4, 305, 305, 11.8, 3.5),
                SplitData(5, 300, 300, 12.0, 1.2),
                SplitData(6, 60, 300, 12.0, 0.5)
            )

            dao.insertActivity(
                ActivityEntity(
                    title = "Lari Pagi Sudirman - GBK Loop",
                    sportType = "RUN",
                    timestamp = now - dayMillis,
                    durationSeconds = 1610L, // ~26 min 50s
                    distanceMeters = 5200.0,
                    elevationGainMeters = 42.0,
                    avgSpeedKmh = 11.6,
                    maxSpeedKmh = 14.8,
                    calories = 368,
                    avgPaceSecondsPerKm = 310, // 5:10 min/km
                    routeJson = RouteSerializer.serializeGpsPoints(run1Points),
                    splitsJson = RouteSerializer.serializeSplits(run1Splits),
                    kudosCount = 14,
                    userNotes = "Pagi yang cerah di CFD! Cuaca sejuk dan lari terasa ringan."
                )
            )

            // Sample Ride (3 hari lalu)
            val ridePoints = generateSampleRoute(
                startLat = -6.2297,
                startLng = 106.8295,
                pointCount = 70,
                distanceTotalMeters = 24500.0,
                baseElevation = 24.0
            )
            val rideSplits = (1..24).map { km ->
                SplitData(
                    kilometer = km,
                    durationSeconds = 135L + (km % 4) * 8,
                    paceSecondsPerKm = 135 + (km % 4) * 8,
                    avgSpeedKmh = 25.5 + (km % 5),
                    elevationChangeMeters = 5.0 + (km % 3) * 3
                )
            }

            dao.insertActivity(
                ActivityEntity(
                    title = "Gowes Keliling Jakarta Selatan",
                    sportType = "RIDE",
                    timestamp = now - (dayMillis * 3),
                    durationSeconds = 3420L, // ~57 mins
                    distanceMeters = 24500.0,
                    elevationGainMeters = 185.0,
                    avgSpeedKmh = 25.8,
                    maxSpeedKmh = 38.4,
                    calories = 620,
                    avgPaceSecondsPerKm = 139,
                    routeJson = RouteSerializer.serializeGpsPoints(ridePoints),
                    splitsJson = RouteSerializer.serializeSplits(rideSplits),
                    kudosCount = 22,
                    userNotes = "Peloton santai bareng teman komunitas, aspal mulus dan angin tenang."
                )
            )
        }

        private fun generateSampleRoute(
            startLat: Double,
            startLng: Double,
            pointCount: Int,
            distanceTotalMeters: Double,
            baseElevation: Double
        ): List<GpsPoint> {
            val list = mutableListOf<GpsPoint>()
            var currLat = startLat
            var currLng = startLng
            var currAlt = baseElevation
            val now = System.currentTimeMillis()

            for (i in 0 until pointCount) {
                val angle = (i.toDouble() / pointCount) * 2.0 * Math.PI
                // loop circuit
                val latOffset = Math.sin(angle) * 0.008 + Math.cos(angle * 2) * 0.003
                val lngOffset = Math.cos(angle) * 0.012 + Math.sin(angle * 2) * 0.002
                currLat = startLat + latOffset
                currLng = startLng + lngOffset
                currAlt = baseElevation + Math.sin(angle * 3) * 12.0 + (i % 5)

                list.add(
                    GpsPoint(
                        latitude = currLat,
                        longitude = currLng,
                        altitude = currAlt,
                        speedMps = 3.2f + (i % 4) * 0.4f,
                        timestamp = now + (i * 30_000L)
                    )
                )
            }
            return list
        }
    }
}

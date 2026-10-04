package com.example

import com.example.data.model.FriendUser
import com.example.data.model.SplitData
import com.example.data.model.SportType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testSportType_properties() {
        assertEquals("Lari", SportType.RUN.title)
        assertEquals("Bersepeda", SportType.RIDE.title)
        assertTrue(SportType.RUN.isPacePrimary)
        assertEquals(false, SportType.RIDE.isPacePrimary)
        assertEquals(SportType.RUN, SportType.fromString("run"))
        assertEquals(SportType.RIDE, SportType.fromString("ride"))
    }

    @Test
    fun testSplitData_formatting() {
        val split = SplitData(
            kilometer = 1,
            durationSeconds = 315,
            paceSecondsPerKm = 315,
            avgSpeedKmh = 11.4,
            elevationChangeMeters = 2.5
        )
        assertEquals("5:15", split.formatPace())
        assertEquals("5:15", split.formatDuration())
    }

    @Test
    fun testFriendUser_computations() {
        val friend = FriendUser(
            id = "f_1",
            name = "Budi Santoso",
            username = "@budi",
            avatarColorHex = 0xFF00D2D3,
            weeklyRunKm = 25.5,
            weeklyRideKm = 40.0,
            weeklyActivityCount = 4
        )
        assertEquals(65.5, friend.totalWeeklyKm, 0.001)
        assertEquals("BS", friend.initials)
    }
}

package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.RouteSerializer
import com.example.data.model.GpsPoint
import com.example.data.model.SplitData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Velorun: Lari & Sepeda", appName)
    }

    @Test
    fun `test route serialization in robolectric`() {
        val points = listOf(
            GpsPoint(latitude = -6.2146, longitude = 106.8451, altitude = 15.0, speedMps = 3.2f, timestamp = 1000L),
            GpsPoint(latitude = -6.2150, longitude = 106.8460, altitude = 16.5, speedMps = 3.4f, timestamp = 2000L)
        )
        val json = RouteSerializer.serializeGpsPoints(points)
        assertTrue(json.contains("-6.2146"))

        val deserialized = RouteSerializer.deserializeGpsPoints(json)
        assertEquals(2, deserialized.size)
        assertEquals(-6.2146, deserialized[0].latitude, 0.0001)
    }

    @Test
    fun `test splits serialization in robolectric`() {
        val splits = listOf(
            SplitData(kilometer = 1, durationSeconds = 300, paceSecondsPerKm = 300, avgSpeedKmh = 12.0, elevationChangeMeters = 2.0)
        )
        val json = RouteSerializer.serializeSplits(splits)
        val deserialized = RouteSerializer.deserializeSplits(json)
        assertEquals(1, deserialized.size)
        assertEquals("5:00", deserialized[0].formatPace())
    }
}

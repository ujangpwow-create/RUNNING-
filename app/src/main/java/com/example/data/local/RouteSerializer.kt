package com.example.data.local

import com.example.data.model.GpsPoint
import com.example.data.model.SplitData
import org.json.JSONArray
import org.json.JSONObject

object RouteSerializer {

    fun serializeGpsPoints(points: List<GpsPoint>): String {
        val array = JSONArray()
        for (p in points) {
            val obj = JSONObject()
            obj.put("lat", p.latitude)
            obj.put("lng", p.longitude)
            obj.put("alt", p.altitude)
            obj.put("spd", p.speedMps.toDouble())
            obj.put("time", p.timestamp)
            array.put(obj)
        }
        return array.toString()
    }

    fun deserializeGpsPoints(json: String): List<GpsPoint> {
        if (json.isBlank()) return emptyList()
        val list = mutableListOf<GpsPoint>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    GpsPoint(
                        latitude = obj.optDouble("lat", 0.0),
                        longitude = obj.optDouble("lng", 0.0),
                        altitude = obj.optDouble("alt", 0.0),
                        speedMps = obj.optDouble("spd", 0.0).toFloat(),
                        timestamp = obj.optLong("time", 0L)
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    fun serializeSplits(splits: List<SplitData>): String {
        val array = JSONArray()
        for (s in splits) {
            val obj = JSONObject()
            obj.put("km", s.kilometer)
            obj.put("dur", s.durationSeconds)
            obj.put("pace", s.paceSecondsPerKm)
            obj.put("spd", s.avgSpeedKmh)
            obj.put("elev", s.elevationChangeMeters)
            array.put(obj)
        }
        return array.toString()
    }

    fun deserializeSplits(json: String): List<SplitData> {
        if (json.isBlank()) return emptyList()
        val list = mutableListOf<SplitData>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    SplitData(
                        kilometer = obj.optInt("km", i + 1),
                        durationSeconds = obj.optLong("dur", 0L),
                        paceSecondsPerKm = obj.optInt("pace", 0),
                        avgSpeedKmh = obj.optDouble("spd", 0.0),
                        elevationChangeMeters = obj.optDouble("elev", 0.0)
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }
}

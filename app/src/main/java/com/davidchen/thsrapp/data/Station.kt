package com.davidchen.thsrapp.data

import com.google.android.gms.maps.model.LatLng

class Station {
    lateinit var StationName: Name
    lateinit var StationAddress: String
    lateinit var StationPosition: Position

    class Name {
        val Zh_tw: String = ""
        val En: String = ""
    }

    class Position {
        val PositionLat = 0.0
        val PositionLon = 0.0
    }

    fun getLatLng(): LatLng {
        return LatLng(StationPosition.PositionLat, StationPosition.PositionLon)
    }

    override fun toString(): String {
        return """
            name:${StationName.Zh_tw},
            addr:${StationAddress},
            position:{${StationPosition.PositionLat},${StationPosition.PositionLon}}
        """.trimIndent()
    }
}
package com.davidchen.thsrapp.data.THSR

import com.google.android.gms.maps.model.LatLng
import java.io.Serializable

class Station : Serializable{
    lateinit var StationName: Name
    lateinit var StationID: String
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
            name:{Zh_tw:${StationName.Zh_tw}, En:${StationName.En}},
            addr:${StationAddress},
            position:{${StationPosition.PositionLat},${StationPosition.PositionLon}}
        """.trimIndent()
    }
}
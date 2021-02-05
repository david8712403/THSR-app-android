package com.davidchen.thsrapp.data.THSR

import com.google.android.gms.maps.model.LatLng
import java.io.Serializable

class Shape : Serializable{
    lateinit var LineID: String
    lateinit var LineName: Name
    lateinit var Geometry: String

    class Name : Serializable{
        val Zh_tw: String = ""
        val En: String = ""
    }

    fun getLatLngArr(): ArrayList<LatLng> {
        val rtn = ArrayList<LatLng>()

        // Geometry     ->  "LINESTRING(121.620899799986 25.0545610101825, ... , 121.62083639958 25.0545585802143)"
        val lineString = Geometry.split("(").toTypedArray()[1]
            .split(")").toTypedArray()[0]
        // lineString   ->  "121.620899799986 25.0545610101825,121.62083639958 25.0545585802143"
        val points = lineString.split(",").toTypedArray()
        // points       ->  ["121.620899799986 25.0545610101825"], ... ,["121.62083639958 25.0545585802143"]
        for (p in points) {
            // "121.62083639958 25.0545585802143"
            val position = p.split(" ").toTypedArray()
            // ["25.0545585802143"], ["121.62083639958"]
            rtn.add(LatLng(position[1].toDouble(), position[0].toDouble()))
        }
        return rtn
    }
}
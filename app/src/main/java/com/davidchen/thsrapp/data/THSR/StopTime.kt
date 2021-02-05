package com.davidchen.thsrapp.data.THSR

import java.io.Serializable

class StopTime : Serializable{
    var StopSequence: Int = 0
    lateinit var StationID: String
    lateinit var StationName: Station.Name
    lateinit var ArrivalTime: String
    lateinit var DepartureTime: String
}
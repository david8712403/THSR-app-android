package com.davidchen.thsrapp.data.THSR

import java.io.Serializable

class TrainInfo : Serializable{
    lateinit var TrainNo: String
    var Direction: Int = 0
    lateinit var StartingStationID: String
    lateinit var StartingStationName: Station.Name
    lateinit var EndingStationID: String
    lateinit var EndingStationName: Station.Name
}
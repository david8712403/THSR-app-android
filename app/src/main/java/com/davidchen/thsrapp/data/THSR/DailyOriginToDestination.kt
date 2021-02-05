package com.davidchen.thsrapp.data.THSR

import java.io.Serializable

class DailyOriginToDestination : Serializable{
    lateinit var TrainDate: String
    lateinit var DailyTrainInfo: TrainInfo
    lateinit var OriginStopTime: StopTime
    lateinit var DestinationStopTime: StopTime
    lateinit var UpdateTime: String
}
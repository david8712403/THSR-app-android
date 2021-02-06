package com.davidchen.thsrapp.data.THSR

import java.io.Serializable

class DailyTrainStopTime: Serializable {

    lateinit var TrainDate: String
    lateinit var DailyTrainInfo: TrainInfo
    lateinit var StopTimes: Array<StopTime>
    lateinit var UpdateTime: String

}
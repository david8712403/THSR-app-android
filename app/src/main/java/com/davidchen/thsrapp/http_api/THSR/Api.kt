package com.davidchen.thsrapp.http_api.THSR

import java.text.SimpleDateFormat
import java.util.*

class Api {

    /**
     * GetStation
     * HTTP API
     * GET /v2/Rail/THSR/Station
     */
    class GetStation : ApiBuilder() {
        init {
            baseUrlBuilder
                    .addPathSegment("Station")
        }
    }

    /**
     * GetShape
     * HTTP API
     * GET /v2/Rail/THSR/Shape
     */
    class GetShape : ApiBuilder() {
        init {
            baseUrlBuilder
                    .addPathSegment("Shape")
        }
    }

    /**
     * GetDailyTimetable
     * HTTP API
     * GET /v2/Rail/THSR/DailyTimetable/OD/{OriginStationID}/to/{DestinationStationID}/{TrainDate}
     */
    class GetDailyTimetable() : ApiBuilder() {
        constructor(
                OriginStationID: String,
                DestinationStationID: String,
                TrainDate: Date
        ) : this() {
            val strPatten = "yyyy-MM-dd"
            val mFormat = SimpleDateFormat(strPatten)

            baseUrlBuilder
                    .addPathSegment("DailyTimetable")
                    .addPathSegment("OD")
                    .addPathSegment(OriginStationID)
                    .addPathSegment("to")
                    .addPathSegment(DestinationStationID)
                    .addPathSegment(mFormat.format(TrainDate))
        }
    }

    /**
     * GetDailyTrainInfo
     * HTTP API
     * GET /v2/Rail/THSR/DailyTrainInfo/Today/TrainNo/{TrainNo}
     */
    class GetDailyTrainInfo() : ApiBuilder() {
        constructor(trainNo: String) : this() {
            baseUrlBuilder
                    .addPathSegment("DailyTrainInfo")
                    .addPathSegment("Today")
                    .addPathSegment("TrainNo")
                    .addPathSegment(trainNo)
        }
    }

    /**
     * GetDailyTimetable
     * HTTP API
     * GET /v2/Rail/THSR/DailyTimetable/Today/TrainNo/{TrainNo}
     */
    class GetDailyTrainStopTime() : ApiBuilder() {
        constructor(trainNo: String) : this() {
            baseUrlBuilder
                .addPathSegment("DailyTimetable")
                .addPathSegment("Today")
                .addPathSegment("TrainNo")
                .addPathSegment(trainNo)
        }
    }
}
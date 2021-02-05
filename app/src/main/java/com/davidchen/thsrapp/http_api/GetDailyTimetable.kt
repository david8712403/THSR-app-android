package com.davidchen.thsrapp.http_api

import java.text.SimpleDateFormat
import java.util.*

class GetDailyTimetable() : THSR() {

    // GET -> /v2/Rail/THSR/DailyTimetable/OD/{OriginStationID}/to/{DestinationStationID}/{TrainDate}

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

package com.davidchen.thsrapp.http_api

class GetDailyTrainInfo() : THSR() {

    // GET -> /v2/Rail/THSR/DailyTrainInfo/Today/TrainNo/{TrainNo}

    constructor(trainNo: String) : this() {
        baseUrlBuilder
                .addPathSegment("DailyTrainInfo")
                .addPathSegment("Today")
                .addPathSegment("TrainNo")
                .addPathSegment(trainNo)
    }

}
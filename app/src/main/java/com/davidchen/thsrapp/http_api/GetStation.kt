package com.davidchen.thsrapp.http_api

class GetStation : THSR() {

    // GET -> /v2/Rail/THSR/Station

    init {
        baseUrlBuilder
            .addPathSegment("Station")
    }
}
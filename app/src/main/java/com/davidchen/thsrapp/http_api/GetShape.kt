package com.davidchen.thsrapp.http_api

class GetShape : THSR() {

    // GET -> /v2/Rail/THSR/Shape

    init {
        baseUrlBuilder
                .addPathSegment("Shape")
    }
}
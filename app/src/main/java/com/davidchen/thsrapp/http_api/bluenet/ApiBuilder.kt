package com.davidchen.thsrapp.http_api.bluenet

import android.util.Log
import okhttp3.HttpUrl
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Request
import okhttp3.RequestBody

abstract class ApiBuilder {

    var baseUrlBuilder: HttpUrl.Builder = HttpUrl.Builder()
        .scheme("https")
        .host("api.bluenet-ride.com")
        .addPathSegment("v2_0")
        .addPathSegment("lineBot")
    lateinit var httpUrl: HttpUrl
    private var body = RequestBody.create("application/json; charset=utf-8".toMediaTypeOrNull(), "{}")

    companion object {
        const val TAG = "bluenet"
    }

    fun getRequest(): Request {
        httpUrl = baseUrlBuilder.build()
        Log.d("Request", httpUrl.toString())

        return Request.Builder()
            .url(httpUrl)
            .addHeader("Content-Type", "application/json")
            .post(body)
            .build()
    }

    fun setBody(body: RequestBody) {
        this.body = body
    }
}
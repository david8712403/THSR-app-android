package com.davidchen.thsrapp.http_api.bluenet

import android.util.Log
import okhttp3.MediaType
import okhttp3.RequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

class Api {
    class GetRestaurant(count: Int, lat: Double, lng: Double, range: Int) : ApiBuilder() {
        init {
            val jsonObj = JSONObject()
            jsonObj.put("lastIndex", -1)
            jsonObj.put("count", count)
            jsonObj.put("type", JSONArray().put(7))
            jsonObj.put("lat", lat)
            jsonObj.put("lng", lng)
            jsonObj.put("range", range)
            baseUrlBuilder
                .addPathSegment("restaurant")
                .addPathSegment("get")
            val requestBody =
                RequestBody.create(MediaType.parse(
                    "application/json; charset=utf-8"),
                    jsonObj.toString())
            setBody(requestBody)
        }
    }
}
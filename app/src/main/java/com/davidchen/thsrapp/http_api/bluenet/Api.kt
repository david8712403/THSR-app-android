package com.davidchen.thsrapp.http_api.bluenet

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

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

            val requestBody = jsonObj
                .toString()
                .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

            setBody(requestBody)
        }
    }
}
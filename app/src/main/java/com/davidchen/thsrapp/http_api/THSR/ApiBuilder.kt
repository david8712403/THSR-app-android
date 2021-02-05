package com.davidchen.thsrapp.http_api.THSR

import android.util.Base64
import com.davidchen.thsrapp.BuildConfig.*
import com.davidchen.thsrapp.Util.Companion.getServerTime
import okhttp3.HttpUrl
import okhttp3.Request
import java.security.SignatureException
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

abstract class ApiBuilder {

    // base -> https://ptx.transportdata.tw/MOTC/v2/Rail/THSR/
    var baseUrlBuilder: HttpUrl.Builder = HttpUrl.Builder()
            .scheme("https")
            .host("ptx.transportdata.tw")
            .addPathSegment("MOTC")
            .addPathSegment("v2")
            .addPathSegment("Rail")
            .addPathSegment("THSR")
    lateinit var httpUrl: HttpUrl

    // query
    var select: String? = null
    var filter: String? = null
    var orderby: String? = null
    var top: Int? = null
    var skip: Int? = null

    companion object {
        const val TAG = "THSR"
    }

    fun select(s: String): HttpUrl.Builder {
        select = s
        baseUrlBuilder.addQueryParameter("select", select)
        return this.baseUrlBuilder
    }

    fun filter(s: String): HttpUrl.Builder {
        filter = s
        baseUrlBuilder.addQueryParameter("filter", filter)
        return this.baseUrlBuilder
    }

    fun orderby(s: String): HttpUrl.Builder {
        orderby = s
        baseUrlBuilder.addQueryParameter("orderby", orderby)
        return this.baseUrlBuilder
    }

    fun top(i: Int): HttpUrl.Builder {
        top = i
        baseUrlBuilder.addQueryParameter("top", top.toString())
        return this.baseUrlBuilder
    }

    fun skip(i: Int): HttpUrl.Builder {
        skip = i
        baseUrlBuilder.addQueryParameter("skip", skip.toString())
        return this.baseUrlBuilder
    }

    fun getRequest(): Request {
        httpUrl = baseUrlBuilder
                .addQueryParameter("format","JSON").build()
        return Request.Builder()
            .url(httpUrl)
            .addHeader("Authorization", getAuthString())
            .addHeader("x-date", getServerTime())
            .build()
    }

    private fun getAuthString(): String {
        val appId = MOTC_APP_ID
        val appKey = MOTC_APP_KEY

        var sign = ""
        try {
            // get signature
            sign = signature("x-date: ${getServerTime()}", appKey)!!
        } catch (e1: SignatureException) {
            e1.printStackTrace()
        }

        return "hmac username=\"$appId\", " +
                "algorithm=\"hmac-sha1\", " +
                "headers=\"x-date\", " +
                "signature=\"$sign\""
    }

    private fun signature(xData: String, AppKey: String): String? {
        return try {
            // get an hmac_sha1 key from the raw key bytes
            val signingKey = SecretKeySpec(AppKey.toByteArray(charset("UTF-8")), "HmacSHA1")

            // get an hmac_sha1 Mac instance and initialize with the signing key
            val mac: Mac = Mac.getInstance("HmacSHA1")
            mac.init(signingKey)

            // compute the hmac on input data bytes
            val rawHmac: ByteArray = mac.doFinal(xData.toByteArray(charset("UTF-8")))

            Base64.encodeToString(rawHmac, Base64.NO_WRAP)
        } catch (e: Exception) {
            throw SignatureException("Failed to generate HMAC : " + e.message)
        }
    }
}
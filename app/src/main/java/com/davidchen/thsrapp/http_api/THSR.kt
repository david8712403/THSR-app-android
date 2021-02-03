package com.davidchen.thsrapp.http_api

import android.util.Base64
import com.davidchen.thsrapp.BuildConfig.*
import com.davidchen.thsrapp.Util.Companion.getServerTime
import okhttp3.Request
import java.security.SignatureException
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

class THSR {

    companion object {
        val TAG = "THSR"

        fun getRequest(api: APIs): Request {
            return Request.Builder().url(api.url)
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

    // THSR APIs
    enum class APIs(val url: String){
        STATION("https://ptx.transportdata.tw/MOTC/v2/Rail/THSR/Station?\$format=JSON"),
        SHAPE("https://ptx.transportdata.tw/MOTC/v2/Rail/THSR/Shape?\$format=JSON")
    }
}
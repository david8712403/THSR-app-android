package com.davidchen.thsrapp

import java.text.SimpleDateFormat
import java.util.*

class Util {

    companion object {
        fun getServerTime(): String {
            val calendar = Calendar.getInstance()
            val dateFormat = SimpleDateFormat(
                "EEE, dd MMM yyyy HH:mm:ss z", Locale.US)
            dateFormat.timeZone = TimeZone.getTimeZone("GMT")
            return dateFormat.format(calendar.time)
        }
    }
}
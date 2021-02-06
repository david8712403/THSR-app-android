package com.davidchen.thsrapp.data.bluenet

import java.io.Serializable

class RawDataRestaurant : Serializable{
    lateinit var results: Result

    class Result {
        lateinit var content: Array<Restaurant>
    }
}
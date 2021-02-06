package com.davidchen.thsrapp.data.bluenet

import java.io.Serializable

class Restaurant : Serializable {
    var lat: Double = 0.0
    var lng: Double = 0.0
    lateinit var name: String
    var rating: Float = 0f
    lateinit var vicinity: String
    lateinit var photo: String
    var reviewsNumber: Int = 0
}
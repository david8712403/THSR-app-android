package com.davidchen.thsrapp

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.davidchen.thsrapp.data.THSR.Shape
import com.davidchen.thsrapp.data.THSR.Station
import com.davidchen.thsrapp.http_api.THSR
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.gson.Gson
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Response
import java.io.IOException


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private val REQUEST_PERMISSIONS = 1
    private lateinit var mMap: GoogleMap

    private lateinit var stations: Array<Station>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            )
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_PERMISSIONS
            )
        } else {
            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            val mapFragment = supportFragmentManager
                    .findFragmentById(R.id.map) as SupportMapFragment
            mapFragment.getMapAsync(this)
        }
        sendRequest()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (grantResults.isEmpty()) {
            return
        }
        when (requestCode) {
            REQUEST_PERMISSIONS -> {
                for (result in grantResults) {
                    if (result != PackageManager.PERMISSION_GRANTED) {
                        finish()
                    } else {
                        val map =
                            supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
                        map.getMapAsync(this)
                    }
                }
            }
        }
    }

    private fun sendRequest() {
        val req = THSR.getRequest(THSR.APIs.STATION)

        OkHttpClient().newCall(req).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                TODO("Not yet implemented")
            }

            override fun onResponse(call: Call, response: Response) {
                val json = response.body()?.string()
                if (json != null) {
                    Log.d(THSR.TAG, json)
                    stations = Gson().fromJson(json, Array<Station>::class.java)
                    for (s in stations) {
                        Log.d("[StationInfo]", s.toString())
                        val marker = MarkerOptions()
                        marker.apply {
                            position(s.getLatLng())
                            title(s.StationName.Zh_tw)
                        }
                        runOnUiThread {
                            mMap.addMarker(marker)
                        }
                    }
                }
            }

        })

        val reqShape = THSR.getRequest(THSR.APIs.SHAPE)
        OkHttpClient().newCall(reqShape).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                TODO("Not yet implemented")
            }

            override fun onResponse(call: Call, response: Response) {
                val json = response.body()?.string()
                val line = Gson().fromJson(json, Array<Shape>::class.java)
                runOnUiThread {
                    val polyline =
                        mMap.addPolyline(PolylineOptions().addAll(line[0].getLatLngArr()))
                    polyline.width = 10f
                }
            }

        })
    }

    override fun onMapReady(map: GoogleMap) {

        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED) {
            return
        }

        mMap = map
        mMap.isMyLocationEnabled = true

        val locCenter = LatLng(23.6, 120.973882)
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(locCenter, 8f))
        findViewById<Button>(R.id.bt_view_all).setOnClickListener {
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(locCenter, 8f))
        }
    }
}
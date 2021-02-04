package com.davidchen.thsrapp

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.transition.TransitionInflater
import android.util.Log
import android.view.animation.Animation
import android.widget.Button
import android.widget.FrameLayout
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.davidchen.thsrapp.data.THSR.Shape
import com.davidchen.thsrapp.data.THSR.Station
import com.davidchen.thsrapp.fragment.StationFragment
import com.davidchen.thsrapp.http_api.THSR
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
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

    private lateinit var flFragment: FrameLayout
    private lateinit var btSearchStation: Button
    private lateinit var mMap: GoogleMap
    private val mapMarkers = ArrayList<Marker>()

    private lateinit var stations: Array<Station>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        initUi()

        supportFragmentManager
            .setFragmentResultListener(StationFragment.REQUEST_KEY, this) { _, bundle ->
                val id = bundle.getString("stationId")
                Log.d(this.javaClass.simpleName, "stationFragment: stationId -> $id")
                for (m in mapMarkers) {
                    if (m.tag == id) {
                        m.showInfoWindow()
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(m.position, 10f))
                    }
                }
            }

        btSearchStation.setOnClickListener {
            val f = StationFragment.newInstance(stations)

            supportFragmentManager.beginTransaction()
                .setCustomAnimations(R.anim.enter_from_bottom, R.anim.exit_to_bottom, R.anim.enter_from_bottom, R.anim.exit_to_bottom)
                .add(R.id.root_constraint, f).addToBackStack(f.javaClass.name)
                .commit()
        }

        sendRequest()
    }

    private fun initUi() {
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
        flFragment = findViewById(R.id.fl_fragment)
        btSearchStation = findViewById(R.id.bt_search_station)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
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
                    mapMarkers.clear()
                    stations = Gson().fromJson(json, Array<Station>::class.java)
                    for (s in stations) {
                        Log.d("[StationInfo]", s.toString())
                        val marker = MarkerOptions()
                        marker.apply {
                            position(s.getLatLng())
                            title(s.StationName.Zh_tw)
                        }
                        runOnUiThread {
                            val m = mMap.addMarker(marker).apply {
                                tag = s.StationID
                            }
                            mapMarkers.add(m)
                        }
                    }
                    runOnUiThread { btSearchStation.isEnabled = true }
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
        mMap.setOnMarkerClickListener { m ->
//            val index = m.tag as Int
//            // TODO("implement by Bottom Sheet Dialog Fragment")
//            val popupMenu = PopupMenu(this, findViewById(R.id.v_popup_anchor))
//            popupMenu.menuInflater.inflate(R.menu.popup_menu, popupMenu.menu)
//            popupMenu.menu.findItem(R.id.station_name).title = stations[m.tag as Int].StationName.Zh_tw
//            popupMenu.menu.findItem(R.id.station_name).isEnabled = false
//
//            popupMenu.setOnMenuItemClickListener{ item ->
//                when(item.itemId) {
//                    R.id.station_name -> {
//                        // nothing
//                    }
//                    R.id.start_station -> {
//                        findViewById<TextView>(R.id.tv_start_station).text =
//                                stations[index].StationName.Zh_tw
//                    }
//                    R.id.end_station -> {
//                        findViewById<TextView>(R.id.tv_end_station).text =
//                                stations[index].StationName.Zh_tw
//                    }
//                    else -> { }
//                }
//                true
//            }
//            popupMenu.show()
//            m.showInfoWindow()

            true
        }
    }
}
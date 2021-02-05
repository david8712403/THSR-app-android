package com.davidchen.thsrapp

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.davidchen.thsrapp.data.THSR.Shape
import com.davidchen.thsrapp.data.THSR.Station
import com.davidchen.thsrapp.fragment.MenuDialogFragment
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
    private lateinit var btSwap: Button
    private lateinit var mMap: GoogleMap
    private val mapMarkers = ArrayList<Marker>()

    private lateinit var stations: Array<Station>
    private var startStation: Station? = null
    private var endStation: Station? = null

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        // set orientaion to protrait.
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        initUi()

        supportFragmentManager
            .setFragmentResultListener(StationFragment.REQUEST_KEY, this) { _, bundle ->
                val s = bundle.getSerializable("station") as Station?
                val op = bundle.getString("operation")
                if(s != null) {
                    val operation = bundle.getString("operation")
                    Log.d(this.javaClass.simpleName, "stationFragment: $operation -> ${s.StationName.Zh_tw}")
                    when(op) {
                        "setAsStart" -> {
                            findViewById<TextView>(R.id.tv_start_station).text =
                                "${s.StationName.Zh_tw}${getString(R.string.hsr_station)}"
                            startStation = s
                        }
                        "setAsEnd" -> {
                            findViewById<TextView>(R.id.tv_end_station).text =
                                "${s.StationName.Zh_tw}${getString(R.string.hsr_station)}"
                            endStation = s
                        }
                        "moveCamera" -> {
                            for (m in mapMarkers) {
                                if ((m.tag as Station) == s) {
                                    m.showInfoWindow()
                                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(m.position, 10f))
                                }
                            }
                        }
                        else -> {
                            Log.e("FragmentResult", "unknown operation")
                        }
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

        btSwap.setOnClickListener {
            if (startStation != null && endStation != null) {
                val temp = startStation
                startStation = endStation
                endStation = temp
                findViewById<TextView>(R.id.tv_start_station).text =
                    "${startStation!!.StationName.Zh_tw}${getString(R.string.hsr_station)}"
                findViewById<TextView>(R.id.tv_end_station).text =
                    "${endStation!!.StationName.Zh_tw}${getString(R.string.hsr_station)}"
            }
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
        btSwap = findViewById(R.id.bt_swap)
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
                                tag = s
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
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(locCenter, 7.8f))
        findViewById<Button>(R.id.bt_view_all).setOnClickListener {
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(locCenter, 7.8f))
        }
        mMap.setOnMarkerClickListener { m ->
            // TODO("implement by Bottom Sheet Dialog Fragment")
            MenuDialogFragment.newInstance(
                resources.getStringArray(R.array.menu),
                m.tag as Station,
                startStation,
                endStation
            )
                .show(supportFragmentManager, "dialog")
            m.showInfoWindow()

            true
        }
    }
}
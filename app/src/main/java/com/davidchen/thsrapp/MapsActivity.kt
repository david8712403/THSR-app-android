package com.davidchen.thsrapp

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.davidchen.ProgressDialogUtil
import com.davidchen.thsrapp.data.THSR.DailyOriginToDestination
import com.davidchen.thsrapp.data.THSR.Shape
import com.davidchen.thsrapp.data.THSR.Station
import com.davidchen.thsrapp.fragment.MenuDialogFragment
import com.davidchen.thsrapp.fragment.PathFragment
import com.davidchen.thsrapp.fragment.StationFragment
import com.davidchen.thsrapp.http_api.THSR.*
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
import java.util.*
import kotlin.collections.ArrayList


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private val REQUEST_PERMISSIONS = 1

    private lateinit var flFragment: FrameLayout
    private lateinit var btSearchStation: Button
    private lateinit var btSearchPath: Button
    private lateinit var btSwap: ImageButton
    private lateinit var mMap: GoogleMap
    private val mapMarkers = ArrayList<Marker>()

    private lateinit var stations: Array<Station>
    private var originStation: Station? = null
    private var destinationStation: Station? = null

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        // set orientation to portrait.
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
            sendRequest()
        }

        initUi()
        ProgressDialogUtil.showProgressDialog(this, "init")

        supportFragmentManager
            .setFragmentResultListener(StationFragment.REQUEST_KEY, this) { _, bundle ->
                val s = bundle.getSerializable("station") as Station?
                val op = bundle.getString("operation")
                if(s != null) {
                    val operation = bundle.getString("operation")
                    Log.d(this.javaClass.simpleName, "stationFragment: $operation -> ${s.StationName.Zh_tw}")
                    when(op) {
                        "setAsOrigin" -> {
                            findViewById<TextView>(R.id.tv_origin_station).text =
                                "${s.StationName.Zh_tw}${getString(R.string.hsr_station)}"
                            originStation = s
                        }
                        "setAsDestination" -> {
                            findViewById<TextView>(R.id.tv_destination_station).text =
                                "${s.StationName.Zh_tw}${getString(R.string.hsr_station)}"
                            destinationStation = s
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
                    if (originStation != null && destinationStation != null)
                        btSearchPath.isEnabled = true
                }
            }

        btSearchStation.setOnClickListener {
            val s = ArrayList<Station>()
            s.addAll(stations)
            StationFragment.newInstance(s).show(supportFragmentManager, "dialog")
        }

        // Click search button
        btSearchPath.setOnClickListener {

            // if one of origin or destination station is null, return
            if (originStation == null || destinationStation == null) {
                return@setOnClickListener
            }

            // Show progress dialog
            ProgressDialogUtil.showProgressDialog(this,
                "Get ${originStation!!.StationName.Zh_tw} -> ${destinationStation!!.StationName.Zh_tw}")

            // Call GET DailyTimetable
            val reqDailyTimetable = Api.GetDailyTimetable(
                    originStation!!.StationID,
                    destinationStation!!.StationID,
                    Calendar.getInstance().time
            ).orderby("OriginStopTime/DepartureTime").getRequest()
            OkHttpClient().newCall(reqDailyTimetable).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    ProgressDialogUtil.dismiss()
                    e.message?.let { createFailureDialog(it) }
                }

                override fun onResponse(call: Call, response: Response) {
                    ProgressDialogUtil.dismiss()
                    val json = response.body?.string()
                    if (json != null) {
                        Log.d("${ApiBuilder.TAG}:GetDailyTimetable", json)
                        val paths = Gson().fromJson(json, Array<DailyOriginToDestination>::class.java)
                        val f = PathFragment.newInstance(
                            paths,
                            originStation!!,
                            destinationStation!!
                        )
                        supportFragmentManager.beginTransaction()
                            .setCustomAnimations(
                                R.anim.enter_from_right,
                                R.anim.exit_to_right,
                                R.anim.enter_from_right,
                                R.anim.exit_to_right
                            )
                            .add(R.id.root_constraint, f).addToBackStack(f.javaClass.name)
                            .commit()
                    }else {
                        Log.e("${ApiBuilder.TAG}:GetDailyTimetable", "empty")
                    }
                }

            })
        }

        // click swap button, origin <--> destination
        btSwap.setOnClickListener {
            if (originStation != null && destinationStation != null) {
                val temp = originStation
                originStation = destinationStation
                destinationStation = temp
                findViewById<TextView>(R.id.tv_origin_station).text =
                    "${originStation!!.StationName.Zh_tw}${getString(R.string.hsr_station)}"
                findViewById<TextView>(R.id.tv_destination_station).text =
                    "${destinationStation!!.StationName.Zh_tw}${getString(R.string.hsr_station)}"
            }
        }
    }

    private fun initUi() {
        flFragment = findViewById(R.id.fl_fragment)
        btSearchStation = findViewById(R.id.bt_search_station)
        btSearchPath = findViewById(R.id.bt_search_path)
        btSwap = findViewById(R.id.bt_swap)
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount == 0 ) {
            finish()
            if (ProgressDialogUtil.mAlertDialog?.isShowing == false) {
                ProgressDialogUtil.mAlertDialog = null
            }
        }else {
            supportFragmentManager.popBackStack()
        }
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
                        sendRequest()
                    }
                }
            }
        }
    }

    private fun sendRequest() {
        ProgressDialogUtil.showProgressDialog(this, "GetStation")
        val req = Api.GetStation().getRequest()

        OkHttpClient().newCall(req).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.message?.let { createFailureDialog(it) }
            }

            override fun onResponse(call: Call, response: Response) {
                val json = response.body?.string()
                if (json != null) {
                    Log.d(ApiBuilder.TAG, json)
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

        ProgressDialogUtil.showProgressDialog(this, "GetShape")
        val reqShape = Api.GetShape().getRequest()
        OkHttpClient().newCall(reqShape).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                ProgressDialogUtil.dismiss()
                e.message?.let { createFailureDialog(it) }
            }

            override fun onResponse(call: Call, response: Response) {
                ProgressDialogUtil.dismiss()
                val json = response.body?.string()
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
            MenuDialogFragment.newInstance(
                resources.getStringArray(R.array.menu),
                m.tag as Station,
                originStation,
                destinationStation
            ).show(supportFragmentManager, "dialog")
            m.showInfoWindow()

            true
        }
    }

    private fun createFailureDialog(msg: String) {
        runOnUiThread {
            val builder = AlertDialog.Builder(this)
            val alert = builder
                .setTitle("Error")
                .setMessage(msg)
                .setPositiveButton(R.string.retry, object : DialogInterface.OnClickListener {
                    override fun onClick(dialog: DialogInterface?, which: Int) {
                        dialog?.dismiss()
                        sendRequest()
                    }
                })
                .create()
            alert.show()
        }
    }
}
package com.davidchen.thsrapp.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.davidchen.ProgressDialogUtil
import com.davidchen.thsrapp.R
import com.davidchen.thsrapp.data.THSR.DailyOriginToDestination
import com.davidchen.thsrapp.data.THSR.DailyTrainStopTime
import com.davidchen.thsrapp.data.THSR.Station
import com.davidchen.thsrapp.http_api.THSR.Api
import com.davidchen.thsrapp.http_api.THSR.ApiBuilder
import com.google.gson.Gson
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Response
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

private lateinit var paths: Array<DailyOriginToDestination>

/**
 * A simple [Fragment] subclass.
 * Use the [StationFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PathFragment : Fragment() {

    // ui
    private lateinit var v: View
    private lateinit var rvPath: RecyclerView

    private lateinit var adapter: PathAdapter

    private lateinit var origin: Station
    private lateinit var destination: Station

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_path, container, false)

        paths = arguments?.getSerializable("path") as Array<DailyOriginToDestination>
        if (paths.isEmpty()) {
            Log.e(this.javaClass.name, "paths empty")
        }
        origin = arguments?.getSerializable("origin") as Station
        destination = arguments?.getSerializable("destination") as Station

        initUi()
        adapter = PathAdapter(paths)
        adapter.callback = object : PathAdapter.Callback {
            override fun onClick(path: DailyOriginToDestination) {
                ProgressDialogUtil.showProgressDialog(
                    requireContext(),
                    "Get train No.${path.DailyTrainInfo.TrainNo} detail")
                val reqDailyTrainStopTime = Api.GetDailyTrainStopTime(
                    path.DailyTrainInfo.TrainNo
                ).getRequest()
                OkHttpClient().newCall(reqDailyTrainStopTime).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        ProgressDialogUtil.dismiss()
                        e.message?.let { createFailureDialog(it) }
                    }

                    override fun onResponse(call: Call, response: Response) {
                        ProgressDialogUtil.dismiss()
                        val json = response.body?.string()
                        if (json != null) {
                            Log.d("${ApiBuilder.TAG}:GetTrainStopTime", json)
                            val train = Gson().fromJson(json, Array<DailyTrainStopTime>::class.java)[0]
                            val f = StopTimeFragment.newInstance(train, origin, destination)
                            parentFragmentManager.beginTransaction()
                                .setCustomAnimations(
                                    R.anim.enter_from_right,
                                    R.anim.exit_to_right,
                                    R.anim.enter_from_right,
                                    R.anim.exit_to_right
                                )
                                .add(R.id.root_constraint, f).addToBackStack(f.javaClass.name)
                                .commit()
                        }else {
                            Log.e("${ApiBuilder.TAG}:GetTrainStopTime", "empty")
                        }
                    }
                })
            }
        }
        rvPath.adapter = adapter

        return v
    }

    private fun initUi() {
        val linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        rvPath = v.findViewById(R.id.rv_path)
        rvPath.layoutManager = linearLayoutManager

        // set station title
        v.findViewById<TextView>(R.id.origin_station).text = origin.StationName.Zh_tw
        v.findViewById<TextView>(R.id.destination_station).text = destination.StationName.Zh_tw
    }

    private fun createFailureDialog(msg: String) {
        Looper.prepare()
        AlertDialog.Builder(this.requireContext())
            .setTitle("Error")
            .setMessage(msg)
            .show()
        Looper.loop()
    }

    companion object {
        val REQUEST_KEY = this.javaClass.simpleName
        @JvmStatic
        fun newInstance(
            paths: Array<DailyOriginToDestination>,
            origin: Station,
            destination: Station) =
                PathFragment().apply {
                    arguments = Bundle().apply {
                        putSerializable("path", paths)
                        putSerializable("origin", origin)
                        putSerializable("destination", destination)
                    }
                }
    }

    class PathAdapter(private val path: Array<DailyOriginToDestination>):
        RecyclerView.Adapter<PathAdapter.ViewHolder>() {

        var callback: Callback? = null

        lateinit var v: View

        class ViewHolder(v: View): RecyclerView.ViewHolder(v) {
            val tvTrainDirection = v.findViewById<TextView>(R.id.tv_train_direction)
            val tvTrainNo = v.findViewById<TextView>(R.id.tv_train_no)
            val tvDepartureTime = v.findViewById<TextView>(R.id.tv_departure_time)
            val tvDuration = v.findViewById<TextView>(R.id.tv_duration)
            val tvArrivalTime = v.findViewById<TextView>(R.id.tv_arrival_time)
        }

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): ViewHolder {
            v = LayoutInflater.from(parent.context).inflate(R.layout.item_path, parent, false)
            return ViewHolder(v)
        }

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: PathAdapter.ViewHolder, position: Int) {
            val path = paths[position]
            holder.tvTrainDirection.text = if (path.DailyTrainInfo.Direction == 0) {
                holder.itemView.context.getString(R.string.south)
            } else {
                holder.itemView.context.getString(R.string.north)
            }

            val df = SimpleDateFormat("HH:mm", Locale.ROOT)
            val departureTime = df.parse(path.OriginStopTime.DepartureTime)!!.time
            val arrivalTime = df.parse(path.DestinationStopTime.ArrivalTime)!!.time
            val delta = (arrivalTime - departureTime).toInt().div(60 * 1000)
            val hour = delta.div(60).toString()
            val minute = if (delta.rem(60) < 10) {
                "0${delta.rem(60)}"
            }else {
                "${delta.rem(60)}"
            }
            holder.tvDuration.text = "$hour:$minute"

            holder.tvTrainNo.text = path.DailyTrainInfo.TrainNo
            holder.tvDepartureTime.text = path.OriginStopTime.DepartureTime
            holder.tvArrivalTime.text = path.DestinationStopTime.ArrivalTime
            holder.itemView.setOnClickListener {
                callback?.onClick(paths[position])
            }
        }

        override fun getItemCount(): Int {
            return paths.size
        }

        interface Callback {
            fun onClick(path: DailyOriginToDestination)
        }
    }
}
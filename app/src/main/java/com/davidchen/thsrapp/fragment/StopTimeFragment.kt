package com.davidchen.thsrapp.fragment

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.davidchen.thsrapp.R
import com.davidchen.thsrapp.data.THSR.DailyTrainStopTime
import com.davidchen.thsrapp.data.THSR.Station
import com.davidchen.thsrapp.data.THSR.StopTime

private lateinit var dailyTimetable: DailyTrainStopTime
private lateinit var origin: Station
private lateinit var destination: Station

/**
 * A simple [Fragment] subclass.
 * Use the [StopTimeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class StopTimeFragment : Fragment() {

    // ui
    private lateinit var v: View
    private lateinit var rvPath: RecyclerView

    private lateinit var adapter: StopTimeAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_stop_time, container, false)

        dailyTimetable = arguments?.getSerializable("stopTime") as DailyTrainStopTime
        origin = arguments?.getSerializable("origin") as Station
        destination = arguments?.getSerializable("destination") as Station

        initUi()
        adapter = StopTimeAdapter(dailyTimetable.StopTimes)
        rvPath.adapter = adapter

        return v
    }

    @SuppressLint("SetTextI18n")
    private fun initUi() {
        val linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        rvPath = v.findViewById(R.id.rv_path)
        rvPath.layoutManager = linearLayoutManager

        // set station title
        v.findViewById<TextView>(R.id.tv_station_id).text =
            "${dailyTimetable.DailyTrainInfo.TrainNo} ${getString(R.string.timetable)}"
    }

    companion object {
        val REQUEST_KEY = this.javaClass.simpleName
        @JvmStatic
        fun newInstance(
            timetable: DailyTrainStopTime,
            origin: Station,
            destination: Station) =
                StopTimeFragment().apply {
                    arguments = Bundle().apply {
                        putSerializable("stopTime", timetable)
                        putSerializable("origin", origin)
                        putSerializable("destination", destination)
                    }
                }
    }

    class StopTimeAdapter(private val stopTimes: Array<StopTime>):
        RecyclerView.Adapter<StopTimeAdapter.ViewHolder>() {
        lateinit var v: View

        class ViewHolder(v: View): RecyclerView.ViewHolder(v) {
            val tvStopSequence = v.findViewById<TextView>(R.id.tv_stop_sequence)
            val tvStopStation = v.findViewById<TextView>(R.id.tv_stop_station)
            val tvDepartureTime = v.findViewById<TextView>(R.id.tv_departure_time)
        }

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): ViewHolder {
            v = LayoutInflater.from(parent.context).inflate(R.layout.item_stop_time, parent, false)
            return ViewHolder(v)
        }

        override fun onBindViewHolder(holder: StopTimeAdapter.ViewHolder, position: Int) {
            val selectBgColor = ContextCompat.getColor(
                holder.itemView.context,
                R.color.white
            )
            val selectTextColor = ContextCompat.getColor(
                holder.itemView.context,
                R.color.cardview_dark_background
            )
            holder.tvStopSequence.text = stopTimes[position].StopSequence.toString()
            holder.tvStopStation.text = stopTimes[position].StationName.Zh_tw
            holder.tvDepartureTime.text = stopTimes[position].DepartureTime
            if (stopTimes[position].StationID == origin.StationID ||
                stopTimes[position].StationID == destination.StationID) {
                holder.tvStopSequence.setTextColor(selectTextColor)
                holder.tvStopStation.setTextColor(selectTextColor)
                holder.tvDepartureTime.setTextColor(selectTextColor)
                holder.itemView.setBackgroundColor(selectBgColor)
            }
        }

        override fun getItemCount(): Int {
            return stopTimes.size
        }
    }
}
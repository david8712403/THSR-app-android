package com.davidchen.thsrapp.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.davidchen.thsrapp.R
import com.davidchen.thsrapp.data.THSR.DailyOriginToDestination
import com.davidchen.thsrapp.data.THSR.Station

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
                //TODO("Train detail fragment")
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

        override fun onBindViewHolder(holder: PathAdapter.ViewHolder, position: Int) {
            val path = paths[position]
            holder.tvTrainDirection.text = if (path.DailyTrainInfo.Direction == 0) {
                holder.itemView.context.getString(R.string.south)
            } else {
                holder.itemView.context.getString(R.string.north)
            }
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
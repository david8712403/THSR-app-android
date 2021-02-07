package com.davidchen.thsrapp.fragment

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.davidchen.thsrapp.R
import com.davidchen.thsrapp.data.THSR.Station
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

private lateinit var stations: ArrayList<Station>

/**
 * A simple [Fragment] subclass.
 * Use the [StationFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class StationFragment : BottomSheetDialogFragment() {

    // ui
    private lateinit var v: View
    private lateinit var etSearch: EditText
    private lateinit var rvStation: RecyclerView

    private lateinit var adapter: StationAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_station, container, false)
        initUi()

        stations = arguments?.getSerializable("stations") as ArrayList<Station>
        if (stations.isEmpty()) {
            Log.e(this.javaClass.name, "stations empty")
        }

        adapter = StationAdapter(stations)
        adapter.callback = object : StationAdapter.Callback {
            override fun onClick(station: Station) {
                val b = Bundle()
                b.putString("operation", "moveCamera")
                b.putSerializable("station", station)
                parentFragmentManager.setFragmentResult(REQUEST_KEY, b)
                this@StationFragment.dismiss()
            }
        }
        rvStation.adapter = adapter

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(str: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                filter(s.toString())
            }

        })

        return v
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return if (context == null) {
            super.onCreateDialog(savedInstanceState)
        } else BottomSheetDialog(requireContext(), R.style.TransparentBottomSheetStyle)
    }

    private fun initUi() {
        val linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        rvStation = v.findViewById(R.id.rv_station)
        rvStation.layoutManager = linearLayoutManager
        etSearch = v.findViewById(R.id.et_search)
    }

    private fun filter(text: String) {
        val filteredList: ArrayList<Station> = ArrayList()
        for (item in stations) {
            if (item.StationAddress.contains(text.toLowerCase())) {
                filteredList.add(item)
            }
        }
        adapter.filterList(filteredList)
    }

    companion object {
        val REQUEST_KEY = this.javaClass.simpleName
        @JvmStatic
        fun newInstance(stations: ArrayList<Station>) =
                StationFragment().apply {
                    arguments = Bundle().apply {
                        putSerializable("stations", stations)
                    }
                }
    }

    class StationAdapter(private var stations: ArrayList<Station>):
        RecyclerView.Adapter<StationAdapter.ViewHolder>() {

        var callback: Callback? = null

        lateinit var v: View

        class ViewHolder(v: View): RecyclerView.ViewHolder(v) {
            val tvName = v.findViewById<TextView>(R.id.tv_station_name)
            val tvAddr = v.findViewById<TextView>(R.id.tv_station_address)
        }

        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): ViewHolder {
            v = LayoutInflater.from(parent.context).inflate(R.layout.item_station, parent, false)
            return ViewHolder(v)
        }

        override fun onBindViewHolder(holder: StationAdapter.ViewHolder, position: Int) {
            holder.tvName.text = stations[position].StationName.Zh_tw
            holder.tvAddr.text = stations[position].StationAddress
            holder.itemView.setOnClickListener {
                callback?.onClick(stations[position])
            }
        }

        override fun getItemCount(): Int {
            return stations.size
        }

        fun filterList(filteredList: ArrayList<Station>) {
            stations = filteredList
            notifyDataSetChanged()
        }

        interface Callback {
            fun onClick(station: Station)
        }
    }
}
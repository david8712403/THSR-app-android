package com.davidchen.thsrapp.fragment

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.davidchen.ProgressDialogUtil
import com.davidchen.thsrapp.R
import com.davidchen.thsrapp.data.THSR.Station
import com.davidchen.thsrapp.data.bluenet.RawDataRestaurant
import com.davidchen.thsrapp.data.bluenet.Restaurant
import com.davidchen.thsrapp.http_api.bluenet.Api
import com.davidchen.thsrapp.http_api.bluenet.ApiBuilder
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.gson.Gson
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Response
import java.io.IOException
import java.util.*


class MenuDialogFragment : BottomSheetDialogFragment() {

    lateinit var v: View
    lateinit var rvMenu: RecyclerView
    lateinit var menuItems: Array<String>
    lateinit var station: Station
    var originStation: Station? = null
    var destinationStation: Station? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        v = inflater.inflate(R.layout.fragment_menu_dialog, container, false)
        initUi()
        val adapter = arguments?.getStringArray("menuItems")?.let { ItemAdapter(it) }
        station = arguments?.getSerializable("station") as Station
        originStation = arguments?.getSerializable("originStation") as? Station
        destinationStation = arguments?.getSerializable("destinationStation") as? Station
        rvMenu.layoutManager = LinearLayoutManager(context)
        rvMenu.adapter = adapter

        adapter?.callback = object : Callback {
            override fun onClick(position: Int) {
                val b = Bundle()
                b.putSerializable("station", station)
                when(position) {
                    1 -> { // set station as start
                        b.putString("operation", "setAsOrigin")
                        parentFragmentManager.setFragmentResult(StationFragment.REQUEST_KEY, b)
                        this@MenuDialogFragment.dismiss()
                    }
                    2 -> { // set station as end
                        b.putString("operation", "setAsDestination")
                        parentFragmentManager.setFragmentResult(StationFragment.REQUEST_KEY, b)
                        this@MenuDialogFragment.dismiss()
                    }
                    3 -> {
                        ProgressDialogUtil.showProgressDialog(
                            requireContext(),
                            "取得${station.StationName.Zh_tw}${getString(R.string.hsr_station)}附近美食")
                        val request = Api.GetRestaurant(count = 15,
                            lat = station.getLatLng().latitude,
                            lng = station.getLatLng().longitude,
                            range = 2000).getRequest()

                        OkHttpClient().newCall(request).enqueue(object : okhttp3.Callback {
                            override fun onFailure(call: Call, e: IOException) {
                                ProgressDialogUtil.dismiss()
                                this@MenuDialogFragment.dismiss()
                            }

                            override fun onResponse(call: Call, response: Response) {
                                ProgressDialogUtil.dismiss()
                                val json = response.body!!.string()
                                Log.d(ApiBuilder.TAG, json)
                                val restaurants: Array<Restaurant> =
                                    Gson().fromJson(json, RawDataRestaurant::class.java).results.content

                                val f = RestaurantFragment.newInstance(station, restaurants)

                                parentFragmentManager.beginTransaction()
                                    .setCustomAnimations(
                                        R.anim.enter_from_right,
                                        R.anim.exit_to_right,
                                        R.anim.enter_from_right,
                                        R.anim.exit_to_right
                                    )
                                    .add(R.id.root_constraint, f).addToBackStack(f.javaClass.name)
                                    .commit()
                                this@MenuDialogFragment.dismiss()
                            }
                        })
                    }
                    4 -> { // cancel
                        this@MenuDialogFragment.dismiss()
                    }
                }
            }
        }

        return v
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return if (context == null) {
            super.onCreateDialog(savedInstanceState)
        } else BottomSheetDialog(requireContext(), R.style.TransparentBottomSheetStyle)
    }

    private fun initUi() {
        val layoutParam = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        layoutParam.setMargins(30, 0, 30, 0)
        v.layoutParams = layoutParam
        rvMenu = v.findViewById(R.id.list)
    }

    override fun onStart() {
        super.onStart()
        //this forces the sheet to appear at max height even on landscape
        val behavior = BottomSheetBehavior.from(requireView().parent as View)
        behavior.state = BottomSheetBehavior.STATE_DRAGGING
    }

    private inner class ViewHolder internal constructor(
        inflater: LayoutInflater,
        parent: ViewGroup
    ) : RecyclerView.ViewHolder(
        inflater.inflate(
            R.layout.fragment_menu_dialog_item,
            parent,
            false
        )
    ) {

        internal val text: TextView = itemView.findViewById(R.id.text)
    }

    private inner class ItemAdapter internal constructor(private val strings: Array<String>) :
        RecyclerView.Adapter<ViewHolder>() {

        var callback: Callback? = null

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(LayoutInflater.from(parent.context), parent)
        }

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            if(position == 0) {
                holder.text.text = station.StationName.Zh_tw + getString(R.string.hsr_station)
                holder.text.setTypeface(holder.text.typeface, Typeface.BOLD)
                holder.itemView.isEnabled = false
            }else {
                // if station have been selected, disable set as origin/destination item
                if ((originStation == station || destinationStation == station) &&
                    (position == 1 || position == 2)) {
                    holder.text.isEnabled = false
                    holder.itemView.isEnabled = false
                }
                holder.text.text = strings[position]
                holder.itemView.setOnClickListener {
                    callback?.onClick(position)
                }
            }
        }

        override fun getItemCount(): Int {
            return strings.size
        }
    }

    interface Callback {
        fun onClick(position: Int)
    }

    companion object {

        fun newInstance(str: Array<String>, station: Station, originStation: Station?, destinationStation: Station?): MenuDialogFragment =
            MenuDialogFragment().apply {
                arguments = Bundle().apply {
                    putStringArray("menuItems", str)
                    putSerializable("station", station)
                    putSerializable("originStation", originStation)
                    putSerializable("destinationStation", destinationStation)
                }
            }

    }
}
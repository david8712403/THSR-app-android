package com.davidchen.thsrapp.fragment

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.davidchen.thsrapp.R
import com.davidchen.thsrapp.data.THSR.Station
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class MenuDialogFragment : BottomSheetDialogFragment() {

    lateinit var v: View
    lateinit var rvMenu: RecyclerView
    lateinit var menuItems: Array<String>
    lateinit var station: Station
    var startStation: Station? = null
    var endStation: Station? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        v = inflater.inflate(R.layout.fragment_menu_dialog, container, false)
        initUi()
        val adapter = arguments?.getStringArray("menuItems")?.let { ItemAdapter(it) }
        station = arguments?.getSerializable("station") as Station
        startStation = arguments?.getSerializable("startStation") as? Station
        endStation = arguments?.getSerializable("endStation") as? Station
        rvMenu.layoutManager = LinearLayoutManager(context)
        rvMenu.adapter = adapter

        adapter?.callback = object : Callback {
            override fun onClick(position: Int) {
                val b = Bundle()
                b.putSerializable("station", station)
                when(position) {
                    1 -> { // set station as start
                        b.putString("operation", "setAsStart")
                        parentFragmentManager.setFragmentResult(StationFragment.REQUEST_KEY, b)
                        this@MenuDialogFragment.dismiss()
                    }
                    2 -> { // set station as end
                        b.putString("operation", "setAsEnd")
                        parentFragmentManager.setFragmentResult(StationFragment.REQUEST_KEY, b)
                        this@MenuDialogFragment.dismiss()
                    }
                    3 -> { // view restaurant nearby

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

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            if(position == 0) {
                holder.text.text = station.StationName.Zh_tw
            }else {
                // if station have been selected, disable set as start/end item
                if ((startStation == station || endStation == station) &&
                    (position == 1 || position == 2)) {
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

        // TODO: Customize parameters
        fun newInstance(str: Array<String>, station: Station, startStation: Station?, endStation: Station?): MenuDialogFragment =
            MenuDialogFragment().apply {
                arguments = Bundle().apply {
                    putStringArray("menuItems", str)
                    putSerializable("station", station)
                    putSerializable("startStation", startStation)
                    putSerializable("endStation", endStation)
                }
            }

    }
}
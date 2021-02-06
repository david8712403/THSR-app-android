package com.davidchen.thsrapp.fragment

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.davidchen.thsrapp.R
import com.davidchen.thsrapp.data.THSR.Station
import com.davidchen.thsrapp.data.bluenet.Restaurant
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.SphericalUtil


private const val STATION = "station"
private const val RESTAURANT = "restaurant"

private lateinit var station: Station
private lateinit var restaurants: Array<Restaurant>

/**
 * A simple [Fragment] subclass.
 * Use the [RestaurantFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RestaurantFragment : Fragment() {

    private lateinit var v: View
    private lateinit var adapter: RestaurantAdapter
    private lateinit var rvRestaurant: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            station = it.getSerializable(STATION) as Station
            restaurants = it.getSerializable(RESTAURANT) as Array<Restaurant>
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_restaurant, container, false)

        // set title
        v.findViewById<TextView>(R.id.tv_restaurant_title).text =
            "${station.StationName.Zh_tw}${this.getString(R.string.hsr_station)}${this.getString(R.string.near_restaurant)}"

        // setup RecyclerView
        val linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        adapter = RestaurantAdapter(restaurants)
        rvRestaurant = v.findViewById(R.id.rv_restaurant)
        rvRestaurant.layoutManager = linearLayoutManager
        rvRestaurant.adapter = adapter
        return v
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param station station
         * @param restaurant restaurant nearby station
         * @return A new instance of fragment RestaurantFragment.
         */
        @JvmStatic
        fun newInstance(station: Station, restaurant: Array<Restaurant>) =
            RestaurantFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(STATION, station)
                    putSerializable(RESTAURANT, restaurant)
                }
            }
    }

    class RestaurantAdapter(private val restaurant: Array<Restaurant>):
            RecyclerView.Adapter<RestaurantAdapter.ViewHolder>() {

        lateinit var v: View

        class ViewHolder(v: View): RecyclerView.ViewHolder(v) {
            val tvName = v.findViewById<TextView>(R.id.tv_restaurant_name)
            val tvAddress = v.findViewById<TextView>(R.id.tv_restaurant_address)
            val tvDistance = v.findViewById<TextView>(R.id.tv_restaurant_distance)
            val tvRating = v.findViewById<TextView>(R.id.tv_restaurant_rating)
            val ivImg = v.findViewById<ImageView>(R.id.iv_img)
            val progressBar = v.findViewById<ProgressBar>(R.id.pg_img_loading)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            v = LayoutInflater.from(parent.context).inflate(R.layout.item_restaurant, parent, false)
            return ViewHolder(v)
        }

        @SuppressLint("SetTextI18n")
        override fun onBindViewHolder(holder: RestaurantAdapter.ViewHolder, position: Int) {
            val r = restaurant[position]
            val context = holder.itemView.context
            holder.tvName.text = r.name
            holder.tvAddress.text = r.vicinity

            // load image by url
            Glide.with(holder.itemView.context)
                .load(r.photo)
                .listener( object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        holder.ivImg.setImageDrawable(context.getDrawable(android.R.drawable.stat_notify_error))
                        holder.progressBar.visibility = View.INVISIBLE
                        return true
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
//                        holder.ivImg.setImageDrawable(resource)
                        holder.progressBar.visibility = View.INVISIBLE
                        return false
                    }

                })
                .into(holder.ivImg)

            // calculate distance
            val distance = SphericalUtil.computeDistanceBetween(
                station.getLatLng(),
                LatLng(r.lat, r.lng)
            ).div(1000)
            val distanceStr = String.format("%1$,.2f", distance)
            holder.tvDistance.text =
                "${context.getString(R.string.restaurant_distance)}: " +
                        distanceStr +
                        context.getString(R.string.restaurant_km)

            // rating and review info
            holder.tvRating.text =
                "${context.getString(R.string.restaurant_rating)}: " +
                        "${r.rating} (${r.reviewsNumber}${context.getString(R.string.restaurant_review)})"
        }

        override fun getItemCount(): Int {
            return restaurant.size
        }

    }
}
package com.example.recordtrack

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class LocationAdapter(context : Context,locationList : ArrayList<LocationDetails>,onClickListerner: OnClickListerner) : RecyclerView.Adapter<LocationAdapter.ViewHolder>() {

    lateinit var locationList : ArrayList<LocationDetails>
    lateinit var context: Context
    lateinit var onClickListerner: OnClickListerner
    init {
        this.locationList= locationList
        this.context = context
        this.onClickListerner = onClickListerner
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LocationAdapter.ViewHolder {

        var viewHolder: LocationAdapter.ViewHolder? = null
        val inflater = LayoutInflater.from(parent.context)

        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.loction_view, parent, false)
        viewHolder = LocationAdapter.ViewHolder(view)
        return viewHolder
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        lateinit var locationName : TextView
        lateinit var location : TextView
        lateinit var view : LinearLayout
        init {
            location= itemView.findViewById(R.id.place)
            locationName = itemView.findViewById(R.id.place1)
            view = itemView.findViewById(R.id.location_layout)
        }
    }

    override fun getItemCount(): Int {
        return locationList.size
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: LocationAdapter.ViewHolder, position: Int) {
        val viewHolder = ViewHolder(itemView = holder.itemView)
        viewHolder.location.text = locationList[position].location
        viewHolder.locationName.text = locationList[position].latitude.toString() +","+locationList[position].longitude.toString()
        viewHolder.view.setOnClickListener {
            onClickListerner.onItemClickListener(position)
        }
    }

    interface OnClickListerner{
        fun onItemClickListener(position: Int)
    }
}
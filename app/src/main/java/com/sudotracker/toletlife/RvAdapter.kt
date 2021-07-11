package com.sudotracker.toletlife

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView
import com.sudotracker.toletlife.Responses.UserAllRentDetailsItem

class RvAdapter(private val rentList: ArrayList<UserAllRentDetailsItem>) :
    RecyclerView.Adapter<RvAdapter.RvViewHolder>() {

    private lateinit var mListener: onItemClickListener

    interface onItemClickListener {
        fun onItemClick(position: Int)
    }

    fun setOnItemClickListener(listener: onItemClickListener) {
        mListener = listener

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RvViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.list_item, parent, false)
        return RvViewHolder(itemView, mListener)
    }

    override fun onBindViewHolder(holder: RvViewHolder, position: Int) {
        val currentItem = rentList[position]
        val imageUrl = currentItem.imageUrls[0].imageUrl
        Glide.with(holder.titleImage).load(imageUrl).centerCrop()
            .placeholder(R.drawable.bg_triangle)
            .into(holder.titleImage)
        holder.tvHeading.text = "City: ${currentItem.city}"
        holder.tvArea.text = "Area: ${currentItem.area}"
        holder.tvRent.text = "Rent: Rs. ${currentItem.monthlyRent}/month"

    }

    override fun getItemCount(): Int {
        return rentList.size
    }


    class RvViewHolder(itemView: View, listener: onItemClickListener) :
        RecyclerView.ViewHolder(itemView) {
        val titleImage: ShapeableImageView = itemView.findViewById(R.id.title_image)
        val tvHeading: TextView = itemView.findViewById(R.id.tvHeading)
        val tvArea: TextView = itemView.findViewById(R.id.tvArea)
        val tvRent: TextView = itemView.findViewById(R.id.tvRent)

        init {
            itemView.setOnClickListener {
                listener.onItemClick(adapterPosition)
            }
        }
    }

}
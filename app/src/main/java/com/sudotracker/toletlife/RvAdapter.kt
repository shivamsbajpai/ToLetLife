package com.sudotracker.toletlife

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView
import com.google.gson.Gson
import com.sudotracker.toletlife.Responses.ProductCategoryResponse
import com.sudotracker.toletlife.Responses.UserAllRentDetailsItem

class RvAdapter(
    private val rentList: ArrayList<UserAllRentDetailsItem>,
    private val productCategories: Map<String, String>
) :
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
        var imageUrl: String = ""
        if(currentItem.imageUrls.isNotEmpty()){
            imageUrl = currentItem.imageUrls[0].imageUrl
        }


        Glide.with(holder.titleImage).load(imageUrl).centerCrop()
            .placeholder(R.drawable.bg_triangle)
            .into(holder.titleImage)
        holder.tvProduct.text = currentItem.productName
        holder.tvCategory.text = productCategories[currentItem.productCategoryId]
        holder.tvRent.text = currentItem.monthlyRent
        holder.tvCity.text = currentItem.city

    }

    override fun getItemCount(): Int {
        return rentList.size
    }


    class RvViewHolder(itemView: View, listener: onItemClickListener) :
        RecyclerView.ViewHolder(itemView) {
        val titleImage: ShapeableImageView = itemView.findViewById(R.id.title_image)
        val tvProduct: TextView = itemView.findViewById(R.id.tvProduct)
        val tvCategory: TextView = itemView.findViewById(R.id.tvCategory)
        val tvRent: TextView = itemView.findViewById(R.id.tvRent)
        val tvCity: TextView = itemView.findViewById(R.id.tvCity)

        init {
            itemView.setOnClickListener {
                listener.onItemClick(adapterPosition)
            }
        }
    }

}
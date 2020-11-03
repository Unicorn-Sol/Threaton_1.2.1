package com.theatron2.uiforthreaton.adapterForAllClasses

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.theatron2.uiforthreaton.R
import kotlinx.android.synthetic.main.notification_cell.view.*


class AdapterForNotifications() : RecyclerView.Adapter<AdapterForNotifications.MyViewHolder>(){

    //private var myList = emptyList<HashMap<String, String>>()
    val map =   hashMapOf<String, String>(
    )


    private var myList :List<HashMap<String, String>> = listOf(map)
    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.notification_cell, parent, false))
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = map
        Log.e("adapter", "onBindViewHolder: ${item["title"]}" )
        Log.e("adapter", "onBindViewHolder: ${item["body"]}" )
        Log.e("adapter", "onBindViewHolder: ${item["time"]}" )
        holder.itemView.notification_title_tv.text = item["title"]
        holder.itemView.notification_body_tv.text = item["body"]
        holder.itemView.notification_time_tv.text = item["time"]
    }

    override fun getItemCount() =  myList.size

    fun setData(newList: List<HashMap<String, String>>){
        map["title"] = "lavda lassang"
        map["body"] = "lavda lassang"
        map["time"] = "lavda lassang"
        Log.e("adapter", "setData: called ", )
        myList = newList
        notifyDataSetChanged()
        notifyDataSetChanged()
    }
}
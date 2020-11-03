package com.theatron2.uiforthreaton.adapterForAllClasses

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.theatron2.uiforthreaton.R
import com.theatron2.uiforthreaton.ui.feed.PlayerViewHolder
import com.theatron2.uiforthreaton.ui.feed.UserFeed


@Suppress("DEPRECATION", "NAME_SHADOWING")
class AdapterForFeedFragment(var context: Context, var listOfFeed: ArrayList<UserFeed>,val requestManager: RequestManager):RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view=LayoutInflater.from(parent.context).inflate(R.layout.feed_recycler_view_layout,parent,false)
        return PlayerViewHolder(view)
    }
    override fun getItemCount(): Int =listOfFeed.size
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as PlayerViewHolder).onBind(listOfFeed[position],context,requestManager)
    }

}
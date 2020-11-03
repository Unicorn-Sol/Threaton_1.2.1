package com.theatron2.uiforthreaton.adapterForAllClasses

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.theatron2.uiforthreaton.R
import com.squareup.picasso.Picasso
import com.theatron2.uiforthreaton.ui.profile.ProfileUser
import com.theatron2.uiforthreaton.ui.profile_search_feed

class CustomAdapterForSearchGrid(val context: Context, private val arrayList:ArrayList<ProfileUser>): RecyclerView.Adapter<CustomAdapterForSearchGrid.ViewHolder>(){
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.layout_for_search_fragment,parent,false))
    }

    override fun getItemCount(): Int =arrayList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Picasso.get().load(arrayList[position].thumbnailphoto).into(holder.image)
        //holder.image.background=Drawable.createFromPath("#${arrayList[position]}")
        holder.layout.setOnClickListener {
            val bundle = Bundle()
            bundle.putSerializable("videolist", arrayList)
            bundle.putSerializable("position", arrayList)
            (context as FragmentActivity).supportFragmentManager.beginTransaction().replace(
                R.id.nav_host_fragment,
                profile_search_feed().newInstance(arrayList, position)
            ).addToBackStack("SearchF").commit()
        }
    }
    class ViewHolder(itemView:View):RecyclerView.ViewHolder(itemView)
    {
        val image:ImageView=itemView.findViewById(R.id.imageViewForSearchGrid)
        val layout:LinearLayout=itemView.findViewById(R.id.layoutForSearch)
    }
}
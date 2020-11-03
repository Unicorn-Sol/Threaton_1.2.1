package com.theatron2.uiforthreaton.adapterForAllClasses

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.theatron2.uiforthreaton.R
import com.theatron2.uiforthreaton.ui.friends.USER_FRIEND_ADMIRERS
import com.squareup.picasso.Picasso
import com.theatron2.uiforthreaton.Activities.Main2Activity
import de.hdodenhof.circleimageview.CircleImageView

class CustomAdapterForAdmirers(val context: Context, private val admirers: ArrayList<USER_FRIEND_ADMIRERS>) : RecyclerView.Adapter<CustomAdapterForAdmirers.ViewHolder>() {


    class ViewHolder(itemView: View):RecyclerView.ViewHolder(itemView)
    {
        var imageView: CircleImageView =itemView.findViewById<CircleImageView>(R.id.imageViewForAdmirers)
        var textView: TextView =itemView.findViewById<TextView>(R.id.textViewForAdmirers)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.friend_recycler_view_layout,parent,false))
    }

    override fun getItemCount(): Int=admirers.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        Picasso.get().load(admirers[position].photo).placeholder(R.drawable.ic_account_circle_black_24dp).into(holder.imageView)
        //Picasso.get().load(R.drawable.ic_account_circle_black_24dp).placeholder(R.drawable.ic_account_circle_black_24dp).into(holder.imageView)
        holder.textView.text= admirers[position].name

        holder.imageView.setOnClickListener {
            val intent= Intent(context,
                Main2Activity::class.java)
            intent.putExtra("SuggestId",admirers[position].id)
            context.startActivity(intent)
        }

        holder.textView.setOnClickListener {
            val intent= Intent(context,
                Main2Activity::class.java)
            intent.putExtra("SuggestId",admirers[position].id)
            context.startActivity(intent)
        }
    }
}

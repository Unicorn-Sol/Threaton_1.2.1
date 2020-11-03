package com.theatron2.uiforthreaton.adapterForAllClasses

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.theatron2.uiforthreaton.Activities.Main2Activity
import com.theatron2.uiforthreaton.R
import com.theatron2.uiforthreaton.ui.feed.UserComments
import de.hdodenhof.circleimageview.CircleImageView

@Suppress("UNCHECKED_CAST", "CAST_NEVER_SUCCEEDS")
class CustomAdapterOFFeedForComments(val context: Context, var arrayOFComments: ArrayList<UserComments>, val videoID:String, val videoNum:String) : RecyclerView.Adapter<CustomAdapterOFFeedForComments.ViewHolder>() {
    val currentUser = FirebaseAuth.getInstance().currentUser
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view=LayoutInflater.from(parent.context).inflate(R.layout.feed_recycler_view_comments_layout,parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int =arrayOFComments.size

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.UserName.text = "@"+arrayOFComments[position].userName
        holder.comment.text=arrayOFComments[position].comments
        Picasso.get().load(arrayOFComments[position].photo)
            .placeholder(R.drawable.ic_account_circle_black_24dp).into(holder.photo)


        holder.layout.setOnClickListener {
            val intent= Intent(context,
                Main2Activity::class.java)
            intent.putExtra("SuggestId",arrayOFComments[position].id)
            context.startActivity(intent)
        }

        holder.layout.setOnLongClickListener {
            if(currentUser!=null) {
                if (arrayOFComments[position].id == currentUser.uid) {
                    val popUp = PopupMenu(context, holder.layout)
                    popUp.menuInflater.inflate(R.menu.delete_video_menu, popUp.menu)
                    popUp.setOnMenuItemClickListener {
                        if (it.itemId == R.id.deleteVideo) {
                            val myRef = FirebaseDatabase.getInstance().reference
                            myRef.child("USER").child(videoID).child("comments").child(videoNum).addListenerForSingleValueEvent(
                                object :ValueEventListener
                                {
                                    override fun onCancelled(error: DatabaseError) {}

                                    override fun onDataChange(snapshot: DataSnapshot) {

                                        if (snapshot.exists())
                                        {
                                            for (dataObj in snapshot.children)
                                            {
                                                val databaseUser = dataObj.value as HashMap<String,String>
                                                if (databaseUser["id"] == arrayOFComments[position].id && databaseUser["comments"] == arrayOFComments[position].comments) {
                                                    val key = dataObj.key!!
                                                    myRef.child("USER").child(videoID).child("comments").child(videoNum).child(key).removeValue()
                                                    arrayOFComments.remove(UserComments(databaseUser["userName"]!!,databaseUser["photo"]!!,databaseUser["comments"]!!,databaseUser["id"]!!))
                                                    notifyDataSetChanged()
                                                    break
                                                }
                                            }
                                        }
                                    }

                                }

                            )
                            val myRefCommentsList = myRef.child("USER").child(videoID).child("videolist").child(videoNum).child("shares")
                            myRefCommentsList.addListenerForSingleValueEvent(
                                object :ValueEventListener
                                {
                                    override fun onCancelled(error: DatabaseError) {}

                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        myRefCommentsList.setValue((snapshot.value.toString().toInt() -1).toString())
                                    }

                                }

                            )
                            val myRefSharedBy = myRef.child("USER").child(videoID).child("sharedby").child(videoNum)
                            myRefSharedBy.addListenerForSingleValueEvent(
                                object :ValueEventListener
                                {
                                    override fun onCancelled(error: DatabaseError) {}

                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        if(snapshot.exists()){
                                            for (dataObj in snapshot.children){
                                                if (dataObj.value.toString() == currentUser.uid){
                                                    val key = dataObj.key!!
                                                    myRefSharedBy.child(key).removeValue()
                                                    break
                                                }
                                            }
                                        }
                                    }
                                }
                            )
                        }
                        true
                    }
                    popUp.show()
                }
            }
            true
        }
    }
    class ViewHolder(itemView:View):RecyclerView.ViewHolder(itemView)
    {
        val UserName=itemView.findViewById<TextView>(R.id.textViewForComments)
        val comment=itemView.findViewById<TextView>(R.id.commentsForComments)
        val photo=itemView.findViewById<CircleImageView>(R.id.imageViewForComments)
        val layout = itemView.findViewById<LinearLayout>(R.id.layoutForComments)
    }
}

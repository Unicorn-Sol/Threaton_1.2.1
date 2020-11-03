@file:Suppress("DEPRECATION")

package com.theatron2.uiforthreaton.adapterForAllClasses

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import com.theatron2.uiforthreaton.Activities.MainActivity3
import com.theatron2.uiforthreaton.R
import com.theatron2.uiforthreaton.ui.profile.ProfileUser
import com.theatron2.uiforthreaton.ui.profile_search_feed

@Suppress("DEPRECATION", "UNCHECKED_CAST", "CAST_NEVER_SUCCEEDS")
class CustomAdapterForProfileFragment(val context: Context, private val videolist:ArrayList<ProfileUser>,
                                      private val xyz:Int = 0):RecyclerView.Adapter<CustomAdapterForProfileFragment.ViewHolder>() {
    val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private var myRef= FirebaseDatabase.getInstance().reference
    private var storageRef= FirebaseStorage.getInstance().reference
    class ViewHolder(itemView: View):RecyclerView.ViewHolder(itemView)
    {
        val imageView:ImageView=itemView.findViewById(R.id.imageViewForProfileGrid)
        val titleTextView:TextView=itemView.findViewById(R.id.titleTextView)
        //val descTextView:TextView=itemView.findViewById(R.id.textViewForDesc)
        val frameLayout:FrameLayout=itemView.findViewById(R.id.frameLayoutForGidProfile)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view=LayoutInflater.from(parent.context).inflate(R.layout.layout_for_grid_layout_profile,parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int=videolist.size

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.titleTextView.text=videolist[position].title
        val descText=videolist[position].date+"  "+videolist[position].time
        //holder.descTextView.text=descText
        Picasso.get().load(videolist[position].thumbnailphoto).placeholder(R.drawable.ic_account_circle_black_24dp).into(holder.imageView)
        if(xyz==0) {
            holder.frameLayout.setOnLongClickListener {
                val popup = PopupMenu(context, holder.frameLayout)
                popup.menuInflater.inflate(R.menu.delete_video_menu, popup.menu)
                popup.setOnMenuItemClickListener {
                    if (it.itemId == R.id.deleteVideo) {
                        val viewForAlertDialog = LayoutInflater.from(context)
                            .inflate(R.layout.background_for_alert_dialog, null, false)
                        val alertDialog = AlertDialog.Builder(context).setView(viewForAlertDialog)
                            .setCancelable(false)
                            .show()
                        val textView = viewForAlertDialog.findViewById<TextView>(R.id.txt_dia)!!
                        val yesButton = viewForAlertDialog.findViewById<Button>(R.id.btn_yes)!!
                        val noButton = viewForAlertDialog.findViewById<Button>(R.id.btn_no)!!
                        val imageView =
                            viewForAlertDialog.findViewById<ImageView>(R.id.imageViewForAlertDialog)
                        Picasso.get().load(videolist[position].thumbnailphoto)
                            .placeholder(R.drawable.ic_account_circle_black_24dp).into(imageView)
                        textView.text = "Are you sure you want to delete this video ?"
                        yesButton.setOnClickListener {
                            val progressDialog = ProgressDialog(context)
                            progressDialog.setTitle("Deleting Video......")
                            progressDialog.setCancelable(false)
                            progressDialog.show()
                            val removedProfileUser = videolist[position]
                            videolist.remove(removedProfileUser)
                            storageRef.child("videos")
                                .child(removedProfileUser.id + removedProfileUser.vnum + ".mp4")
                                .delete().addOnSuccessListener {
                                storageRef.child("thumbnails")
                                    .child(removedProfileUser.id + removedProfileUser.vnum + ".jpeg")
                                    .delete().addOnSuccessListener {
                                    myRef.child("USER").child(mAuth.currentUser!!.uid)
                                        .child("videolist").setValue(videolist)

                                    myRef.child("USER").child(mAuth.currentUser!!.uid)
                                        .child("likedislike").child(removedProfileUser.vnum)
                                        .removeValue()
                                    myRef.child("USER").child(mAuth.currentUser!!.uid)
                                        .child("comments").child(removedProfileUser.vnum)
                                        .removeValue()
                                    myRef.child("USER").child(mAuth.currentUser!!.uid)
                                        .child("sharedby").child(removedProfileUser.vnum)
                                        .removeValue()
                                    progressDialog.dismiss()
                                    Toast.makeText(
                                        context,
                                        "Video Deleted Successfully.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    notifyDataSetChanged()
                                }

                            }.addOnFailureListener {
                                Toast.makeText(
                                    context,
                                    "Sorry Video Can't be deleted.\nPlease Check Your Internet Connection.",
                                    Toast.LENGTH_LONG
                                ).show()
                                videolist.add(removedProfileUser)
                                notifyDataSetChanged()
                                progressDialog.dismiss()
                            }

                            alertDialog.dismiss()
                        }
                        noButton.setOnClickListener {
                            alertDialog.dismiss()
                        }
                    }
                    true
                }
                popup.show()
                true
            }
        }

        holder.frameLayout.setOnClickListener {

            if(xyz!=0)
            {
                val intent = Intent(context,MainActivity3::class.java)
                intent.putExtra("videolist",videolist)
                intent.putExtra("position",position.toString())
                context.startActivity(intent)

                //(context as Activity).finishAffinity()
            }
            else {
                val bundle =Bundle()
                bundle.putSerializable("videolist",videolist)
                bundle.putSerializable("position",videolist)
                (context as FragmentActivity).supportFragmentManager.beginTransaction().replace(
                    R.id.nav_host_fragment,
                    profile_search_feed().newInstance(videolist, position)
                ).addToBackStack("ProfileF").commit()
            }
        }
    }
}
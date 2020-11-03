package com.theatron2.uiforthreaton.adapterForAllClasses

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.theatron2.uiforthreaton.Activities.Main2Activity
import com.theatron2.uiforthreaton.Activities.user_with_id_phoneNumber
import com.theatron2.uiforthreaton.R
import de.hdodenhof.circleimageview.CircleImageView

@Suppress("UNCHECKED_CAST", "CAST_NEVER_SUCCEEDS")
class AdapterForFriendRequest (val context: Context, val arrayListOfFriendRequest: ArrayList<user_with_id_phoneNumber>):RecyclerView.Adapter<AdapterForFriendRequest.ViewHolder>()
{
    val myRef= FirebaseDatabase.getInstance().reference
    val currentUser= FirebaseAuth.getInstance().currentUser
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.layout_for_friend_request,parent,false))
    }

    override fun getItemCount(): Int=arrayListOfFriendRequest.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.name.text=arrayListOfFriendRequest[position].name
        Picasso.get().load(arrayListOfFriendRequest[position].photo).placeholder(R.drawable.ic_account_circle_black_24dp).into(holder.image)
        var phoneNumber:String="-1"
        myRef.child("USER").child(currentUser!!.uid).child("phonenumber").addListenerForSingleValueEvent(
            object :ValueEventListener
            {
                override fun onCancelled(p0: DatabaseError) {}
                override fun onDataChange(p0: DataSnapshot) {
                    if (p0.exists()) {
                        phoneNumber = p0.value as String
                    }
                }
            }
        )
        holder.accept.setOnClickListener{
            val removedUser = arrayListOfFriendRequest[position]
            val new_ref=myRef.child("USER").child(removedUser.id).child("friends")
            val key = new_ref.push().key!!
            new_ref.child(key).setValue(user_with_id_phoneNumber(currentUser.displayName!!,currentUser.photoUrl.toString(),currentUser.uid,phoneNumber))

            val new_ref2=myRef.child("USER").child(currentUser.uid).child("friends")
            val key2 = new_ref2.push().key!!
            new_ref2.child(key2).setValue(user_with_id_phoneNumber(removedUser.name,removedUser.photo,removedUser.id,removedUser.phonenumber))
            val new_ref3=myRef.child("USER").child(currentUser.uid).child("friendrns")
            new_ref3.addListenerForSingleValueEvent(object :ValueEventListener
            {
                override fun onCancelled(p0: DatabaseError) {}
                override fun onDataChange(p0: DataSnapshot) {

                    for(databaseList in p0.children)
                    {
                        val databaseUser = databaseList.value as HashMap<String,String>
                        if(databaseUser["id"] == removedUser.id )
                        {
                            val key3 = databaseList.key!!
                            new_ref3.child(key3).removeValue()
                            break
                        }
                    }
                    val new_ref4=myRef.child("USER").child(removedUser.id).child("friendrns")
                    new_ref4.addListenerForSingleValueEvent(
                        object :ValueEventListener
                        {
                            override fun onCancelled(p0: DatabaseError) {}
                            override fun onDataChange(p0: DataSnapshot) {
                                for(databaseList in p0.children)
                                {
                                    val databaseUser = databaseList.value as HashMap<String,String>
                                    if(databaseUser["id"] == currentUser.uid)
                                    {
                                        val key4 = databaseList.key!!
                                        new_ref4.child(key4).removeValue()
                                        break
                                    }
                                }
                            }
                        }
                    )
                }
            }
            )
            val myRef_new5=myRef.child("USER").child(removedUser.id)
            myRef_new5.addListenerForSingleValueEvent(
                    object :ValueEventListener
                    {
                        override fun onCancelled(p0: DatabaseError) {
                            Toast.makeText(context, p0.message, Toast.LENGTH_LONG).show()
                        }

                        override fun onDataChange(p0: DataSnapshot) {
                            val followerscount=p0.child("followerscount").value as String
                            myRef_new5.child("followerscount").setValue((followerscount.toInt()+1).toString())
                            val myRef_new6=myRef.child("USER").child(currentUser.uid)
                            myRef_new6.addListenerForSingleValueEvent(
                                object :ValueEventListener
                                {
                                    override fun onCancelled(p0: DatabaseError) {
                                        Toast.makeText(context, p0.message, Toast.LENGTH_LONG).show()
                                    }

                                    override fun onDataChange(p0: DataSnapshot) {
                                        val followerscount1=p0.child("followerscount").value as String
                                        myRef_new6.child("followerscount").setValue((followerscount1.toInt()+1).toString())
                                    }
                                }
                            )

                        }
                    }
                )
            arrayListOfFriendRequest.remove(arrayListOfFriendRequest[position])
            notifyDataSetChanged()
            Toast.makeText(context,"Friend Request Accepted",Toast.LENGTH_SHORT).show()
        }
        holder.decline.setOnClickListener {
            val removedUser=arrayListOfFriendRequest[position]
            val new_ref3=myRef.child("USER").child(currentUser.uid).child("friendrns")
            new_ref3.addListenerForSingleValueEvent(object :ValueEventListener
            {
                override fun onCancelled(p0: DatabaseError) {}
                override fun onDataChange(p0: DataSnapshot) {
                    for(databaseList in p0.children)
                    {
                        val databaseUser = databaseList.value as HashMap<String,String>
                        if(databaseUser["id"] == removedUser.id )
                        {
                            val key = databaseList.key!!
                            new_ref3.child(key).removeValue()
                            break
                        }
                    }
                    val new_ref4=myRef.child("USER").child(removedUser.id).child("friendrns")
                    new_ref4.addListenerForSingleValueEvent(
                        object :ValueEventListener
                        {
                            override fun onCancelled(p0: DatabaseError) {}
                            override fun onDataChange(p0: DataSnapshot) {
                                for(databaseList in p0.children)
                                {
                                    val databaseUser = databaseList.value as HashMap<String,String>
                                    if(databaseUser["id"] == currentUser.uid)
                                    {
                                        val key4 = databaseList.key!!
                                        new_ref4.child(key4).removeValue()
                                        break
                                    }
                                }
                            }
                        }
                    )
                }
            }
            )
            arrayListOfFriendRequest.remove(arrayListOfFriendRequest[position])
            notifyDataSetChanged()
            Toast.makeText(context,"Friend Request Declined",Toast.LENGTH_SHORT).show()
        }

        holder.name.setOnClickListener {
            val intent= Intent(context,
                Main2Activity::class.java)
            intent.putExtra("SuggestId",arrayListOfFriendRequest[position].id)
            context.startActivity(intent)
        }

        holder.image.setOnClickListener {
            val intent= Intent(context,
                Main2Activity::class.java)
            intent.putExtra("SuggestId",arrayListOfFriendRequest[position].id)
            context.startActivity(intent)
        }
    }
    class ViewHolder(itemView:View):RecyclerView.ViewHolder(itemView)
    {
        val image:CircleImageView =itemView.findViewById(R.id.imageViewForRequest)
        val name:TextView=itemView.findViewById(R.id.textViewForRequests)
        val accept:MaterialButton=itemView.findViewById(R.id.Accept)
        val decline:MaterialButton=itemView.findViewById(R.id.Decline)
    }
}
package com.theatron2.uiforthreaton.Activities

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.theatron2.uiforthreaton.R
import com.theatron2.uiforthreaton.adapterForAllClasses.AdapterForFriendRequest
import kotlinx.android.synthetic.main.activity_friend_request.*

@Suppress("UNCHECKED_CAST", "CAST_NEVER_SUCCEEDS")
class FriendRequestActivity : AppCompatActivity() {
    val myRef= FirebaseDatabase.getInstance().reference
    val currentUser= FirebaseAuth.getInstance().currentUser
    val arrayListOFRequests = ArrayList<user_with_id_phoneNumber>()
    @SuppressLint("CommitPrefEdits")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_friend_request)
        setSupportActionBar(toolbar2)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.title = "Friend Requests"
        val sharedPreference=getSharedPreferences("USERNAME", Context.MODE_PRIVATE).edit()
        sharedPreference.putString("page","5")
        sharedPreference.apply()
        val adapterForFriendRequest=AdapterForFriendRequest(this,arrayListOFRequests)
        recyclerViewForRequests.hasFixedSize()
        recyclerViewForRequests.layoutManager= LinearLayoutManager(this)
        recyclerViewForRequests.adapter=adapterForFriendRequest

        myRef.child("USER").child(currentUser!!.uid).child("friendrns").addListenerForSingleValueEvent(
            object :ValueEventListener
            {
                override fun onCancelled(p0: DatabaseError) {}
                override fun onDataChange(p0: DataSnapshot) {
                    if(p0.exists())
                    {
//                        val databaseList=p0.value as ArrayList<RequestTypeData>
                        for (databaseList in p0.children)
                        {
                            val databaseUser=databaseList.value as HashMap<String,String>
                            if(databaseUser["type"]=="R")
                            {
                                arrayListOFRequests.add(user_with_id_phoneNumber(databaseUser["name"]!!,databaseUser["photo"]!!,databaseUser["id"]!!,databaseUser["phonenumber"]!!))
                            }
                        }
                        for(i in 0 until arrayListOFRequests.size)
                        {
                            myRef.child("USER").child(arrayListOFRequests[i].id).addListenerForSingleValueEvent(
                                object:ValueEventListener
                                {
                                    override fun onCancelled(error: DatabaseError) {

                                    }

                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        arrayListOFRequests[i].name = snapshot.child("name").value.toString()
                                        arrayListOFRequests[i].photo = snapshot.child("photourl").value.toString()
                                        adapterForFriendRequest.notifyDataSetChanged()
                                    }
                                }
                            )
                        }
                    }
                }
            }
        )
    }

    override fun onBackPressed() {
        val sharedPreference=getSharedPreferences("USERNAME", Context.MODE_PRIVATE).edit()
        sharedPreference.putString("page","0")
        sharedPreference.apply()
        super.onBackPressed()
    }
}
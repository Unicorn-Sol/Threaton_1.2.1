@file:Suppress("UNCHECKED_CAST")

package com.theatron2.uiforthreaton.ui.friends
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.theatron2.uiforthreaton.Activities.user_with_id
import com.theatron2.uiforthreaton.R
import com.theatron2.uiforthreaton.adapterForAllClasses.CustomAdapterForAdmirers
import com.theatron2.uiforthreaton.adapterForAllClasses.CustomAdapterForAdmiring
import kotlinx.android.synthetic.main.fragment_friend.*

@Suppress("SENSELESS_COMPARISON", "CAST_NEVER_SUCCEEDS")
class FriendFragment : Fragment() {
    private var listADMIRERS=ArrayList<USER_FRIEND_ADMIRERS>()
    private var listADMIRING=ArrayList<USER_FRIEND_ADMIRING>()
    private var listADMIRINGTruly=ArrayList<String>()
    val myRef=FirebaseDatabase.getInstance().reference
    val currentUser=FirebaseAuth.getInstance().currentUser!!.uid
    private lateinit var adapterForAdmirers:CustomAdapterForAdmirers
    private lateinit var adapterForADMIRING:CustomAdapterForAdmiring
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        //retainInstance=true
        return inflater.inflate(R.layout.fragment_friend, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerViewForAdmirers.layoutManager=LinearLayoutManager(requireContext())
        recyclerViewForAdmirers.hasFixedSize()
        adapterForAdmirers=CustomAdapterForAdmirers(requireContext(),listADMIRERS)
        recyclerViewForAdmirers.adapter= adapterForAdmirers
        adapterForAdmirers.notifyDataSetChanged()
        recyclerViewForAdmiring.layoutManager=LinearLayoutManager(requireContext())
        recyclerViewForAdmiring.hasFixedSize()
        adapterForADMIRING= CustomAdapterForAdmiring(requireContext(),listADMIRING)
        recyclerViewForAdmiring.adapter=adapterForADMIRING
        adapterForADMIRING.notifyDataSetChanged()



        ////////////////
        updateContentInitially()
//        swipeForAdmirers.setWaveColor(Color.WHITE)
//        swipeForAdmirers.setMaxDropHeight(800)
//        swipeForAdmirers.setColorSchemeColors( Color.BLACK,Color.WHITE)
//
//        swipeForAdmirers.setOnRefreshListener {
//            updateContentFinallyForAdmirers()
//        }
//        ///////////////////
//        swipeForAdmiring.setWaveColor(Color.BLACK)
//        swipeForAdmiring.setMaxDropHeight(800)
//        swipeForAdmiring.setColorSchemeColors( Color.WHITE,Color.BLACK)
//        swipeForAdmiring.setOnRefreshListener {
//            updateContentFinallyForAdmiring()
//        }
        /////////////////////////
        super.onViewCreated(view, savedInstanceState)
    }

    private fun updateContentFinallyForAdmiring() {
        var x=0
        myRef.child("USER").child(currentUser).child("admiring").addListenerForSingleValueEvent(
            object :ValueEventListener
            {
                override fun onCancelled(p0: DatabaseError) {}
                override fun onDataChange(p0: DataSnapshot) {
                    if(p0.exists())
                    {
                        myRef.child("USER").child(currentUser).child("admiring").keepSynced(true)
                        val mapOfAdmiring=p0.value as ArrayList<user_with_id>
                        for( i in 0 until mapOfAdmiring.size)
                        {
                            if (mapOfAdmiring[i]!=null)
                            {
                                val databaseUser=mapOfAdmiring[i] as HashMap<String,String>
                                if (databaseUser["id"]!=currentUser) {
                                    listADMIRING.add(USER_FRIEND_ADMIRING(databaseUser["name"]!!, databaseUser["photo"]!!, databaseUser["id"]!!, true))
                                    listADMIRINGTruly.add(databaseUser["id"]!!)
                                }
                            }
                        }
                        adapterForADMIRING.notifyDataSetChanged()
                    }
                }
            }
        )

        ////
        myRef.child("ALLUSER").addListenerForSingleValueEvent(
            object :ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {}
                override fun onDataChange(p0: DataSnapshot) {
                    myRef.child("ALLUSER").keepSynced(true)
                    if (p0.exists()) {
                        val mapOfUser = p0.value as ArrayList<user_with_id>
                        for (i in 0 until mapOfUser.size) {
                            if (mapOfUser[i] != null) {
                                val databaseUser = mapOfUser[i] as HashMap<String, String>
                                if (!listADMIRINGTruly.contains(databaseUser["id"]!!) && databaseUser["id"] != currentUser) {
                                    listADMIRING.add(USER_FRIEND_ADMIRING(databaseUser["name"]!!, databaseUser["photo"]!!,databaseUser["id"]!!,false))
                                }
                            }
                        }
                        adapterForADMIRING.notifyDataSetChanged()

                    }
                }
            }
        )
    }

    private fun updateContentFinallyForAdmirers() {
        myRef.child("USER").child(currentUser).child("admirers").addListenerForSingleValueEvent(
            object :ValueEventListener
            {
                override fun onCancelled(p0: DatabaseError) {}
                override fun onDataChange(p0: DataSnapshot) {
                    if(p0.exists())
                    {
                        myRef.child("USER").child(currentUser).child("admirers").keepSynced(true)
                        val mapOfAdmirers=p0.value as ArrayList<user_with_id>
                        for( i in 0 until mapOfAdmirers.size)
                        {
                            if (mapOfAdmirers[i]!=null)
                            {
                                val databaseUser=mapOfAdmirers[i] as HashMap<String,String>
                                listADMIRERS.add(USER_FRIEND_ADMIRERS(databaseUser["name"]!!,databaseUser["photo"]!!,databaseUser["id"]!!))

                            }
                        }
                        adapterForAdmirers.notifyDataSetChanged()
                    }
                }
            }
        )
    }


    private fun updateContentInitially() {
        myRef.child("USER").child(currentUser).child("admirers").addListenerForSingleValueEvent(
            object :ValueEventListener
            {
                override fun onCancelled(p0: DatabaseError) {}
                override fun onDataChange(p0: DataSnapshot) {
                    if(p0.exists())
                    {
                        myRef.child("USER").child(currentUser).child("admirers").keepSynced(true)
                        val mapOfAdmirers=p0.value as ArrayList<user_with_id>
                        for( i in 0 until mapOfAdmirers.size)
                        {
                            if (mapOfAdmirers[i]!=null)
                            {
                                val databaseUser=mapOfAdmirers[i] as HashMap<String,String>
                                listADMIRERS.add(USER_FRIEND_ADMIRERS(databaseUser["name"]!!,databaseUser["photo"]!!,databaseUser["id"]!!))

                            }
                        }
                        for(i in 0 until listADMIRERS.size)
                        {
                            myRef.child("USER").child(listADMIRERS[i].id).addListenerForSingleValueEvent(
                                object:ValueEventListener
                                {
                                    override fun onCancelled(error: DatabaseError) {

                                    }

                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        listADMIRERS[i].name = snapshot.child("name").value.toString()
                                        listADMIRERS[i].photo = snapshot.child("photourl").value.toString()
                                        adapterForAdmirers.notifyDataSetChanged()
                                    }
                                }
                            )
                        }

                    }
                }
            }
        )
        ////////
        myRef.child("USER").child(currentUser).child("admiring").addListenerForSingleValueEvent(
            object :ValueEventListener
            {
                override fun onCancelled(p0: DatabaseError) {}
                override fun onDataChange(p0: DataSnapshot) {
                    if(p0.exists())
                    {
                        myRef.child("USER").child(currentUser).child("admiring").keepSynced(true)
                        val mapOfAdmiring=p0.value as ArrayList<user_with_id>
                        for( i in 0 until mapOfAdmiring.size)
                        {
                            if (mapOfAdmiring[i]!=null)
                            {
                                val databaseUser=mapOfAdmiring[i] as HashMap<String,String>
                                if (databaseUser["id"]!=currentUser) {
                                    listADMIRING.add(USER_FRIEND_ADMIRING(databaseUser["name"]!!, databaseUser["photo"]!!, databaseUser["id"]!!, true))
                                    listADMIRINGTruly.add(databaseUser["id"]!!)
                                }
                            }
                        }
                        for(i in 0 until listADMIRING.size)
                        {
                            myRef.child("USER").child(listADMIRING[i].id).addListenerForSingleValueEvent(
                                object:ValueEventListener
                                {
                                    override fun onCancelled(error: DatabaseError) {

                                    }

                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        listADMIRING[i].name = snapshot.child("name").value.toString()
                                        listADMIRING[i].photo = snapshot.child("photourl").value.toString()
                                        adapterForADMIRING.notifyDataSetChanged()
                                    }
                                }
                            )
                        }

                    }
                }
            }
        )

        //
        myRef.child("ALLUSER").addListenerForSingleValueEvent(
            object :ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {}
                override fun onDataChange(p0: DataSnapshot) {
                    myRef.child("ALLUSER").keepSynced(true)
                    if (p0.exists()) {
                        val mapOfUser = p0.value as ArrayList<user_with_id>
                        for (i in 0 until mapOfUser.size) {
                            if (mapOfUser[i] != null) {
                                val databaseUser = mapOfUser[i] as HashMap<String, String>
                                if (!listADMIRINGTruly.contains(databaseUser["id"]!!) && databaseUser["id"] != currentUser) {
                                    listADMIRING.add(USER_FRIEND_ADMIRING(databaseUser["name"]!!, databaseUser["photo"]!!,databaseUser["id"]!!,false))
                                }
                            }
                        }
                        adapterForADMIRING.notifyDataSetChanged()
                    }
                }
            }
        )
    }


}

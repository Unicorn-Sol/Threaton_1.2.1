@file:Suppress("DEPRECATION")

package com.theatron2.uiforthreaton.ui.feed

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.theatron2.uiforthreaton.R
import com.theatron2.uiforthreaton.adapterForAllClasses.AdapterForFeedFragment
import com.theatron2.uiforthreaton.ui.profile.ProfileUser
import java.lang.Exception
import java.text.SimpleDateFormat


@Suppress("UNCHECKED_CAST", "NAME_SHADOWING", "SENSELESS_COMPARISON", "CAST_NEVER_SUCCEEDS",
    "RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS"
)
class FeedFragment : Fragment() {


    lateinit var recyclerView: ExoPlayerRecyclerView
    private  var feedList=ArrayList<UserFeed>()
    val commentsList = ArrayList<UserComments>()
    private var firstTime = true
    var adapter:AdapterForFeedFragment?=null
    val currentUser=FirebaseAuth.getInstance().currentUser
    val myRef=FirebaseDatabase.getInstance().reference
    val mAuth = FirebaseAuth.getInstance()
    var currentVisiblePosition: Long = 0
    lateinit var tokenForApp : MutableLiveData<String>
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_feed, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        tokenForApp.observe(viewLifecycleOwner, {
            tokenForApp.value = it
        })
        val shit = FirebaseAuth.getInstance().currentUser?.getIdToken(true)
       shit!!.addOnSuccessListener {
           Log.e( "feedFragment",  it.token.toString())
       }
        val admiringList=ArrayList<String>()
        recyclerView=view.findViewById(R.id.recyclerViewForFeed)
        recyclerView.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)

        PagerSnapHelper().attachToRecyclerView(recyclerView)
        recyclerView.init()
        recyclerView.setMediaObjects(feedList)
        adapter = initGlide()?.let { AdapterForFeedFragment(requireContext(),feedList, it) }
        recyclerView.adapter = adapter

        if(currentUser!=null) {
            val arrayListOfFeedShare = ArrayList<feedList_id_vnum>()
            myRef.child("USER").child(currentUser.uid).child("feedlist").addListenerForSingleValueEvent(
                object :ValueEventListener
                {
                    override fun onCancelled(p0: DatabaseError) {}
                    override fun onDataChange(p0: DataSnapshot) {
                        if(p0.exists()) {
                            for(x in p0.children){
                                val databaseUser = x.value as HashMap<String,String>
                                arrayListOfFeedShare.add(feedList_id_vnum(databaseUser["id"]!!,databaseUser["vnum"]!!))
                            }
                            if(arrayListOfFeedShare.isNotEmpty())
                            {
                                val userRef = myRef.child("USER")
                                for (i in arrayListOfFeedShare) {
                                if (i.id != currentUser.uid) {
                                    userRef.child(i.id).child("videolist")
                                        .addListenerForSingleValueEvent(
                                            object : ValueEventListener {
                                                override fun onCancelled(p0: DatabaseError) {}

                                                @SuppressLint("SimpleDateFormat")
                                                override fun onDataChange(p0: DataSnapshot) {
                                                    if (p0.exists()) {
                                                        val arrayList =
                                                            p0.value as ArrayList<ProfileUser>
                                                        for (j in 0 until arrayList.size) {
                                                            if (arrayList[j] != null) {
                                                                val databaseUser =
                                                                    arrayList[j] as HashMap<String, String>
                                                                    val feedUserForTemp = UserFeed(
                                                                        false,
                                                                        databaseUser["photo"]!!,
                                                                        databaseUser["name"]!!,
                                                                        databaseUser["url"]!!,
                                                                        databaseUser["view"]!!.toInt(),
                                                                        databaseUser["likes"]!!.toInt(),
                                                                        databaseUser["dislikes"]!!.toInt(),
                                                                        databaseUser["shares"]!!.toInt(),
                                                                        LikedORNot = false,
                                                                        DislikedOrNot = false,
                                                                        Title = databaseUser["title"]!!,
                                                                        details = databaseUser["desc"]!!,
                                                                        arrayOfComments = commentsList,
                                                                        id = databaseUser["id"]!!,
                                                                        vnum = databaseUser["vnum"]!!,
                                                                        thumbnail = databaseUser["thumbnailphoto"]!!,
                                                                        date = databaseUser["date"]!!,
                                                                        time = databaseUser["time"]!!)
                                                                if (databaseUser["vnum"] == i.vnum && feedUserForTemp !in feedList) {
                                                                    feedList.add(feedUserForTemp)
                                                                }
                                                            }
                                                            feedList.sortWith(Comparator { o1, o2 ->
                                                                val dateTimeFormatter1 =
                                                                    SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
                                                                val dateTimeFormatter2 =
                                                                    SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
                                                                (dateTimeFormatter2.parse(o2.date + " " + o2.time)).compareTo(
                                                                    dateTimeFormatter1.parse(o1.date + " " + o1.time)
                                                                )
                                                            })
                                                            adapter!!.notifyDataSetChanged()
                                                        }
                                                    }
                                                }
                                            }
                                        )
                                }
                            }
                            }
                        }
                    }

                }
            )

            myRef.child("USER").child(currentUser.uid).child("admiring")
                .addListenerForSingleValueEvent(
                    object : ValueEventListener {
                        override fun onCancelled(p0: DatabaseError) {}

                        override fun onDataChange(p0: DataSnapshot) {
                            if (p0.exists()) {
                                for (arrayList in p0.children) {
                                    val databaseUser = arrayList.value as HashMap<String,String>
                                    admiringList.add(databaseUser["id"]!!)
                                }
                                if (admiringList.isNotEmpty()) {
                                    for (i in 0 until admiringList.size) {
                                        myRef.child("USER").child(admiringList[i])
                                            .child("videolist").addListenerForSingleValueEvent(
                                            object : ValueEventListener {
                                                override fun onCancelled(p0: DatabaseError) {}
                                                @SuppressLint("SimpleDateFormat")
                                                override fun onDataChange(p0: DataSnapshot) {
                                                    if (p0.exists()) {
                                                        myRef.child("USER").child(admiringList[i])
                                                            .child("videolist").keepSynced(true)
                                                       // try {

                                                        if(p0.value is HashMap<*,*>){
                                                            val arrayList =
                                                                p0.value as HashMap<String, HashMap<String, String>>
                                                            for (j in 0 until arrayList.size) {
                                                                if (arrayList[j.toString()] != null) {
                                                                    val databaseUser =
                                                                        arrayList[j.toString()] as HashMap<String, String>
                                                                    val feedUserForTemp = UserFeed(
                                                                        false,
                                                                        databaseUser["photo"]!!,
                                                                        databaseUser["name"]!!,
                                                                        databaseUser["url"]!!,
                                                                        databaseUser["view"]!!.toInt(),
                                                                        databaseUser["likes"]!!.toInt(),
                                                                        databaseUser["dislikes"]!!.toInt(),
                                                                        databaseUser["shares"]!!.toInt(),
                                                                        LikedORNot = false,
                                                                        DislikedOrNot = false,
                                                                        Title = databaseUser["title"]!!,
                                                                        details = databaseUser["desc"]!!,
                                                                        arrayOfComments = commentsList,
                                                                        id = databaseUser["id"]!!,
                                                                        vnum = databaseUser["vnum"]!!,
                                                                        thumbnail = databaseUser["thumbnailphoto"]!!,
                                                                        date = databaseUser["date"]!!,
                                                                        time = databaseUser["time"]!!
                                                                    )
                                                                    if(databaseUser!=null && feedUserForTemp !in feedList)
                                                                    {
                                                                        feedList.add(feedUserForTemp)
                                                                    }
                                                                }
                                                                feedList.sortWith(
                                                                    Comparator { o1, o2 ->
                                                                        val dateTimeFormatter1 = SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
                                                                        val dateTimeFormatter2 = SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
                                                                        (dateTimeFormatter2.parse(o2.date+" "+o2.time)).compareTo(dateTimeFormatter1.parse(o1.date+" "+o1.time))
                                                                    }
                                                                )
                                                                adapter!!.notifyDataSetChanged()
                                                            }
                                                        }else{
                                                            val arrayList =
                                                                p0.value as ArrayList<ProfileUser>
                                                            for (j in 0 until arrayList.size) {
                                                                if (arrayList[j] != null) {
                                                                    val databaseUser =
                                                                        arrayList[j] as HashMap<String, Any>

                                                                    if (databaseUser["photo"] == null){
                                                                        databaseUser["photo"] = "https://www.ibts.org/wp-content/uploads/2017/08/iStock-476085198.jpg"
                                                                    }
                                                                    if(databaseUser["name"]== null){
                                                                        databaseUser["name"] = "no name"
                                                                    }
                                                                    if(databaseUser["url"] == null){
                                                                        databaseUser["url"] = "https://firebasestorage.googleapis.com/v0/b/theatronfinal.appspot.com/o/videos%2FmN8VTp8QxmT1bD5Th9s0b5MahjQ23.mp4?alt=media&token=6d582c9a-4a09-4ad8-8111-528dde677f54"
                                                                    }
                                                                    if(databaseUser["view"].toString().isEmpty()||databaseUser["view"].toString()=="null"){
                                                                        databaseUser["view"] = 0.toString()
                                                                    }
                                                                    if(databaseUser["likes"].toString().isNullOrEmpty()||databaseUser["view"].toString()=="null"){
                                                                        databaseUser["likes"] = "0"
                                                                    }
                                                                    if(databaseUser["dislikes"].toString().isNullOrEmpty()||databaseUser["view"].toString()=="null"){
                                                                        databaseUser["dislikes"] = "0"
                                                                    }
                                                                    if(databaseUser["shares"].toString().isNullOrEmpty()||databaseUser["view"].toString()=="null"){
                                                                        databaseUser["shares"] = "0"
                                                                    }
                                                                    if (databaseUser["thumbnailphoto"]==null){
                                                                        databaseUser["thumbnailphoto"] = "https://www.pngkit.com/png/full/267-2678423_bacteria-video-thumbnail-default.png"
                                                                    }
                                                                    if(databaseUser["date"]==null){
                                                                        databaseUser["date"] = "2020-01-12"
                                                                    }
                                                                    if(databaseUser["time"]==null){
                                                                        databaseUser["time"] = "10:10:55"
                                                                    }
                                                                    val feedUserForTemp = UserFeed(
                                                                        false,

                                                                        databaseUser["photo"]!!.toString(),
                                                                        databaseUser["name"]!!.toString(),
                                                                        databaseUser["url"]!!.toString(),
                                                                        databaseUser["view"]!!.toString().toInt(),
                                                                        databaseUser["likes"]!!.toString().toInt(),
                                                                        databaseUser["dislikes"]!!.toString().toInt(),
                                                                        databaseUser["shares"]!!.toString().toInt(),
                                                                        LikedORNot = false,
                                                                        DislikedOrNot = false,
                                                                        Title = databaseUser["title"]!!.toString(),
                                                                        details = databaseUser["desc"]!!.toString(),
                                                                        arrayOfComments = commentsList,
                                                                        id = databaseUser["id"]!!.toString(),
                                                                        vnum = databaseUser["vnum"]!!.toString(),
                                                                        thumbnail = databaseUser["thumbnailphoto"]!!.toString(),
                                                                        date = databaseUser["date"]!!.toString(),
                                                                        time = databaseUser["time"]!!.toString()
                                                                    )
                                                                    if(databaseUser!=null && feedUserForTemp !in feedList)
                                                                    {
                                                                        feedList.add(feedUserForTemp)
                                                                    }
                                                                }
                                                                feedList.sortWith(
                                                                    Comparator { o1, o2 ->
                                                                        val dateTimeFormatter1 = SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
                                                                        val dateTimeFormatter2 = SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
                                                                        (dateTimeFormatter2.parse(o2.date+" "+o2.time)).compareTo(dateTimeFormatter1.parse(o1.date+" "+o1.time))
                                                                    }
                                                                )
                                                                adapter!!.notifyDataSetChanged()
                                                            }
                                                        }
//                                                        }catch (e:Exception){
//                                                            e.printStackTrace()
//                                                        }

                                                    }
                                                }
                                            })
                                    }
                                }
                            }
                        }
                    }
                )
        }

        if (firstTime) {
            Handler(Looper.getMainLooper()).post{
                recyclerView.playVideo(false)
            }
            firstTime = false
        }

        super.onViewCreated(view, savedInstanceState)
    }



    private fun initGlide(): RequestManager? {
        val options = RequestOptions()
        return Glide.with(this).setDefaultRequestOptions(options)
    }

    override fun onDestroy() {
        recyclerView.releasePlayer()
        super.onDestroy()
    }

    override fun onStop() {
        recyclerView.releasePlayer()
        super.onStop()
    }

    override fun onPause() {
        recyclerView.releasePlayer()
         // this variable should be static in class
        super.onPause()
    }


    override fun onStart() {
        recyclerView.init()
        recyclerView.setMediaObjects(feedList)
        recyclerView.adapter=adapter
        recyclerView.playVideo(false)
        super.onStart()
    }

    override fun onResume() {
        recyclerView.init()
        recyclerView.setMediaObjects(feedList)
        recyclerView.adapter=adapter
        recyclerView.playVideo(false)
        super.onResume()
    }

    override fun onDestroyView() {
        recyclerView.onPausePlayer()

        super.onDestroyView()
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        recyclerView.onPausePlayer()

    }

}
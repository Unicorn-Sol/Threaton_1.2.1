package com.theatron2.uiforthreaton.Activities

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.RequestOptions
import com.theatron2.uiforthreaton.R
import com.theatron2.uiforthreaton.adapterForAllClasses.AdapterForFeedFragment
import com.theatron2.uiforthreaton.ui.feed.ExoPlayerRecyclerView
import com.theatron2.uiforthreaton.ui.feed.UserFeed
import com.theatron2.uiforthreaton.ui.profile.ProfileUser
import kotlinx.android.synthetic.main.fragment_profile_search_feed.*

@Suppress("UNCHECKED_CAST")
class MainActivity3 : AppCompatActivity() {
    lateinit var recyclerView: ExoPlayerRecyclerView
    private  var feedList=ArrayList<UserFeed>()
    private var firstTime = true
    var adapter: AdapterForFeedFragment?=null
    var temporaryList=ArrayList<ProfileUser>()
    var position:Int=0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main3)
        setSupportActionBar(toolbar3)

        if (supportActionBar!=null) {
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        }
        if(intent.extras!=null) {
            temporaryList = intent.extras!!.get("videolist") as ArrayList<ProfileUser>
            position = intent.extras!!.getString("position","0").toInt()
        }

        for (i in temporaryList) {
            feedList.add(UserFeed(false, i.photo, i.name, i.url, i.view.toInt(), i.likes.toInt(), i.dislikes.toInt(), i.shares.toInt(), LikedORNot = false, DislikedOrNot = false, Title = i.title, details = i.desc, arrayOfComments = arrayListOf(), id = i.id, vnum = i.vnum, thumbnail = i.thumbnailphoto,date = i.date,time = i.time))
        }
        recyclerView = findViewById(R.id.recyclerViewForProfileSearchFeed)
        recyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        recyclerView.scrollToPosition(position)
        PagerSnapHelper().attachToRecyclerView(recyclerView)
        recyclerView.init()
        recyclerView.setMediaObjects(feedList)
        adapter = initGlide()?.let { AdapterForFeedFragment(this, feedList, it) }
        recyclerView.adapter = adapter

        if (firstTime) {
            Handler(Looper.getMainLooper()).post {
                recyclerView.playVideo(false)
            }
            firstTime = false
        }
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

}
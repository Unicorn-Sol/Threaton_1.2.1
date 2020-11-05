package com.theatron2.uiforthreaton.ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.RequestOptions
import com.theatron2.uiforthreaton.R
import com.theatron2.uiforthreaton.adapterForAllClasses.AdapterForFeedFragment
import com.theatron2.uiforthreaton.ui.feed.ExoPlayerRecyclerView
import com.theatron2.uiforthreaton.ui.feed.UserComments
import com.theatron2.uiforthreaton.ui.feed.UserFeed
import com.theatron2.uiforthreaton.ui.profile.ProfileUser
import kotlinx.android.synthetic.main.activity_sign_in.*
import kotlinx.android.synthetic.main.fragment_profile_search_feed.*


@Suppress("UNCHECKED_CAST")
class profile_search_feed() : Fragment() {
    lateinit var recyclerView: ExoPlayerRecyclerView
    private  var feedList=ArrayList<UserFeed>()
    private var firstTime = true
    var adapter: AdapterForFeedFragment?=null
    var temporaryList=ArrayList<ProfileUser>()
    var position:Int=0
    fun newInstance( videoList:ArrayList<ProfileUser>, position:Int):profile_search_feed
    {
        val profileSearchFeed = profile_search_feed()
        val bundle=Bundle()
        bundle.putSerializable("videolist",videoList)
        bundle.putInt("pos",position)
        profileSearchFeed.arguments = bundle
        return profileSearchFeed
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(arguments!=null) {
            temporaryList = requireArguments().getSerializable("videolist") as ArrayList<ProfileUser>
            position = requireArguments().getInt("pos")
        }
        else
        {
            if(savedInstanceState!=null) {
                temporaryList = savedInstanceState.getSerializable("videolist") as ArrayList<ProfileUser>
                position = savedInstanceState.getInt("pos")
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putSerializable("videolist",temporaryList)
        outState.putInt("pos",position)
        super.onSaveInstanceState(outState)
    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_profile_search_feed, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (activity as AppCompatActivity?)!!.setSupportActionBar(toolbar3)
        toolbarButton.setOnClickListener {
            (context as FragmentActivity).supportFragmentManager.popBackStackImmediate()
        }

        for (i in temporaryList) {
            if (i.view =="null"){
                i.view = "0"
            }
            if (i.likes =="null"){
                i.likes = "0"
            }
            if (i.dislikes =="null"){
                i.dislikes = "0"
            }
            if (i.shares =="null"){
                i.shares = "0"
            }
            feedList.add(UserFeed(false, i.photo, i.name, i.url, i.view.toInt(), i.likes.toInt(), i.dislikes.toInt(), i.shares.toInt(), LikedORNot = false, DislikedOrNot = false, Title = i.title, details = i.desc, arrayOfComments = arrayListOf(), id = i.id, vnum = i.vnum, thumbnail = i.thumbnailphoto,date = i.date,time = i.time))
        }
        recyclerView = view.findViewById(R.id.recyclerViewForProfileSearchFeed)
        recyclerView.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
        recyclerView.scrollToPosition(position)
        PagerSnapHelper().attachToRecyclerView(recyclerView)
        recyclerView.init()
        recyclerView.setMediaObjects(feedList)
        adapter = initGlide()?.let { AdapterForFeedFragment(requireContext(), feedList, it) }
        recyclerView.adapter = adapter

        if (firstTime) {
            Handler(Looper.getMainLooper()).post {
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
        recyclerView.releasePlayer()
        super.onDestroyView()
    }


    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        recyclerView.onPausePlayer()
    }

}
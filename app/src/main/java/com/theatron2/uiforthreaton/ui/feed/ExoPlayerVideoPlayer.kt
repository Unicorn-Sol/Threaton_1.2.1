@file:Suppress("DEPRECATION", "DEPRECATION")

package com.theatron2.uiforthreaton.ui.feed
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.graphics.Point
import android.media.AudioManager
import android.net.Uri
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.util.Log
import android.view.*
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Toast
import androidx.annotation.Nullable
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.*
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.PlayerControlView
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.BandwidthMeter
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.theatron2.uiforthreaton.Activities.user_with_id_phoneNumber
import com.theatron2.uiforthreaton.EmojiUtils
import com.theatron2.uiforthreaton.R
import com.theatron2.uiforthreaton.ui.friends.USER_FRIEND_ADMIRERS
import kotlinx.android.synthetic.main.layout_sharing_edit_alert_dialog.view.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.math.pow

@Suppress("UNCHECKED_CAST", "CAST_NEVER_SUCCEEDS")
@SuppressLint("ClickableViewAccessibility")
class ExoPlayerRecyclerView(context: Context, @Nullable attrs: AttributeSet?) : RecyclerView(context,attrs) {
    private val TAG = "ExoPlayerRecyclerView"
    private val AppName = "Theatron"
    private var mediaCoverImage: PlayerView? = null
    private var progressBar: ProgressBar? = null
    private var viewHolderParent: View? = null
    private var mediaContainer: FrameLayout? = null
    var videoSurfaceView: PlayerView? = null
    var videoPlayer: SimpleExoPlayer? = null
    private var mediaObjects: ArrayList<UserFeed> = ArrayList()
    private var videoSurfaceDefaultHeight = 0
    private var screenDefaultHeight = 0
    private var playPosition = -1
    private var isVideoViewAdded = false
    private var requestManager: RequestManager? = null
    var fullscreen:Boolean = false
    val currentUser= FirebaseAuth.getInstance().currentUser
    val myRef= FirebaseDatabase.getInstance().reference
    val likedByList=ArrayList<String>()
    val dislikedByList=ArrayList<String>()
    val viewList=ArrayList<String>()
    var commentsList=ArrayList<UserComments>()
    val sharedByList = ArrayList<String>()
    var finalHolder:PlayerViewHolder? =null
    var finalTargetPosition:Int = 0
    lateinit var mFullScreenDialog:Dialog
    lateinit var fullScreenButton:ImageButton

    fun init() {
        val display = (Objects.requireNonNull(context.getSystemService(Context.WINDOW_SERVICE)) as WindowManager).defaultDisplay
        val point = Point()
        display.getSize(point)
        videoSurfaceDefaultHeight = point.x
        screenDefaultHeight = point.y
        videoSurfaceView = PlayerView(context)
        videoSurfaceView!!.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM


        val bandwidthMeter: BandwidthMeter = DefaultBandwidthMeter()
        val videoTrackSelectionFactory: TrackSelection.Factory =
            AdaptiveTrackSelection.Factory(bandwidthMeter)
        val trackSelector: TrackSelector = DefaultTrackSelector(videoTrackSelectionFactory)

        videoPlayer = ExoPlayerFactory.newSimpleInstance(context, trackSelector,CustomLoadControl())
        // Bind the player to the view.
        videoSurfaceView!!.player = videoPlayer
        videoSurfaceView!!.setFastForwardIncrementMs(10000)
        videoSurfaceView!!.setRewindIncrementMs(10000)


        addOnScrollListener(object :RecyclerView.OnScrollListener()
        {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == SCROLL_STATE_IDLE) {
                    mediaCoverImage?.visibility = VISIBLE
                    if (!recyclerView.canScrollHorizontally(1)) {
                        playVideo(true)
                    } else {
                        playVideo(false)
                    }
                }
            }
        }
        )
        addOnChildAttachStateChangeListener(object : OnChildAttachStateChangeListener {
            override fun onChildViewAttachedToWindow(view: View) {
//                scrollToPosition(finalTargetPosition)
            }
            override fun onChildViewDetachedFromWindow(view: View) {
                if (viewHolderParent != null && viewHolderParent == view) {
                    resetVideoView()
                }
            }
        })
        videoPlayer!!.addListener(object :Player.EventListener
        {
            override fun onTimelineChanged(timeline: Timeline?, manifest: Any?, reason: Int) {

            }

            override fun onTracksChanged(trackGroups: TrackGroupArray?, trackSelections: TrackSelectionArray?) {
            }

            override fun onLoadingChanged(isLoading: Boolean) {

            }
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                when (playbackState) {
                    Player.STATE_BUFFERING -> {
                        if (progressBar != null) {
                            progressBar!!.visibility = View.VISIBLE
                        }
                    }
                    Player.STATE_ENDED -> {

                        if (!viewList.contains(currentUser!!.uid)) {
                            viewList.add(currentUser.uid)
                            mediaObjects[finalTargetPosition].view += 1
                            if(finalHolder!=null) {
                                finalHolder!!.views.text =
                                    mediaObjects[finalTargetPosition].view.toString()
                            }
                            Log.i("Ended",mediaObjects[finalTargetPosition].view.toString())
                            myRef.child("USER").child(mediaObjects[finalTargetPosition].id).child("videolist")
                                .child(mediaObjects[finalTargetPosition].vnum)
                                .child("view").setValue(mediaObjects[finalTargetPosition].view.toString())
                            val key = myRef.child("USER").child(mediaObjects[finalTargetPosition].id).child("likedislike")
                                .child(mediaObjects[finalTargetPosition].vnum).child("viewlist").push().key!!
                            myRef.child("USER").child(mediaObjects[finalTargetPosition].id).child("likedislike")
                                .child(mediaObjects[finalTargetPosition].vnum).child("viewlist").child(key).setValue(currentUser.uid)
                        }
                        videoPlayer!!.seekTo(0)
                    }
                    Player.STATE_IDLE -> {
                    }
                    Player.STATE_READY -> {
                        Log.e(TAG, "onPlayerStateChanged: Ready to play.")
                        if (progressBar != null) {
                            progressBar!!.visibility = View.GONE
                        }
                        if (!isVideoViewAdded) {
                            addVideoView()
                        }
                    }
                    else -> {
                    }
                }
            }

            override fun onRepeatModeChanged(repeatMode: Int) {}
            override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {}
            override fun onPlayerError(error: ExoPlaybackException?) {}
            override fun onPositionDiscontinuity(reason: Int) {}
            override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters?) {}
            override fun onSeekProcessed() {}

        })
    }




    fun playVideo(isEndOfList: Boolean) {
        val targetPosition: Int
        if (!isEndOfList) {
            val startPosition = (Objects.requireNonNull(
                layoutManager
            ) as LinearLayoutManager).findFirstVisibleItemPosition()
            var endPosition =
                (layoutManager as LinearLayoutManager?)!!.findLastVisibleItemPosition()
            // if there is more than 2 list-items on the screen, set the difference to be 1
            if (endPosition - startPosition > 1) {
                endPosition = startPosition + 1
            }
            // something is wrong. return.
            if (startPosition < 0 || endPosition < 0) {
                return
            }
            // if there is more than 1 list-item on the screen
            targetPosition = if (startPosition != endPosition) {
                val startPositionVideoHeight: Int = getVisibleVideoSurfaceHeight(startPosition)
                val endPositionVideoHeight: Int = getVisibleVideoSurfaceHeight(endPosition)
                if (startPositionVideoHeight > endPositionVideoHeight) startPosition else endPosition
            } else {
                startPosition
            }
        } else {
            targetPosition = mediaObjects.size - 1
        }
        // video is already playing so return


        if (targetPosition == playPosition) {
            return
        }
        // set the position of the list-item that is to be played
        playPosition = targetPosition
        if (videoSurfaceView != null) {
            videoSurfaceView!!.visibility = View.INVISIBLE
            removeVideoView(videoSurfaceView!!)
        }
        // remove any old surface views from previously playing videos

        val currentPosition = targetPosition - (Objects.requireNonNull(
            layoutManager
        ) as LinearLayoutManager).findFirstVisibleItemPosition()
        val child = getChildAt(currentPosition) ?: return
        val holder = child.tag as PlayerViewHolder
        if (holder == null) {
            playPosition = -1
            return
        }
        finalHolder = holder
        finalTargetPosition = targetPosition
        //////////////////////////
        myRef.child("USER").child(mediaObjects[targetPosition].id).child("likedislike")
            .child(mediaObjects[targetPosition].vnum).addListenerForSingleValueEvent(
                object: ValueEventListener
                {
                    override fun onCancelled(p0: DatabaseError) {}

                    override fun onDataChange(p0: DataSnapshot) {
                        if(p0.exists())
                        {
                            val databaseUser=p0.value as HashMap<String,ArrayList<String>>
                            if(databaseUser.containsKey("likedby")) {
                                for (childSnapshot in p0.child("likedby").children) {
                                    if (childSnapshot.value is String) {
                                        likedByList.add(childSnapshot.value as String)
                                    } else{
                                    val ch = childSnapshot.value as HashMap<String, String>
                                    ch["id"]?.let { likedByList.add(it) }
                                }
                                   // likedByList.add(childSnapshot.value as String)
                                }
                            }
                            if(databaseUser.containsKey("dislikedby")) {
                                for (childSnapshot in p0.child("dislikedby").children) {
                                    if(childSnapshot.value is String){
                                        dislikedByList.add(childSnapshot.value as String)
                                    }else{
                                    val ch = childSnapshot.value as HashMap<String, String>
                                    ch["id"]?.let { dislikedByList.add(it) }
                                    }
                                    //dislikedByList.add(childSnapshot.value as String)
                                }
                            }
                            if(databaseUser.containsKey("viewlist"))
                            {
                                for (childSnapshot in p0.child("viewlist").children) {
                                    if(childSnapshot.value is String){
                                        viewList.add(childSnapshot.value as String)
                                    }else{
                                    val ch = childSnapshot.value as HashMap<String, String>
                                    ch["id"]?.let { viewList.add(it) }
                                    }
                                   // viewList.add(childSnapshot.value as String)
                                }
                            }
                        }
                    }
                }
            )

        ////////////
        commentsList = ArrayList()
        myRef.child("USER").child(mediaObjects[targetPosition].id).child("comments").child(mediaObjects[targetPosition].vnum)
            .addListenerForSingleValueEvent(
                object: ValueEventListener
                {
                    override fun onCancelled(p0: DatabaseError) {}

                    override fun onDataChange(p0: DataSnapshot) {
                        if(p0.exists())
                        {
                            for (x in p0.children){
                                val userCommentOfDta = x.value as HashMap<String,String>
                                myRef.child("USER").child(userCommentOfDta["id"]!!).addListenerForSingleValueEvent(
                                    object:ValueEventListener{
                                        override fun onCancelled(error: DatabaseError) {
                                        }

                                        override fun onDataChange(snapshot: DataSnapshot) {
                                            if (snapshot.exists()){
                                                val userCommentOnce = UserComments(snapshot.child("name").value.toString(),snapshot.child("photourl").value.toString(),userCommentOfDta["comments"]!!,userCommentOfDta["id"]!!)
                                                if (userCommentOnce !in commentsList) {
                                                    commentsList.add(0,userCommentOnce)
                                                }
                                            }
                                            adapter!!.notifyDataSetChanged()
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            )

        myRef.child("USER").child(mediaObjects[targetPosition].id).child("sharedby").child(mediaObjects[targetPosition].vnum)
            .addListenerForSingleValueEvent(
                object :ValueEventListener
                {
                    override fun onCancelled(error: DatabaseError) {
                    }

                    override fun onDataChange(snapshot: DataSnapshot) {
                        if(snapshot.exists())
                        {
                            for(x in snapshot.children){
                                sharedByList.add(x.value as String)
                            }
                        }
                    }

                }
            )
        /////////////////////////

        ///////////
        holder.mute.setOnClickListener {
            if(videoPlayer!!.volume!=0f)
            {
                videoPlayer!!.volume=0f
                Picasso.get().load(R.drawable.ic_sounf_off).placeholder(R.drawable.ic_sounf_off).into(holder.mute)
            }
            else
            {
                val am: AudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                videoPlayer!!.volume=am.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat()
                Picasso.get().load(R.drawable.ic_music_note_black_24dp).placeholder(R.drawable.ic_music_note_black_24dp).into(holder.mute)
            }
        }
        holder.buttonOfLike.setOnClickListener {
            if (mediaObjects[targetPosition].DislikedOrNot) {
                Picasso.get().load(R.drawable.ic_thumb_down_black_24dp)
                    .placeholder(R.drawable.ic_thumb_down_black_24dp).into(holder.buttonOfDislike)
                mediaObjects[targetPosition].DislikedOrNot = false
                mediaObjects[targetPosition].disLikeNumber -= 1
                holder.numberOfDislikes.text = getStringFormLikes(mediaObjects[targetPosition].disLikeNumber)
                //////////
                myRef.child("USER").child(mediaObjects[targetPosition].id).child("videolist").child(mediaObjects[targetPosition].vnum)
                    .child("dislikes").setValue(mediaObjects[targetPosition].disLikeNumber.toString())
                if(dislikedByList.isNotEmpty()) {
                    dislikedByList.remove(currentUser!!.uid)
                    myRef.child("USER").child(mediaObjects[targetPosition].id)
                        .child("likedislike").child(mediaObjects[targetPosition].vnum)
                        .child("dislikedby").addListenerForSingleValueEvent(
                         object:ValueEventListener
                         {
                             override fun onCancelled(error: DatabaseError) {

                             }

                             override fun onDataChange(snapshot: DataSnapshot) {
                                 if (snapshot.exists()){
                                     for(dataObj in snapshot.children){
                                         if(dataObj.value.toString() == currentUser.uid)
                                         {
                                             val key = dataObj.key!!
                                             myRef.child("USER").child(mediaObjects[targetPosition].id)
                                                 .child("likedislike").child(mediaObjects[targetPosition].vnum)
                                                 .child("dislikedby").child(key).removeValue()
                                             break
                                         }
                                     }
                                 }
                             }

                         }
                        )
                }
                //////////
            }
            if (mediaObjects[targetPosition].LikedORNot) {
                Picasso.get().load(R.drawable.ic_thumb_up_black_white_24dp)
                    .placeholder(R.drawable.ic_thumb_up_black_white_24dp)
                    .into(holder.buttonOfLike)
                mediaObjects[targetPosition].LikedORNot = false
                mediaObjects[targetPosition].likeNumber -= 1

                ////////////////////////
                myRef.child("USER").child(mediaObjects[targetPosition].id).child("videolist").child(mediaObjects[targetPosition].vnum)
                    .child("likes").setValue(mediaObjects[targetPosition].likeNumber.toString())
                if(likedByList.isNotEmpty()) {
                    likedByList.remove(currentUser!!.uid)
                    myRef.child("USER").child(mediaObjects[targetPosition].id)
                        .child("likedislike").child(mediaObjects[targetPosition].vnum)
                        .child("likedby").addListenerForSingleValueEvent(
                            object:ValueEventListener
                            {
                                override fun onCancelled(error: DatabaseError) {

                                }

                                override fun onDataChange(snapshot: DataSnapshot) {
                                    if (snapshot.exists()){
                                        for(dataObj in snapshot.children){
                                            if(dataObj.value.toString() == currentUser.uid)
                                            {
                                                val key = dataObj.key!!
                                                myRef.child("USER").child(mediaObjects[targetPosition].id)
                                                    .child("likedislike").child(mediaObjects[targetPosition].vnum)
                                                    .child("likedby").child(key).removeValue()
                                                break
                                            }
                                        }
                                    }
                                }
                            }
                        )
                }
                //////////////////////
            } else {
                Picasso.get().load(R.drawable.ic_thumb_up_black_24dp)
                    .placeholder(R.drawable.ic_thumb_up_black_24dp).into(holder.buttonOfLike)
                mediaObjects[targetPosition].LikedORNot = true
                mediaObjects[targetPosition].likeNumber += 1
                /////////////////////
                myRef.child("USER").child(mediaObjects[targetPosition].id).child("videolist").child(mediaObjects[targetPosition].vnum)
                    .child("likes").setValue(mediaObjects[targetPosition].likeNumber.toString())
                likedByList.add(currentUser!!.uid)
                val key = myRef.child("USER").child(mediaObjects[targetPosition].id)
                    .child("likedislike").child(mediaObjects[targetPosition].vnum)
                    .child("likedby").push().key!!
                myRef.child("USER").child(mediaObjects[targetPosition].id)
                    .child("likedislike").child(mediaObjects[targetPosition].vnum)
                    .child("likedby").child(key).setValue(currentUser.uid)
                ////////////////////
            }
            holder.numberOfLikes.text = getStringFormLikes(mediaObjects[targetPosition].likeNumber)
        }
        holder.buttonOfDislike.setOnClickListener {
            if (mediaObjects[targetPosition].LikedORNot) {
                Picasso.get().load(R.drawable.ic_thumb_up_black_white_24dp)
                    .placeholder(R.drawable.ic_thumb_up_black_white_24dp)
                    .into(holder.buttonOfLike)
                mediaObjects[targetPosition].LikedORNot = false
                mediaObjects[targetPosition].likeNumber -= 1
                holder.numberOfLikes.text = getStringFormLikes(mediaObjects[targetPosition].likeNumber)
                ///////
                myRef.child("USER").child(mediaObjects[targetPosition].id).child("videolist")
                    .child(mediaObjects[targetPosition].vnum).child("likes").setValue(mediaObjects[targetPosition].likeNumber.toString())
                likedByList.remove(currentUser!!.uid)
                myRef.child("USER").child(mediaObjects[targetPosition].id)
                    .child("likedislike").child(mediaObjects[targetPosition].vnum)
                    .child("likedby").addListenerForSingleValueEvent(
                        object:ValueEventListener
                        {
                            override fun onCancelled(error: DatabaseError) {

                            }

                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.exists()){
                                    for(dataObj in snapshot.children){
                                        if(dataObj.value.toString() == currentUser.uid)
                                        {
                                            val key = dataObj.key!!
                                            myRef.child("USER").child(mediaObjects[targetPosition].id)
                                                .child("likedislike").child(mediaObjects[targetPosition].vnum)
                                                .child("likedby").child(key).removeValue()
                                            break
                                        }
                                    }
                                }
                            }

                        }
                    )
                ///////////
            }
            if (mediaObjects[targetPosition].DislikedOrNot) {
                Picasso.get().load(R.drawable.ic_thumb_down_black_24dp)
                    .placeholder(R.drawable.ic_thumb_down_black_24dp)
                    .into(holder.buttonOfDislike)
                mediaObjects[targetPosition].DislikedOrNot = false
                mediaObjects[targetPosition].disLikeNumber -= 1
                //////////////
                myRef.child("USER").child(mediaObjects[targetPosition].id).child("videolist")
                    .child(mediaObjects[targetPosition].vnum).child("dislikes").setValue(mediaObjects[targetPosition].disLikeNumber.toString())
                if(dislikedByList.isNotEmpty()) {
                    dislikedByList.remove(currentUser!!.uid)
                    myRef.child("USER").child(mediaObjects[targetPosition].id)
                        .child("likedislike").child(mediaObjects[targetPosition].vnum)
                        .child("dislikedby").addListenerForSingleValueEvent(
                            object:ValueEventListener
                            {
                                override fun onCancelled(error: DatabaseError) {

                                }

                                override fun onDataChange(snapshot: DataSnapshot) {
                                    if (snapshot.exists()){
                                        for(dataObj in snapshot.children){
                                            if(dataObj.value.toString() == currentUser.uid)
                                            {
                                                val key = dataObj.key!!
                                                myRef.child("USER").child(mediaObjects[targetPosition].id)
                                                    .child("likedislike").child(mediaObjects[targetPosition].vnum)
                                                    .child("dislikedby").child(key).removeValue()
                                                break
                                            }
                                        }
                                    }
                                }

                            }
                        )
                }
                //////////////
            } else {
                Picasso.get().load(R.drawable.ic_thumb_down_black_blue24dp)
                    .placeholder(R.drawable.ic_thumb_down_black_blue24dp)
                    .into(holder.buttonOfDislike)
                mediaObjects[targetPosition].disLikeNumber += 1
                mediaObjects[targetPosition].DislikedOrNot = true
                ////////////////////
                myRef.child("USER").child(mediaObjects[targetPosition].id).child("videolist")
                    .child(mediaObjects[targetPosition].vnum).child("dislikes").setValue(mediaObjects[targetPosition].disLikeNumber.toString())
                dislikedByList.add(currentUser!!.uid)
                val key = myRef.child("USER").child(mediaObjects[targetPosition].id)
                    .child("likedislike").child(mediaObjects[targetPosition].vnum)
                    .child("dislikedby").push().key!!
                myRef.child("USER").child(mediaObjects[targetPosition].id)
                    .child("likedislike").child(mediaObjects[targetPosition].vnum)
                    .child("dislikedby").child(key).setValue(currentUser.uid)
                ////////////////////
            }
            holder.numberOfDislikes.text = getStringFormLikes(mediaObjects[targetPosition].disLikeNumber)
        }
        val list_friends = ArrayList<USER_FRIEND_ADMIRERS>()
        holder.buttonOfShare.setOnClickListener {
            if (currentUser != null) {
                when (currentUser.uid) {
                    !in sharedByList -> {
                        myRef.child("USER").child(currentUser.uid).child("friends")
                            .addListenerForSingleValueEvent(
                                object : ValueEventListener {
                                    override fun onCancelled(p0: DatabaseError) {}

                                    override fun onDataChange(p0: DataSnapshot) {
                                        if (p0.exists()) {
                                            val databaseList = p0.value as ArrayList<user_with_id_phoneNumber>
                                            for (i in 0 until databaseList.size) {
                                                val databaseUser = databaseList[i] as HashMap<String, String>
                                                list_friends.add(
                                                    USER_FRIEND_ADMIRERS(
                                                        databaseUser["name"]!!,
                                                        databaseUser["photo"]!!,
                                                        databaseUser["id"]!!
                                                    )
                                                )
                                            }
                                        }
                                    }

                                }
                            )
                        val view = LayoutInflater.from(context)
                            .inflate(R.layout.layout_sharing_edit_alert_dialog, null, false)
                        val alertDialog =
                            AlertDialog.Builder(context).setCancelable(true).setView(view).show()
                        view.editTextForSharing.addTextChangedListener(
                            object : TextWatcher {
                                override fun afterTextChanged(s: Editable?) {}

                                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                                }

                                @SuppressLint("SetTextI18n")
                                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                                    if (s != null) {
                                        view.caption.text = (140 - s.length).toString() + "/140 "

                                        if (s.length >= 140) {
                                            view.caption.setTextColor(Color.RED)
                                        } else {
                                            view.caption.setTextColor(Color.BLACK)
                                        }
                                    }
                                }
                            })

                        view.sendLink.setOnClickListener {
                            //                if (sheetBehavior.state != BottomSheetBehavior.STATE_EXPANDED) {
                            //                    sheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
                            //                } else {
                            //                    sheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
                            //                }

                            if (view.editTextForSharing.text.length in 1..140) {

                                if (!EmojiUtils.containsEmoji(view.editTextForSharing.text.toString())) {
                                    mediaObjects[targetPosition].ShareNumber += 1
                                    sharedByList.add(currentUser.uid)
                                    holder.numberOfShares.text =
                                        mediaObjects[targetPosition].ShareNumber.toString()
                                    //////////////////
                                    ////////////////////////////
                                    ///////////////////////////
                                    val userRef = myRef.child("USER")
                                    for (i in list_friends) {
                                        val key = userRef.child(i.id).child("feedlist").push().key!!
                                        userRef.child(i.id).child("feedlist").child(key).setValue(feedList_id_vnum(
                                            mediaObjects[targetPosition].id,
                                            mediaObjects[targetPosition].vnum
                                        ))
                                    }

                                    myRef.child("USER").child(mediaObjects[targetPosition].id)
                                        .child("videolist")
                                        .child(mediaObjects[targetPosition].vnum).child("shares")
                                        .setValue(mediaObjects[targetPosition].ShareNumber.toString())

                                    val key = myRef.child("USER").child(mediaObjects[targetPosition].id)
                                        .child("sharedby")
                                        .child(mediaObjects[targetPosition].vnum).push().key!!

                                    myRef.child("USER").child(mediaObjects[targetPosition].id)
                                        .child("sharedby")
                                        .child(mediaObjects[targetPosition].vnum).child(key).setValue(currentUser.uid)
                                    /////////////////
                                    val sharedPref =
                                        context.getSharedPreferences("USERNAME", Context.MODE_PRIVATE)
                                    val name = sharedPref.getString("name_of_person", currentUser.displayName)!!
                                    val photourl = sharedPref.getString("photo_of_person", currentUser.photoUrl.toString())!!
                                    val usercomment = UserComments(
                                        name,
                                        photourl,
                                        view.editTextForSharing.text.toString(),
                                        currentUser.uid
                                    )
                                    commentsList.add(0,
                                        usercomment
                                    )
                                    mediaObjects[targetPosition].arrayOfComments = commentsList

                                    val keyComments = myRef.child("USER").child(mediaObjects[targetPosition].id).child("comments")
                                        .child(mediaObjects[targetPosition].vnum).push().key!!
                                    myRef.child("USER").child(mediaObjects[targetPosition].id).child("comments")
                                        .child(mediaObjects[targetPosition].vnum).child(keyComments).setValue(usercomment)
                                    holder.recyclerView.adapter!!.notifyDataSetChanged()
                                    alertDialog.dismiss()
                                    adapter!!.notifyDataSetChanged()
                                } else {
                                    Toast.makeText(context, "Emojis Not Allowed.", Toast.LENGTH_LONG).show()
                                }
                            } else {
                                Toast.makeText(
                                    context,
                                    "Caption Length Cannot be greater than 140 Characters or less than 0 Characters.",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                    mediaObjects[targetPosition].id -> {
                        Toast.makeText(context,"You Cannot share your own video.",Toast.LENGTH_LONG).show()
                    }
                    else -> {
                        Toast.makeText(context,"You Can Share Video Only Once.",Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        mediaCoverImage = holder.playerView

        mFullScreenDialog = object:Dialog(context,android.R.style.Theme_Black_NoTitleBar_Fullscreen) {
            override fun onBackPressed() {
                if (fullscreen) {
                    closeDialog()
                }
                super.onBackPressed()
            }
        }

//
        val controlView: PlayerControlView = videoSurfaceView!!.findViewById(R.id.exo_controller)
        videoSurfaceView!!.controllerShowTimeoutMs = 1500
        videoSurfaceView!!.controllerHideOnTouch = true
        videoSurfaceView!!.controllerAutoShow = true
        fullScreenButton = controlView.findViewById(R.id.exo_fullscreen_icon)
        controlView.setOnTouchListener(object : OnTouchListener {
            private val gestureDetector =
                GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {

                    @SuppressLint("CommitPrefEdits")
                    override fun onDoubleTap(e: MotionEvent): Boolean {
                        if (!fullscreen) {
                            (context as Activity).requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                            if (videoSurfaceView != null) {
                                (videoSurfaceView!!.parent as ViewGroup).removeView(videoSurfaceView)
                                mFullScreenDialog.addContentView(
                                    videoSurfaceView!!,
                                    ViewGroup.LayoutParams(
                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                        ViewGroup.LayoutParams.MATCH_PARENT
                                    )
                                )

                                fullscreen = true
                                mFullScreenDialog.show()
                            }
                        } else {
                            (context as Activity).requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
                            closeDialog()
                        }
                        return super.onDoubleTap(e)
                    }
                })

            override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
                gestureDetector.onTouchEvent(motionEvent)

                return true

            }
        })

        fullScreenButton.setOnClickListener {
            if (!fullscreen) {
                (context as Activity).requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
                if (videoSurfaceView != null) {
                    (videoSurfaceView!!.parent as ViewGroup).removeView(videoSurfaceView)
                    mFullScreenDialog.addContentView(
                        videoSurfaceView!!,
                        ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                    )
                    fullScreenButton.setImageDrawable(
                        ContextCompat.getDrawable(
                            context,
                            R.drawable.exo_controls_fullscreen_exit
                        )
                    )

                    fullscreen = true
                    mFullScreenDialog.show()
                }
            } else {
                (context as Activity).requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
                closeDialog()
            }
        }
        progressBar = holder.progressBar
        viewHolderParent = holder.itemView
        requestManager = holder.requestManager
        mediaContainer = holder.mainMediaFrame
        videoSurfaceView!!.player = videoPlayer
        val dataSourceFactory: DataSource.Factory = DefaultDataSourceFactory(context, Util.getUserAgent(context, AppName))
        val mediaUrl = Uri.parse(mediaObjects[targetPosition].videoUrl)
        //val mediaUrl= RawResourceDataSource.buildRawResourceUri(R.raw.video_sample)
        //CacheDataSourceFactory(context,100*1024*1024,5*1024*1024)
        val videoSource = ExtractorMediaSource(mediaUrl,dataSourceFactory,DefaultExtractorsFactory(),null,null)
        videoPlayer?.prepare(videoSource)
        videoPlayer?.playWhenReady = true
    }
    private fun getVisibleVideoSurfaceHeight(playPosition: Int): Int {
        val at = playPosition - (Objects.requireNonNull(
            layoutManager
        ) as LinearLayoutManager).findFirstVisibleItemPosition()
        Log.d(TAG, "getVisibleVideoSurfaceHeight: at: $at")
        val child = getChildAt(at) ?: return 0
        val location = IntArray(2)
        child.getLocationInWindow(location)
        return if (location[1] < 0) {
            location[1] + videoSurfaceDefaultHeight
        } else {
            screenDefaultHeight - location[1]
        }
    }
    private fun removeVideoView(videoView: PlayerView) {
        val parent = videoView.parent as ViewGroup?
        var index=-1
        if(parent!=null) {
            index = parent.indexOfChild(videoView)

            if (index >= 0) {
                parent.removeViewAt(index)
                isVideoViewAdded = false
                //viewHolderParent!!.setOnClickListener(null)
            }
        }
    }
    private fun addVideoView() {
        mediaContainer!!.addView(videoSurfaceView)
        isVideoViewAdded = true
        videoSurfaceView!!.requestFocus()
        videoSurfaceView!!.visibility = View.VISIBLE
        videoSurfaceView!!.alpha = 1f
        if(mediaCoverImage!=null) {
            mediaCoverImage!!.visibility = View.GONE
        }
    }

    private fun resetVideoView() {
        if (isVideoViewAdded) {
            removeVideoView(videoSurfaceView!!)
            playPosition = -1
            videoSurfaceView!!.visibility = View.INVISIBLE
            if (mediaCoverImage!=null) {
                mediaCoverImage!!.visibility = View.VISIBLE
            }
        }
    }

    fun releasePlayer() {
        if (videoPlayer != null) {
            videoPlayer!!.release()
            videoPlayer = null
        }
        viewHolderParent = null
    }
    fun onPausePlayer() {
        if (videoPlayer != null) {
            videoPlayer!!.stop(true)
        }
    }


    fun setMediaObjects(mediaObjects: ArrayList<UserFeed>) {
        this.mediaObjects = mediaObjects
    }

    fun getStringFormLikes(disLikeNumber: Int):String {
        return when {
            disLikeNumber> (10.0).pow(6) -> {
                (((disLikeNumber/(10.0).pow(5)).toInt())/10.0).toString()+"M"
            }
            disLikeNumber>(10.0).pow(3) -> {
                (((disLikeNumber/(10.0).pow(2)).toInt())/10.0).toString()+"K"
            }
            else -> {
                disLikeNumber.toString()
            }
        }
    }

    fun closeDialog(){
        (context as Activity).requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
        if (videoSurfaceView != null) {
            (videoSurfaceView!!.parent as ViewGroup).removeView(videoSurfaceView)
            if (mediaContainer != null) {
                mediaContainer!!.addView(videoSurfaceView)
            }

            fullScreenButton.setImageDrawable(
                ContextCompat.getDrawable(
                    context,
                    R.drawable.exo_controls_fullscreen_enter
                )
            )
            fullscreen = false
            mFullScreenDialog.dismiss()
        }
    }
}
package com.theatron2.uiforthreaton.ui.feed

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetView
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.ms.square.android.expandabletextview.ExpandableTextView
import com.squareup.picasso.Picasso
import com.theatron2.uiforthreaton.Activities.Main2Activity
import com.theatron2.uiforthreaton.Activities.user_with_id
import com.theatron2.uiforthreaton.R
import com.theatron2.uiforthreaton.adapterForAllClasses.CustomAdapterOFFeedForComments
import de.hdodenhof.circleimageview.CircleImageView
import kotlin.math.pow

@Suppress("UNCHECKED_CAST", "CAST_NEVER_SUCCEEDS")
class PlayerViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
//    var bookmark: ImageButton =itemView.findViewById(R.id.BookmarkForFeed)
    var image: CircleImageView =itemView.findViewById(R.id.imageForFeed)
    var profilename: TextView =itemView.findViewById(R.id.NameForFeed)
    var copy: MaterialButton =itemView.findViewById(R.id.copyLinkforFeed)
    var playerView: PlayerView =itemView.findViewById(R.id.video_view)
    var mediaCoverImage:ImageView = itemView.findViewById(R.id.ivMediaCoverImage);
    var mute: AppCompatImageView =itemView.findViewById(R.id.exo_sound2)
    var views: TextView =itemView.findViewById(R.id.viewsForFeed)
    var numberOfLikes: TextView =itemView.findViewById(R.id.numberOfLikes)
    var numberOfDislikes: TextView =itemView.findViewById(R.id.numberOfDislikes)
    var numberOfShares: TextView =itemView.findViewById(R.id.numberOfShares)
    var buttonOfLike: ImageButton =itemView.findViewById(R.id.buttonOfLike)
    var buttonOfShare: TextView =itemView.findViewById(R.id.buttonOfShare)
    var buttonOfDislike: ImageButton =itemView.findViewById(R.id.buttonOfDislike)
    var title: TextView =itemView.findViewById(R.id.titleForFeed)
    var details: ExpandableTextView =itemView.findViewById(R.id.expandable_text_View)
//    var commentForLastUserLayout: LinearLayout =itemView.findViewById(R.id.layoutForFeed)
//    var commentForLastUserImageView: CircleImageView =itemView.findViewById(R.id.imageViewForCommentsForFeed)
//    var commentForLastUserTextView: TextView =itemView.findViewById(R.id.textViewForCommentsForFeed)
//    var commentForLastUserComments: TextView =itemView.findViewById(R.id.commentsForCommentsForFeed)
    val recyclerView:RecyclerView = itemView.findViewById(R.id.recyclerViewForComments)
    val progressBar =itemView.findViewById<ProgressBar>(R.id.progressBar2)
    val mainMediaFrame: FrameLayout =itemView.findViewById(R.id.frameLayout)
    val admireTextView:MaterialButton = itemView.findViewById(R.id.admireTextView2)
//    var fullscreenButton: ImageButton  = playerView.findViewById(R.id.exo_fullscreen_icon)
//    val controlView: PlayerControlView = mainMediaFrame.findViewById(R.id.exo_controller)
    val likedByList=ArrayList<String>()
    val dislikedByList=ArrayList<String>()
    val viewList=ArrayList<String>()
    val currentUser = FirebaseAuth.getInstance().currentUser
    var commentsList=ArrayList<UserComments>()
    lateinit var requestManager:RequestManager
    var parent :View=itemView



    fun onBind(feed: UserFeed,context:Context,requestManage: RequestManager) {
        parent.tag = this
        requestManager=requestManage
        requestManager.load(feed.thumbnail).into(mediaCoverImage)
        val myRef = FirebaseDatabase.getInstance().reference
        myRef.child("USER").child(feed.id).child("likedislike")
            .child(feed.vnum).addListenerForSingleValueEvent(
                object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {}

                    override fun onDataChange(p0: DataSnapshot) {
                        if (p0.exists()) {
                            val databaseUser = p0.value as HashMap<String, ArrayList<String>>

                            if (databaseUser.containsKey("likedby")) {
                                for (childSnapshot in p0.child("likedby").children) {

                                    if (childSnapshot.value is String){
                                        likedByList.add(childSnapshot.value as String)
                                    }else{
                                        val ch = childSnapshot.value as HashMap<String, String>
                                        ch["id"]?.let { likedByList.add(it) }
                                    }
                                    Log.e("player view holder", childSnapshot.value.toString())
                                    //likedByList.add(childSnapshot.value as String)
                                }


                                if (likedByList.contains(currentUser!!.uid)) {
                                    feed.LikedORNot = true
                                    Picasso.get().load(R.drawable.ic_thumb_up_black_24dp)
                                        .placeholder(R.drawable.ic_thumb_up_black_24dp)
                                        .into(buttonOfLike)
                                }
                            }
                            if (databaseUser.containsKey("dislikedby")) {
                                for (childSnapshot in p0.child("dislikedby").children) {

                                    if(childSnapshot.value is String){
                                        dislikedByList.add(childSnapshot.value as String)
                                    }else{
                                        val ch = childSnapshot.value as HashMap<String, String>
                                        ch["id"]?.let { dislikedByList.add(it) }
                                    }

                                    Log.e( "player video holder",  childSnapshot.value.toString())
                                    //dislikedByList.add(childSnapshot.value as String)
                                }

                                if (dislikedByList.contains(currentUser!!.uid)) {
                                    feed.DislikedOrNot = true
                                    Picasso.get().load(R.drawable.ic_thumb_down_black_blue24dp)
                                        .placeholder(R.drawable.ic_thumb_down_black_blue24dp)
                                        .into(buttonOfDislike)

                                }
                            }
                            if (databaseUser.containsKey("viewlist")) {
                                for (childSnapshot in p0.child("viewlist").children) {
                                    viewList.add(childSnapshot.value as String)
                                }
                                views.text = feed.view.toString()
                            }
                        }
                    }
                }
            )
        commentsList=ArrayList()
        ////////////
        recyclerView.layoutManager = LinearLayoutManager(context)
        val adapter = CustomAdapterOFFeedForComments(context,commentsList,feed.id,feed.vnum)
        recyclerView.adapter=adapter

        myRef.child("USER").child(feed.id).child("comments").child(feed.vnum)
            .addListenerForSingleValueEvent(
                object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {}

                    override fun onDataChange(p0: DataSnapshot) {
                        if (p0.exists()) {
//                            commentsList=ArrayList()
                            //  try {
                            for (i in p0.children) {

                                val commentUserForData = i.value as HashMap<String, String>
                                Log.e(
                                    "palyere videw holder",
                                    commentUserForData["id"].toString()
                                )
                                myRef.child("USER").child(commentUserForData["id"]!!)
                                    .addListenerForSingleValueEvent(
                                        object : ValueEventListener {
                                            override fun onCancelled(error: DatabaseError) {
                                            }

                                            override fun onDataChange(snapshot: DataSnapshot) {
                                                if (snapshot.exists()) {
                                                    Log.e("snapshot player view h", snapshot.toString())
                                                    Log.e(
                                                        "user",
                                                        snapshot.child("name").value.toString()
                                                    )
                                                    Log.e(
                                                        "user",
                                                        snapshot.child("photourl").value.toString()
                                                    )
                                                    Log.e(
                                                        "user",
                                                        commentUserForData["comments"].toString()
                                                    )
                                                    Log.e(
                                                        "user",
                                                        commentUserForData["id"].toString()
                                                    )
                                                    val userCommentOnce = UserComments(
                                                        snapshot.child("name").value.toString(),
                                                        snapshot.child("photourl").value.toString(),
                                                        commentUserForData["comments"]!!,
                                                        commentUserForData["id"]!!
                                                    )
                                                    if (userCommentOnce !in commentsList) {
                                                        commentsList.add(0, userCommentOnce)
                                                    }
                                                }
                                                adapter.notifyDataSetChanged()
                                            }
                                        }
                                    )
                            }
//                            } catch (e: Exception) {
//                                e.printStackTrace()
//                            }
                        }
                    }
                }
            )

        views.text = getStringFormLikes(feed.view)
//        bookmark.setOnClickListener {
//            if (!feed.bookmark) {
//                Picasso.get().load(R.drawable.ic_star_black_24dp)
//                    .placeholder(R.drawable.ic_star_black_24dp).into(bookmark)
//                feed.bookmark = true
//            } else {
//                Picasso.get().load(R.drawable.ic_star_border_black_24dp)
//                    .placeholder(R.drawable.ic_star_border_black_24dp).into(bookmark)
//                feed.bookmark = false
//            }
//        }
        var isChecked = false
        val listADMIRINGTruly = ArrayList<String>()
        if (currentUser != null) {
            myRef.child("USER").child(currentUser.uid).child("admiring")
                .addListenerForSingleValueEvent(
                    object : ValueEventListener {
                        override fun onCancelled(p0: DatabaseError) {}

                        @SuppressLint("SetTextI18n")
                        override fun onDataChange(p0: DataSnapshot) {
                            if (p0.exists()) {
                                for (mapOfAdmiring in p0.children ){
                                    val databaseUser = mapOfAdmiring.value as java.util.HashMap<String, String>
                                    if (databaseUser["id"] != currentUser.uid) {
                                        listADMIRINGTruly.add(databaseUser["id"]!!)
                                    }
                                }
                                if (feed.id in listADMIRINGTruly) {
                                    admireTextView.text = "Admiring"
                                    isChecked = true
                                } else {
                                    admireTextView.text = "Admire"
                                    isChecked = false
                                }
                            }
                        }
                    }
                )
        }
        val myRef_user_userId = myRef.child("USER").child(feed.id)
        admireTextView.setOnClickListener {
            if (isChecked)
            {
                isChecked = !isChecked
                admireTextView.text = "Admire"
//                    val viewForAlertDialog= LayoutInflater.from(this).inflate(R.layout.background_for_alert_dialog,null,false)
//                    val alertDialog= AlertDialog.Builder(this).setView(viewForAlertDialog)
//                        .setCancelable(false)
//                        .show()
//                    val textView=viewForAlertDialog.findViewById<TextView>(R.id.txt_dia)!!
//                    val yesButton=viewForAlertDialog.findViewById<Button>(R.id.btn_yes)!!
//                    val noButton=viewForAlertDialog.findViewById<Button>(R.id.btn_no)!!
//                    val imageView=viewForAlertDialog.findViewById<CircleImageView>(R.id.imageViewForAlertDialog)
//                    Picasso.get().load(admiring[position].photo).placeholder(R.drawable.ic_account_circle_black_24dp).into(imageView)
//                    textView.text="Are you sure you want to stop admiring  ${holder.textView.text} ? "
//                    yesButton.setOnClickListener {
//                        //////////////////////////////
//                        holder.switch.isChecked=false
//                        admiring[position].friends=holder.switch.isChecked

                myRef_user_userId.addListenerForSingleValueEvent(
                    object :ValueEventListener
                    {
                        override fun onCancelled(p0: DatabaseError) {
                            Toast.makeText(context, p0.message, Toast.LENGTH_LONG).show()
                        }

                        override fun onDataChange(p0: DataSnapshot) {
                            val admirerscount=p0.child("admirerscount").value as String
                            myRef_user_userId.child("admirerscount").setValue((admirerscount.toInt()-1).toString())
                        }
                    }
                )
                /////////////////////////
                myRef_user_userId.child("admirers").addListenerForSingleValueEvent(
                    object :ValueEventListener
                    {
                        override fun onCancelled(p0: DatabaseError) {}

                        override fun onDataChange(p0: DataSnapshot) {
                            if (p0.exists())
                            {
                                for(databaseList in p0.children)
                                {
                                    val databaseUser = databaseList.value as java.util.HashMap<String, String>
                                    if(databaseUser["id"]== currentUser!!.uid)
                                    {
                                        val key = databaseList.key!!
                                        myRef_user_userId.child("admirers").child(key).removeValue()
                                        break
                                    }
                                }
                            }
                        }
                    }
                )
                /////////////////////////
                if(currentUser!=null) {
                    myRef.child("USER").child(currentUser.uid).child("admiring")
                        .addListenerForSingleValueEvent(
                            object : ValueEventListener {
                                override fun onCancelled(p0: DatabaseError) {}
                                override fun onDataChange(p0: DataSnapshot) {
                                    if (p0.exists()) {
                                        for (databaseList in p0.children) {
                                            val databaseUser =
                                                databaseList.value as java.util.HashMap<String, String>
                                            if (databaseUser["id"] == feed.id) {
                                                val key = databaseList.key!!
                                                myRef.child("USER").child(currentUser.uid).child("admiring").child(key).removeValue()
                                            }
                                        }
                                    }
                                }
                            }
                        )
                }
                //////////////////////////////
//                    }
//                    noButton.setOnClickListener {
//                        holder.switch.isChecked=true
//                        admiring[position].friends=holder.switch.isChecked
//                        alertDialog.dismiss()
//                    }
                Toast.makeText(context,"You Stopped Admiring ${feed.UserName}",Toast.LENGTH_SHORT).show()
            }
            else
            {
                isChecked=true
                admireTextView.text = "Admiring"
                myRef_user_userId.addListenerForSingleValueEvent(
                    object :ValueEventListener
                    {
                        override fun onCancelled(p0: DatabaseError) {
                            Toast.makeText(context, p0.message, Toast.LENGTH_LONG).show()
                        }

                        override fun onDataChange(p0: DataSnapshot) {
                            val admirerscount=p0.child("admirerscount").value as String
                            myRef_user_userId.child("admirerscount").setValue((admirerscount.toInt()+1).toString())
                        }
                    }
                )
                if(currentUser!=null) {

                    val key = myRef_user_userId.child("admirers").push().key!!
                    myRef_user_userId.child("admirers").child(key).setValue(user_with_id(
                        currentUser.displayName!!,
                        currentUser.photoUrl.toString(),
                        currentUser.uid
                    ))
                    val myRef_current_user_userid_admiring = myRef.child("USER").child(currentUser.uid).child("admiring")
                    val key2 = myRef_current_user_userid_admiring.push().key!!
                    myRef_current_user_userid_admiring.child(key2).setValue(user_with_id(feed.UserName, feed.image, feed.id))
                    Toast.makeText(
                        context,
                        "You Started Admiring ${feed.UserName}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        if(feed.id == currentUser!!.uid)
        {
            admireTextView.visibility = View.INVISIBLE
        }

        Picasso.get().load(feed.image).placeholder(R.drawable.ic_account_circle_black_24dp).into(image)
        profilename.text = feed.UserName
        copy.setOnClickListener {
            val copyText = feed.videoUrl
            val clipboard: ClipboardManager =
                context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = android.content.ClipData.newPlainText("Clip", copyText)
            Toast.makeText(context, "Link Copied", Toast.LENGTH_LONG).show()
            clipboard.setPrimaryClip(clip)
        }

        numberOfLikes.text = getStringFormLikes(feed.likeNumber)
        numberOfDislikes.text = getStringFormLikes(feed.disLikeNumber)
        numberOfShares.text = getStringFormLikes(feed.ShareNumber)
//
        if (feed.LikedORNot) {
            Picasso.get().load(R.drawable.ic_thumb_up_black_24dp)
                .placeholder(R.drawable.ic_thumb_up_black_24dp).into(buttonOfLike)
        } else {
            Picasso.get().load(R.drawable.ic_thumb_up_black_white_24dp)
                .placeholder(R.drawable.ic_thumb_up_black_white_24dp).into(buttonOfLike)
        }
        if (feed.DislikedOrNot) {
            Picasso.get().load(R.drawable.ic_thumb_down_black_blue24dp)
                .placeholder(R.drawable.ic_thumb_down_black_blue24dp).into(buttonOfDislike)
        } else {
            Picasso.get().load(R.drawable.ic_thumb_down_black_24dp)
                .placeholder(R.drawable.ic_thumb_down_black_24dp).into(buttonOfDislike)
        }

        title.text = feed.Title
        details.text = feed.details
//        commentForLastUserLayout.setOnClickListener {
//            val intent = Intent(
//                context,
//                CommentsActivity::class.java
//            )
//            intent.putExtra("Array", commentsList)
//            intent.flags= Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK
//            context.startActivity(intent)
//        }

        val sharedPRRR = context.getSharedPreferences("USERNAME",Context.MODE_PRIVATE)
        if(sharedPRRR.getBoolean("firsttimeshare",true) && !sharedPRRR.getBoolean("firsttime",true)) {

            TapTargetView.showFor(context as Activity,  // `this` is an Activity
                TapTarget.forView(
                    buttonOfShare,
                    "Share",
                    "Share Videos Among Friends with 140 Character Caption."
                )
                    .outerCircleAlpha(0.96f)
                    .targetCircleColor(R.color.white)
                    .titleTextSize(20)
                    .titleTextColor(R.color.transparentBlack)
                    .descriptionTextSize(14)
                    .textTypeface(Typeface.SANS_SERIF)
                    .descriptionTextColor(R.color.darkblue)
                    .descriptionTextAlpha(1.0f)
                    .drawShadow(true)
                    .cancelable(false)
                    .tintTarget(true)
                    .transparentTarget(true)
                    .targetRadius(40),
                object : TapTargetView.Listener() {
                    override fun onTargetClick(view: TapTargetView) {
                        super.onTargetClick(view)
                        view.dismiss(true)
                        sharedPRRR.edit().putBoolean("firsttimeshare", false).apply()
                    }
                })
        }


        profilename.setOnClickListener {
                val intent= Intent(context,
                    Main2Activity::class.java)
                intent.putExtra("SuggestId",feed.id)
                context.startActivity(intent)
        }

        image.setOnClickListener {
            val intent= Intent(context,
                Main2Activity::class.java)
            intent.putExtra("SuggestId",feed.id)
            context.startActivity(intent)
        }


//

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

}
package com.theatron2.uiforthreaton.Activities

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.theatron2.uiforthreaton.R
import com.theatron2.uiforthreaton.adapterForAllClasses.CustomAdapterForProfileFragment
import com.theatron2.uiforthreaton.ui.profile.ProfileUser
import kotlinx.android.synthetic.main.content_main2.*
import kotlinx.android.synthetic.main.fragment_profile.profile_image
import kotlinx.android.synthetic.main.fragment_profile.profile_name
import kotlinx.android.synthetic.main.fragment_profile.recyclerViewForProfileFrg
import kotlinx.android.synthetic.main.fragment_profile.sharecount
import kotlinx.android.synthetic.main.fragment_profile.user_name
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.math.pow


@Suppress("UNCHECKED_CAST", "CAST_NEVER_SUCCEEDS", "LocalVariableName")
class Main2Activity : AppCompatActivity() {
    private var videoList=ArrayList<ProfileUser>()
    var listADMIRINGTruly = ArrayList<String>()
    val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    lateinit var currentUser:FirebaseUser
    lateinit var adapter: CustomAdapterForProfileFragment
    private var myRef= FirebaseDatabase.getInstance().reference
    var photoUrl:String=""
    var username:String=""
    var name:String=""
    var countOfShares:Int=0
    var vnum = 0
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_of_others)
        val sharedPreferences = getSharedPreferences("USERNAME", Context.MODE_PRIVATE).edit()
        var userId=""
        userId = if (intent.extras!=null) {
            sharedPreferences.putString("userIDD",intent.extras!!.getString("SuggestId").toString())
            sharedPreferences.apply()
            intent.extras!!.getString("SuggestId").toString()
        } else{
            val sharedPreferences2 = getSharedPreferences("USERNAME", Context.MODE_PRIVATE)
            sharedPreferences2.getString("userIDD","781xlHykM4avnqu612AzBgJBlqt2").toString()
        }
        adapter= CustomAdapterForProfileFragment(this,videoList,123)
        recyclerViewForProfileFrg.layoutManager=
            GridLayoutManager(this,2, RecyclerView.VERTICAL,false)
        recyclerViewForProfileFrg.hasFixedSize()
        recyclerViewForProfileFrg.adapter= adapter
        if(userId != null) {
            val myRef_user_userId = myRef.child("USER").child(userId)
            myRef_user_userId.addValueEventListener(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {}
                override fun onDataChange(p0: DataSnapshot) {
                    // try {
                    Log.e(" Main2 activity snap", p0.toString())
                    Log.e(" Main2 activity name", p0.child("name").toString())
                    name = p0.child("name").value.toString()
                    photoUrl = p0.child("photourl").value.toString()
                    val shareCount = p0.child("sharescount").value.toString()
                    Log.e(" Main2 activ sharec", shareCount)
                    val followCount = p0.child("admirerscount").value.toString()
                    if (profile_name != null) {
                        profile_name.text = name
                    }
                    if (user_name != null) {
                        user_name.text = "@" + name.toLowerCase(Locale.ROOT).replace(" ", "")
                    }
                    if (profile_image != null) {
                        Picasso.get().load(photoUrl)
                            .placeholder(R.drawable.ic_account_circle_black_24dp)
                            .into(profile_image)
                    }
                    if (shareCount == "null") {
                        sharecount.text = getStringFormLikes(0)
                    }
                    if (sharecount != null) {
                        sharecount.text = getStringFormLikes(shareCount.toInt())
                    }

//                if(admirerscount!=null) {
//                    admirerscount.text = getStringFormLikes(followCount.toInt())
//                }
//                }catch (e:Exception){
//                        e.printStackTrace()
//                    }
                }
            })

            myRef_user_userId.child("videolist")
                .addListenerForSingleValueEvent(
                    object : ValueEventListener {
                        override fun onCancelled(p0: DatabaseError) {}

                        override fun onDataChange(p0: DataSnapshot) {
                            if (p0.exists()) {
                                //try {

                                for (arrayList in p0.children) {

                                    val databaseUser =
                                        arrayList.value as HashMap<String, String>
                                    Log.e( "Main 2 activity",arrayList.value.toString() )

                                    if (databaseUser["thumbnailphoto"].isNullOrEmpty()){
                                        databaseUser["thumbnailphoto"] = "https://www.pngkit.com/png/full/267-2678423_bacteria-video-thumbnail-default.png"
                                    }
                                    if (databaseUser["photo"].isNullOrEmpty()){
                                        databaseUser["photo"] = "https://www.ibts.org/wp-content/uploads/2017/08/iStock-476085198.jpg"
                                    }
                                    if(databaseUser["date"].isNullOrEmpty()){
                                        databaseUser["date"] = "2020-01-12"
                                    }
                                    if(databaseUser["time"].isNullOrEmpty()){
                                        databaseUser["time"] =  "10:10:55"
                                    }
                                    if(databaseUser["name"].isNullOrEmpty()){
                                        databaseUser["name"] = "no name"
                                    }
                                    if(databaseUser["title"].isNullOrEmpty()){
                                        databaseUser["title"] = "no title"
                                    }
                                    if(databaseUser["url"].isNullOrEmpty()){
                                        databaseUser["url"] = "https://firebasestorage.googleapis.com/v0/b/theatronfinal.appspot.com/o/videos%2FmN8VTp8QxmT1bD5Th9s0b5MahjQ23.mp4?alt=media&token=6d582c9a-4a09-4ad8-8111-528dde677f54"
                                    }
                                    if(databaseUser["id"].isNullOrEmpty()){
                                        //id of marco pilloni, Ios developer
                                        databaseUser["id"] = "mMr3YSDUlhXXCsyiHIs95L1klMc2"
                                    }
                                    if(databaseUser["desc"].isNullOrEmpty()){
                                        databaseUser["desc"] = "no description"
                                    }
                                    if (databaseUser["vnum"].isNullOrEmpty()){
                                        databaseUser["vnum"] = (vnum+1).toString()
                                    }
                                    vnum = databaseUser["vnum"]!!.toInt()
                                    if(databaseUser["view"].toString().isEmpty()||databaseUser["view"].toString()=="null"){
                                        databaseUser["view"] = "0"
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
                                    videoList.add(
                                        ProfileUser(
                                            databaseUser["name"]!!,
                                            databaseUser["title"]!!,
                                            databaseUser["url"]!!,
                                            databaseUser["thumbnailphoto"]!!,
                                            databaseUser["id"]!!,
                                            databaseUser["desc"]!!,
                                            databaseUser["view"].toString(),
                                            databaseUser["likes"].toString(),
                                            databaseUser["dislikes"].toString(),
                                            databaseUser["shares"].toString(),
                                            databaseUser["photo"]!!,
                                            databaseUser["vnum"]!!,
                                            databaseUser["date"]!!,
                                            databaseUser["time"]!!
                                        )
                                    )
                                    countOfShares += databaseUser["shares"].toString().toInt()
                                }
//                                }catch (e:Exception){
//                                    e.printStackTrace()
//                                }
                                adapter.notifyDataSetChanged()
                            }
                            myRef_user_userId.child("sharescount").setValue(countOfShares)
                        }
                    })
            var isChecked = false

            if (mAuth.currentUser != null) {
                currentUser = mAuth.currentUser!!
                myRef.child("USER").child(currentUser.uid).child("admiring")
                    .addListenerForSingleValueEvent(
                        object : ValueEventListener {
                            override fun onCancelled(p0: DatabaseError) {}

                            @SuppressLint("SetTextI18n")
                            override fun onDataChange(p0: DataSnapshot) {
                                if (p0.exists()) {

                                    for (mapOfAdmiring in p0.children) {
                                        val databaseUser =
                                            mapOfAdmiring.value as HashMap<String, String>

                                        if (databaseUser["id"] != currentUser.uid) {
                                            listADMIRINGTruly.add(databaseUser["id"]!!)
                                        }
                                    }
                                    if (userId in listADMIRINGTruly) {
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
                        object : ValueEventListener {
                            override fun onCancelled(p0: DatabaseError) {
                                Toast.makeText(this@Main2Activity, p0.message, Toast.LENGTH_LONG)
                                    .show()
                            }

                            override fun onDataChange(p0: DataSnapshot) {
                                Log.e( "Main 2 Activity", p0.child("admirerscount").value.toString())

                                val admirerscount = p0.child("admirerscount").value as String
                                myRef_user_userId.child("admirerscount")
                                    .setValue((admirerscount.toInt() - 1).toString())
                            }
                        }
                    )
                    //////////////////////////////////////////////////////////////////////////////////
                    //////////////////////////////////////////////////////////////////////////////////
                    myRef_user_userId.child("admirers").addListenerForSingleValueEvent(
                        object :ValueEventListener
                        {
                                override fun onCancelled(p0: DatabaseError) {}

                                override fun onDataChange(p0: DataSnapshot) {

                                    for (databaseList in p0.children){
                                        val databaseUser = databaseList.value as HashMap<String,String>
                                        if (databaseUser["id"]==currentUser.uid){
                                            val key = databaseList.key!!
                                            myRef_user_userId.child("admirers").child(key).removeValue()
                                            break
                                        }
                                    }
                                }
                            }
                        )
                        /////////////////////////
                        myRef.child("USER").child(currentUser.uid).child("admiring").addListenerForSingleValueEvent(
                            object :ValueEventListener
                            {
                                override fun onCancelled(p0: DatabaseError) {}
                                override fun onDataChange(p0: DataSnapshot) {
                                    if (p0.exists()) {
                                        for (databaseList in p0.children) {
                                            val databaseUser =
                                                databaseList.value as HashMap<String, String>
                                            if (databaseUser["id"] == userId) {
                                                val key = databaseList.key!!
                                                myRef.child("USER").child(currentUser.uid)
                                                    .child("admiring").child(key).removeValue()
                                            }
                                        }
                                    }
                                }
                            }
                        )
                        //////////////////////////////
//                    }
//                    noButton.setOnClickListener {
//                        holder.switch.isChecked=true
//                        admiring[position].friends=holder.switch.isChecked
//                        alertDialog.dismiss()
//                    }
                    Toast.makeText(this,"You Stopped Admiring $name",Toast.LENGTH_SHORT).show()
                }
                else
                {
                    isChecked=true
                    admireTextView.text = "Admiring"
                    myRef_user_userId.addListenerForSingleValueEvent(
                        object : ValueEventListener {
                            override fun onCancelled(p0: DatabaseError) {
                                Toast.makeText(this@Main2Activity, p0.message, Toast.LENGTH_LONG)
                                    .show()
                            }

                            override fun onDataChange(p0: DataSnapshot) {


                                Log.e( "Main 2 Activity",p0.child("admirerscount").value.toString() )
                                val admirerscount = p0.child("admirerscount").value as String
                                myRef_user_userId.child("admirerscount")
                                    .setValue((admirerscount.toInt() + 1).toString())
                            }
                        }
                    )

                    val key = myRef_user_userId.child("admirers").push().key!!
                    myRef_user_userId.child("admirers").child(key).setValue(user_with_id(currentUser.displayName!!, currentUser.photoUrl.toString(),currentUser.uid))

                    val myRef_current_user_userid_admiring = myRef.child("USER").child(currentUser.uid).child("admiring")
                    val keyAdmiring = myRef_current_user_userid_admiring.push().key!!
                    Log.e("main2activity name", name)
                    Log.e("main2activity photo", photoUrl)
                    Log.e("main2activity uid", userId)
                    myRef_current_user_userid_admiring.child(keyAdmiring).setValue(user_with_id(name, photoUrl,userId))

                    Toast.makeText(this,"You Started Admiring $name",Toast.LENGTH_SHORT).show()
                }
            }

            if(userId == currentUser.uid)
            {
                admireTextView.visibility = View.INVISIBLE
            }
        }
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

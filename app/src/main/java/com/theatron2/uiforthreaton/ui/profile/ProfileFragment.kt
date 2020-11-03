@file:Suppress("UNCHECKED_CAST", "DEPRECATION", "NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")

package com.theatron2.uiforthreaton.ui.profile

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.abedelazizshe.lightcompressorlibrary.VideoCompressor
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import com.theatron2.uiforthreaton.Activities.Edit_profile
import com.theatron2.uiforthreaton.Activities.FriendRequestActivity
import com.theatron2.uiforthreaton.Activities.UserTypeActivity
import com.theatron2.uiforthreaton.EmojiUtils
import com.theatron2.uiforthreaton.R
import com.theatron2.uiforthreaton.adapterForAllClasses.CustomAdapterForProfileFragment
import kotlinx.android.synthetic.main.alert_dialog_tit_desc_view.view.*
import kotlinx.android.synthetic.main.fragment_profile.*
import wseemann.media.FFmpegMediaMetadataRetriever
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.max
import kotlin.math.pow


@Suppress("DEPRECATION", "CAST_NEVER_SUCCEEDS", "SENSELESS_COMPARISON")
class ProfileFragment : Fragment() {
    private var videoList = ArrayList<ProfileUser>()
    val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    lateinit var adapter: CustomAdapterForProfileFragment
    private var myRef = FirebaseDatabase.getInstance().reference
    private var storageRef = FirebaseStorage.getInstance().reference
    var photoUrl: String = ""
    var username: String = ""
    var name: String = ""
    var thumbList = ArrayList<ProfileUser>()
    var countOfShares: Int = 0
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                159
            )
        }

        videoList = ArrayList()
        thumbList = ArrayList()
        // swipeForProfile.setOnRefreshListener {
        //  updateContent()
        //}

        /////////////////
        //videoList
        val drawerLayout: DrawerLayout = view.findViewById(R.id.drawer_layout)
        val navView: NavigationView = view.findViewById(R.id.nav_view)
        val navController = findNavController()
        navView.setupWithNavController(navController)
        val sharedPreference =
            requireContext().getSharedPreferences("USERNAME", Context.MODE_PRIVATE)
        val type = sharedPreference.getString("type", "0")!!
        if (type == "2") {
            //typeOfProfile.text="ADMIRERS"
            updateContent()
            adapter = CustomAdapterForProfileFragment(requireContext(), videoList)
            recyclerViewForProfileFrg.layoutManager =
                GridLayoutManager(requireContext(), 2, RecyclerView.VERTICAL, false)
            recyclerViewForProfileFrg.hasFixedSize()
            recyclerViewForProfileFrg.adapter = adapter
            imageButtonPf.setOnClickListener {
                if (ContextCompat.checkSelfPermission(
                        requireContext(),
                        android.Manifest.permission.READ_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    val intent =
                        Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
                    intent.type = "video/*"
                    startActivityForResult(intent, 1234)
                }
            }
            edit_profile.setOnClickListener {
                val intent = Intent(context, Edit_profile::class.java)
                startActivity(intent)

            }
        } else if (type == "1") {
            //typeOfProfile.text="FRIENDS"
            edit_profile.visibility = View.GONE
            imageButtonPf.visibility = View.INVISIBLE
            recyclerViewForProfileFrg.visibility = View.GONE
            updateContentOnlyFriends()
        }
        imageViewForPf.setOnClickListener {
            if (!drawerLayout.isDrawerVisible(navView)) {
                drawerLayout.openDrawer(navView)
            } else {
                drawerLayout.closeDrawer(navView)
            }
        }
        logout.setOnClickListener {
            // GraphRequest(AccessToken.getCurrentAccessToken(),"/me/permission/",null,HttpMethod.DELETE).executeAsync()
            mAuth.signOut()

            val intent = Intent(requireContext(), UserTypeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }
//        buttonHome.setOnClickListener {
//            val url = "https://www.theatron.in/home"
//            val i = Intent(Intent.ACTION_VIEW)
//            i.data = Uri.parse(url)
//            startActivity(Intent.createChooser(i,"Choose Browser..."))
//        }
        buttonAbout.setOnClickListener {
            val url = "https://www.theatron.in/"
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(url)
            startActivity(Intent.createChooser(i, "Choose Browser..."))
        }
        buttonTNC.setOnClickListener {
            val url = "https://www.theatron.in/community-guidelines"
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(url)
            startActivity(Intent.createChooser(i, "Choose Browser..."))
        }
        buttonPP.setOnClickListener {
            val url = "https://www.theatron.in/privacy-policy"
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(url)
            startActivity(Intent.createChooser(i, "Choose Browser..."))
        }
        friendRequest.setOnClickListener {
            val intent = Intent(context, FriendRequestActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }



        super.onViewCreated(view, savedInstanceState)
    }

    private fun updateContentOnlyFriends() {
        if (mAuth.currentUser != null)
            myRef.child("USER").child(mAuth.currentUser!!.uid)
                .addValueEventListener(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {}

                    @SuppressLint("SetTextI18n")
                    override fun onDataChange(p0: DataSnapshot) {
                        name = p0.child("name").value.toString()
                        photoUrl = p0.child("photourl").value.toString()
                        val shareCount = p0.child("sharescount").value.toString()
                        //val followCount=p0.child("followerscount").value.toString()
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
                        if (sharecount != null) {
                            sharecount.text = getStringFormLikes(shareCount.toInt())
                        }
//                if(admirerscount!=null) {
//                    admirerscount.text = getStringFormLikes(followCount.toInt())
//                }
                    }

                })
    }

    private fun updateContent() {
        if (mAuth.currentUser != null) {
            myRef.child("USER").child(mAuth.currentUser!!.uid)
                .addValueEventListener(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {}
                    override fun onDataChange(p0: DataSnapshot) {
                        if (mAuth.currentUser != null) {
                            name = p0.child("name").value.toString()
                            photoUrl = p0.child("photourl").value.toString()
                            val shareCount = p0.child("sharescount").value.toString()
                            val followCount = p0.child("admirerscount").value.toString()
                            if (profile_name != null) {
                                profile_name.text = name
                            }
                            if (user_name != null) {
                                user_name.text =
                                    "@" + name.toLowerCase(Locale.ROOT).replace(" ", "")
                            }
                            if (profile_image != null) {
                                Picasso.get().load(photoUrl)
                                    .placeholder(R.drawable.ic_account_circle_black_24dp)
                                    .into(profile_image)
                            }
                            if (sharecount != null) {
                                sharecount.text = getStringFormLikes(shareCount.toInt())
                            }
//                if(admirerscount!=null) {
//                    admirerscount.text = getStringFormLikes(followCount.toInt())
//                }
                        }
                    }

                })
        }

        //////////
        if (mAuth.currentUser != null) {
            myRef.child("USER").child(mAuth.currentUser!!.uid).child("videolist")
                .addListenerForSingleValueEvent(
                    object : ValueEventListener {
                        override fun onCancelled(p0: DatabaseError) {}

                        override fun onDataChange(p0: DataSnapshot) {
                            if (p0.exists()) {
                                //myRef.child("USER").child(mAuth.currentUser!!.uid).child("videolist").keepSynced(true)
                                val arrayList = p0.value as ArrayList<ProfileUser>
                                try{
                                for (i in 0 until arrayList.size) {
                                    if (arrayList[i] != null) {
                                        val databaseUser = arrayList[i] as HashMap<String, String>
                                        Log.e( "Profile fragment", databaseUser.toString() )
                                        videoList.add(
                                            ProfileUser(
                                                databaseUser["name"]!!,
                                                databaseUser["title"]!!,
                                                databaseUser["url"]!!,
                                                databaseUser["thumbnailphoto"]!!,
                                                databaseUser["id"]!!,
                                                databaseUser["desc"]!!,
                                                databaseUser["view"]!!,
                                                databaseUser["likes"]!!,
                                                databaseUser["dislikes"]!!,
                                                databaseUser["shares"]!!,
                                                databaseUser["photo"]!!,
                                                databaseUser["vnum"]!!,
                                                databaseUser["date"]!!,
                                                databaseUser["time"]!!,
                                                databaseUser["status"]!!
                                            )
                                        )

                                        countOfShares += databaseUser["shares"]!!.toInt()
                                    }
                                }}
                                catch(e:Exception){
                                    e.printStackTrace()
                                }
                                adapter.notifyDataSetChanged()

                            }
                            myRef.child("USER").child(mAuth.currentUser!!.uid)
                                .child("sharescount").setValue(countOfShares)

                        }
                    })
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 1234) {
            if (data != null && data.data != null) {
                val uri = data.data!!
                if (uri.toString().isNotEmpty()) {
                    val path = uri.path!!

//                    val retiever = MediaMetadataRetriever()
                    val retiever = FFmpegMediaMetadataRetriever()
                    retiever.setDataSource(
                        context,
                        Uri.fromFile(File(getPath(requireContext(), uri)))
                    )
                    val time =
                        retiever.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_DURATION)
                            .toLong()
                    retiever.release()
                    if (time <= 302 * 1000) {
                        val uriPort = path.subSequence(path.lastIndexOf("/") + 1, path.length)
                        Log.e("profile fragment", uriPort.toString())
                        val id = System.currentTimeMillis()
                       // val id = uriPort.toString().toLong()
                        val crThumb: ContentResolver = requireContext().contentResolver
                        var thumbnailImage: Bitmap? = MediaStore.Video.Thumbnails.getThumbnail(
                            crThumb,
                            id,
                            MediaStore.Video.Thumbnails.MINI_KIND,
                            null
                        )
                        if (thumbnailImage == null) {
                            thumbnailImage = Bitmap.createBitmap(100, 100, Bitmap.Config.ALPHA_8)
                        }
                        val outputStream = ByteArrayOutputStream()
                        thumbnailImage!!.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                        val data2 = outputStream.toByteArray()
                        val progressDialog = ProgressDialog(requireContext())
                        progressDialog.setTitle("Uploading Video......")
                        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
                        progressDialog.max = 100
                        progressDialog.setCancelable(false)
                        progressDialog.show()
                        var thumbnailURL: Uri?
                        var url: Uri?
                        val x =
                            if (videoList.size > 0) {
                                (videoList[videoList.size - 1].vnum.toInt() + 1).toString()
                            } else {
                                0.toString()
                            }
                        val fstorageRef =
                            storageRef.child("videos/${mAuth.currentUser!!.uid}${x}.mp4")
                        var maxProgres = 0.0
                        //VideoCompressor.start(path,)

                        fstorageRef.putFile(uri).addOnProgressListener {
                            ////////////
                            val progress: Double =
                                (100.0 * it.bytesTransferred.toFloat()) / it.totalByteCount.toFloat()
                            if (maxProgres < progress) {
                                maxProgres = progress
                                progressDialog.progress = maxProgres.toInt()
                            }
                        }.addOnSuccessListener {
                            progressDialog.progress = 100
                            it.storage.downloadUrl.addOnSuccessListener {
                                url = it!!
                                val fstorageRef2 =
                                    storageRef.child("thumbnails/${mAuth.currentUser!!.uid}${x}.jpeg")
                                fstorageRef2.putBytes(data2).addOnSuccessListener {
                                    it.storage.downloadUrl.addOnSuccessListener {
                                        thumbnailURL = it
                                        progressDialog.dismiss()
                                        if (url != null && thumbnailURL != null) {
                                            var title: String
                                            var desc: String
                                            val view = LayoutInflater.from(requireContext())
                                                .inflate(R.layout.alert_dialog_tit_desc_view, null)
                                            val alertDialog =
                                                AlertDialog.Builder(requireContext()).setView(view)
                                                    .setCancelable(false).show()
                                            val imgView =
                                                view.findViewById<ImageView>(R.id.imageViewForTD)
                                            Picasso.get().load(thumbnailURL)
                                                .placeholder(R.mipmap.ic_launcher).into(imgView)
                                            view.doneButton.setOnClickListener {

                                                title =
                                                    view.findViewById<EditText>(R.id.editTextForTitle).text.toString()
                                                desc =
                                                    view.findViewById<EditText>(R.id.editTextForDesc).text.toString()
                                                if (title.isNotEmpty() && desc.isNotEmpty()) {
                                                    if (EmojiUtils.containsEmoji(title) || EmojiUtils.containsEmoji(
                                                            desc
                                                        )
                                                    ) {
                                                        Toast.makeText(
                                                            context,
                                                            "Emojis Not Allowed in Title And Description",
                                                            Toast.LENGTH_LONG
                                                        ).show()
                                                    } else {
                                                        val currentDate: String = SimpleDateFormat(
                                                            "yyyy-MM-dd",
                                                            Locale.getDefault()
                                                        ).format(Date())
                                                        val currentTime: String = SimpleDateFormat(
                                                            "HH:mm:ss",
                                                            Locale.getDefault()
                                                        ).format(Date())
                                                        myRef.child("USER")
                                                            .child(mAuth.currentUser!!.uid)
                                                            .addListenerForSingleValueEvent(
                                                                object : ValueEventListener {
                                                                    override fun onCancelled(error: DatabaseError) {}

                                                                    override fun onDataChange(
                                                                        snapshot: DataSnapshot
                                                                    ) {
                                                                        val name =
                                                                            snapshot.child("name").value as String
                                                                        val photUrl =
                                                                            snapshot.child("photourl").value as String
                                                                        val profileUserData =
                                                                            ProfileUser(
                                                                                name,
                                                                                title,
                                                                                url.toString(),
                                                                                thumbnailURL.toString(),
                                                                                mAuth.currentUser!!.uid,
                                                                                desc,
                                                                                "0",
                                                                                "0",
                                                                                "0",
                                                                                "0",
                                                                                photUrl,
                                                                                x,
                                                                                currentDate,
                                                                                currentTime,
                                                                                "pending"
                                                                            )
                                                                        videoList.add(
                                                                            profileUserData
                                                                        )
                                                                        thumbList.add(
                                                                            profileUserData
                                                                        )
                                                                        myRef.child("USER")
                                                                            .child(mAuth.currentUser!!.uid)
                                                                            .child("videolist")
                                                                            .setValue(videoList)

                                                                        val pendingVideo = ProfileUser(
                                                                            name,
                                                                            title,
                                                                            url.toString(),
                                                                            thumbnailURL.toString(),
                                                                            mAuth.currentUser!!.uid,
                                                                            desc,
                                                                            "0",
                                                                            "0",
                                                                            "0",
                                                                            "0",
                                                                            photUrl,
                                                                            x,
                                                                            currentDate,
                                                                            currentTime,
                                                                            "pending"
                                                                        )
                                                                        val pendingRef = FirebaseDatabase.getInstance().reference
                                                                        pendingRef.child("PENDING_VIDEOS")
                                                                            .child(mAuth.currentUser!!.uid)
                                                                            .push()
                                                                            .setValue(pendingVideo)
                                                                    }
                                                                }
                                                            )
                                                        Toast.makeText(
                                                            requireContext(),
                                                            "We are reviewing your video",
                                                            Toast.LENGTH_LONG
                                                        ).show()
                                                        alertDialog.dismiss()
                                                    }
                                                } else {
                                                    Toast.makeText(
                                                        context,
                                                        "Title And Description Are Mandatory",
                                                        Toast.LENGTH_LONG
                                                    ).show()
                                                }
                                            }
                                            adapter.notifyDataSetChanged()
                                        }
                                    }
                                }.addOnFailureListener {
                                    Toast.makeText(
                                        requireContext(),
                                        it.message.toString(),
                                        Toast.LENGTH_LONG
                                    ).show()
                                    progressDialog.dismiss()
                                }
                            }
                        }.addOnFailureListener {
                            progressDialog.dismiss()
                            Toast.makeText(
                                requireContext(),
                                it.message.toString(),
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } else {
                        Toast.makeText(
                            context,
                            "Sorry Length of video cant be greater than 5 minutes.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Sorry Unable to upload video.",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    fun getPath(context: Context, uri: Uri?): String? {
        var result: String? = null
        val proj = arrayOf(MediaStore.Images.Media.DATA)
       // MediaStore.Images.Media.DATA
        val cursor = context.contentResolver.query(uri!!, proj, null, null, null)

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                val columnIndex = cursor.getColumnIndexOrThrow(proj[0])
                result = cursor.getString(columnIndex)
            }
            cursor.close()
        }
        if (result == null) {
            result = "Not found"
        }
        return result
    }

    fun getStringFormLikes(disLikeNumber: Int): String {
        return when {
            disLikeNumber > (10.0).pow(6) -> {
                (((disLikeNumber / (10.0).pow(5)).toInt()) / 10.0).toString() + "M"
            }
            disLikeNumber > (10.0).pow(3) -> {
                (((disLikeNumber / (10.0).pow(2)).toInt()) / 10.0).toString() + "K"
            }
            else -> {
                disLikeNumber.toString()
            }
        }
    }

}
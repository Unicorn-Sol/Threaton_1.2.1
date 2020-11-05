package com.theatron2.uiforthreaton.ui.search

import android.content.Context.LAYOUT_INFLATER_SERVICE
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.theatron2.uiforthreaton.R
import com.theatron2.uiforthreaton.adapterForAllClasses.CustomAdapterForSearchGrid
import com.theatron2.uiforthreaton.adapterForAllClasses.CustomSuggestionsAdapterSearchFrag
import com.theatron2.uiforthreaton.ui.friends.USER_FRIEND_ADMIRERS
import com.theatron2.uiforthreaton.ui.profile.ProfileUser
import kotlinx.android.synthetic.main.fragment_search.*

@Suppress("UNCHECKED_CAST", "CAST_NEVER_SUCCEEDS")
class SearchFragment : Fragment() {

    val myRef = FirebaseDatabase.getInstance().reference
    val namesList = ArrayList<USER_FRIEND_ADMIRERS>()
    var gridList = ArrayList<ProfileUser>()
    var vnum = 0
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        gridList = ArrayList()
        val adapter = CustomAdapterForSearchGrid(requireContext(), gridList)
        recyclerViewForSearchFrag.adapter = adapter

        myRef.child("ALLUSER").addListenerForSingleValueEvent(
            object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {}

                override fun onDataChange(snapshot: DataSnapshot) {
                    val userIDList = ArrayList<String>()
                    for (databaseUser in snapshot.children) {
                        try{
                        Log.e(  "database user", databaseUser.value.toString() )
                        val databaseUserValue = databaseUser.value as HashMap<String, String>
                        userIDList.add(databaseUserValue["id"]!!)
                    }catch (e :Exception){
                            e.printStackTrace()
                        }
                    }
                    val myRef_user = myRef.child("USER")
                    for (j in userIDList) {
                        myRef_user.child(j).addListenerForSingleValueEvent(
                            object : ValueEventListener {
                                override fun onCancelled(error: DatabaseError) {}

                                override fun onDataChange(snapshot: DataSnapshot) {
                                    if (snapshot.exists()) {
                                        if (snapshot.hasChild("videolist")) {
                                            Log.e(
                                                "Search snapshot",
                                                snapshot.child("videolist").value.toString()
                                            )

                                            if (snapshot.child("videolist").value is HashMap<*, *>) {
                                                val videoListFetched =
                                                    snapshot.child("videolist").value as HashMap<String, HashMap<String, String>>
                                                //Log.e("video list fetched",videoListFetched[0.toString()].toString() )
                                                for (i in 0 until videoListFetched.size) {
                                                    Log.e(
                                                        "video LIst fetched",
                                                        videoListFetched.size.toString()
                                                    )

                                                        Log.e(
                                                            "loop Inside",
                                                            videoListFetched[i.toString()].toString()
                                                        )
                                                        val databaseUser =
                                                            videoListFetched[i.toString()] as HashMap<String, String>
                                                        val proUser = ProfileUser(
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
                                                            ""
                                                        )
                                                        if (databaseUser["status"].equals(
                                                                "approved",
                                                                false
                                                            )
                                                        ) {
                                                            gridList.add(proUser)
                                                        }
                                                }
                                            } else {
                                                val videoListFetched =
                                                    snapshot.child("videolist").value as ArrayList<ProfileUser>
                                                //Log.e("video list fetched",videoListFetched[0.toString()].toString() )
                                                for (i in 0 until videoListFetched.size) {
                                                    Log.e(
                                                        "video LIst fetched",
                                                        videoListFetched.size.toString()
                                                    )

                                                     //   Log.e("loop Inside", videoListFetched[i].toString())
                                                        val databaseUser =
                                                            videoListFetched[i] as HashMap<String, String>
                                                    if (databaseUser["thumbnailphoto"].isNullOrEmpty()){
                                                        databaseUser["thumbnailphoto"] = "https://www.pngkit.com/png/full/267-2678423_bacteria-video-thumbnail-default.png"
                                                    }
                                                    if (databaseUser["photo"].isNullOrEmpty()){
                                                        databaseUser["photo"] = "https://www.ibts.org/wp-content/uploads/2017/08/iStock-476085198.jpg"
                                                    }
                                                    //todo : default date need to be changed. time tooo. name too
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
                                                        val proUser = ProfileUser(
                                                            databaseUser["name"]!!,
                                                            databaseUser["title"]!!,
                                                            databaseUser["url"]!!,
                                                            databaseUser["thumbnailphoto"]!!,
                                                            databaseUser["id"]!!,
                                                            databaseUser["desc"]!!,
                                                            databaseUser["view"].toString()!!,
                                                            databaseUser["likes"].toString()!!,
                                                            databaseUser["dislikes"].toString()!!,
                                                            databaseUser["shares"].toString()!!,
                                                            databaseUser["photo"]!!,
                                                            databaseUser["vnum"]!!,
                                                            databaseUser["date"]!!,
                                                            databaseUser["time"]!!,
                                                            ""
                                                        )

                                                        if (databaseUser["status"].equals(
                                                                "approved",
                                                                false
                                                            )
                                                        ) {
                                                            gridList.add(proUser)
                                                        }
                                                    }
                                                }
                                            }

                                            gridList.shuffle()
                                            adapter!!.notifyDataSetChanged()
                                        }


                                }

                            }
                        )
                    }
                }

            }
        )

        gridList.shuffle()
        recyclerViewForSearchFrag.layoutManager = GridLayoutManager(context, 3)
        recyclerViewForSearchFrag.addItemDecoration(
            DividerItemDecoration(
                context,
                DividerItemDecoration.HORIZONTAL
            )
        )
        recyclerViewForSearchFrag.addItemDecoration(
            DividerItemDecoration(
                context,
                DividerItemDecoration.VERTICAL
            )
        )


        val layoutInflater =
            requireContext().getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val customAdapterSuggestion =
            CustomSuggestionsAdapterSearchFrag(requireContext(), layoutInflater)
        customAdapterSuggestion.suggestions = namesList
        searchBarForSearch.setCustomSuggestionAdapter(customAdapterSuggestion)
        myRef.child("ALLUSER").addValueEventListener(
            object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {}

                override fun onDataChange(p0: DataSnapshot) {
                    for (database in p0.children){
                        val user = database.getValue(USER_FRIEND_ADMIRERS::class.java)!!
                        if(isTypeArtist(user.id)) {
                            namesList.add(user)
                        }
                    }
                    customAdapterSuggestion.notifyDataSetChanged()
                }
            }
        )
        searchBarForSearch.addTextChangeListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                customAdapterSuggestion.filter.filter(s)
            }
        })

        super.onViewCreated(view, savedInstanceState)

    }

    private fun isTypeArtist(id: String): Boolean {
        var type = "artist"
        myRef.child("USER").child(id).child("type").addListenerForSingleValueEvent(
            object :ValueEventListener{
                override fun onCancelled(error: DatabaseError) {

                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists())
                    {
                        type = if (snapshot.value.toString() == "artist") {
                            "artist"
                        } else{
                            "audience"
                        }
                    }

                }
            }
        )
        return type == "artist"
    }


}
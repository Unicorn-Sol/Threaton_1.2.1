package com.theatron2.uiforthreaton.ui.friends2

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.telephony.TelephonyManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.collection.ArraySet
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.theatron2.uiforthreaton.Activities.RequestTypeData
import com.theatron2.uiforthreaton.Activities.Request_type_data_with_friend
import com.theatron2.uiforthreaton.Activities.user_with_id_phoneNumber
import com.theatron2.uiforthreaton.R
import com.theatron2.uiforthreaton.adapterForAllClasses.CustomAdapterForAdmirers
import com.theatron2.uiforthreaton.ui.friends.USER_FRIEND_ADMIRERS
import kotlinx.android.synthetic.main.fragment_friends2_.*


@Suppress("UNCHECKED_CAST", "CAST_NEVER_SUCCEEDS")
class Friends2_Fragment : Fragment() {
    private var list_friends = ArrayList<USER_FRIEND_ADMIRERS>()
    private var list_can_be_friends = ArrayList<Request_type_data_with_friend>()
    private var listContacts = ArraySet<String>()
    private var listContactsFromFirebase=ArrayList<user_with_id_phoneNumber>()
    val myRef = FirebaseDatabase.getInstance().reference
    var currentUser :String?=null
    private lateinit var adapterForFriends: CustomAdapterForAdmirers
    private lateinit var adapterForCanBeFriends: CustomAdapterForFriends
    val arrayListOfSent = ArrayList<RequestTypeData>()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_friends2_, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        if (FirebaseAuth.getInstance().currentUser!=null)
        {
            currentUser = FirebaseAuth.getInstance().currentUser!!.uid
        }
        recyclerViewForfriends2.layoutManager = LinearLayoutManager(requireContext())
        recyclerViewForfriends2.hasFixedSize()
        adapterForFriends = CustomAdapterForAdmirers(requireContext(), list_friends)
        recyclerViewForfriends2.adapter = adapterForFriends

        recyclerViewForCanBeFriends.layoutManager = LinearLayoutManager(requireContext())
        recyclerViewForCanBeFriends.hasFixedSize()
        adapterForCanBeFriends = CustomAdapterForFriends(requireContext(), list_can_be_friends)
        recyclerViewForCanBeFriends.adapter = adapterForCanBeFriends

        if(context!=null) {
            val sharedPreferences = requireContext().getSharedPreferences("USERNAME", Context.MODE_PRIVATE)
            if (!sharedPreferences.getBoolean("firsttime",true) ) {
                val permissionsList = ArrayList<String>();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(
                        requireContext(),
                        Manifest.permission.READ_CONTACTS
                    ) != PackageManager.PERMISSION_GRANTED ){
                    permissionsList.add(Manifest.permission.READ_CONTACTS)
                }
                if(ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_PHONE_NUMBERS) !=PackageManager.PERMISSION_GRANTED){
                    permissionsList.add(Manifest.permission.READ_PHONE_NUMBERS)
                }
                if(ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_PHONE_STATE)!= PackageManager.PERMISSION_GRANTED) {
                    permissionsList.add(Manifest.permission.READ_PHONE_STATE)
                }
                if (permissionsList.isNotEmpty()) {
                    val array = Array(permissionsList.size) {""}
                    for (i in 0 until permissionsList.size){
                        array[i] = permissionsList[i]
                    }
                    requestPermissions(array, 9876)

                }
                else {
                    getContacts()
                    getAllContactsFromFirebase()
                    currentUser?.let { myRef.child("USER").child(it).child("phonenumber").setValue(getUserPhoneNumber()) }
                }
            }
        }
        if (currentUser!=null) {
            myRef.child("USER").child(currentUser!!).child("friends").addListenerForSingleValueEvent(
                object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) {}

                    override fun onDataChange(p0: DataSnapshot) {
                        if (p0.exists()) {
                            for (databaseList in p0.children) {
                                val databaseUser = databaseList.value as HashMap<String, String>
                                list_friends.add(USER_FRIEND_ADMIRERS(databaseUser["name"]!!, databaseUser["photo"]!!, databaseUser["id"]!!))
                            }
                            for (i in 0 until list_friends.size) {
                                myRef.child("USER").child(list_friends[i].id)
                                    .addListenerForSingleValueEvent(
                                        object : ValueEventListener {
                                            override fun onCancelled(error: DatabaseError) {

                                            }

                                            override fun onDataChange(snapshot: DataSnapshot) {
                                                list_friends[i].name =
                                                    snapshot.child("name").value.toString()
                                                list_friends[i].photo =
                                                    snapshot.child("photourl").value.toString()
                                                adapterForFriends.notifyDataSetChanged()
                                            }
                                        }
                                    )
                            }


                        }
                    }

                }
            )
        }

        super.onViewCreated(view, savedInstanceState)
    }

    @SuppressLint("Recycle")
    private fun getContacts() {
        val c=requireContext().contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,null,null,ContactsContract.Contacts.DISPLAY_NAME+" ASC ")
        if (c!=null) {
            while (c.moveToNext()) {
                val number = c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                if(number.length>=10) {
                    listContacts.add(number.substring(number.length - 10))
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == 9876) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getContacts()
                getAllContactsFromFirebase()
                currentUser?.let { myRef.child("USER").child(it).child("phonenumber").setValue(getUserPhoneNumber()) }
            } else {
                Toast.makeText(requireContext(),"Permission must be granted in order to display contacts information",Toast.LENGTH_LONG).show()
            }
        }
    }

    fun getAllContactsFromFirebase()
    {

        myRef.child("USER").addListenerForSingleValueEvent(
            object:ValueEventListener
            {
                override fun onCancelled(p0: DatabaseError) {}

                override fun onDataChange(p0: DataSnapshot) {
                    for(arrayList in p0.children)
                    {
                        val databaseUser = arrayList.value as HashMap<String,String>
                        listContactsFromFirebase.add(user_with_id_phoneNumber(databaseUser["name"]!!,databaseUser["photo"]!!,databaseUser["id"]!!,databaseUser["phonenumber"]!!))
                    }
                    if(currentUser!=null) {
                        myRef.child("USER").child(currentUser!!).child("friendrns")
                            .addListenerForSingleValueEvent(
                                object : ValueEventListener {
                                    override fun onCancelled(p0: DatabaseError) {}

                                    override fun onDataChange(p0: DataSnapshot) {
                                        if (p0.exists()) {
                                            for (databaseList in p0.children) {
                                                val databaseUser = databaseList.value as HashMap<String, String>
                                                arrayListOfSent.add(RequestTypeData(databaseUser["name"]!!, databaseUser["photo"]!!, databaseUser["id"]!!, databaseUser["phonenumber"]!!, databaseUser["type"]!!))
                                            }
                                        }
                                        setAdapterAndNotify()
                                    }
                                }
                            )
                    }
                }
            }
        )
    }

    fun setAdapterAndNotify()
    {

        for(i in listContactsFromFirebase)
        {
            if( i.phonenumber in listContacts && i.id!=currentUser)
            {
                list_can_be_friends.add(Request_type_data_with_friend(i.name,i.photo,i.id,false,i.phonenumber))
            }
        }
        for(j in list_friends)
        {
            var i =0
            while (i<list_can_be_friends.size) {
                if (list_can_be_friends[i].id == j.id) {
                    list_can_be_friends.remove(list_can_be_friends[i])
                    i -= 1
                    break
                }
                i++
            }
        }
        for ( i in 0 until list_can_be_friends.size)
        {
            val x = list_can_be_friends[i]
            for ( j in 0 until arrayListOfSent.size)
            {
                val y = arrayListOfSent[j]
                if(x.id==y.id && y.type=="S")
                {
                    list_can_be_friends[i].friends=true
                }
            }
        }
        for(i in 0 until list_can_be_friends.size)
        {
            myRef.child("USER").child(list_can_be_friends[i].id).addListenerForSingleValueEvent(
                object:ValueEventListener
                {
                    override fun onCancelled(error: DatabaseError) {

                    }

                    override fun onDataChange(snapshot: DataSnapshot) {
                        list_can_be_friends[i].name = snapshot.child("name").value.toString()
                        list_can_be_friends[i].photo = snapshot.child("photourl").value.toString()
                        adapterForCanBeFriends.notifyDataSetChanged()
                    }
                }
            )
        }
    }

    @SuppressLint("MissingPermission", "HardwareIds")
    fun getUserPhoneNumber():String{

        val telephonyManager = requireContext().getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager?
        val phoneNumber = if(telephonyManager!=null && telephonyManager.line1Number!=null)
        {
            telephonyManager.line1Number
        }
        else
        {
            "-1"
        }

        myRef.child("USER").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                for (allUserList in p0.children) {
                        val userData =
                            allUserList.value as HashMap<String, String>
                        if (userData["id"] == currentUser!!) {
                            userData["phonenumber"] = phoneNumber
                            break
                        }
                }
            }
        })
        return phoneNumber
    }
}
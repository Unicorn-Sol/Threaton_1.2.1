package com.theatron2.uiforthreaton.ui.friends2

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.theatron2.uiforthreaton.Activities.Main2Activity
import com.theatron2.uiforthreaton.Activities.RequestTypeData
import com.theatron2.uiforthreaton.Activities.Request_type_data_with_friend
import com.theatron2.uiforthreaton.R
import de.hdodenhof.circleimageview.CircleImageView

@Suppress("UNCHECKED_CAST", "CAST_NEVER_SUCCEEDS")
class CustomAdapterForFriends(val context: Context, val friendsRNSLIST:ArrayList<Request_type_data_with_friend>):RecyclerView.Adapter<CustomAdapterForFriends.ViewHolder>() {
    val myRef= FirebaseDatabase.getInstance().reference
    val currentUser= FirebaseAuth.getInstance().currentUser
    var currentUserPhoneNumber :String="-1"
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.friend_recycler_view_layout, parent, false))
    }

    override fun getItemCount(): Int= friendsRNSLIST.size

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.layout.setBackgroundColor(Color.parseColor("#FFFFFF"))
        holder.imageView.setBackgroundColor(Color.parseColor("#FFFFFF"))
        holder.textView.setTextColor(Color.parseColor("#000000"))
        holder.switch.visibility=View.VISIBLE
        Picasso.get().load(friendsRNSLIST[position].photo).placeholder(R.drawable.ic_account_circle_black_24dp).into(holder.imageView)
        holder.textView.text=friendsRNSLIST[position].name
        holder.switch.setThumbResource(R.drawable.ic_fiber_manual_record_black_24dp)

        holder.switch.isChecked = friendsRNSLIST[position].friends
        myRef.child("USER").child(currentUser!!.uid).child("phonenumber").addListenerForSingleValueEvent(
            object : ValueEventListener
            {
                override fun onCancelled(p0: DatabaseError) {                }
                override fun onDataChange(p0: DataSnapshot) {
                    currentUserPhoneNumber = p0.value as String
                }
            }
        )

        holder.switch.setOnClickListener{
            val isChecked=holder.switch.isChecked
            if (!isChecked)
            {
                val viewForAlertDialog=LayoutInflater.from(context).inflate(R.layout.background_for_alert_dialog,null,false)
                val alertDialog= AlertDialog.Builder(context).setView(viewForAlertDialog)
                    .setCancelable(false)
                    .show()
                val textView=viewForAlertDialog.findViewById<TextView>(R.id.txt_dia)!!
                val yesButton=viewForAlertDialog.findViewById<Button>(R.id.btn_yes)!!
                val noButton=viewForAlertDialog.findViewById<Button>(R.id.btn_no)!!
                val imageView=viewForAlertDialog.findViewById<CircleImageView>(R.id.imageViewForAlertDialog)
                Picasso.get().load(friendsRNSLIST[position].photo).placeholder(R.drawable.ic_account_circle_black_24dp).into(imageView)
                textView.text="Are you sure you want to cancel friend request send to  ${holder.textView.text} ? "
                yesButton.setOnClickListener {
                    //////////////////////////////
                    holder.switch.isChecked=false
                    friendsRNSLIST[position].friends=holder.switch.isChecked
                    /////////////////////////
                    myRef.child("USER").child(friendsRNSLIST[position].id).child("friendrns").addListenerForSingleValueEvent(
                        object :ValueEventListener
                        {
                            override fun onCancelled(p0: DatabaseError) {}

                            override fun onDataChange(p0: DataSnapshot) {
                                if(p0.exists()){
                                    for (databaseList in p0.children){
                                        val databaseUser = databaseList.value as HashMap<String,String>
                                        if (databaseUser["id"]==currentUser.uid){
                                            val key = databaseList.key!!
                                            myRef.child("USER").child(friendsRNSLIST[position].id).child("friendrns").child(key).removeValue()
                                            break
                                        }
                                    }
                                }
                            }
                        })
                    /////////////////////////
                    myRef.child("USER").child(currentUser.uid).child("friendrns").addListenerForSingleValueEvent(
                        object :ValueEventListener
                        {
                            override fun onCancelled(p0: DatabaseError) {}
                            override fun onDataChange(p0: DataSnapshot) {
                                val arrayList=ArrayList<RequestTypeData>()
                                if (p0.exists())
                                {

                                    for(databaseList in p0.children)
                                    {
                                        val databaseUser = databaseList.value as HashMap<String,String>
                                        if(databaseUser["id"]==friendsRNSLIST[position].id)
                                        {
                                            val key = databaseList.key!!
                                            myRef.child("USER").child(currentUser.uid).child("friendrns")
                                                .child(key)
                                                .setValue(RequestTypeData(databaseUser["name"]!!,databaseUser["photo"]!!,databaseUser["id"]!!,databaseUser["phonenumber"]!!,databaseUser["type"]!!))
                                            break
                                        }
                                    }
                                }
                            }
                        }
                    )
                    notifyDataSetChanged()
                    //////////////////////////////
                    alertDialog.dismiss()
                    Toast.makeText(context,"Friend Request Cancelled",Toast.LENGTH_SHORT).show()
                }
                noButton.setOnClickListener {
                    holder.switch.isChecked=true
                    friendsRNSLIST[position].friends=holder.switch.isChecked
                    alertDialog.dismiss()
                }
            }
            else
            {
                holder.switch.isChecked=true
                friendsRNSLIST[position].friends=holder.switch.isChecked
                val myrefUserIdFr = myRef.child("USER").child(friendsRNSLIST[position].id).child("friendrns")
                val key = myrefUserIdFr.push().key!!
                myrefUserIdFr
                    .child(key)
                    .setValue(RequestTypeData(currentUser.displayName!!, currentUser.photoUrl.toString(),currentUser.uid,currentUserPhoneNumber,"R"))


                /////////////////////////

                val key2 = myRef.child("USER").child(currentUser.uid).child("friendrns").push().key!!
                myRef.child("USER").child(currentUser.uid).child("friendrns")
                    .child(key2)
                    .setValue(RequestTypeData(friendsRNSLIST[position].name, friendsRNSLIST[position].photo,friendsRNSLIST[position].id,friendsRNSLIST[position].phonenumber,"S"))
                Toast.makeText(context,"Friend Request Sent To ${friendsRNSLIST[position].name}",Toast.LENGTH_SHORT).show()
                notifyDataSetChanged()
                /////////////////
            }
        }

        holder.imageView.setOnClickListener {
            val intent= Intent(context,
                Main2Activity::class.java)
            intent.putExtra("SuggestId",friendsRNSLIST[position].id)
            context.startActivity(intent)
        }

        holder.textView.setOnClickListener {
            val intent= Intent(context,
                Main2Activity::class.java)
            intent.putExtra("SuggestId",friendsRNSLIST[position].id)
            context.startActivity(intent)
        }


    }
    class ViewHolder(itemView: View):RecyclerView.ViewHolder(itemView)
    {
        val imageView: CircleImageView =itemView.findViewById(R.id.imageViewForAdmirers)
        val textView: TextView =itemView.findViewById(R.id.textViewForAdmirers)
        val switch: Switch =itemView.findViewById(R.id.switchForAdmirers)
        val layout: LinearLayout =itemView.findViewById(R.id.layoutForAdmirers)
    }
}
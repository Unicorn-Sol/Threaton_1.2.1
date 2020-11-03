@file:Suppress("UNCHECKED_CAST")

package com.theatron2.uiforthreaton.adapterForAllClasses

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
import com.theatron2.uiforthreaton.Activities.user_with_id
import com.theatron2.uiforthreaton.R
import com.theatron2.uiforthreaton.ui.friends.USER_FRIEND_ADMIRING
import de.hdodenhof.circleimageview.CircleImageView

@Suppress("DEPRECATION", "SENSELESS_COMPARISON", "CAST_NEVER_SUCCEEDS")
class CustomAdapterForAdmiring(private val context: Context, private var admiring: ArrayList<USER_FRIEND_ADMIRING>) : RecyclerView.Adapter<CustomAdapterForAdmiring.ViewHolder>() {
    val myRef=FirebaseDatabase.getInstance().reference
    val currentUser=FirebaseAuth.getInstance().currentUser
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.friend_recycler_view_layout,parent,false))
    }
    override fun getItemCount(): Int=admiring.size

    @SuppressLint("SetTextI18n", "InflateParams")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.layout.setBackgroundColor(Color.parseColor("#FFFFFF"))
        holder.imageView.setBackgroundColor(Color.parseColor("#FFFFFF"))
        holder.textView.setTextColor(Color.parseColor("#000000"))
        holder.switch.visibility=View.VISIBLE
        Picasso.get().load(admiring[position].photo).placeholder(R.drawable.ic_account_circle_black_24dp).into(holder.imageView)
        //Picasso.get().load(R.drawable.ic_account_circle_black_24dp).placeholder(R.drawable.ic_account_circle_black_24dp).into(holder.imageView)
        holder.textView.text=admiring[position].name
        //holder.switch.setTrackResource(R.drawable.ic_remove_black_24dp)
        holder.switch.setThumbResource(R.drawable.ic_fiber_manual_record_black_24dp)

        holder.switch.isChecked = admiring[position].friends
        holder.switch.setOnClickListener{
            val isChecked=holder.switch.isChecked
            if (!isChecked)
            {
                val viewForAlertDialog=LayoutInflater.from(context).inflate(R.layout.background_for_alert_dialog,null,false)
                val alertDialog=AlertDialog.Builder(context).setView(viewForAlertDialog)
                    .setCancelable(false)
                    .show()
                val textView=viewForAlertDialog.findViewById<TextView>(R.id.txt_dia)!!
                val yesButton=viewForAlertDialog.findViewById<Button>(R.id.btn_yes)!!
                val noButton=viewForAlertDialog.findViewById<Button>(R.id.btn_no)!!
                val imageView=viewForAlertDialog.findViewById<CircleImageView>(R.id.imageViewForAlertDialog)
                Picasso.get().load(admiring[position].photo).placeholder(R.drawable.ic_account_circle_black_24dp).into(imageView)
                textView.text="Are you sure you want to stop admiring  ${holder.textView.text} ? "
                yesButton.setOnClickListener {
                    //////////////////////////////
                    holder.switch.isChecked=false
                    admiring[position].friends=holder.switch.isChecked
                    myRef.child("USER").child(admiring[position].id).addListenerForSingleValueEvent(
                        object :ValueEventListener
                        {
                            override fun onCancelled(p0: DatabaseError) {
                                Toast.makeText(context, p0.message,Toast.LENGTH_LONG).show()
                            }

                            override fun onDataChange(p0: DataSnapshot) {
                                val admirerscount=p0.child("admirerscount").value as String
                                myRef.child("USER").child(admiring[position].id).child("admirerscount").setValue((admirerscount.toInt()-1).toString())
                            }
                        }
                    )
                    /////////////////////////
                    myRef.child("USER").child(admiring[position].id).child("admirers").addListenerForSingleValueEvent(
                        object :ValueEventListener
                        {
                            override fun onCancelled(p0: DatabaseError) {}

                            override fun onDataChange(p0: DataSnapshot) {
                                val arrayList=ArrayList<user_with_id>()
                                myRef.child("USER").child(admiring[position].id).child("admirers").keepSynced(true)
                                if (p0.exists())
                                {
                                    val databaseList=p0.value as ArrayList<user_with_id>
                                    for(i in 0 until databaseList.size)
                                    {
                                        if(databaseList[i]!=null) {
                                            val databaseUser = databaseList[i] as HashMap<String,String>
                                            if(databaseUser["id"]!=currentUser!!.uid)
                                            {
                                                arrayList.add(user_with_id(databaseUser["name"]!!,databaseUser["photo"]!!,databaseUser["id"]!!))
                                            }
                                        }
                                    }
                                }
                                myRef.child("USER").child(admiring[position].id).child("admirers").setValue(arrayList)

                            }
                        }
                    )
                    /////////////////////////
                    myRef.child("USER").child(currentUser!!.uid).child("admiring").addListenerForSingleValueEvent(
                        object :ValueEventListener
                        {
                            override fun onCancelled(p0: DatabaseError) {}
                            override fun onDataChange(p0: DataSnapshot) {
                                val arrayList=ArrayList<user_with_id>()
                                if (p0.exists())
                                {
                                    myRef.child("USER").child(currentUser.uid).child("admiring").keepSynced(true)
                                    val databaseList=p0.value as ArrayList<user_with_id>
                                    for(i in 0 until databaseList.size)
                                    {
                                        if(databaseList[i]!=null) {
                                            val databaseUser = databaseList[i] as HashMap<String,String>
                                            if(databaseUser["id"]!=admiring[position].id)
                                            {
                                                arrayList.add(user_with_id(databaseUser["name"]!!,databaseUser["photo"]!!,databaseUser["id"]!!))
                                            }
                                        }
                                    }
                                }
                                myRef.child("USER").child(currentUser.uid).child("admiring").setValue(arrayList)
                            }
                        }
                    )
                    notifyDataSetChanged()
                    //////////////////////////////
                    alertDialog.dismiss()
                }
                noButton.setOnClickListener {
                    holder.switch.isChecked=true
                    admiring[position].friends=holder.switch.isChecked
                    alertDialog.dismiss()
                }
            }
            else
            {
                holder.switch.isChecked=true
                admiring[position].friends=holder.switch.isChecked
                myRef.child("USER").child(admiring[position].id).addListenerForSingleValueEvent(
                    object :ValueEventListener
                    {
                        override fun onCancelled(p0: DatabaseError) {
                            Toast.makeText(context, p0.message,Toast.LENGTH_LONG).show()
                        }

                        override fun onDataChange(p0: DataSnapshot) {
                            val admirerscount=p0.child("admirerscount").value as String
                            myRef.child("USER").child(admiring[position].id).child("admirerscount").setValue((admirerscount.toInt()+1).toString())
                        }
                    }
                )
                ///////////////////////////////
                myRef.child("USER").child(admiring[position].id).child("admirers").addListenerForSingleValueEvent(
                    object :ValueEventListener
                    {
                        override fun onCancelled(p0: DatabaseError) {}

                        override fun onDataChange(p0: DataSnapshot) {

                            val arrayList=ArrayList<user_with_id>()
                            if(!p0.exists())
                            {
                                arrayList.add(user_with_id(currentUser!!.displayName!!, currentUser.photoUrl.toString(),currentUser.uid))
                            }
                            else
                            {
                                myRef.child("USER").child(admiring[position].id).child("admirers").keepSynced(true)
                                val databaseList=p0.value as ArrayList<user_with_id>
                                for (i in 0 until databaseList.size)
                                {
                                    if(databaseList[i]!=null)
                                    {
                                        val xUser=databaseList[i] as HashMap<String,String>
                                        arrayList.add(user_with_id(xUser["name"]!!,xUser["photo"]!!,xUser["id"]!!))
                                    }
                                }
                                arrayList.add(user_with_id(currentUser!!.displayName!!, currentUser.photoUrl.toString(),currentUser.uid))
                            }
                            myRef.child("USER").child(admiring[position].id).child("admirers").setValue(arrayList)

                        }
                    }
                )
                /////////////////////////
                myRef.child("USER").child(currentUser!!.uid).child("admiring").addListenerForSingleValueEvent(
                    object :ValueEventListener
                    {
                        override fun onCancelled(p0: DatabaseError) {}

                        override fun onDataChange(p0: DataSnapshot) {
                            val arrayList=ArrayList<user_with_id>()
                            if(!p0.exists())
                            {
                                arrayList.add(user_with_id(admiring[position].name,
                                    admiring[position].photo,admiring[position].id))

                            }
                            else
                            {
                                myRef.child("USER").child(currentUser.uid).child("admiring").keepSynced(true)
                                val databaseList=p0.value as ArrayList<user_with_id>
                                for (i in 0 until databaseList.size)
                                {
                                    if(databaseList[i]!=null)
                                    {
                                        val xUser=databaseList[i] as HashMap<String,String>
                                        arrayList.add(user_with_id(xUser["name"]!!,xUser["photo"]!!,xUser["id"]!!))
                                    }
                                }
                                arrayList.add(user_with_id(admiring[position].name, admiring[position].photo,admiring[position].id))
                            }
                            myRef.child("USER").child(currentUser.uid).child("admiring").setValue(arrayList)
                        }
                    }
                )
                notifyDataSetChanged()
                /////////////////

            }
        }



        holder.imageView.setOnClickListener {
            val intent= Intent(context,
                Main2Activity::class.java)
            intent.putExtra("SuggestId",admiring[position].id)
            context.startActivity(intent)
        }

        holder.textView.setOnClickListener {
            val intent= Intent(context,
                Main2Activity::class.java)
            intent.putExtra("SuggestId",admiring[position].id)
            context.startActivity(intent)
        }
    }
    class ViewHolder(itemView:View):RecyclerView.ViewHolder(itemView)
    {
        val imageView:CircleImageView=itemView.findViewById(R.id.imageViewForAdmirers)
        val textView:TextView=itemView.findViewById(R.id.textViewForAdmirers)
        val switch:Switch=itemView.findViewById(R.id.switchForAdmirers)
        val layout:LinearLayout=itemView.findViewById(R.id.layoutForAdmirers)
    }

}

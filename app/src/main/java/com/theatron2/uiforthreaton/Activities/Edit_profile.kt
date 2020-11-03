@file:Suppress("DEPRECATED_IDENTITY_EQUALS")

package com.theatron2.uiforthreaton.Activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import com.theatron2.uiforthreaton.EmojiUtils
import com.theatron2.uiforthreaton.R
import kotlinx.android.synthetic.main.activity_edit_profile.*
import java.net.URL
import java.util.*

@Suppress("UNCHECKED_CAST")
class Edit_profile : AppCompatActivity() {
    val mAuth = FirebaseAuth.getInstance()
    val myRef = FirebaseDatabase.getInstance().reference
    val PICK_IMAGE:Int = 1
    val pick_image_permission=1001
    var resultUri :Uri?=null
    var url:String=""
    val profileImgRef = FirebaseStorage.getInstance().reference.child("profileImages")
    lateinit var progressBar:ProgressBar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)
//        val x =mAuth.currentUser!!.email
//        Log.i("EMAIL", "$x 123")
//        if(x!=null) {
//            emailForEP.text = x
//        }
        progressBar=findViewById(R.id.progressBarini)
        val createRef = myRef.child("USER").child(mAuth.currentUser!!.uid)
        if(mAuth?.currentUser != null) {
                createRef.addListenerForSingleValueEvent(
                object :ValueEventListener
                {
                    override fun onCancelled(p0: DatabaseError) {}

                    override fun onDataChange(p0: DataSnapshot) {
                        val name = p0.child("name").value.toString()
                        nameForEP.setText(name)
                        url = p0.child("photourl").value.toString()
                        usernameForEP.setText(name.toLowerCase(Locale.ROOT).replace(" ", ""))

                        phonenumberForEP.setText( p0.child("phonenumber").value.toString())
                        if(p0.child("web").exists())
                        {
                            val web =  p0.child("web").value.toString()
                            website.setText(web)
                        }

                        if(p0.child("bio").exists())
                        {
                            val bio = p0.child("bio").value.toString()
                            bioForEp.setText(bio)
                        }
                        val photo = p0.child("photourl").value.toString()
                        Picasso.get().load(photo).placeholder(R.drawable.ic_account_circle_black_24dp).into(imageViewForEP)
                    }
                }
            )

        }

        changeProfileImage.setOnClickListener {
            checkpermission_gallery()
        }
        val sharedPreferences  = getSharedPreferences("USERNAME", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString("page","5").apply()
        cancel.setOnClickListener {

            val intent = Intent(this,MainActivity::class.java)
            startActivity(intent)
        }
        doneV.setOnClickListener {
            progressBar.visibility = View.VISIBLE
            doneV.isEnabled=false
            cancel.isEnabled=false
            if (resultUri != null) {

                val filePath = profileImgRef.child(mAuth.currentUser!!.uid + ".jpg")
                filePath.putFile(resultUri!!)
                    .continueWithTask {
                        filePath.downloadUrl
                    }
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            val downloadUri = it.result
                            url = URL(downloadUri.toString()).toString()
                            if(EmojiUtils.containsEmoji(nameForEP.text.toString()) || EmojiUtils.containsEmoji(usernameForEP.text.toString()))
                            {
                                Toast.makeText(this,"Emojis Not Allowed in Name And Username.",Toast.LENGTH_LONG).show()
                                doneV.isEnabled=true
                                cancel.isEnabled=true
                                progressBar.visibility = View.GONE
                            }
                            else {
                                updateContent()
                            }
                        } else {
                            progressBar.visibility = View.GONE
                            Toast.makeText(this, "Unable to upload image..", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
            else{
                if(EmojiUtils.containsEmoji(nameForEP.text.toString()) || EmojiUtils.containsEmoji(usernameForEP.text.toString()))
                {
                    Toast.makeText(this,"Emojis Not Allowed in Name And Username.",Toast.LENGTH_LONG).show()
                    doneV.isEnabled=true
                    cancel.isEnabled=true
                    progressBar.visibility = View.GONE
                }
                else {
                    updateContent()
                }
            }
        }
    }
    fun updateContent(){
        val createRef = myRef.child("USER").child(mAuth.currentUser!!.uid)
        createRef.child("photourl").setValue(url).addOnSuccessListener {
            createRef.child("name").setValue(nameForEP.text.toString()).addOnSuccessListener {
                createRef.child("username").setValue(usernameForEP.text.toString())
                    .addOnSuccessListener {
                        createRef.child("web").setValue(website.text.toString())
                            .addOnSuccessListener {
                                createRef.child("bio").setValue(bioForEp.text.toString())
                                    .addOnSuccessListener {
                                        ////////////////////////////
                                        createRef.child("phonenumber").setValue(phonenumberForEP.text.toString())
                                            .addOnSuccessListener {
                                                myRef.child("USER")
                                                    .addListenerForSingleValueEvent(
                                                        object : ValueEventListener {
                                                            override fun onCancelled(p0: DatabaseError) {
                                                            }

                                                            override fun onDataChange(p0: DataSnapshot) {
                                                                for (allUserList in p0.children) {
                                                                        val userData =
                                                                            allUserList.value as HashMap<String, String>
                                                                        if (userData["id"] == mAuth.currentUser!!.uid) {
                                                                            val key = allUserList.key!!
                                                                            myRef.child("USER").child("photo").child(key).setValue(url)
                                                                            myRef.child("USER").child("name").child(key).setValue(nameForEP.text.toString())
                                                                            myRef.child("USER").child("phonenumber").child(key).setValue(phonenumberForEP.text.toString())
                                                                            break
                                                                        }
                                                                }

                                                                val sharedPreferences =
                                                                    getSharedPreferences(
                                                                        "USERNAME",
                                                                        Context.MODE_PRIVATE
                                                                    ).edit()
                                                                sharedPreferences.putString(
                                                                    "name_of_person",
                                                                    nameForEP.text.toString()
                                                                )
                                                                sharedPreferences.putString(
                                                                    "photo_of_person",
                                                                    url
                                                                )
                                                                sharedPreferences.apply()

                                                                Toast.makeText(
                                                                    this@Edit_profile,
                                                                    "Successfully Updated Profile",
                                                                    Toast.LENGTH_SHORT
                                                                ).show()
                                                                doneV.isEnabled = true
                                                                cancel.isEnabled = true
                                                                progressBar.visibility = View.GONE
                                                                val intent = Intent(
                                                                    this@Edit_profile,
                                                                    MainActivity::class.java
                                                                )
                                                                startActivity(intent)
                                                            }
                                                        })
                                                ///////////////////////////
                                            }
                                            .addOnFailureListener {
                                                Toast.makeText(
                                                    this,
                                                    "Unable Updated Profile",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                doneV.isEnabled=true
                                                cancel.isEnabled=true
                                                progressBar.visibility = View.GONE
                                            }

                                    }.addOnFailureListener {
                                        Toast.makeText(
                                            this,
                                            "Unable Updated Profile",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        doneV.isEnabled=true
                                        cancel.isEnabled=true
                                        progressBar.visibility = View.GONE
                                    }
                            }.addOnFailureListener {
                                Toast.makeText(this, "Unable Updated Profile", Toast.LENGTH_SHORT)
                                    .show()
                                doneV.isEnabled=true
                                cancel.isEnabled=true
                                progressBar.visibility = View.GONE
                            }
                    }.addOnFailureListener {
                        Toast.makeText(this, "Unable Updated Profile", Toast.LENGTH_SHORT).show()
                        doneV.isEnabled=true
                        cancel.isEnabled=true
                        progressBar.visibility = View.GONE
                    }
            }.addOnFailureListener {
                Toast.makeText(this, "Unable Updated Profile", Toast.LENGTH_SHORT).show()
                doneV.isEnabled=true
                cancel.isEnabled=true
                progressBar.visibility = View.GONE
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Unable Updated Profile", Toast.LENGTH_SHORT).show()
            doneV.isEnabled=true
            cancel.isEnabled=true
            progressBar.visibility = View.GONE
        }
    }
    private fun checkpermission_gallery() {

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (this.let { ActivityCompat.checkSelfPermission(it,android.Manifest.permission.READ_EXTERNAL_STORAGE) } == PackageManager.PERMISSION_DENIED){
                val permission= arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                requestPermissions(permission,pick_image_permission)
            }
            else{
                pickImageFromGallery()
            }
        }
        else{
            pickImageFromGallery()
        }
    }
    private fun pickImageFromGallery(){
        val intent:Intent= Intent(Intent.ACTION_PICK)
        intent.type=("image/*")
        startActivityForResult(intent,PICK_IMAGE)
    }

    override  fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode== Activity.RESULT_OK && requestCode==PICK_IMAGE){
            if (data != null) {
                CropImage.activity(data.data)
                    .setAspectRatio(1,1)
                    .start(this)
            }
        }
        if (requestCode === CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode === Activity.RESULT_OK) {
                resultUri = result.uri
                imageViewForEP.setImageURI(resultUri)

            }
            else if (resultCode === CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val error = result.error
                progressBar.visibility = View.INVISIBLE
                Toast.makeText(this, error.toString(), Toast.LENGTH_LONG).show()
            }
        }

    }

}
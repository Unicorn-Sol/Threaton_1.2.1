package com.theatron2.uiforthreaton.Activities


import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.facebook.*
import com.facebook.appevents.AppEventsLogger
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.theatron2.uiforthreaton.R
import kotlinx.android.synthetic.main.activity_sign_in.*


@Suppress("DEPRECATION", "UNCHECKED_CAST", "CAST_NEVER_SUCCEEDS")
class SignInActivity : AppCompatActivity() {
    lateinit var mGoogleSignInClient:GoogleSignInClient
    lateinit var  callbackManager :CallbackManager
    var database=FirebaseDatabase.getInstance()
    var myref=database.reference
    var phoneNumber:String="-1"
    var type :String =""
    @SuppressLint("HardwareIds")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FacebookSdk.sdkInitialize(this)
        AppEventsLogger.activateApp(application)
        setContentView(R.layout.activity_sign_in)


        val sharedPreferences = getSharedPreferences("USERNAME", Context.MODE_PRIVATE)
        type = if(sharedPreferences.getString("type","0")!! == "1"){
            "audience"
        }
        else{
            "artist"
        }

        //setSupportActionBar(toolbar)
        //supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        //supportActionBar!!.title="Sign In "

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.we_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        tv_google.setOnClickListener {
            val signInIntent = mGoogleSignInClient.signInIntent
            startActivityForResult(signInIntent, 12345)
        }
        //FaceBook Login

        callbackManager = CallbackManager.Factory.create()

        tv_facebook.setOnClickListener {
            LoginManager.getInstance().logInWithReadPermissions(this, arrayListOf("email","public_profile"))
            LoginManager.getInstance().registerCallback(callbackManager, object :
                FacebookCallback<LoginResult> {
                override fun onSuccess(loginResult: LoginResult) {
                    Log.d("TAG", "facebook:onSuccess:$loginResult")
                    handleFacebookAccessToken(loginResult.accessToken)
                }

                override fun onCancel() {
                    Log.d("TAG", "facebook:onCancel")
                }

                override fun onError(error: FacebookException) {
                    Log.d("TAG", "facebook:onError ${error.message}")
                }
            })

        }

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 12345) {
            progressBar.visibility= View.VISIBLE
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                firebaseAuthWithGoogle(account)
            } catch (e: ApiException) {
                Log.e("TAG12345", "Google sign in failed ${e.statusCode} ${e.message}")
                progressBar.visibility=View.GONE
                Toast.makeText(this,"Please Check Your Internet Connection",Toast.LENGTH_LONG).show()
            }
        }
        else {
            callbackManager.onActivityResult(requestCode, resultCode, data)
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        val auth:FirebaseAuth=FirebaseAuth.getInstance()
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)

        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.e("TAG", "signInWithCredential:success")
                    val user = auth.currentUser!!
                    progressBar.visibility=View.GONE
                    ///////
                    val hashMap=HashMap<String,String>()
                    hashMap["name"]=user.displayName!!
                    hashMap["photourl"]=user.photoUrl.toString()
                    hashMap["sharescount"]="0"
                    hashMap["admirerscount"]="0"
                    hashMap["phonenumber"]= phoneNumber
                    hashMap["followerscount"]="0"
                    myref.child("USER").child(user.uid)
                        .addValueEventListener(object : ValueEventListener {
                            override fun onCancelled(p0: DatabaseError) {}
                            override fun onDataChange(p0: DataSnapshot) {
                                if (!p0.exists()) {
                                    myref.child("USER").child(user.uid).setValue(hashMap)
                                }
                                myref.child("USER").child(user.uid).child("type").setValue(type)
                            }
                        })
                    myref.child("USER").addListenerForSingleValueEvent(
                        object : ValueEventListener {
                            override fun onCancelled(p0: DatabaseError) {
                            }

                            override fun onDataChange(p0: DataSnapshot) {
                                val xUser = user_with_id_phoneNumber(
                                    user.displayName!!,
                                    user.photoUrl.toString(),
                                    user.uid,
                                    phoneNumber
                                )
                                val arrayListOfUID = ArrayList<String>()

                                for (dataBaseUser in p0.children) {
                                    Log.e("sign in activity", dataBaseUser.value.toString())
                                    val databaseUserValue =
                                        dataBaseUser.value as HashMap<String, String>
                                    Log.e( "Sign in Activity", dataBaseUser.value.toString() )
                                    arrayListOfUID.add(databaseUserValue["id"]!!)
                                }
                                if (!arrayListOfUID.contains(xUser.id)) {
                                    val key = myref.child("USER").push().key!!
                                    myref.child("USER").child(key).setValue(xUser)
                                }
                            }
                        }
                    )
                    ///////
                    val intent=Intent(this, MainActivity::class.java)
                    intent.flags=Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    Toast.makeText(this,"Sign In Successfully ..\n Welcome  ${user.displayName}",Toast.LENGTH_LONG).show()
                } else {
                    progressBar.visibility=View.GONE
                    Log.e("TAG", "signInWithCredential:failure", task.exception)
                    Toast.makeText(this,"Please Check Your Internet Connection",Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun handleFacebookAccessToken(token: AccessToken) {
        progressBar.visibility=View.VISIBLE
        Log.e("TAG", "handleFacebookAccessToken:$token")
        val auth=FirebaseAuth.getInstance()
        val credential = FacebookAuthProvider.getCredential(token.token)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.e("TAG", "signInWithCredential:success")
                    val user = auth.currentUser!!
                    progressBar.visibility=View.GONE
                    /////
                    val hashMap=HashMap<String,String>()
                    hashMap["name"]=user.displayName!!
                    hashMap["photourl"]=user.photoUrl.toString()
                    hashMap["sharescount"]="0"
                    hashMap["admirerscount"]="0"
                    hashMap["followerscount"]="0"
                    hashMap["phonenumber"]= phoneNumber
                    myref.child("USER").child(user.uid).addValueEventListener(object :ValueEventListener {
                        override fun onCancelled(p0: DatabaseError) {}
                        override fun onDataChange(p0: DataSnapshot) {
                            if (!p0.exists()) {
                                myref.child("USER").child(user.uid)
                                    .setValue(hashMap)
                            }
                            myref.child("USER").child(user.uid).child("type").setValue(type)
                        }
                    })
                    myref.child("USER").addListenerForSingleValueEvent(
                        object:ValueEventListener
                        {
                            override fun onCancelled(p0: DatabaseError) {

                            }
                            override fun onDataChange(p0: DataSnapshot) {
                                val xUser=user_with_id_phoneNumber(user.displayName!!,user.photoUrl.toString(),user.uid,phoneNumber)
                                val arrayListOfUID=ArrayList<String>()

                                for(dataBaseUser in p0.children){
                                    val databaseUserValue = dataBaseUser.value as HashMap<String,String>
                                    arrayListOfUID.add(databaseUserValue["id"]!!)
                                }
                                if(!arrayListOfUID.contains(xUser.id)){
                                    val key = myref.child("USER").push().key!!
                                    myref.child("USER").child(key).setValue(xUser)
                                }
                            }

                        }
                    )
                    /////
                    val intent=Intent(this,
                        MainActivity::class.java)
                    intent.flags=Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    progressBar.visibility=View.GONE
                    Toast.makeText(this,"Sign In Successfully ..\n Welcome ${user.displayName}",Toast.LENGTH_LONG).show()
                } else {
                    progressBar.visibility=View.GONE
                    Log.e("TAG", "signInWithCredential:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed.", Toast.LENGTH_SHORT).show()
                }
            }
    }

}

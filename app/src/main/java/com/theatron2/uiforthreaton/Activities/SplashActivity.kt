package com.theatron2.uiforthreaton.Activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.theatron2.uiforthreaton.R


class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
//        val sharedpref= getSharedPreferences("USERNAME",Context.MODE_PRIVATE)
//        val type =sharedpref.getString("type","0")!!
        //FirebaseDatabase.getInstance().setPersistenceEnabled(true)
        val sharedPreferences  = getSharedPreferences("USERNAME", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString("page","0").apply()
        Handler().postDelayed({
            val intent=Intent(this,
                MainActivity::class.java)
            intent.flags=Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finishAffinity()
        },3000)


    }

}

package com.theatron2.uiforthreaton.Activities

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetView
import com.theatron2.uiforthreaton.R
import kotlinx.android.synthetic.main.activity_user_type.*

class UserTypeActivity : AppCompatActivity() {

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_type)
        val sharedPreferences=getSharedPreferences("USERNAME", Context.MODE_PRIVATE).edit()

        tv_audience.setOnClickListener {
            sharedPreferences.putString("type","1")
            sharedPreferences.apply()
            val intent= Intent(this,
                SignInActivity::class.java)
            intent.flags=Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)

        }
        tv_artist.setOnClickListener {
            sharedPreferences.putString("type", 2.toString())
            sharedPreferences.apply()
            val intent= Intent(this,
                SignInActivity::class.java)
            intent.flags=Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }


        TapTargetView.showFor(this,  // `this` is an Activity
            TapTarget.forView(
                findViewById(R.id.tv_artist),
                "Artist",
                "Have Something Incredible To Show \nRoll In! "
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
                .targetRadius(70),
            object : TapTargetView.Listener() {
                override fun onTargetClick(view: TapTargetView) {
                    super.onTargetClick(view) // This call is optional
                    view.dismiss(true)
                    TapTargetView.showFor(this@UserTypeActivity,  // `this` is an Activity
                        TapTarget.forView(
                            findViewById(R.id.tv_audience),
                            "Audience",
                            "Want To Watch Something Incredible And Socialise\n Roll In!."
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
                            .targetRadius(74),
                        object : TapTargetView.Listener() {
                            override fun onTargetClick(view: TapTargetView) {
                                super.onTargetClick(view) // This call is optional
                                view.dismiss(true)
                            }
                        })
                }
            })
    }

    override fun onBackPressed() {
        finishAffinity()
        super.onBackPressed()
    }
}

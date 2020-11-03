package com.theatron2.uiforthreaton.Activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.MenuItem
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.iid.FirebaseInstanceId
import com.theatron2.uiforthreaton.R
import com.theatron2.uiforthreaton.ui.feed.FeedFragment
import com.theatron2.uiforthreaton.ui.friends.FriendFragment
import com.theatron2.uiforthreaton.ui.friends2.Friends2_Fragment
import com.theatron2.uiforthreaton.ui.notification.NotificationFragment
import com.theatron2.uiforthreaton.ui.profile.ProfileFragment
import com.theatron2.uiforthreaton.ui.search.SearchFragment
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.fragment_search.*


class MainActivity : AppCompatActivity() {
    val mAuth=FirebaseAuth.getInstance()
    val myRef = FirebaseDatabase.getInstance().reference
    @SuppressLint("CommitPrefEdits", "ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.activity_main)
        //setSupportActionBar(toolbar)

        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        val sharedPreference=getSharedPreferences("USERNAME", Context.MODE_PRIVATE)
        val type=sharedPreference.getString("type","0")!!
//        if(type=="1")
//        {
//            navView.inflateMenu(R.menu.bottom_nav_menu_5)
//        }
//        else if(type=="2")
//        {
//
//        }
        //navView.inflateMenu(R.menu.bottom_nav_menu)


        if(mAuth.currentUser!=null) {
            myRef.child("USER").child(mAuth.currentUser!!.uid).addListenerForSingleValueEvent(
                object : ValueEventListener {
                    override fun onCancelled(error: DatabaseError) {

                    }

                    override fun onDataChange(snapshot: DataSnapshot) {
                        val sharedPreferences = getSharedPreferences("USERNAME", Context.MODE_PRIVATE).edit()
                        sharedPreferences.putString("name_of_person",snapshot.child("name").value.toString())
                        sharedPreferences.putString("photo_of_person",snapshot.child("photourl").value.toString())
                        sharedPreferences.apply()
                    }
                }
            )
        }

        getNotificationToken()

        val navHostFragment = nav_host_fragment as NavHostFragment
        val graphInflater = navHostFragment.navController.navInflater
        val navGraph = graphInflater.inflate(R.navigation.mobile_navigation)
        val navigation_controller = navHostFragment.navController
        val bottomNavigationView=findViewById<BottomNavigationView>(R.id.nav_view)


        val page=sharedPreference.getString("page","0")!!
        if(page.toInt()==2) {
            navGraph.startDestination = R.id.navigation_search
            bottomNavigationView.selectedItemId = R.id.search
            sharedPreference.edit().putString("page","0").apply()
        }
        else if(page.toInt()==5)
        {
            navGraph.startDestination = R.id.navigation_profile
            bottomNavigationView.selectedItemId = R.id.profile
            sharedPreference.edit().putString("page","0").apply()
        }

        navigation_controller.graph=navGraph

        val navController = findNavController(R.id.nav_host_fragment)
        navView.setupWithNavController(navController)
        navView.setOnNavigationItemSelectedListener(object:BottomNavigationView.OnNavigationItemSelectedListener
        {
            @SuppressLint("RestrictedApi")

            override fun onNavigationItemSelected(item: MenuItem): Boolean {
                when (item.itemId) {
                    R.id.feed -> {
                        supportFragmentManager.beginTransaction().replace(R.id.nav_host_fragment,FeedFragment()).addToBackStack(null).commit()
                        return true
                    }
                    R.id.search -> {
                            supportFragmentManager.beginTransaction()
                                .replace(R.id.nav_host_fragment, SearchFragment())
                                .addToBackStack(null).commit()

                        return true
                    }
                    R.id.profile -> {
                            supportFragmentManager.beginTransaction()
                                .replace(R.id.nav_host_fragment, ProfileFragment())
                                .addToBackStack(null).commit()
                        return true
                    }
                    R.id.friends -> {
                        supportFragmentManager.beginTransaction().replace(R.id.nav_host_fragment, FriendFragment()).addToBackStack(null).commit()
                        return true
                    }
                    R.id.friends2->
                    {
                        supportFragmentManager.beginTransaction().replace(R.id.nav_host_fragment,
                            Friends2_Fragment()
                        ).addToBackStack(null).commit()
                        return true
                    }
                    R.id.notifications ->{
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.nav_host_fragment, NotificationFragment())
                            .addToBackStack(null).commit()

                        return true
                    }
                    else-> {
                        return false
                    }
                }
            }
        })
        val sharedPRRR =  getSharedPreferences("USERNAME", Context.MODE_PRIVATE)
        val firstTimer = sharedPRRR.getBoolean("firsttime",true)
        if(firstTimer)
        {
            TapTargetView.showFor(this,  // `this` is an Activity
                TapTarget.forView(
                    findViewById(R.id.search),
                    "Search",
                    "Explore Your Interest \nAdmire Your Favourite Artists."
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
                    .targetRadius(50),
                object : TapTargetView.Listener() {
                    override fun onTargetClick(view: TapTargetView) {
                        super.onTargetClick(view) // This call is optional
                        view.dismiss(true)
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.nav_host_fragment, SearchFragment())
                            .addToBackStack(null).commit()
                        bottomNavigationView.selectedItemId = R.id.search

                        TapTargetView.showFor(this@MainActivity,  // `this` is an Activity
                            TapTarget.forView(
                                findViewById(R.id.friends2),
                                "Friends",
                                "Make Toggle Friends \nShare Happiness."
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
                                .targetRadius(50),
                            object : TapTargetView.Listener() {
                                override fun onTargetClick(view: TapTargetView) {
                                    super.onTargetClick(view) // This call is optional
                                    view.dismiss(true)
                                    supportFragmentManager.beginTransaction()
                                        .replace(R.id.nav_host_fragment, Friends2_Fragment())
                                        .addToBackStack(null).commit()
                                    bottomNavigationView.selectedItemId = R.id.friends2

                                    TapTargetView.showFor(this@MainActivity,  // `this` is an Activity
                                        TapTarget.forView(
                                            findViewById(R.id.feed),
                                            "Feed",
                                            "Your New Home \nAll Activities will broadcast here."
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
                                            .targetRadius(50),
                                        object : TapTargetView.Listener() {
                                            override fun onTargetClick(view: TapTargetView) {
                                                super.onTargetClick(view) // This call is optional
                                                view.dismiss(true)
                                                supportFragmentManager.beginTransaction()
                                                    .replace(R.id.nav_host_fragment, FeedFragment())
                                                    .addToBackStack(null).commit()
                                                bottomNavigationView.selectedItemId = R.id.feed
                                                sharedPRRR.edit().putBoolean("firsttime",false).apply()
                                            }
                                        })
                                }
                            })
                    }
                })
        }
    }

    @SuppressLint("WrongConstant")
    override fun onBackPressed() {
        val sharedPreference=getSharedPreferences("USERNAME", Context.MODE_PRIVATE)


        val bottomNavigationView=findViewById<BottomNavigationView>(R.id.nav_view)
        val selectedItemId=bottomNavigationView.selectedItemId
        if(selectedItemId== R.id.profile )
        {
            if(drawer_layout!=null && drawer_layout.isDrawerOpen(android.view.Gravity.END)) {
                drawer_layout.closeDrawer(android.view.Gravity.END)
            }
            else if(drawer_layout==null)
            {
                if(supportFragmentManager.backStackEntryCount>0) {
                    supportFragmentManager.popBackStackImmediate("ProfileF",FragmentManager.POP_BACK_STACK_INCLUSIVE)
                }
            }
            else if(drawer_layout!=null)
            {
                bottomNavigationView.selectedItemId= R.id.feed
            }
        }
        else if(selectedItemId==R.id.search)
        {
            if(searchBarForSearch==null)
            {
                supportFragmentManager.popBackStackImmediate("SearchF",FragmentManager.POP_BACK_STACK_INCLUSIVE)
            }
            else
            {
                bottomNavigationView.selectedItemId=R.id.feed
            }
        }
        else if(selectedItemId!= R.id.feed)
        {
            bottomNavigationView.selectedItemId= R.id.feed
//            val x=supportFragmentManager.getBackStackEntryAt(supportFragmentManager.backStackEntryCount-1)
//            bottomNavigationView.selectedItemId=x.id//supportFragmentManager.findFragmentById(x)
//            supportFragmentManager.popBackStackImmediate()
        }
        else {
            finishAffinity()
            super.onBackPressed()
        }
    }
    override fun onStart() {
        super.onStart()
        val user=mAuth.currentUser
        if(user==null)
        {
            val intent=Intent(this, UserTypeActivity::class.java)
            intent.flags=Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
    override fun onSaveInstanceState(InstanceState: Bundle) {
        super.onSaveInstanceState(InstanceState)
        InstanceState.clear()
    }

    fun getNotificationToken() {
        if (mAuth.currentUser != null) {
            val user = mAuth.currentUser!!.uid
            FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener {
                if (!it.isSuccessful) {
                    return@addOnCompleteListener
                }
                val token = it.result!!.token
                myRef.child("TOKENS").child(user).setValue(token)
            }
        }
    }
}
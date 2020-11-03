package com.theatron2.uiforthreaton.Activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.theatron2.uiforthreaton.R
import com.theatron2.uiforthreaton.ui.feed.UserComments
import kotlinx.android.synthetic.main.activity_comments.*

@Suppress("UNCHECKED_CAST")
class CommentsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comments)
        setSupportActionBar(toolbar)
        supportActionBar!!.title="Comments"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        val arrayOfComments:ArrayList<UserComments> =intent.extras!!.get("Array") as ArrayList<UserComments>
//        val adapter=CustomAdapterOFFeedForComments(this,arrayOfComments)
//        recyclerViewForComments.layoutManager=LinearLayoutManager(this)
//        recyclerViewForComments.addItemDecoration(
//            DividerItemDecoration(this,
//                DividerItemDecoration.HORIZONTAL)
//        )
//        recyclerViewForComments.hasFixedSize()
//        recyclerViewForComments.adapter=adapter
//        adapter.notifyDataSetChanged()
    }

    override fun onBackPressed() {
        val intent=Intent(this,MainActivity::class.java)
        intent.flags=Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        super.onBackPressed()
    }

}

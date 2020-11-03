package com.theatron2.uiforthreaton.ui.notification

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import com.theatron2.uiforthreaton.R
import com.theatron2.uiforthreaton.adapterForAllClasses.AdapterForNotifications
import kotlinx.android.synthetic.main.fragment_notification.*


class NotificationFragment : Fragment() {

    val uid = FirebaseAuth.getInstance().uid

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.e("Notification fragment", "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result

            Log.e("Notification fragment", token)
            Toast.makeText(requireContext(), token, Toast.LENGTH_SHORT).show()
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_notification, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        FirebaseMessaging.getInstance().subscribeToTopic(uid!!).addOnCompleteListener { task ->
            var msg = getString(R.string.msg_subscribed)
            if (!task.isSuccessful) {
                msg = getString(R.string.msg_subscribe_failed)
            }
            Log.e("Notification fragment", msg)
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
        }

        recycler_view_notifications.adapter = AdapterForNotifications()
        recycler_view_notifications.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.VERTICAL,
            false
        )
    }



}
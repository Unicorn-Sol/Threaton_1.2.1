package com.theatron2.uiforthreaton.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.theatron2.uiforthreaton.Activities.MainActivity;
import com.theatron2.uiforthreaton.R;
import com.theatron2.uiforthreaton.adapterForAllClasses.AdapterForNotifications;
import com.theatron2.uiforthreaton.db.notification_db.Notification;
import com.theatron2.uiforthreaton.db.notification_db.NotificationDatabase;

public class FirebaseMessagingServiceClass extends FirebaseMessagingService {

    String uid = FirebaseAuth.getInstance().getUid();
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        if(remoteMessage.getNotification()!=null){
            String body = remoteMessage.getNotification().getBody();
            String title = remoteMessage.getNotification().getTitle();

            Notification notification = new Notification(0,title,body, "few seconds ago" );
            NotificationDatabase.Companion.invoke(getBaseContext()).getNotificationDao().insertNotification(notification);
            //Send notification function when app is in foreground
            sendNotification(body,title);
        }


    }
    @Override
    public void onNewToken(@NonNull String token) {

        Log.e("FCM", token );
        //sendRegistrationToServer(token);
    }

    private void sendNotification(String messageBody, String title) {
        String channelId = "visible_complaints";
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.ic_star_black_24dp)
                        .setContentTitle(title)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(messageBody)
                        )
                        .setContentText(messageBody)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setPriority(NotificationCompat.PRIORITY_MAX);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "General",
                    NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify((int) System.currentTimeMillis(), notificationBuilder.build());
    }

    public void subscribe() {
        FirebaseMessaging.getInstance().subscribeToTopic(uid)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        String msg = getString(R.string.msg_subscribed);
                        if (!task.isSuccessful()) {
                            msg = getString(R.string.msg_subscribe_failed);
                        }
                        Log.d("FB messaging service", msg);
                        Toast.makeText( getBaseContext(), msg, Toast.LENGTH_SHORT).show();
                    }
                });

    }
}

package com.theatron2.uiforthreaton.db.notification_db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase


@Database(entities = [Notification::class], version = 1, exportSchema = false)
abstract class NotificationDatabase : RoomDatabase() {

    abstract fun getNotificationDao() : NotificationDao

    companion object{

        @Volatile private var instance : NotificationDatabase? = null

        operator fun invoke(context: Context): NotificationDatabase {
            if (instance == null){
                synchronized(this){
                    instance = Room.databaseBuilder(context.applicationContext, NotificationDatabase::class.java, "notificationDB").build()
                }
            }
            return instance!!
        }


    }
}
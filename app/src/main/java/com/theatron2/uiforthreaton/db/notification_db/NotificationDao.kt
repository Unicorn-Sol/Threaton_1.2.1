package com.theatron2.uiforthreaton.db.notification_db

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface NotificationDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertNotification(notification : Notification)

    @Query("select * from Notification order by id desc")
    suspend fun getAllNotifications() : List<Notification>

    @Delete
    suspend fun deleteNotification(task: Notification)

    @Query("delete from Notification")
    suspend fun deleteAllNotifications()
}
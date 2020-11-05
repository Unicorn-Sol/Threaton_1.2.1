package com.theatron2.uiforthreaton.db.notification_db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Notification (
    @PrimaryKey(autoGenerate = true)
    var id:Int = 0,
    val title: String,
    val body : String,
    var time : String
)
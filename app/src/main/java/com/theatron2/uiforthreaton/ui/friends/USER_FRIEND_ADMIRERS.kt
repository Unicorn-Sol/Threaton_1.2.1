package com.theatron2.uiforthreaton.ui.friends

import java.io.Serializable

data class USER_FRIEND_ADMIRERS(var name:String, var photo:String, val id:String):Serializable
{
    constructor():this("","","")
}
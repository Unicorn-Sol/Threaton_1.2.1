package com.theatron2.uiforthreaton.ui.friends

import java.io.Serializable

data class USER_FRIEND_ADMIRING(var name:String, var photo:String, var id:String, var friends:Boolean):Serializable
{
    constructor():this("","","",false)
}

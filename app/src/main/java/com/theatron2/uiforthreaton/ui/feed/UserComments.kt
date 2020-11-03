package com.theatron2.uiforthreaton.ui.feed

data class UserComments(var userName:String,var photo:String,var comments: String,val id:String):java.io.Serializable
{
    constructor():this("", "","","")
}
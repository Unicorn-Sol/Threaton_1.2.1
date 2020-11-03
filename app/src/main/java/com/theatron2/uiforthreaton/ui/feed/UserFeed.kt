package com.theatron2.uiforthreaton.ui.feed

import java.io.Serializable

data class UserFeed(var bookmark:Boolean,var image:String,var UserName:String,var videoUrl:String,var view:Int,var likeNumber:Int,var disLikeNumber:Int,var ShareNumber:Int,var LikedORNot:Boolean,var DislikedOrNot:Boolean,var Title:String,var details:String,var arrayOfComments:ArrayList<UserComments>,var id:String,var vnum:String,val thumbnail:String,val date:String,val time:String) :Serializable
{
    constructor():this(false, "","","",0,0,0,0,false,false,"","", arrayListOf(),"","","","","")
}
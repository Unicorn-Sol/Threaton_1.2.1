package com.theatron2.uiforthreaton.Activities

data class Request_type_data_with_friend(var name:String, var photo:String, var id:String, var friends:Boolean, val phonenumber:String)
{
    constructor():this("","","",false,"")
}

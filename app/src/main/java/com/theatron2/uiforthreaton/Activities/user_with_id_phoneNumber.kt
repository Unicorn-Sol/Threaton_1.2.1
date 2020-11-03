package com.theatron2.uiforthreaton.Activities

data class user_with_id_phoneNumber(var name:String, var photo:String, val id:String, val phonenumber:String)
{
    constructor():this("","","","")
}
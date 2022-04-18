package edu.icesi.pokedex.model

import android.graphics.Bitmap
import java.util.Date

data class Pokemon(val name:String, val type:String, val imgUrl:String, val hp:Int, val attack:Int, val defense:Int, val speed:Int, val date:Date, var imgBitmap: Bitmap?, val trainerUserName:String){
    override fun equals(other: Any?): Boolean {
        return if(other is Pokemon){
            (this.name==other.name)
        }else{
            super.equals(other)
        }
    }
}
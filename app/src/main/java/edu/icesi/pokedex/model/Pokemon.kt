package edu.icesi.pokedex.model

import android.graphics.Bitmap
import java.util.Date

data class Pokemon(val name:String, val type:String, val imgUrl:String, val hp:Int, val attack:Int, val defense:Int, val speed:Int, val date:Date, var imgBitmap: Bitmap?)
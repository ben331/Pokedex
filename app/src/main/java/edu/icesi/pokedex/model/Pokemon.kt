package edu.icesi.pokedex.model

import android.graphics.Bitmap
import java.util.Date

data class Pokemon(val name:String, val type:String, val image:Bitmap, val defense:Int, val attack:Int, val speed:Int, val life:Int, val date:Date)
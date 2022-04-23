package edu.icesi.pokedex.model

import android.icu.util.Calendar
import android.os.Build
import java.io.Serializable
import java.util.*


data class Pokemon(
    val name:String="",
    val type:String="",
    val imgUrl:String="",
    val hp:Int=0,
    val attack:Int=0,
    val defense:Int=0,
    val speed:Int=0,
    val trainerUserName:String="") : Serializable{

    val id = UUID.randomUUID().toString()
    val date: Date = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        Calendar.getInstance().time
    } else TODO("VERSION.SDK_INT < N")

    override fun equals(other: Any?): Boolean {
        return if (other is Pokemon) {
            (this.name == other.name)
        } else {
            super.equals(other)
        }
    }

    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + imgUrl.hashCode()
        result = 31 * result + hp
        result = 31 * result + attack
        result = 31 * result + defense
        result = 31 * result + speed
        result = 31 * result + date.hashCode()
        result = 31 * result + trainerUserName.hashCode()
        return result
    }
}
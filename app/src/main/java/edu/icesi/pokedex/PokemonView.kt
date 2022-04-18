package edu.icesi.pokedex

import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import edu.icesi.pokedex.model.Pokemon

class PokemonView(itemView: View): RecyclerView.ViewHolder(itemView) {

    lateinit var pokemon:Pokemon

    var listener : OnShowPokemon? = null
        set(value) {
            field = value
            itemPokemonImg.setOnClickListener{
                listener?.show(pokemon)
            }
        }

    var itemPokemonName : TextView = itemView.findViewById(R.id.itemPokemonName)
    var pokemonDate : TextView = itemView.findViewById(R.id.pokemonDate)
    var itemPokemonImg : ImageButton = itemView.findViewById(R.id.itemPokemonImg)

    interface OnShowPokemon{
        fun show(pokemon:Pokemon)
    }
}
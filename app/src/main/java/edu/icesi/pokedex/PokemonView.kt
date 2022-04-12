package edu.icesi.pokedex

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import edu.icesi.pokedex.model.Pokemon

class PokemonView(itemView: View): RecyclerView.ViewHolder(itemView) {

    private lateinit var pokemon:Pokemon

    var listener : OnShowPokemon? = null
    var itemPokemonName : TextView = itemView.findViewById(R.id.itemPokemonName)
    var pokemonDate : TextView = itemView.findViewById(R.id.pokemonDate)
    var itemPokemonImg : ImageView = itemView.findViewById(R.id.itemPokemonImg)

    init{
        itemPokemonImg.setOnClickListener{
            listener?.show(pokemon)
        }
    }

    interface OnShowPokemon{
        fun show(pokemon:Pokemon)
    }
}
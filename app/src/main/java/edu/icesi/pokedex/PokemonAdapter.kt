package edu.icesi.pokedex

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.toolbox.StringRequest
import edu.icesi.pokedex.model.Pokemon
import edu.icesi.pokedex.model.SingleLoggedUser

class PokemonAdapter: RecyclerView.Adapter<PokemonView>(), PokemonView.OnShowPokemon {

    private val pokedex = ArrayList<Pokemon>()

    lateinit var listener : PokemonView.OnShowPokemon

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PokemonView {
        val inflater = LayoutInflater.from(parent.context)
        val item = inflater.inflate(R.layout.item_pokemon, parent, false)
        return PokemonView(item)
    }

    override fun onBindViewHolder(holder: PokemonView, position: Int) {
       val pokemon = pokedex[position]
        holder.pokemon = pokemon
        holder.listener = this
        holder.itemPokemonImg.setImageBitmap(pokemon.imgBitmap!!)
        holder.itemPokemonName.text = pokemon.name
        holder.pokemonDate.text = pokemon.date.toString()
    }

    override fun getItemCount(): Int {
       return pokedex.size
    }

    override fun show(pokemon: Pokemon) {
        listener.show(pokemon)
    }

    fun addPokemon(pokemon:Pokemon){
        pokedex.add(pokemon)
        notifyItemInserted(pokedex.size-1)
    }

    fun delete(pokemon: Pokemon) {
        val index = pokedex.indexOf(pokemon)
        pokedex.removeAt(index)
        notifyItemRemoved(index)
    }
}
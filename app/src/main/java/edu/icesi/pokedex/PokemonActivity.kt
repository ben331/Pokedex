package edu.icesi.pokedex

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.google.gson.Gson
import edu.icesi.pokedex.databinding.ActivityPokemonBinding
import edu.icesi.pokedex.model.Pokemon

class PokemonActivity : AppCompatActivity() {

    //Binding
    private var _binding : ActivityPokemonBinding?=null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Binding
        _binding = ActivityPokemonBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val type = intent.extras?.getInt("type", -1)
        val json = intent.extras?.getString("pokemon", "NO_DATA")
        when(type){
            HomeActivity.OLD_POKEMON-> binding.pokemonCatchBtn.visibility = View.GONE
            HomeActivity.NEW_POKEMON-> binding.dropBtn.visibility = View.GONE
            else -> comeBack( -1, null) //Impossible case
        }

        val pokemon = Gson().fromJson(json, Pokemon::class.java)

        //Binding-----------------------------------------------------------
        binding.pokemonImg.setImageBitmap(pokemon.image)
        binding.pokemonName.text = pokemon.name
        binding.pokemonType.text = pokemon.type
        (pokemon.defense.toString()).also { binding.defenseTxt.text = it }
        (pokemon.attack.toString()).also { binding.attackTxt.text = it }
        (pokemon.speed.toString()).also { binding.speedTxt.text = it }
        (pokemon.life.toString()).also { binding.lifeTxt.text = it }

        //Functions--------------------------------------------------------
        binding.pokemonCatchBtn.setOnClickListener{comeBack(HomeActivity.NEW_POKEMON, pokemon)}
        binding.dropBtn.setOnClickListener{comeBack(HomeActivity.OLD_POKEMON, pokemon)}
    }

    private fun comeBack(type:Int, pokemon:Pokemon?,){
        val intent = Intent(this, HomeActivity::class.java).apply {
            putExtra("type", type)
            putExtra("pokemon", Gson().toJson(pokemon))
        }
    }
}
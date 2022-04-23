package edu.icesi.pokedex

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import com.android.volley.RequestQueue
import com.android.volley.VolleyError
import com.android.volley.toolbox.ImageRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import edu.icesi.pokedex.databinding.ActivityPokemonBinding
import edu.icesi.pokedex.model.Pokemon

class PokemonActivity : AppCompatActivity() {

    //Binding
    private var _binding : ActivityPokemonBinding?=null
    private val binding get() = _binding!!

    //RequestQueue of Volley
    private lateinit var queue : RequestQueue

    private lateinit var pokemon:Pokemon

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Volley
        queue = Volley.newRequestQueue(this)

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

        pokemon = Gson().fromJson(json, Pokemon::class.java)

        //Binding-----------------------------------------------------------
        binding.pokemonName.text = pokemon.name
        binding.pokemonType.text = pokemon.type
        (pokemon.defense.toString()).also { binding.defenseTxt.text = it }
        (pokemon.attack.toString()).also { binding.attackTxt.text = it }
        (pokemon.speed.toString()).also { binding.speedTxt.text = it }
        (pokemon.hp.toString()).also { binding.lifeTxt.text = it }

        //LoadImg
        val imgRequest = ImageRequest(
            pokemon.imgUrl, ::imgResponse,
            0,0,
            ImageView.ScaleType.CENTER,
            Bitmap.Config.ARGB_8888, ::errorResponse)

        queue.add(imgRequest)

        //Functions--------------------------------------------------------
        binding.pokemonCatchBtn.setOnClickListener{comeBack(HomeActivity.NEW_POKEMON, pokemon)}
        binding.dropBtn.setOnClickListener{
            comeBack(HomeActivity.OLD_POKEMON, pokemon)}
    }

    private fun imgResponse(bitmap: Bitmap?) {
        binding.pokemonImg.setImageBitmap(bitmap)
    }

    private fun errorResponse(volleyError: VolleyError?) {
        val msg = "Img not founded. Error:\n\n${volleyError?.message}"
        Toast.makeText(this,msg, Toast.LENGTH_LONG).show()
    }

    private fun comeBack(type:Int, pokemon:Pokemon?){
        val intent = Intent(this, HomeActivity::class.java).apply {
            putExtra("type", type)
            putExtra("pokemon", Gson().toJson(pokemon))
        }
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {

        if(keyCode == KeyEvent.KEYCODE_BACK){
            val intent = Intent(this, HomeActivity::class.java)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
        return true
    }
}
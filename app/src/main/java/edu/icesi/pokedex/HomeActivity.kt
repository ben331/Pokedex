package edu.icesi.pokedex

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.KeyEvent
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import edu.icesi.pokedex.databinding.ActivityHomeBinding
import edu.icesi.pokedex.model.Pokemon

class HomeActivity : AppCompatActivity(), PokemonView.OnShowPokemon {

    //Constants
    companion object{
        const val OLD_POKEMON = 1
        const val NEW_POKEMON = 2
    }

    //Binding
    private var _binding: ActivityHomeBinding? = null
    private val binding get() = _binding!!

    //RecyclerView elements
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var adapter:PokemonAdapter

    //Launchers
    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult(), ::onResult)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Binding
        _binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Recreate RecyclerView
        layoutManager = LinearLayoutManager(this)
        binding.pokemonList.layoutManager = layoutManager
        binding.pokemonList.setHasFixedSize(true)
        binding.pokemonList.adapter = adapter
        adapter.listener = this

        binding.watchBtn.setOnClickListener{
            val pokemon = request(binding.searchTxt.text.toString())
            if(pokemon!=null) show(pokemon, NEW_POKEMON)
            else Toast.makeText(this,R.string.not_founded, Toast.LENGTH_LONG).show()
        }

        binding.homeCatchBtn.setOnClickListener{
            val pokemon = request(binding.searchTxt.text.toString())
            if(pokemon!=null) adapter.addPokemon(pokemon, true)
        }

        binding.searchBtn.setOnClickListener{
            val pokemon = request2(binding.searchTxt.text.toString())
            if(pokemon!=null) show(pokemon, OLD_POKEMON)
            else Toast.makeText(this,R.string.not_founded, Toast.LENGTH_LONG).show()
        }
    }

    private fun request(pokemonName:String):Pokemon?{
        return null
    }

    private fun request2(pokemonName:String):Pokemon?{
        return null
    }

    private fun show(pokemon:Pokemon, type:Int){
        val intent = Intent(this, PokemonActivity::class.java).apply{
            putExtra("pokemon", Gson().toJson(pokemon))
            putExtra("type", type)
        }
        launcher.launch(intent)
    }

    override fun show(pokemon: Pokemon) {
        show(pokemon, OLD_POKEMON)
    }

    private fun onResult(result: ActivityResult) {
        val json = result.data?.extras?.getString("pokemon","NO_DATA")
        if(json!="NO_DATA") adapter.delete(Gson().fromJson(json, Pokemon::class.java))
    }

    //-----------------------------------------------   CLOSE APP   ---------------------------------------------
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {

        if(keyCode== KeyEvent.KEYCODE_BACK){
            val builder = AlertDialog.Builder(this)
            builder.setMessage(R.string.alert_exit)
            builder.apply {
                setPositiveButton(R.string.yes) { _, _ ->
                    val intent = Intent(Intent.ACTION_MAIN)
                    intent.addCategory(Intent.CATEGORY_HOME)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                }
                setNegativeButton(R.string.no) { dialog, _ -> dialog.dismiss() }
            }
            builder.create()
            builder.show()
        }
        return true
    }
}
package edu.icesi.pokedex

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import edu.icesi.pokedex.databinding.ActivityHomeBinding
import edu.icesi.pokedex.model.Pokemon

class HomeActivity : AppCompatActivity(), PokemonView.OnShowPokemon {

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

        }

        binding.catchBtn.setOnClickListener{

        }

        binding.searchBtn.setOnClickListener{

        }
    }

    override fun show(pokemon: Pokemon) {
        val intent = Intent(this, PokemonActivity::class.java).apply{
            putExtra("pokemon", Gson().toJson(pokemon))
        }
        launcher.launch(intent)
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
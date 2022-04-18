package edu.icesi.pokedex

import android.content.Intent
import android.graphics.Bitmap
import android.icu.util.Calendar
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.ImageRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import edu.icesi.pokedex.databinding.ActivityHomeBinding
import edu.icesi.pokedex.model.Pokemon
import edu.icesi.pokedex.model.SingleLoggedUser
import org.json.JSONObject

class HomeActivity : AppCompatActivity(), PokemonView.OnShowPokemon {

    //Constants
    companion object{
        const val OLD_POKEMON = 1
        const val NEW_POKEMON = 2

        const val ERROR = -1
        //const val CANCEL = 0
    }

    //Binding
    private var _binding: ActivityHomeBinding? = null
    private val binding get() = _binding!!

    //RecyclerView elements
    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var adapter:PokemonAdapter

    //Launchers
    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult(), ::onResultShowPokemon)

    //RequestQueue of Volley
    private lateinit var queue: RequestQueue

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Volley
        queue = Volley.newRequestQueue(this)

        //Binding
        _binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Recreate RecyclerView
        layoutManager = LinearLayoutManager(this)
        binding.pokemonList.layoutManager = layoutManager
        binding.pokemonList.setHasFixedSize(true)
        adapter = PokemonAdapter()
        binding.pokemonList.adapter = adapter
        adapter.listener = this
        loadPokemons()

        binding.watchBtn.setOnClickListener{
           requestNewPokemon(binding.searchTxt.text.toString().trim().lowercase(), true)
        }

        binding.homeCatchBtn.setOnClickListener{
            requestNewPokemon(binding.searchTxt.text.toString().trim().lowercase(), false)
        }

        binding.searchBtn.setOnClickListener{
            requestOldPokemon(binding.searchTxt.text.toString().trim().lowercase())
        }
    }

    private fun loadPokemons(){
        val url = "${Constants.BASE_URL}/pokemon/${SingleLoggedUser.user?.username}.json"
        val stringRequest = StringRequest(Request.Method.GET, url,
            { response->
                if(response!="null"){
                    val hashmap = JSONObject(response)
                    for(key in hashmap.keys()){
                        val pokemon = Gson().fromJson(hashmap.get(key).toString(), Pokemon::class.java)
                        recreate(pokemon, NEW_POKEMON)
                    }
                }
            },{
                val msg = "Error :\n\n${it.message}"
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
                Log.e(">>>>>>>>>>>>", msg)
            }
        )
        queue.add(stringRequest)
    }

    private fun requestOldPokemon(pokemonName:String):Pokemon?{
        // Request a string response from the provided URL.
        val url = "${Constants.BASE_URL}/pokemon/${SingleLoggedUser.user!!.username}/${pokemonName}.json"
        val stringRequest = StringRequest(Request.Method.GET, url,
            { response ->
                val pokemon = Gson().fromJson(response, Pokemon::class.java)
                show(pokemon, OLD_POKEMON)
            },
            { })

        // Add the request to the RequestQueue.
        queue.add(stringRequest)
        return null
    }

    private fun requestNewPokemon(pokemonName:String, toShow:Boolean){
        // Request a string response from the provided URL.
        val url = "${Constants.POKE_API}/pokemon/${pokemonName}"
        val stringRequest = StringRequest(Request.Method.GET, url,
            { response ->
                val jsonObject = JSONObject(response)
                val name = jsonObject.optJSONObject("species")?.optString("name")
                val type = jsonObject.optJSONArray("types")?.getJSONObject(0)?.optJSONObject("type")?.optString("name")
                //val img = jsonObject.optJSONObject("sprites")?.optJSONObject("other")?.optJSONObject("dream_world")?.optString("front_default")
                val img = jsonObject.optJSONObject("sprites")?.optString("front_default")
                val stat = jsonObject.optJSONArray("stats")
                val hp = stat?.getJSONObject(0)?.optInt("base_stat")
                val attack = stat?.getJSONObject(1)?.optInt("base_stat")
                val defense = stat?.getJSONObject(2)?.optInt("base_stat")
                val speed = stat?.getJSONObject(5)?.optInt("base_stat")
                val date = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    Calendar.getInstance().time
                } else TODO("VERSION.SDK_INT < N")

                val pokemon = Pokemon(name!!, type!!, img!!, hp!!, attack!!, defense!!, speed!!, date, null, SingleLoggedUser.user!!.username)

                if(toShow) show(pokemon, NEW_POKEMON) else recreate(pokemon, NEW_POKEMON)
            },
            {
                val msg = "Error: ${R.string.not_founded}:\n\n${it.message}"
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
                Log.e(">>>>>>>>>>>>", msg)
            })

        // Add the request to the RequestQueue.
        queue.add(stringRequest)
    }

    private fun recreate(pokemon: Pokemon, type:Int) {

        val imgRequest = ImageRequest(pokemon.imgUrl, {
                pokemon.imgBitmap = it
                if(type== NEW_POKEMON) putPokemon(pokemon)
            }, 0,0, ImageView.ScaleType.CENTER, Bitmap.Config.ARGB_8888, {
                val msg = "Img not founded. Error:\n\n${it.message}"
                Toast.makeText(this,msg, Toast.LENGTH_LONG).show()
                Log.e(">>>>>>>>>>>>", msg)
            }
        )
        queue.add(imgRequest)
    }

    private fun putPokemon(pokemon: Pokemon) {
        val url = "${Constants.BASE_URL}/pokemon/${SingleLoggedUser.user?.username}/${pokemon.name}.json"
        val jsonObj = JSONObject(Gson().toJson(pokemon))
        val objRequest = JsonObjectRequest(Request.Method.PUT, url, jsonObj,{
            adapter.addPokemon(pokemon)
        },{
            val msg = "unshipped pokemon. Error:\n\n${it.message}"
            Toast.makeText(this,msg, Toast.LENGTH_LONG).show()
            Log.e(">>>>>>>>>>>>", msg)
        })
        queue.add(objRequest)
    }

    private fun removePokemon(pokemon: Pokemon){
        val url = "${Constants.BASE_URL}/pokemon/${SingleLoggedUser.user?.username}/${pokemon.name}.json"
        val deleteRequest = StringRequest(Request.Method.DELETE, url,{
            adapter.delete(pokemon)
            Toast.makeText(this,"Pokemon was send successfully", Toast.LENGTH_LONG).show()
        },{
            val msg = "unshipped pokemon. Error:\n\n${it.message}"
            Toast.makeText(this,msg, Toast.LENGTH_LONG).show()
            Log.e(">>>>>>>>>>>>", msg)
        })
        queue.add(deleteRequest)
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

    private fun onResultShowPokemon(result: ActivityResult) {
        val type = result.data?.extras?.getInt("type", ERROR)
        val pokemon = Gson().fromJson(result.data?.extras?.getString("pokemon","NO_DATA"), Pokemon::class.java)
        when(type){
            NEW_POKEMON-> {
                Log.e("enter", "enter")
                recreate(pokemon, type)
            }
            OLD_POKEMON-> removePokemon(pokemon)
            ERROR -> Toast.makeText(this,R.string.not_founded, Toast.LENGTH_SHORT).show()
        }
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
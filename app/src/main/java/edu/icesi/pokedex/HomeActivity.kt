package edu.icesi.pokedex

import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
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
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
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
        const val CANCEL = 0
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

    //Database
    private var db = Firebase.firestore

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
            binding.searchTxt.setText("")
        }

        binding.homeCatchBtn.setOnClickListener{
            requestNewPokemon(binding.searchTxt.text.toString().trim().lowercase(), false)
            binding.searchTxt.setText("")
        }

        binding.searchBtn.setOnClickListener{
            requestOldPokemon(binding.pokemonTxt.text.toString().trim().lowercase())
            binding.pokemonTxt.setText("")
        }
    }

    private fun loadPokemons(){

        db.collection("users").document(SingleLoggedUser.user?.username!!)
            .collection("pokemons").orderBy("date").get()
                .addOnSuccessListener { pokemons ->
                    for(document in pokemons){
                            val pokemon = document.toObject(Pokemon::class.java)
                            loadImageAndSave(pokemon)
                        }
                }.addOnFailureListener {
                val msg = "${R.string.pokemons_error}:\n${it.message}"
                showError(msg)
                Log.e(">>>>>>>>>>>>", msg)
            }
    }

    private fun loadImageAndSave(pokemon: Pokemon){
        val imgRequest = ImageRequest( pokemon.imgUrl,{ bitmap->
            adapter.addPokemon(pokemon, bitmap)
        },0,0, ImageView.ScaleType.CENTER, Bitmap.Config.ARGB_8888, {
            val msg = "${R.string.img_fetch_error}:\n\n${it.message}"
            Log.e(">>>>>>>>>>>>", msg)
        })
        queue.add(imgRequest)
    }

    private fun requestOldPokemon(pokemonName:String){

        db.collection("users").document(SingleLoggedUser.user!!.username)
            .collection("pokemons").whereEqualTo("name",pokemonName).get().addOnSuccessListener {
                for(document in it){
                    val pokemon = document.toObject(Pokemon::class.java)
                    show(pokemon, OLD_POKEMON)
                    break
                }
            }.addOnFailureListener {
                val msg = "${R.string.not_founded}:\n\n${it.message}"
                showAlert(msg)
                Log.e(">>>>>>>>>>>>", msg)
            }
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

                val pokemon = Pokemon(name!!, type!!, img!!, hp!!, attack!!, defense!!, speed!!, SingleLoggedUser.user!!.username)

                if(toShow) show(pokemon, NEW_POKEMON) else putPokemon(pokemon)
            },
            {
                val msg = "${R.string.not_founded}:\n\n${it.message}"
                showAlert(msg)
                Log.e(">>>>>>>>>>>>", msg)
            })

        // Add the request to the RequestQueue.
        queue.add(stringRequest)
    }

    private fun putPokemon(pokemon: Pokemon) {

        db.collection("users").document(SingleLoggedUser.user?.username!!)
            .collection("pokemons").document(pokemon.id).set(pokemon).addOnSuccessListener {
                loadImageAndSave(pokemon)
            }.addOnFailureListener {
                val msg = "${R.string.upload_error}\n${it.message}"
                showAlert(msg)
                Log.e(">>>>>>>>>>>>", msg)
            }
    }

    private fun removePokemon(pokemon: Pokemon){

        db.collection("users").document(SingleLoggedUser.user?.username!!)
            .collection("pokemons").document(pokemon.id).delete().addOnSuccessListener {
                adapter.delete(pokemon)
                Toast.makeText(this,"Pokemon was sent successfully", Toast.LENGTH_LONG).show()
            }.addOnFailureListener {
                val msg = "${R.string.delete_error}:\n\n${it.message}"
                showAlert(msg)
                Log.e(">>>>>>>>>>>>", msg)
            }
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
            NEW_POKEMON-> putPokemon(pokemon)
            OLD_POKEMON-> removePokemon(pokemon)
            CANCEL-> Toast.makeText(this,"Home", Toast.LENGTH_SHORT).show()
            ERROR -> Toast.makeText(this,R.string.not_founded, Toast.LENGTH_SHORT).show()
        }
    }

    private fun showAlert(msg:String){
        val alert = AlertDialog.Builder(this)
        alert.setMessage(msg)
        alert.create()
        alert.apply {
            setNeutralButton("Ok"){dialog,_->dialog.dismiss()}
        }
        alert.show()
    }

    private fun showError(msg:String){
        val alert = AlertDialog.Builder(this)
        alert.setMessage("Internal error:" +
                "\n${msg}\n\n" +
                "Contact with Benjamin S")
        alert.create()
        alert.apply {
            setNeutralButton("Ok"){ _: DialogInterface, _: Int -> finishAndRemoveTask()}
        }
        alert.show()
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
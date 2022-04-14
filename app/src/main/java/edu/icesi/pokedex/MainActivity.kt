package edu.icesi.pokedex

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import edu.icesi.pokedex.databinding.ActivityMainBinding
import edu.icesi.pokedex.model.SingleLoggedUser
import edu.icesi.pokedex.model.User
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    //Binding
    private var _binding: ActivityMainBinding? = null
    private val binding get() = _binding!!

    //RequestQueue of Volley
    private val queue = Volley.newRequestQueue(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Binding
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.loginBtn.setOnClickListener{
            val url = "${Constants.BASE_URL}/users/${binding.username.text}"
            val stringRequest = StringRequest(Request.Method.GET, url, ::onResultUserRequest, ::onErrorRequest)
            queue.add(stringRequest)
        }
    }

    private fun onResultUserRequest(response: String?){
        val user = Gson().fromJson(response, User::class.java)
        log(user)
    }

    private fun onErrorRequest(volleyError: VolleyError?){
        val url = "${Constants.BASE_URL}/users/${binding.username.text}"
        if(volleyError?.networkResponse?.statusCode==404){
            val user = User(binding.username.text.toString())
            val jsonObj = JSONObject(Gson().toJson(user))
            val objectRequest = JsonObjectRequest(Request.Method.PUT, url, jsonObj, {
                log(user)
            }
            ) {
                val msg = "Error: ${R.string.not_founded}:\n\n${it.message}"
                Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
            }
            queue.add(objectRequest)
        }
    }

    private fun log(user:User){
        SingleLoggedUser.user = user
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
    }
}
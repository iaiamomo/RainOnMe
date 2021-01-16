package com.example.rainonme

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.util.JsonReader
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.firebase.auth.FirebaseAuth
import org.json.JSONArray
import org.json.JSONObject
import org.w3c.dom.Text

class ProfileActivity : AppCompatActivity(){

    private lateinit var auth : FirebaseAuth
    private lateinit var queue : RequestQueue
    private val url = Conf.url
    private var reply : JSONObject? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        setSupportActionBar(findViewById(R.id.toolbarProfile))
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        queue = Volley.newRequestQueue(this)
        auth = FirebaseAuth.getInstance()

        val user = auth.currentUser
        val email = user?.email

        val nameVal = findViewById<TextView>(R.id.textViewName)
        val emailVal = findViewById<TextView>(R.id.editTextTextEmail)
        val passwordVal = findViewById<TextView>(R.id.editTextPassword)

        nameVal.text = Conf.nameSurname
        emailVal.text = email

        findViewById<Button>(R.id.buttonModify).setOnClickListener {
            if(emailVal.text.isEmpty() || !("@" in emailVal.text.toString())){
                emailVal.setError("Insert a valid Email")
            }else if(passwordVal.text.isEmpty() || passwordVal.text.length < 5){
                passwordVal.setError("Password must be at least 5 characters long")
            }else{
                val newEmail = emailVal.text.toString()
                val newPassword = passwordVal.text.toString()

                if(email != newEmail){
                    user!!.updateEmail(newEmail)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful){
                                    Log.i("infoapp", "Email updated")}}}

                if(newPassword.length > 5){
                    user!!.updatePassword(newPassword)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Log.i("infoapp", "Password updated")}}
                }else{
                    passwordVal.setError("Password must be at least 5 characters long")}
            }
        }

        queue = Volley.newRequestQueue(this)
        var games: JSONArray

        val url_req = url+"req_type="+0+"&who="+Conf.userUID
        val stringRequest = StringRequest(Request.Method.GET, url_req, { response ->
            reply = JSONObject(response.toString())
            games = reply!!["games"] as JSONArray
            Log.i("infoapp", games.toString())

            val itemDecoration = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
            val listVal = findViewById<RecyclerView>(R.id.listGames)
            listVal.addItemDecoration(itemDecoration)
            listVal.layoutManager = LinearLayoutManager(this)
            listVal.adapter = GamesAdapter(this, games)

        }, { error: VolleyError? -> Log.i("info", "Errore games " + error) })
        queue.add(stringRequest)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.app_bar_play_weather, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.home -> {
                Conf.gameID = ""
                Conf.gameOver = false
                finish()
                return true}
            else -> {
                return super.onOptionsItemSelected(item)}
        }
    }
}
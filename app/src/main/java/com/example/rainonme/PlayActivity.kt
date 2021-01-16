package com.example.rainonme

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONObject

class PlayActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play)
        setSupportActionBar(findViewById(R.id.toolbarPlay))
        Log.i("infoapp", "onCreate PLAY")
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
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
                Conf.polli = false
                finish()
                return true}
            else -> {
                return super.onOptionsItemSelected(item)}
        }
    }

    override fun onBackPressed() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Really go Home?")
        builder.setMessage("You are in game session. Do you really want to go home?")
        builder.setNegativeButton(R.string.no, null)
        builder.setPositiveButton(R.string.yes) { _,_->
            Conf.gameOver = false
            Conf.gameID = ""
            Conf.polli = false
            finish()}
        builder.show()
    }

    override fun onPause() {
        super.onPause()
        Log.i("infoapp", "onPause PLAY")
        if(Conf.gameID == ""){
            Log.i("infoapp", "play offline")
            Conf.alone = false
            Conf.gameOver = false
            Conf.polli = false
            finish()
        }else if(Conf.gameID != "" && Conf.shareCode){
            Log.i("infoapp", "sharecode")
        }else if(Conf.gameID != "" && !Conf.gameOver){
            val url_req = Conf.url+"game_id="+Conf.gameID+"&who="+Conf.userUID
            val stringRequest = StringRequest(Request.Method.DELETE, url_req, { _ ->
                Log.i("infoapp", "delete game from user")
                Conf.gameID = ""
                Conf.gameOver = false
                Conf.polli = false
                finish()
            }, { error: VolleyError? -> Log.i("info", "Errore removing user " + error)})
            Volley.newRequestQueue(this).add(stringRequest)
        }else{
            Log.i("infoapp", "in game over, so leaderboard shown")
            Conf.gameID = ""
            Conf.gameOver = false
            Conf.polli = false
            finish()
        }
    }
}
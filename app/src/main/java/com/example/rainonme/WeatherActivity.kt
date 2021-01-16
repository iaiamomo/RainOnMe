package com.example.rainonme

import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class WeatherActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weather)
        setSupportActionBar(findViewById(R.id.toolbarWeather))
        Log.i("infoapp", "onCreate WEATHER")
        Log.i("infoapp", "user "+ Conf.userUID)
        Log.i("infoapp", "display name "+ Conf.nameSurname)
        Log.i("infoapp", "game "+ Conf.gameID)
        Log.i("infoapp", "gameover "+ Conf.gameOver.toString())
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.app_bar_play_weather, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.home -> {
                Log.i("infoapp", "going back home")
                finish()
                return true}
            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        Log.i("infoapp", "onPause WEATHER")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i("infoapp", "onDestroy WEATHER")
    }

    override fun onResume() {
        super.onResume()
        Log.i("infoapp", "onResume WEATHER")
    }

}
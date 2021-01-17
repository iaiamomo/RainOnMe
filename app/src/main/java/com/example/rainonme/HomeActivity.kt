package com.example.rainonme

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        setSupportActionBar(findViewById(R.id.toolbar))

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        Log.i("infoapp", "onCreate HOME")
        Log.i("infoapp", "user "+ Conf.userUID)
        Log.i("infoapp", "display name "+ Conf.nameSurname)
        Log.i("infoapp", "game "+ Conf.gameID)
        Log.i("infoapp", "gameover "+ Conf.gameOver.toString())

        findViewById<Button>(R.id.buttonPlay).setOnClickListener {
            Conf.shareCode = true
            val i = Intent(this, PlayActivity::class.java)
            startActivity(i)
        }

        findViewById<Button>(R.id.buttonWeather).setOnClickListener {
            val i = Intent(this, WeatherActivity::class.java)
            startActivity(i)
        }

        findViewById<TextView>(R.id.textViewHi).text = "Hi "+Conf.nameSurname

        findViewById<Button>(R.id.buttonHowToPlay).setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Instructions")
            builder.setMessage(R.string.howto)
            builder.setPositiveButton("OK") {_,_->}
            builder.show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.app_bar, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.logout -> {
                Log.i("infoapp", "logout going to finish")
                Conf.userUID = ""
                Conf.gameID = ""
                Conf.gameOver = false
                Conf.nameSurname = ""
                finish()
                return true}
            R.id.profile -> {
                Log.i("infoapp", "profile")
                val i = Intent(this, ProfileActivity::class.java)
                startActivity(i)
                return true}
            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
    }

    override fun onBackPressed() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(R.string.backpressed)
        builder.setNegativeButton(R.string.no, null)
        builder.setPositiveButton(R.string.yes) {dialog, which ->
            Log.i("infoapp", "onbackpressed going to finish")
            Conf.userUID = ""
            Conf.gameID = ""
            Conf.gameOver = false
            Conf.nameSurname = ""
            finish()
        }
        builder.show()
    }

    override fun onPause() {
        super.onPause()
        Log.i("infoapp", "onPause HOME")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i("infoapp", "onDestroy HOME")
    }

    override fun onResume() {
        super.onResume()
        Log.i("infoapp", "onResume HOME")
    }

}
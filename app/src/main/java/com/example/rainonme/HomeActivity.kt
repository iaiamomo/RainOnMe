package com.example.rainonme

import android.app.Dialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        setSupportActionBar(findViewById(R.id.toolbar))

        Log.i("infoapp", "user "+ Conf.userUID)
        Log.i("infoapp", "game "+ Conf.gameID)
        Log.i("infoapp", "gameover "+ Conf.gameOver.toString())

        findViewById<Button>(R.id.buttonPlay).setOnClickListener {
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
                Toast.makeText(this, "logout", Toast.LENGTH_SHORT)
                Conf.userUID = ""
                Conf.gameID = ""
                Conf.gameOver = false
                Conf.nameSurname = ""

                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)

                return true}
            R.id.profile -> {
                Log.i("infoapp", "profile")
                Toast.makeText(this, "profile", Toast.LENGTH_SHORT)

                val intent = Intent(this, ProfileActivity::class.java)
                startActivity(intent)

                return true}
            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
    }

    override fun onBackPressed() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Really Exit?")
        builder.setMessage("Do you really want to exit?")
        builder.setNegativeButton(R.string.no, null)
        builder.setPositiveButton(R.string.yes) {dialog, which ->
            val a = Intent(Intent.ACTION_MAIN)
                    .addCategory(Intent.CATEGORY_HOME)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(a)
        }
        builder.show()
    }
}
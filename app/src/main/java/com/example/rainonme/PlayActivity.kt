package com.example.rainonme

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class PlayActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play)
        setSupportActionBar(findViewById(R.id.toolbarPlay))
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.app_bar_play_weather, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.home -> {
                Toast.makeText(this, "go back home", Toast.LENGTH_SHORT)
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

}
package com.example.rainonme

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest

class MainActivity : AppCompatActivity() {

    private lateinit var auth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Log.i("infoapp", "onCreate MAIN")

        auth = FirebaseAuth.getInstance()

        findViewById<Button>(R.id.buttonSignIn).setOnClickListener {
            createAccount()
        }

        findViewById<Button>(R.id.buttonLogIn).setOnClickListener {
            signIn()
        }
    }

    private fun createAccount(){
        val emailVal = findViewById<TextView>(R.id.editTextTextEmailAddress)
        val passwordVal = findViewById<TextView>(R.id.editTextTextPassword)
        if(emailVal.text.isEmpty() || !("@" in emailVal.text.toString())){
            emailVal.setError("Insert a valid Email")
        }else if(passwordVal.text.isEmpty() || passwordVal.text.length < 5){
            passwordVal.setError("Password must be at least 5 characters long")
        }else{
            val email = emailVal.text.toString()
            val password = passwordVal.text.toString()

            showNameSurname()

            findViewById<Button>(R.id.buttonsign).setOnClickListener {
                updateAccount(email, password)
            }
        }

    }

    private fun updateAccount(email: String, password: String){
        val nameVal = findViewById<TextView>(R.id.editTextTextName)
        val surnameVal = findViewById<TextView>(R.id.editTextTextSurname)

        if(nameVal.text.isEmpty()){
            nameVal.setError("Insert a valid Name")
        }else if(surnameVal.text.isEmpty()){
            surnameVal.setError("Insert a valid Surname")
        }else{
            val name = nameVal.text.toString()
            val surname = surnameVal.text.toString()
            val displayName = name+" "+surname

            Conf.nameSurname = displayName

            showProgressBar()
            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this) {task ->
                if(task.isSuccessful){
                    val user = auth.currentUser
                    Conf.userUID = user!!.uid

                    val updRequest = UserProfileChangeRequest.Builder().setDisplayName(displayName).build()
                    user.updateProfile(updRequest).addOnCompleteListener(this) { task ->
                        if(task.isSuccessful){
                            Toast.makeText(baseContext, "Authentication OK "+displayName, Toast.LENGTH_SHORT).show()
                            changeActivity()
                        }else{
                            Toast.makeText(baseContext, "Authentication FAILED", Toast.LENGTH_SHORT).show()
                        }
                    }
                }else{
                    hideProgressBar()
                    Toast.makeText(baseContext, "Authentication FAILED", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun signIn(){
        val emailVal = findViewById<TextView>(R.id.editTextTextEmailAddress)
        val passwordVal = findViewById<TextView>(R.id.editTextTextPassword)

        if(emailVal.text.isEmpty() || !("@" in emailVal.text.toString())){
            emailVal.setError("Insert a valid Email")
        }else if(passwordVal.text.isEmpty() || passwordVal.text.length < 5){
            passwordVal.setError("Password must be at least 5 characters long")
        }else{
            val email = emailVal.text.toString()
            val password = passwordVal.text.toString()

            showProgressBar()
            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
                if(task.isSuccessful){
                    val user = auth.currentUser
                    Conf.userUID = user!!.uid
                    Conf.nameSurname = user!!.displayName.toString()
                    Toast.makeText(baseContext, "Authentication OK", Toast.LENGTH_SHORT).show()
                    changeActivity()
                } else {
                    hideProgressBar()
                    Toast.makeText(baseContext, "Authentication FAILED", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showProgressBar(){
        findViewById<ProgressBar>(R.id.progressBar).visibility = View.VISIBLE
    }

    private fun hideProgressBar(){
        findViewById<ProgressBar>(R.id.progressBar).visibility = View.INVISIBLE
    }

    private fun showNameSurname(){
        findViewById<LinearLayout>(R.id.name_surname).visibility = View.VISIBLE
        findViewById<LinearLayout>(R.id.email_password_buttons).visibility = View.GONE
    }

    private fun changeActivity(){
        hideProgressBar()

        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        Log.i("infoapp", "onResume MAIN")
    }
}
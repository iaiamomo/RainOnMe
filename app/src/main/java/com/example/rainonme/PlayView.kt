package com.example.rainonme

import android.content.Context
import android.graphics.*
import android.hardware.*
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.fragment.app.findFragment
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.android.volley.Request
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class PlayView(context: Context?, navigator: NavController) : View(context), SensorEventListener2 {

    private var mLastAccelerometer = FloatArray(3)

    private var xAcc = 0f
    private var CM = Matrix()

    private lateinit var man : Bitmap

    private val numThunders = 5
    private lateinit var thunders : Array<Bitmap>

    private lateinit var umbrella : Array<Bitmap>
    private lateinit var cross : Array<Bitmap>

    private var converter = false

    private var manWidth = 0f
    private var manHeight = 0f
    private var thunderWidth = 0f
    private var thunderHeight = 0f
    private var lifeWidth = 0f
    private var lifeHeight = 0f

    private var xMan = 0f
    private var yMan = 0f

    private lateinit var xThunder : Array<Float>
    private lateinit var yThunder : Array<Float>

    private lateinit var xLifes : Array<Float>

    var current = System.currentTimeMillis()
    var vx = 200f
    var vy = 200f

    var lifes = 3
    var maxLifes = 3
    var currScore = 0L         //time in seconds

    var initialTime = 0L
    var finalTime = 0L

    var navigator = navigator

    init {
        val sensorManager = context?.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorManager.registerListener(
            this,
            sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            SensorManager.SENSOR_DELAY_NORMAL
        )
        initialTime = System.currentTimeMillis()
        Log.i("infoapp", "initial time "+initialTime.toString())
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if(!converter){
            converter = true

            //MAN
            manWidth = width/6f
            manHeight = height/6f

            xMan = width/2f - (manWidth/2f)
            yMan = height - manHeight

            man = ResourcesCompat.getDrawable(resources, R.drawable.ic_man, null)?.toBitmap(
                    manWidth.toInt(), manHeight.toInt())!!

            //THUNDERS
            thunderWidth = width/9f
            thunderHeight = height/11f

            thunders = Array<Bitmap>(numThunders) {
                ResourcesCompat.getDrawable(resources, R.drawable.ic_thunder, null)?.toBitmap(
                        thunderWidth.toInt(), thunderHeight.toInt())!!
            }

            xThunder = Array<Float>(numThunders) { i ->
                width / numThunders.toFloat() * i.toFloat()
            }

            yThunder = Array<Float>(numThunders) { i ->
                0f - (height / numThunders.toFloat() * i.toFloat())
            }

            //LIFES
            lifeWidth = width/10f
            lifeHeight = height/10f

            umbrella = Array<Bitmap>(maxLifes) {
                ResourcesCompat.getDrawable(resources, R.drawable.ic_umbrella, null)?.toBitmap(
                        lifeWidth.toInt(), lifeHeight.toInt())!!
            }

            cross = Array<Bitmap>(maxLifes) {
                ResourcesCompat.getDrawable(resources, R.drawable.ic_x, null)?.toBitmap(
                        lifeWidth.toInt(), lifeHeight.toInt())!!
            }

            xLifes = Array<Float>(maxLifes) { i ->
                0f + lifeWidth * i.toFloat()
            }
        }

        val now = System.currentTimeMillis()
        val dt = now - current
        current = now

        if(xAcc > 0f){
            if(xMan > 0) {
                xMan -= vx * dt / 1000
            }
        }else if(xAcc < 0f){
            if(xMan < width-manWidth) {
                xMan += vx * dt / 1000
            }
        }else{
            xMan = xMan
        }

        CM.setTranslate(xMan, yMan)
        canvas.drawBitmap(man, CM, null)

        for(i in 0..numThunders-1){
            var TM = Matrix()
            if(yThunder[i] > height)
                yThunder[i] = 0f
            else
                yThunder[i] = yThunder[i]+vy*dt/1000
            TM.setTranslate(xThunder[i], yThunder[i])
            canvas.drawBitmap(thunders[i], TM, null)
        }

        for(i in 0..maxLifes-1){
            canvas.drawBitmap(umbrella[i], xLifes[i], 0f, null)
        }

        for(i in 0..(maxLifes-lifes-1)){
            canvas.drawBitmap(cross[i], xLifes[i], 0f, null)
        }

        check_collision()

        if(!Conf.gameOver){
            invalidate()
        }
    }

    fun check_collision(){
        if(lifes!=0){
            for(i in 0..numThunders-1){
                if(xThunder[i] > (xMan-manWidth/2) &&
                        xThunder[i] < (xMan+manWidth/2) &&
                        yThunder[i] > height-manHeight-thunderHeight){
                    lifes-=1
                    yThunder[i] = 0f
                }
            }
        }
        if(lifes==0){
            finalTime = System.currentTimeMillis()
            Log.i("infoapp", "finaltime "+finalTime.toString())
            currScore = (finalTime - initialTime)/2
            Log.i("infoapp", "current score "+currScore.toString())
            Log.i("infoapp", "YOU LOSE")
            Toast.makeText(context, "YOU LOSE", Toast.LENGTH_SHORT).show()
            Conf.gameOver = true
            Conf.score = currScore.toInt()
            updateScore(currScore)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if(event?.sensor?.type == Sensor.TYPE_ACCELEROMETER){
            mLastAccelerometer = event?.values.clone()
        }
        xAcc = mLastAccelerometer[0]
        if(!Conf.gameOver){
            invalidate()
        }
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        Log.i("info", "Not yet implemented")
    }

    override fun onFlushCompleted(p0: Sensor?) {
        Log.i("info", "Not yet implemented")
    }

    private fun updateScore(curr_score : Long){
        if(Conf.alone){
            navigator.navigate(R.id.action_play_to_gameOver)
        }else{
            val url_req = Conf.url+"req_type=1&who="+Conf.userUID+"&game_id="+Conf.gameID+"&score="+curr_score
            Log.i("infoapp", "update score: "+url_req)
            val queue = Volley.newRequestQueue(context)
            val stringRequest = StringRequest(Request.Method.PUT, url_req,{_ ->
                    Toast.makeText(this.context, "Updated user's code", Toast.LENGTH_SHORT).show()
                    navigator.navigate(R.id.action_play_to_gameOver)}
                ,{_ -> Log.i("info", "Errore updateScore")})
            queue.add(stringRequest)
        }
    }

}
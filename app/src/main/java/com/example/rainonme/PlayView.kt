package com.example.rainonme

import android.content.Context
import android.graphics.*
import android.hardware.*
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.navigation.NavController
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley

class PlayView(context: Context?, navigator: NavController) : View(context), SensorEventListener2 {

    private var mLastAccelerometer = FloatArray(3)

    private var xAcc = 0f
    private var CM = Matrix()

    private lateinit var man : Bitmap

    private var rndThunders : Array<Int>
    private val maxThunders = 5
    private var numThunders = 0
    private lateinit var thunders : Array<Bitmap>
    private var showThunders = MutableList<Any>(0, {})
    private lateinit var matrixThunder : Array<Matrix>

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
        //setBackgroundColor(Color.CYAN)
        setBackgroundResource(R.drawable.ic_background)
        val sensorManager = context?.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorManager.registerListener(
                this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL
        )
        initialTime = System.currentTimeMillis()
        Log.i("infoapp", "initial time "+initialTime.toString())

        val list = List(maxThunders, {i->i}).shuffled()
        rndThunders = Array<Int>(maxThunders, {i->list[i]})
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
            thunderWidth = width/8f
            thunderHeight = height/10f

            thunders = Array<Bitmap>(maxThunders) {
                ResourcesCompat.getDrawable(resources, R.drawable.ic_thunder, null)?.toBitmap(
                        thunderWidth.toInt(), thunderHeight.toInt())!!
            }

            matrixThunder = Array<Matrix>(maxThunders) {
                _ -> Matrix()
            }

            xThunder = Array<Float>(maxThunders) { i ->
                width / maxThunders.toFloat() * rndThunders[i].toFloat()
            }

            yThunder = Array<Float>(maxThunders) { i ->
                //0f - (height / maxThunders.toFloat() * rndThunders[i].toFloat())
                0f - (thunderHeight * rndThunders[i])
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

        if(lifes > 0) {
            if (numThunders == maxThunders) {
                val list = List(maxThunders, { i -> i }).shuffled()
                rndThunders = Array<Int>(maxThunders, { i -> list[i] })
            }

            val now = System.currentTimeMillis()
            val dt = now - current
            current = now

            if (xAcc > 0f) {
                if (xMan > 0) {
                    xMan -= vx * dt / 1000
                }
            } else if (xAcc < 0f) {
                if (xMan < width - manWidth) {
                    xMan += vx * dt / 1000
                }
            } else {
                xMan = xMan
            }

            CM.setTranslate(xMan, yMan)
            canvas.drawBitmap(man, CM, null)

            if (numThunders == 0)
                loadThunder(canvas)
            positionThunder(canvas, dt)

            for (i in 0..maxLifes - 1) {
                canvas.drawBitmap(umbrella[i], xLifes[i], 0f, null)
            }

            for (i in 0..(maxLifes - lifes - 1)) {
                canvas.drawBitmap(cross[i], xLifes[i], 0f, null)
            }
        }

        check_collision(canvas)

        if(!Conf.gameOver){
            invalidate()
        }
    }

    fun check_collision(canvas: Canvas){
        if(lifes!=0){
            for(i in 0..maxThunders-1){
                if(xThunder[i] > (xMan-manWidth/2) &&
                        xThunder[i] < (xMan+manWidth/2) &&
                        yThunder[i] > height-manHeight-thunderHeight){
                    lifes-=1
                    yThunder[i] = 0f - (thunderHeight * rndThunders[i])
                    xThunder[i] = width / maxThunders.toFloat() * rndThunders[i].toFloat()
                    if(numThunders < maxThunders){
                        showThunders.add(numThunders, thunders[numThunders])
                        matrixThunder[numThunders].setTranslate(xThunder[numThunders], yThunder[numThunders])
                        canvas.drawBitmap(showThunders[numThunders] as Bitmap, matrixThunder[numThunders], null)
                        numThunders += 1
                    }
                }
            }
        }
        if(lifes==0){
            finalTime = System.currentTimeMillis()
            Log.i("infoapp", "finaltime "+finalTime.toString())
            currScore = (finalTime - initialTime)/2
            Log.i("infoapp", "current score "+currScore.toString())
            Log.i("infoapp", "YOU LOSE")
            Conf.gameOver = true
            Conf.score = currScore.toInt()
            updateScore(currScore)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if(event?.sensor?.type == Sensor.TYPE_ACCELEROMETER){
            mLastAccelerometer = event.values.clone()
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
                navigator.navigate(R.id.action_play_to_gameOver)}
                    ,{_ -> Log.i("info", "Errore updateScore")})
            queue.add(stringRequest)
        }
    }

    private fun positionThunder(canvas: Canvas, dt: Long){
        for(i in 0..numThunders-1){
            if(yThunder[i] > height){
                yThunder[i] = 0f
                xThunder[i] = width / maxThunders.toFloat() * rndThunders[i].toFloat()
                if(numThunders < maxThunders){
                    showThunders.add(numThunders, thunders[numThunders])
                    matrixThunder[numThunders].setTranslate(xThunder[numThunders], yThunder[numThunders])
                    canvas.drawBitmap(showThunders[numThunders] as Bitmap, matrixThunder[numThunders], null)
                    numThunders += 1
                }
            }else
                yThunder[i] = yThunder[i]+vy*dt/1000
            matrixThunder[i].setTranslate(xThunder[i], yThunder[i])
            canvas.drawBitmap(showThunders[i] as Bitmap, matrixThunder[i], null)
        }
    }

    private fun loadThunder(canvas: Canvas){
        showThunders.add(numThunders, thunders[numThunders])
        matrixThunder[numThunders].setTranslate(xThunder[numThunders], yThunder[numThunders])
        canvas.drawBitmap(showThunders[numThunders] as Bitmap, matrixThunder[numThunders], null)
        numThunders += 1
    }

}
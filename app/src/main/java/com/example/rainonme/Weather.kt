package com.example.rainonme

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.squareup.picasso.Picasso
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class Weather : Fragment() {

    private lateinit var queue : RequestQueue
    private val resArray = ArrayList<String>()

    private var location = ""

    private val weatherApiKey = "75e0aae47c5ae6bcdac0746cb31293c7"  //OpenWeather One Call
    private val locApiKey = "4ffec860b966ab60a9de3875eae90fab"      //positionstack

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_weather, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        queue = Volley.newRequestQueue(context)

        view.findViewById<Button>(R.id.buttonGo).setOnClickListener {
            val locationVal = view.findViewById<EditText>(R.id.editTextCity)
            if(locationVal.text.isEmpty() || !("," in locationVal.text.toString())){
                locationVal.setError("Insert a valid location")
            }else{
                location = locationVal.text.toString()
                Log.i("infoapp", location)
                if(" " in location){
                    location = location.replace(" ", "%")
                    Log.i("infoapp", location)
                }
                getLatitudeLongitude(view)
            }
        }
    }

    private fun getLatitudeLongitude(view: View){
        val locUrl = "http://api.positionstack.com/v1/forward?access_key=$locApiKey&query=$location"
        Log.i("infoapp", locUrl)

        val stringRequest = StringRequest(Request.Method.GET, locUrl, { response ->
            val reply = JSONObject(response.toString())
            val data = reply.getJSONArray("data").getJSONObject(0)
            val lat = data.getString("latitude")
            val lon = data.getString("longitude")
            val label = data.getString("label")
            Log.i("infoapp", "lat "+lat+" lon "+lon+" label"+label)
            resArray.add(lat)
            resArray.add(lon)
            resArray.add(label)
            getWeather(view) }
                , {_-> Log.i("infoapp", "Error get latitude longitude")})

        queue.add(stringRequest)
    }

    private fun getWeather(view: View){
        val lat = resArray[0]
        val lon = resArray[1]
        val label = resArray[2]
        val exclude = "minutely,hourly"
        val units = "metric"
        val weatherUrl = "https://api.openweathermap.org/data/2.5/onecall?lat=$lat&lon=$lon&exclude=$exclude&units=$units&appid=$weatherApiKey"
        Log.i("infoapp", weatherUrl)

        val stringRequest = StringRequest(Request.Method.GET, weatherUrl, {response ->
            val reply = JSONObject(response.toString())
            val daily = reply.getJSONArray("daily")

            val current = reply.getJSONObject("current")
            val updatedAt = current.getLong("dt")
            val updatedAtText = "Updated at: "+ SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH).format(Date(updatedAt*1000))
            val temp = current.getString("temp")+"°C"
            val tempMin = daily.getJSONObject(0).getJSONObject("temp").getString("min")+"°C"
            val tempMax = daily.getJSONObject(0).getJSONObject("temp").getString("max")+"°C"
            val weather = current.getJSONArray("weather").getJSONObject(0).getString("main")
            val weatherIcon = current.getJSONArray("weather").getJSONObject(0).getString("icon")
            val iconUrl = "http://openweathermap.org/img/wn/$weatherIcon@2x.png"

            view.findViewById<TextView>(R.id.textViewToday).visibility = View.VISIBLE
            view.findViewById<TextView>(R.id.location).text = label
            view.findViewById<TextView>(R.id.statusWeather).text = weather
            view.findViewById<TextView>(R.id.updated_at).text = updatedAtText
            view.findViewById<TextView>(R.id.temp).text = "Current temperature: "+temp
            view.findViewById<TextView>(R.id.temp_min).text = "Min: "+tempMin
            view.findViewById<TextView>(R.id.temp_max).text = "Max: "+tempMax
            Picasso.get().load(iconUrl).into(view.findViewById<ImageView>(R.id.icon))

            val data = JSONArray()
            for(i in 1..daily.length()-1)
                data.put(i-1, daily[i])

            view.findViewById<TextView>(R.id.textWeather).visibility = View.VISIBLE
            val itemDecoration = DividerItemDecoration(context, DividerItemDecoration.HORIZONTAL)
            val listVal = view.findViewById<RecyclerView>(R.id.weatherList)
            listVal.addItemDecoration(itemDecoration)
            listVal.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            listVal.adapter = WeatherAdapter(context, data)}
                ,{ _-> Log.i("infoapp", "error get weather")})

        queue.add(stringRequest)
    }
}
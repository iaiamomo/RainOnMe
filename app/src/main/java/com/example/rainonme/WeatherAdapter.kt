package com.example.rainonme

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.*

class WeatherAdapter(context: Context?, daily: JSONArray) : RecyclerView.Adapter<WeatherAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){}

    private var context: Context? = null
    private var daily: JSONArray

    init{
        this.context = context
        this.daily = daily
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.weather_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val current = daily.getJSONObject(position)
        val date = current.getLong("dt")
        val dateText = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH).format(Date(date*1000))
        val tempMin = current.getJSONObject("temp").getString("min")+"°C"
        val tempMax = current.getJSONObject("temp").getString("max")+"°C"
        val weather = current.getJSONArray("weather").getJSONObject(0).getString("main")
        val weatherIcon = current.getJSONArray("weather").getJSONObject(0).getString("icon")
        val iconUrl = "http://openweathermap.org/img/wn/$weatherIcon@2x.png"

        holder.itemView.findViewById<TextView>(R.id.statusDaily).text = weather
        holder.itemView.findViewById<TextView>(R.id.dateDaily).text = dateText
        holder.itemView.findViewById<TextView>(R.id.minDaily).text = "Min: " + tempMin
        holder.itemView.findViewById<TextView>(R.id.maxDaily).text = "Max: " + tempMax
        Picasso.get().load(iconUrl).into(holder.itemView.findViewById<ImageView>(R.id.iconDaily))
    }

    override fun getItemCount(): Int {
        return daily.length()
    }

}
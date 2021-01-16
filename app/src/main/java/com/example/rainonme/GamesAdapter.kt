package com.example.rainonme

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONObject

class GamesAdapter(context: Context?, games: JSONArray) : RecyclerView.Adapter<GamesAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){}

    private var context: Context? = null
    private  var games: JSONArray
    private var clipboardManager : ClipboardManager
    private val queue = Volley.newRequestQueue(context)
    private val url = Conf.url

    init {
        this.context = context
        this.games = games
        this.clipboardManager = context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.games_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val game_id =  games[position].toString()
        holder.itemView.findViewById<TextView>(R.id.textViewGame).text = game_id
        holder.itemView.findViewById<ImageButton>(R.id.imageButtonCopy).setOnClickListener(){
            val clip = ClipData.newPlainText("GAME ID", game_id)
            clipboardManager.setPrimaryClip(clip)
        }
        holder.itemView.findViewById<Button>(R.id.buttonRemove).setOnClickListener(){
            val url_req = url+"game_id="+game_id+"&who="+Conf.userUID
            val stringRequest = StringRequest(Request.Method.DELETE, url_req, { _ ->

                val url_req_update = url+"req_type="+0+"&who="+Conf.userUID
                val stringRequest = StringRequest(Request.Method.GET, url_req_update, { response ->
                            val reply = JSONObject(response.toString())
                            games = reply!!["games"] as JSONArray
                            notifyDataSetChanged()}
                        ,{ error: VolleyError? -> Log.i("info", "Errore removing user " + error)})
                queue.add(stringRequest)
            }, { error: VolleyError? -> Log.i("info", "Errore removing user " + error) })
            queue.add(stringRequest)
        }
    }

    override fun getItemCount(): Int {
        return games.length()
    }

}
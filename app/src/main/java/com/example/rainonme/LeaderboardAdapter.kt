package com.example.rainonme

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONObject

class LeaderboardAdapter(context: Context?, leaderboard: JSONArray) : RecyclerView.Adapter<LeaderboardAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){}

    private var context: Context? = null
    private var leaderboard: JSONArray? = null
    private val url = Conf.url
    private var queue : RequestQueue
    private var reply : JSONObject? = null

    init {
        this.context = context
        this.leaderboard = leaderboard
        this.queue = Volley.newRequestQueue(context)
        Conf.polli = true
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.leaderboard_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemView.findViewById<TextView>(R.id.textViewPosition).text = (position+1).toString()
        holder.itemView.findViewById<TextView>(R.id.textViewUser).text = JSONObject(leaderboard!![position].toString()).getString("name")
        holder.itemView.findViewById<TextView>(R.id.textViewScore).text = JSONObject(leaderboard!![position].toString()).getString("score")

        CoroutineScope(Dispatchers.IO).launch {
            pollFunction()
        }
    }

    override fun getItemCount(): Int {
        if(leaderboard == null) return 0
        return leaderboard!!.length()
    }

    suspend fun pollFunction(){
        delay(3000L)

        if(Conf.polli){
            var gameid = Conf.gameID
            var userid = Conf.userUID

            val url_req = url+"req_type="+1+"&game_id="+gameid+"&who="+userid
            Log.i("infoapp", "pollfunction: "+url_req)

            val stringRequest = StringRequest(Request.Method.GET, url_req, { response ->
                reply = JSONObject(response.toString())
                var leaderboardReply = reply!!["leaderboard"] as JSONObject
                leaderboard = getLeaderboard(leaderboardReply)
                Log.i("infoapp", "pollfunction "+leaderboard.toString())
                notifyDataSetChanged() }
                , { error: VolleyError? -> Log.i("info", "Errore leaderboard " + error) })

            queue.add(stringRequest)
        }

    }

    private fun getLeaderboard(reply: JSONObject) : JSONArray {
        val score  = mutableListOf<Int>()
        val players = mutableListOf<String>()

        val leader = JSONArray()

        var idx = 0

        val sortedKeys = mutableListOf<Int>()
        for(key in reply.keys()){ sortedKeys.add(key.toInt()) }
        sortedKeys.sort()

        for(key in sortedKeys){
            val names = reply.get(key.toString()) as JSONArray
            for(i in 0 until names.length()){
                val name = names.getString(i)
                score.add(key)
                players.add(name)

                val player = JSONObject()
                player.put("score", key)
                player.put("name", name)

                leader.put(idx, player)

                idx+=1
            }
        }

        return leader
    }

}
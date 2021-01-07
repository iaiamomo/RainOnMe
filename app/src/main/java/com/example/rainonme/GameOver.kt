package com.example.rainonme

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import org.json.JSONArray
import org.json.JSONObject

class GameOver : Fragment(){

    private lateinit var queue : RequestQueue
    private val url = Conf.url
    private var reply : JSONObject? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_game_over, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        queue = Volley.newRequestQueue(context)
        var leaderboard: JSONArray

        if(Conf.alone){
            view.findViewById<LinearLayout>(R.id.title).visibility = View.GONE
            view.findViewById<RecyclerView>(R.id.list).visibility = View.GONE
            view.findViewById<Space>(R.id.space).visibility = View.GONE
            view.findViewById<TextView>(R.id.textViewCurrentScore).text = Conf.score.toString()
        }else{
            val url_req = url+"req_type="+1+"&game_id="+Conf.gameID+"&who="+Conf.userUID
            Log.i("infoapp", url_req)

            val stringRequest = StringRequest(Request.Method.GET, url_req, { response ->
                reply = JSONObject(response.toString())
                var leaderboardReply = reply!!["leaderboard"] as JSONObject
                leaderboard = getLeaderboard(leaderboardReply)
                Log.i("infoapp", "GameOver.kt " + leaderboard.toString())

                val itemDecoration = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
                val listVal = view.findViewById<RecyclerView>(R.id.list)
                listVal.addItemDecoration(itemDecoration)
                listVal.layoutManager = LinearLayoutManager(context)
                listVal.adapter = LeaderboardAdapter(context, leaderboard)
                view.findViewById<TextView>(R.id.textViewCurrentScore).text = Conf.score.toString()
                Toast.makeText(this.context, "leaderboard OK", Toast.LENGTH_SHORT).show()}
                , { error: VolleyError? -> Log.i("info", "Errore leaderboard " + error) })

            queue.add(stringRequest)
        }

        view.findViewById<Button>(R.id.buttonPlayAgain).setOnClickListener(){
            playAgain()
        }
    }

    private fun playAgain(){
        Conf.alone = false
        Conf.gameID = ""
        Conf.score = 0
        Conf.gameOver = false
        Conf.polli = false
        findNavController().navigate(R.id.action_gameOver_to_settingPlay)
    }

    private fun getLeaderboard(reply: JSONObject) : JSONArray {
        val score  = mutableListOf<Int>()
        val players = mutableListOf<String>()

        val leader = JSONArray()

        var idx = 0

        val sortedKeys = mutableListOf<Int>()
        for(key in reply.keys()){ sortedKeys.add(key.toInt()) }
        sortedKeys.sort()
        Log.i("infoapp", sortedKeys.toString())

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

        Log.i("infoapp", score.toString())
        Log.i("infoapp", players.toString())

        return leader
    }

}
package com.example.rainonme

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import android.content.pm.PackageManager as PackageManager

class SettingPlay : Fragment() {

    lateinit var queue : RequestQueue
    lateinit var clipboardManager : ClipboardManager
    private val url = Conf.url
    var reply : JSONObject? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_setting_play, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val userid = Conf.userUID

        queue = Volley.newRequestQueue(context)
        clipboardManager = context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        view.findViewById<Button>(R.id.buttonCreate).setOnClickListener {
            createGame(view, userid)
        }

        view.findViewById<Button>(R.id.buttonEnter).setOnClickListener {
            val gameidVal = view.findViewById<TextView>(R.id.editTextCode)
            if (gameidVal.text.isEmpty()) {
                gameidVal.setError("Insert a valid code")
            } else {
                Toast.makeText(context, "Code inserted " + gameidVal.text.toString(), Toast.LENGTH_SHORT).show()
                Conf.gameID = gameidVal.text.toString()
                enterGame(userid)
            }
        }

        view.findViewById<Button>(R.id.buttonOffline).setOnClickListener {
            aloneGame()
        }

    }

    private fun createGame(view:View, userid: String){
        val url_req = url+"who="+userid
        val stringRequest = StringRequest(Request.Method.POST, url_req
                ,{response ->
                    reply = JSONObject(response.toString())
                    val gameid = reply!!["game_id"].toString()
                    Conf.gameID = gameid
                    Toast.makeText(this.context, gameid, Toast.LENGTH_SHORT).show()
                    hideElements(view)
                    view.findViewById<LinearLayout>(R.id.shareCode).visibility = View.VISIBLE
                    view.findViewById<Button>(R.id.buttonPlayGame).setOnClickListener { goToGame() }
                    view.findViewById<Button>(R.id.buttonCopy).setOnClickListener { copyCode(gameid) }
                    view.findViewById<Button>(R.id.buttonShare).setOnClickListener { shareCode(gameid) } }
                ,{error: VolleyError? -> Log.i("info", "Errore createGame "+error)})
        queue.add(stringRequest)
    }

    private fun hideElements(view: View){
        view.findViewById<Button>(R.id.buttonCreate).isClickable = false
        view.findViewById<LinearLayout>(R.id.enterCodeLayout).visibility = View.GONE
        view.findViewById<Button>(R.id.buttonOffline).visibility = View.GONE
    }

    private fun enterGame(userid: String){
        val gameid = Conf.gameID
        val url_req = url+"req_type=0&who="+userid+"&game_id="+gameid
        Log.i("infoapp", "url "+url_req)
        val stringRequest = StringRequest(Request.Method.PUT, url_req, {response ->
            reply = JSONObject(response.toString())
            Toast.makeText(this.context, "Added user to the game", Toast.LENGTH_SHORT).show()
            Log.i("infoapp", "added user to "+Conf.gameID)
            goToGame() }, { _ ->
            Log.i("info", "Errore enterGame")
            view?.findViewById<TextView>(R.id.editTextCode)?.setError("Inser a valide code") })
        queue.add(stringRequest)
    }

    private fun goToGame(){
        Conf.alone = false
        findNavController().navigate(R.id.action_settingPlay_to_play)
    }

    private fun copyCode(game_id: String){
        val clip = ClipData.newPlainText("GAME ID", game_id)
        clipboardManager.setPrimaryClip(clip)
    }

    private fun shareCode(game_id: String){
        try{
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"
            val info = context?.packageManager?.getPackageInfo(Conf.whatsapp, PackageManager.GET_META_DATA)
            intent.`package` = Conf.whatsapp
            intent.putExtra(Intent.EXTRA_TEXT, game_id)
            startActivity(intent)
        } catch (e: PackageManager.NameNotFoundException) {
            Toast.makeText(context, "WhatsApp not installed", Toast.LENGTH_SHORT).show()
            Log.i("info", "WhatsApp not installed")
        }
    }

    private fun aloneGame(){
        Conf.alone = true
        findNavController().navigate(R.id.action_settingPlay_to_play)
    }

}
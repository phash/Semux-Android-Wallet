package de.phash.manuel.asw

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.google.gson.Gson
import de.phash.manuel.asw.semux.APIService
import de.phash.manuel.asw.semux.json.CheckBalance
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.my_toolbar))
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        when (item.itemId) {
            R.id.balancesMenu -> balanceActivity(this)
            R.id.createAccout -> createActivity(this)

        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(receiver, IntentFilter(
                APIService.NOTIFICATION))
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(receiver)
    }

    fun onClick(view: View) {

        val intent = Intent(this, APIService::class.java)
        // add infos for the service which file to download and where to store
        intent.putExtra(APIService.ADDRESS, searchBalance.text.toString())
        intent.putExtra(APIService.TYP,
                APIService.check)
        startService(intent)
        Toast.makeText(this, "service started", Toast.LENGTH_SHORT)

    }


    private val receiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val bundle = intent.extras
            if (bundle != null) {
                val json = bundle.getString(APIService.JSON)
                val resultCode = bundle.getInt(APIService.RESULT)
                if (resultCode == Activity.RESULT_OK) {
                    val account = Gson().fromJson(json, CheckBalance::class.java)
                    Log.i("RES", json)
                    Toast.makeText(this@MainActivity,
                            "checked: ${account.message}",
                            Toast.LENGTH_LONG).show()
                    Log.i("RES", account.message)
                    Log.i("RES", account.result.available)

                    balanceAvailable.text = "${account.result.available}"
                    balanceLocked.text = "${account.result.locked}"
                    balancePendingTx.text = "${account.result.pendingTransactionCount}"
                } else {
                    Toast.makeText(this@MainActivity, "check failed",
                            Toast.LENGTH_LONG).show()

                }
            }
        }
    }


}

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

    val service = APIService()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        when (item.getItemId()) {
            R.id.sendMenu -> sendActivity()

            R.id.receiveMenu -> receiveActivity()

            R.id.createAccout -> createActivity()

        }
        return super.onOptionsItemSelected(item)
    }

    private fun createActivity() {
        val intent = Intent(this, CreateAccountActivity::class.java);
        startActivity(intent)
    }

    private fun receiveActivity() {
        val intent = Intent(this, MainActivity::class.java);
        startActivity(intent)
    }

    private fun sendActivity() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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

                    balanceAvailable.setText("${account.result.available}")
                    balanceLocked.setText("${account.result.locked}")
                    balancePendingTx.setText("${account.result.pendingTransactionCount}")
                } else {
                    Toast.makeText(this@MainActivity, "check failed",
                            Toast.LENGTH_LONG).show()

                }
            }
        }
    }


}

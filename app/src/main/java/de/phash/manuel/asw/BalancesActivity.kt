package de.phash.manuel.asw

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.widget.Toast
import com.google.gson.Gson
import de.phash.manuel.asw.database.MyDatabaseOpenHelper
import de.phash.manuel.asw.database.database
import de.phash.manuel.asw.semux.APIService
import de.phash.manuel.asw.semux.SemuxAddress
import de.phash.manuel.asw.semux.json.CheckBalance
import org.jetbrains.anko.db.classParser
import org.jetbrains.anko.db.parseList
import org.jetbrains.anko.db.select

class BalancesActivity : AppCompatActivity() {
    val rowParser = classParser<SemuxAddress>()
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    private var balancesList = ArrayList<CheckBalance>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_balances)
        viewManager = LinearLayoutManager(this)
        viewAdapter = SemuxBalanceAdapter(balancesList)

        var adresses = getAdresses(database)
        adresses.forEach { Log.i("ADDR", "Address ${it.address}") }

        updateBalanceList(adresses)

        recyclerView = findViewById<RecyclerView>(R.id.balancesRecycler).apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter

        }
    }

    private fun updateBalanceList(adresses: List<SemuxAddress>) {

        adresses.forEach {
            updateAddress(it.address)
        }

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

    private fun updateAddress(address: String) {

        val intent = Intent(this, APIService::class.java)
        // add infos for the service which file to download and where to store
        intent.putExtra(APIService.ADDRESS, address)
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
                    Toast.makeText(this@BalancesActivity,
                            "checked: ${account.message}",
                            Toast.LENGTH_LONG).show()
                    Log.i("RES", account.message)
                    Log.i("RES", account.result.available)
                    Log.i("JSON", "${account.result.address}")
                    Log.i("JSON", "${account.result.available}")
                    Log.i("JSON", "${account.result.locked}")
                    Log.i("JSON", "${account.result.pendingTransactionCount}")
                    balancesList.add(account)
                    viewAdapter.notifyDataSetChanged()
                } else {
                    Toast.makeText(this@BalancesActivity, "check failed",
                            Toast.LENGTH_LONG).show()

                }
            }
        }
    }

    fun getAdresses(db: MyDatabaseOpenHelper): List<SemuxAddress> = db.use {

        select(MyDatabaseOpenHelper.SEMUXADDRESS_TABLENAME).exec {
            parseList(rowParser)

        }

    }
}

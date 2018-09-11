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
import de.phash.manuel.asw.semux.APIService
import de.phash.manuel.asw.semux.json.transactions.Result
import de.phash.manuel.asw.semux.json.transactions.TransactionsResult
import java.text.DecimalFormat


class TransactionsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    val df = DecimalFormat("0.#########")
    var transactionsList = ArrayList<Result>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transactions)
        loadTransactions(intent.getStringExtra("address"))
        viewManager = LinearLayoutManager(this)
        viewAdapter = SemuxTransactionAdapter(transactionsList)


    }

    private fun loadTransactions(address: String?) {
        val intent = Intent(this, APIService::class.java)
        address?.let {
            intent.putExtra(APIService.ADDRESS, it)
        }
        intent.putExtra(APIService.TYP,
                APIService.transactions)
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
                    val transactionsResult = Gson().fromJson(json, TransactionsResult::class.java)
                    Log.i("RES", json)
                    transactionsList.addAll(transactionsResult.result)
                    Log.i("BAL", "" + transactionsList.size)
                    viewAdapter.notifyDataSetChanged()
                } else {
                    Toast.makeText(this@TransactionsActivity, "check failed",
                            Toast.LENGTH_LONG).show()

                }
            }
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


}

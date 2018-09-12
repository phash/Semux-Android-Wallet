/*
 * MIT License
 *
 * Copyright (c) 2018 Manuel Roedig / Phash
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
import android.view.Menu
import android.view.MenuItem
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
        setSupportActionBar(findViewById(R.id.my_toolbar))
        viewManager = LinearLayoutManager(this)
        viewAdapter = TransactionAdapter(transactionsList)
        loadTransactions(intent.getStringExtra("address"))
        recyclerView = findViewById<RecyclerView>(R.id.transactionsRecyclerView).apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter

        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle item selection
        startNewActivity(item, this)
        return super.onOptionsItemSelected(item)
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
                    Log.i("TRX", json)
                    Log.i("JSON", "transactions: ${transactionsResult.result.size}")
                    transactionsList.addAll(transactionsResult.result)
                    Log.i("TRX", "" + transactionsList.size)
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



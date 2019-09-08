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
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.CompoundButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import de.phash.manuel.asw.semux.APIService
import de.phash.manuel.asw.semux.json.accountvotes.AccountVotes
import de.phash.manuel.asw.semux.json.delegates.Delegates
import de.phash.manuel.asw.semux.json.delegates.Result
import de.phash.manuel.asw.semux.key.Network
import kotlinx.android.synthetic.main.activity_select_delegate.*

class SelectDelegate : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    var address = ""

    private var delegatesResult = ArrayList<Result>()
    private var ownVotes = ArrayList<de.phash.manuel.asw.semux.json.accountvotes.Result>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_delegate)
        setSupportActionBar(findViewById(R.id.my_toolbar))
        setTitle(if (APIService.NETWORK == Network.MAINNET)  R.string.semuxMain else R.string.semuxTest)
        address = intent.getStringExtra("address")
        viewManager = LinearLayoutManager(this)
        checkOwnSwitch.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked -> checkDelegates(address) })

        viewAdapter = DelegatesAdapter(delegatesResult, ownVotes, address, checkOwnSwitch.isChecked)
        checkDelegates(address)
        recyclerView = findViewById<RecyclerView>(R.id.delegatesRecylcer).apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }
    }

    fun checkDelegates(address: String) {

        val intent = Intent(this, APIService::class.java)
        intent.putExtra(APIService.TYP,
                APIService.delegates)
        intent.putExtra(APIService.ADDRESS,
                address)
        startService(intent)
    }


    override fun onResume() {
        super.onResume()
        registerReceiver(receiver, IntentFilter(
                APIService.NOTIFICATION_DELEGATES))
        registerReceiver(receiver, IntentFilter(
                APIService.NOTIFICATION_ACCOUNTVOTES))
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(receiver)
    }

    private val receiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            Log.i("RECEIVE", "SelectDelegate received Broadcast: ${intent.getStringExtra(APIService.TYP)}")
            when (intent.getStringExtra(APIService.TYP)) {
                APIService.delegates -> updateDelegates(context, intent)
                APIService.accountvotes -> updateVotes(context, intent)
                else -> Log.i("RECEIVE", "SelectDelegate received Broadcast: ${intent.getStringExtra(APIService.TYP)} - but did not match any case!")
            }


        }
    }

    private fun updateVotes(context: Context, intent: Intent) {
        Log.i("RECEIVE", "SelectDelegate received NOTIFICATION_DELEGATES Broadcast")
        val bundle = intent.extras
        if (bundle != null) {
            val json = bundle.getString(APIService.JSON)
            val resultCode = bundle.getInt(APIService.RESULT)
            if (resultCode == Activity.RESULT_OK) {
                var votes = Gson().fromJson(json, AccountVotes::class.java)

                ownVotes.addAll(votes.result)
                delegatesResult.sortedByDescending { it.votes.toDouble() }
                viewAdapter.notifyDataSetChanged()
            } else {
                Toast.makeText(this@SelectDelegate, "check votes failed",
                        Toast.LENGTH_LONG).show()

            }
        }
    }

    private fun updateDelegates(context: Context, intent: Intent) {
        Log.i("RECEIVE", "SelectDelegate received NOTIFICATION_ACCOUNTVOTES Broadcast")
        val bundle = intent.extras
        if (bundle != null) {
            val json = bundle.getString(APIService.JSON)
            val resultCode = bundle.getInt(APIService.RESULT)
            if (resultCode == Activity.RESULT_OK) {
                var delegates = Gson().fromJson(json, Delegates::class.java)
                delegatesResult.clear()
                if (checkOwnSwitch.isChecked) {
                    Log.i("FILTER", "show only voted by me")

                    delegatesResult.addAll(getAllDelegatesWithVotesFromMe(delegates))

                } else {
                    Log.i("FILTER", "show all delegates")
                    delegatesResult.addAll(delegates.result)
                }
                delegatesResult.sortByDescending { it.votes.toDouble() }
                viewAdapter.notifyDataSetChanged()
            } else {
                Toast.makeText(this@SelectDelegate, "check failed",
                        Toast.LENGTH_LONG).show()

            }
        }
    }

    private fun getAllDelegatesWithVotesFromMe(delegates: Delegates): Collection<Result> {
        var res =
                delegates.result.filter { delegate ->
                    ownVotes.filter {
                        delegate.address.equals(it.delegate.address)
                    }.firstOrNull() != null
                }
        return res

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
}



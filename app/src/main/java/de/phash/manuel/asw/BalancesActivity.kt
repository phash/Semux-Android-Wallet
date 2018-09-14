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
import android.view.MotionEvent
import android.widget.Toast
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.gson.Gson
import de.phash.manuel.asw.database.database
import de.phash.manuel.asw.semux.APIService
import de.phash.manuel.asw.semux.SemuxAddress
import de.phash.manuel.asw.semux.json.CheckBalance
import de.phash.manuel.asw.semux.json.Result
import de.phash.manuel.asw.util.getAdresses
import kotlinx.android.synthetic.main.activity_balances.*
import java.math.BigDecimal
import java.util.*


class BalancesActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    private var balancesMap = HashMap<String, CheckBalance>()
    var balancesList = ArrayList<Result>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_balances)
        setSupportActionBar(findViewById(R.id.my_toolbar))
        viewManager = LinearLayoutManager(this)
        viewAdapter = SemuxBalanceAdapter(balancesList)
        balancesTotalAvailable.text = "0 SEM"
        balancesTotalLocked.text = "0 SEM"
        timer = Timer()
        timer.scheduleAtFixedRate(UpdateBalTask(), 30000, 30000)
        updateBalances()

        recyclerView = findViewById<RecyclerView>(R.id.balancesRecycler).apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
            addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {
                override fun onTouchEvent(p0: RecyclerView, p1: MotionEvent) {
                    Log.i("COPY", "onTouch")
                }

                override fun onInterceptTouchEvent(p0: RecyclerView, p1: MotionEvent): Boolean {
                    Log.i("COPY", "intercept")
                    return false
                }

                override fun onRequestDisallowInterceptTouchEvent(p0: Boolean) {
                    Log.i("COPY", "disallowed")
                }

            })
        }

    }


    private var timer = Timer()

    inner class UpdateBalTask : TimerTask() {
        override fun run() {
            Log.i("TIMER", "timer runs")
            updateBalances()
        }
    }

    private var mFirebaseAnalytics: FirebaseAnalytics? = null

    private fun updateBalances() {

        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "5")
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "send")

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)
        mFirebaseAnalytics!!.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)

        val adresses = getAdresses(database)

        updateBalanceList(adresses)
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
    }

    private val receiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val bundle = intent.extras
            if (bundle != null) {
                val json = bundle.getString(APIService.JSON)
                val resultCode = bundle.getInt(APIService.RESULT)
                if (resultCode == Activity.RESULT_OK) {
                    val account = Gson().fromJson(json, CheckBalance::class.java)

                    balancesMap.put(account.result.address, account)
                    val total = balancesMap.values.map { BigDecimal(it.result.available) }.fold(BigDecimal.ZERO, BigDecimal::add)
                    val totallocked = balancesMap.values.map { BigDecimal(it.result.locked) }.fold(BigDecimal.ZERO, BigDecimal::add)

                    balancesTotalAvailable.text = "${APIService.SEMUXFORMAT.format(BigDecimal.ZERO.add(total.divide(APIService.SEMUXMULTIPLICATOR)))} SEM"
                    balancesTotalLocked.text = "${APIService.SEMUXFORMAT.format(BigDecimal.ZERO.add(totallocked.divide(APIService.SEMUXMULTIPLICATOR)))} SEM"

                    balancesList.clear()

                    balancesList.addAll(balancesMap.values.map { it.result })
                    balancesList.sortByDescending { it.available }
                    //   balancesList.sortWith<Result>(compareBy(Result::available, Result::locked, Result::transactionCount))
                    viewAdapter.notifyDataSetChanged()
                } else {
                    Toast.makeText(this@BalancesActivity, "check failed",
                            Toast.LENGTH_LONG).show()

                }
            }
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


}

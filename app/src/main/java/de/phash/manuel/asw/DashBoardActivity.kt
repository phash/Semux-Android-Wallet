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
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.Toast
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.gson.Gson
import de.phash.manuel.asw.database.database
import de.phash.manuel.asw.semux.APIService
import de.phash.manuel.asw.semux.json.CheckBalance
import de.phash.manuel.asw.util.checkBalanceForWallet
import kotlinx.android.synthetic.main.activity_dash_board.*
import java.math.BigDecimal


class DashBoardActivity : AppCompatActivity() {
    private var mFirebaseAnalytics: FirebaseAnalytics? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dash_board)
        setSupportActionBar(findViewById(R.id.my_toolbar))
        checkBalanceForWallet(database, this)
// Obtain the FirebaseAnalytics instance.
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "1")
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "dashboard")

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)
        mFirebaseAnalytics!!.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)
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

    fun onCreateClick(view: View) {
        val intent = Intent(this, CreateAccountActivity::class.java)
        startActivity(intent)
    }

    fun onImportClick(view: View) {
        val intent = Intent(this, ImportKeyActivity::class.java)
        startActivity(intent)

    }

    fun onBalancesClick(view: View) {

        val intent = Intent(this, BalancesActivity::class.java)
        startActivity(intent)
    }


    private val balancesList = ArrayList<CheckBalance>()
    private val receiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val bundle = intent.extras
            if (bundle != null) {
                val json = bundle.getString(APIService.JSON)
                val resultCode = bundle.getInt(APIService.RESULT)
                if (resultCode == Activity.RESULT_OK) {
                    val account = Gson().fromJson(json, CheckBalance::class.java)
                    Log.i("RES", json)

                    balancesList.add(account)

                    dashboardCreate.visibility = INVISIBLE
                    dashboardImport.visibility = INVISIBLE
                    noAccountsText.visibility = INVISIBLE

                    dashLocked.visibility = VISIBLE
                    dashTotal.visibility = VISIBLE
                    viewAccountsButton.visibility = VISIBLE
                    dashLockedtextView.visibility = VISIBLE
                    dashTotaltextView.visibility = VISIBLE

                    val total = balancesList.map { BigDecimal(it.result.available) }.fold(BigDecimal.ZERO, BigDecimal::add)
                    val totallocked = balancesList.map { BigDecimal(it.result.locked) }.fold(BigDecimal.ZERO, BigDecimal::add)

                    dashTotal.text = "${APIService.SEMUXFORMAT.format(BigDecimal.ZERO.add(total.divide(APIService.SEMUXMULTIPLICATOR)))} SEM"
                    dashLocked.text = "${APIService.SEMUXFORMAT.format(BigDecimal.ZERO.add(totallocked.divide(APIService.SEMUXMULTIPLICATOR)))} SEM"

                    Log.i("BAL", "" + balancesList.size)
                    //balancesList.sortBy { it.result.available }


                } else {
                    Toast.makeText(this@DashBoardActivity, "check failed",
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

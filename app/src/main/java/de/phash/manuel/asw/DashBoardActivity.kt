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
import de.phash.manuel.asw.semux.APIService.Companion.SEMUXFORMAT
import de.phash.manuel.asw.semux.json.CheckBalance
import de.phash.manuel.asw.util.checkBalanceForWallet
import de.phash.manuel.asw.util.firebase
import de.phash.manuel.asw.util.isPasswordSet
import kotlinx.android.synthetic.main.activity_dash_board.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.browse
import java.math.BigDecimal
import java.util.*


class DashBoardActivity : AppCompatActivity() {


    private var balancesMap = HashMap<String, CheckBalance>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dash_board)
        setSupportActionBar(findViewById(R.id.my_toolbar))
        checkBalanceForWallet(database, this)
        firebase("1", type = "dashboard", mFirebaseAnalytics = FirebaseAnalytics.getInstance(this))
        versionView.text = this.packageManager.getPackageInfo(this.packageName, 0).versionName

        setPWButton.visibility = if (isPasswordSet(this)) INVISIBLE else VISIBLE
    }

    override fun onRestart() {
        super.onRestart()
        checkBalanceForWallet(database, this)
        firebase("1", type = "dashboard", mFirebaseAnalytics = FirebaseAnalytics.getInstance(this))
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

    fun onGetSemux(view: View) {
        browse("https://app.stex.com/?ref=77187592")
    }

    fun onCreateClick(view: View) {
        createActivity(this)
    }

    fun onImportClick(view: View) {
        importActivity(this)
    }

    fun onSetPWClick(view: View) {
        setPasswordActivity(this)
    }

    fun onBalancesClick(view: View) {
        val intent = Intent(this, BalancesActivity::class.java)
        startActivity(intent)
    }


    private val receiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            Log.i("RECEIVE", "DashBoard received Broadcast")
            val bundle = intent.extras
            if (bundle != null) {
                val json = bundle.getString(APIService.JSON)
                val resultCode = bundle.getInt(APIService.RESULT)
                if (resultCode == Activity.RESULT_OK) {
                    val account = Gson().fromJson(json, CheckBalance::class.java)
                    Log.i("RES", json)

                    showRecycler()
                    if (balancesMap.containsKey(account.result.address)) {
                        val oldResult = balancesMap.get(account.result.address)
                        val compareAvailable = BigDecimal(oldResult?.result?.available).compareTo(BigDecimal(account.result.available))
                        when {
                            compareAvailable < 0 -> alert("Incoming Transaction of ${SEMUXFORMAT.format(BigDecimal(oldResult?.result?.available).subtract(BigDecimal(account.result.available)).toPlainString())}").show()
                            compareAvailable > 0 -> alert("Incoming Transaction of ${SEMUXFORMAT.format(BigDecimal(account.result.available).subtract(BigDecimal(oldResult?.result?.available)).toPlainString())}").show()
                        }
                    }

                    balancesMap.put(account.result.address, account)

                    val total = balancesMap.values.map { BigDecimal(it.result.available) }.fold(BigDecimal.ZERO, BigDecimal::add)
                    val totallocked = balancesMap.values.map { BigDecimal(it.result.locked) }.fold(BigDecimal.ZERO, BigDecimal::add)

                    dashTotal.text = "${APIService.SEMUXFORMAT.format(BigDecimal.ZERO.add(total.divide(APIService.SEMUXMULTIPLICATOR)))} SEM"
                    dashLocked.text = "${APIService.SEMUXFORMAT.format(BigDecimal.ZERO.add(totallocked.divide(APIService.SEMUXMULTIPLICATOR)))} SEM"

                } else {
                    Toast.makeText(this@DashBoardActivity, "check failed",
                            Toast.LENGTH_LONG).show()

                }
            }
        }
    }

    private fun showRecycler() {
        dashboardCreate.visibility = INVISIBLE
        dashboardImport.visibility = INVISIBLE
        noAccountsText.visibility = INVISIBLE

        dashLocked.visibility = VISIBLE
        dashTotal.visibility = VISIBLE
        viewAccountsButton.visibility = VISIBLE
        dashLockedtextView.visibility = VISIBLE
        dashTotaltextView.visibility = VISIBLE
        setPWButton.visibility = INVISIBLE
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

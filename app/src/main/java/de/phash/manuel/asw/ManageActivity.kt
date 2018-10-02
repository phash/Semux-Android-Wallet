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
import android.content.*
import android.os.Bundle

import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import de.phash.manuel.asw.database.database
import de.phash.manuel.asw.semux.APIService
import de.phash.manuel.asw.semux.ManageAccounts
import de.phash.manuel.asw.semux.json.CheckBalance
import de.phash.manuel.asw.util.*
import kotlinx.android.synthetic.main.password_prompt.view.*

class ManageActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage)
        setSupportActionBar(findViewById(R.id.my_toolbar))
        viewManager = LinearLayoutManager(this)
        viewAdapter = ManageAdapter(accountList, this, DEFAULT_PW, database)
        recyclerView = findViewById<RecyclerView>(R.id.manageRecycler).apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }
        if (isPasswordSet(this)) {
            passwordSecured()
        } else {
            createContent(DEFAULT_PW)
        }
    }


    private var accountList = ArrayList<ManageAccounts>()
    private var accounts = HashMap<String, ManageAccounts>()

    private var password: String = DEFAULT_PW

    private fun createContent(password: String) {
        checkBalanceForWallet(database, this)
        this.password = password
    }


    fun passwordSecured() {
        val dialogBuilder = AlertDialog.Builder(this)
        val inflater = LayoutInflater.from(this)
        val promptView = inflater.inflate(R.layout.password_prompt, null)
        dialogBuilder.setView(promptView)

        dialogBuilder.setCancelable(true).setOnCancelListener(DialogInterface.OnCancelListener { dialog ->
            dialog.dismiss()
            settingsActivity(this)
        })
                .setPositiveButton("SEND") { dialog, which ->
                    Log.i("PASSWORD", "positive button")
                    if (promptView.enterOldPassword.text.toString().isEmpty()) {
                        Toast.makeText(this, "Input does not match your current password", Toast.LENGTH_LONG).show()
                    } else {
                        if (isPasswordCorrect(this, promptView.enterOldPassword.text.toString())) {
                            createContent(promptView.enterOldPassword.text.toString())


                        } else {
                            Log.i("PASSWORD", "PW false")
                            Toast.makeText(this, "Input does not match your current password", Toast.LENGTH_LONG).show()
                            settingsActivity(this)
                        }
                    }
                }
                .setNegativeButton("CANCEL") { dialog, which ->
                    Log.i("PASSWORD", "negative button")
                    dialog.dismiss()
                    settingsActivity(this)
                }
        val dialog: AlertDialog = dialogBuilder.create()
        dialog.show()
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

    private val balancesMap: HashMap<String, CheckBalance> = HashMap()
    private val receiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            Log.i("RECEIVE", "BalancesActivity received Broadcast")
            val bundle = intent.extras
            if (bundle != null) {
                val json = bundle.getString(APIService.JSON)
                val resultCode = bundle.getInt(APIService.RESULT)
                if (resultCode == Activity.RESULT_OK) {
                    val checkBalance = Gson().fromJson(json, CheckBalance::class.java)
                    val addressFromDB = getSemuxAddress(database, checkBalance.result.address)
                    addressFromDB.let {
                        val decryptedAcc = decryptAccount(addressFromDB!!, password)
                        val manage = ManageAccounts(decryptedAcc, checkBalance)
                        accounts.put(manage.account.address, manage)
                        accountList.clear()
                        accountList.addAll(accounts.values)

                    }

                    //   balancesList.sortWith<Result>(compareBy(Result::available, Result::locked, Result::transactionCount))
                    viewAdapter.notifyDataSetChanged()
                } else {
                    Toast.makeText(this@ManageActivity, "check failed",
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

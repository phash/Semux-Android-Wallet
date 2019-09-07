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

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import de.phash.manuel.asw.database.database
import de.phash.manuel.asw.semux.APIService
import de.phash.manuel.asw.semux.APIService.Companion.FORCE
import de.phash.manuel.asw.semux.APIService.Companion.TYP
import de.phash.manuel.asw.semux.APIService.Companion.checkall
import de.phash.manuel.asw.semux.key.Network
import de.phash.manuel.asw.util.isPasswordCorrect
import de.phash.manuel.asw.util.persistNewPassword
import de.phash.manuel.asw.util.updateAllAddresses
import kotlinx.android.synthetic.main.activity_passwords.*
import kotlinx.android.synthetic.main.password_prompt.view.*
import java.util.*
import java.util.Arrays.asList

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        setSupportActionBar(findViewById(R.id.my_toolbar))
    }

    fun onImportKeyClick(view: View) {
        importActivity(this)
    }

    fun onSetPasswordClick(view: View) {
        setPasswordActivity(this)
    }

    fun onManageClick(view: View) {
        manageActivity(this)
    }

    fun onCreateAccountClick(view: View) {
        createActivity(this)
    }

    fun onNetworkClick(view: View) {

    }

    val networks = arrayOf( "MAINNET", "TESTNET", "DEVNET");

    fun onApiClick(view: View) {

        val dialogBuilder = AlertDialog.Builder(this)
        val inflater = LayoutInflater.from(this)

        with(dialogBuilder) {
            setCancelable(true).setOnCancelListener(DialogInterface.OnCancelListener { dialog ->
                dialog.dismiss()
            })

            setItems(networks,  DialogInterface.OnClickListener { dialog, which ->
                change(which)
                dialog.dismiss()
            })

            show()
        }
    }

    private fun change(which: Int) {
        Log.i("SETTINGS", "chosen Networktyp -> $which")
        when (which){
            0 -> APIService.changeNetwork(Network.MAINNET)

            1 -> APIService.changeNetwork(Network.TESTNET)

            2 -> APIService.changeNetwork(Network.DEVNET)

        }

        val intent = Intent(this, APIService::class.java)

        intent.putExtra(TYP, checkall)
        intent.putExtra(FORCE, true)
        startService(intent)
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

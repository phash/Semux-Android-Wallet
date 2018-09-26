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
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import de.phash.manuel.asw.database.MyDatabaseOpenHelper
import de.phash.manuel.asw.database.database
import de.phash.manuel.asw.semux.key.Key
import de.phash.manuel.asw.util.createAccount
import de.phash.manuel.asw.util.isPasswordCorrect
import de.phash.manuel.asw.util.isPasswordSet
import kotlinx.android.synthetic.main.activity_create_account.*
import kotlinx.android.synthetic.main.password_prompt.view.*
import org.bouncycastle.util.encoders.Hex
import org.jetbrains.anko.alert
import org.jetbrains.anko.noButton
import org.jetbrains.anko.okButton
import org.jetbrains.anko.yesButton

class CreateAccountActivity : AppCompatActivity() {

    var key = Key()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_account)
        setSupportActionBar(findViewById(R.id.my_toolbar))
        updateViews()
    }

    fun saveClick(view: View) {
        if (isPasswordSet(this)) {
            passwordSecured()
        } else {
            save("default")
        }
    }

    fun updateViews() {
        privateKey.text = Hex.toHexString(key.privateKey)
        publicKey.text = Hex.toHexString(key.publicKey)
        createdaddress.text = key.toAddressString()
    }

    fun onDashKey(view: View) {

        alert("please write down your private key!\n${Hex.toHexString(key.privateKey)}") {
            okButton { }
            yesButton { dashboardActivity(this@CreateAccountActivity) }
            noButton {}
        }.show()
    }

    override fun onRestart() {
        super.onRestart()
        dashboardActivity(this)
    }

    override fun onBackPressed() {
        alert("please write down your private key!\n${Hex.toHexString(key.privateKey)}") {
            okButton { }
            yesButton { dashboardActivity(this@CreateAccountActivity) }
            noButton {}
        }.show()
    }


    fun save(password: String) {
        val semuxAddress = createAccount(key, password)
        val values = semuxAddress.toContentValues()

        database.use { insert(MyDatabaseOpenHelper.SEMUXADDRESS_TABLENAME, null, values) }
        Toast.makeText(this, "new account created! Save your private key!", Toast.LENGTH_LONG)
        //balanceActivity(this)

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
                            save(promptView.enterOldPassword.text.toString())

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

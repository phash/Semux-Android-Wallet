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
import android.view.View
import android.widget.Toast
import de.phash.manuel.asw.util.isPasswordCorrect
import de.phash.manuel.asw.util.isPasswordSet
import de.phash.manuel.asw.util.persistNewPassword
import kotlinx.android.synthetic.main.activity_passwords.*
import kotlinx.android.synthetic.main.password_prompt.view.*

class PasswordActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_passwords)
    }

    fun onSavePasswordsClick(view: View) {
        if (passwordAndRepeatMatch()) {
            setNewPassword()
        } else {
            Log.i("PASSWORD", "passwords did not match")
            Toast.makeText(this, "Passwords don't not match", Toast.LENGTH_LONG).show()
        }
    }

    private fun passwordAndRepeatMatch(): Boolean {
        return enterNewPassword.text?.toString().equals(other = repeatNewPassword.text?.toString())
    }

    private fun setNewPassword() {
        Log.i("PASSWORD", "set new password")
        if (isPasswordSet(this)) {
            val dialogBuilder = AlertDialog.Builder(this)
            val inflater = LayoutInflater.from(this)
            val promptView = inflater.inflate(R.layout.password_prompt, null)
            dialogBuilder.setView(promptView)

            dialogBuilder.setCancelable(true).setOnCancelListener(DialogInterface.OnCancelListener { dialog ->
                dialog.dismiss()
            })
                    .setPositiveButton("SAVE") { dialog, which ->
                        Log.i("PASSWORD", "positive button")
                        if (promptView.enterOldPassword.text.toString().isEmpty()) {
                            Toast.makeText(this, "Input does not match your current password", Toast.LENGTH_LONG).show()
                        } else {
                            if (isPasswordCorrect(this, promptView.enterOldPassword.text.toString())) {
                                persistNewPassword(this, enterNewPassword.text.toString())
                                Toast.makeText(this, "New password set", Toast.LENGTH_LONG).show()
                                settingsActivity(this)
                            } else {
                                Toast.makeText(this, "Input does not match your current password", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                    .setNegativeButton("CANCEL") { dialog, which ->
                        Log.i("PASSWORD", "negative button")
                        dialog.dismiss()
                    }
            val dialog: AlertDialog = dialogBuilder.create()
            dialog.show()
        } else {
            persistNewPassword(this, enterNewPassword.text.toString())
            Log.i("PASSWORD", "persisted")
            Toast.makeText(this, "New password set", Toast.LENGTH_LONG).show()
            settingsActivity(this)
        }
    }
}

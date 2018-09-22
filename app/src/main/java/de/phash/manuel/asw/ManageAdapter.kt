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

import android.content.Context
import android.content.DialogInterface
import android.support.v7.app.AlertDialog
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import de.phash.manuel.asw.database.MyDatabaseOpenHelper
import de.phash.manuel.asw.semux.APIService
import de.phash.manuel.asw.semux.ManageAccounts
import de.phash.manuel.asw.semux.key.Hex
import de.phash.manuel.asw.util.DeCryptor
import de.phash.manuel.asw.util.deleteSemuxDBAccount
import de.phash.manuel.asw.util.isPasswordCorrect
import de.phash.manuel.asw.util.isPasswordSet
import kotlinx.android.synthetic.main.password_prompt.view.*
import java.math.BigDecimal
import java.text.DecimalFormat

class ManageAdapter(private val myDataset: ArrayList<ManageAccounts>, private val context: Context, private val database: MyDatabaseOpenHelper) :
        RecyclerView.Adapter<ManageAdapter.MyViewHolder>() {

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val address = itemView.findViewById<TextView>(R.id.manageAddress)
        val pkey = itemView.findViewById<TextView>(R.id.managePkey)
        val available = itemView.findViewById<TextView>(R.id.manageAvailable)
        val locked = itemView.findViewById<TextView>(R.id.manageLocked)
        val transactions = itemView.findViewById<TextView>(R.id.manageTx)
        val pending = itemView.findViewById<TextView>(R.id.managePending)
        val removeButton = itemView.findViewById<Button>(R.id.removeButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): ManageAdapter.MyViewHolder {

        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.manage_balance_row, parent, false)

        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        val df = DecimalFormat("0.#########")
        Log.i("MGMT", "DatasetSize: ${myDataset.size}")
        val account = myDataset[position]
        holder.address?.text = account.account.address
        holder.available.text = df.format(BigDecimal(account.check.result.available).divide(APIService.SEMUXMULTIPLICATOR))
        holder.locked.text = df.format(BigDecimal(account.check.result.locked).divide(APIService.SEMUXMULTIPLICATOR))
        holder.transactions.text = account.check.result.transactionCount.toString()
        holder.pending.text = account.check.result.pendingTransactionCount.toString()

        val decryptedKey = DeCryptor().decryptData(account.account.address + "s", Hex.decode0x(account.account.privateKey), Hex.decode0x(account.account.ivs))
        holder.pkey?.text = decryptedKey
        holder.removeButton.setOnClickListener(View.OnClickListener {
            removeClick(account)

        })

    }

    fun removeClick(account: ManageAccounts) {
        Log.i("MGMT", "remove button clicked for address ")
        if (isPasswordSet(context = context)) {
            passwordSecured(account)
        } else {
            deleteAccount(account)
        }
    }

    private fun deleteAccount(account: ManageAccounts) {
        deleteSemuxDBAccount(database, account)
        settingsActivity(context)
    }

    fun passwordSecured(account: ManageAccounts) {
        val dialogBuilder = AlertDialog.Builder(context)
        val inflater = LayoutInflater.from(context)
        val promptView = inflater.inflate(R.layout.password_prompt, null)
        dialogBuilder.setView(promptView)

        dialogBuilder.setCancelable(true).setOnCancelListener(DialogInterface.OnCancelListener { dialog ->
            dialog.dismiss()
            settingsActivity(context)
        })
                .setPositiveButton("SEND") { dialog, which ->
                    Log.i("PASSWORD", "positive button")
                    if (promptView.enterOldPassword.text.toString().isEmpty()) {
                        Toast.makeText(context, "Input does not match your current password", Toast.LENGTH_LONG).show()
                    } else {
                        if (isPasswordCorrect(context, promptView.enterOldPassword.text.toString())) {
                            deleteAccount(account)

                        } else {
                            Log.i("PASSWORD", "PW false")
                            Toast.makeText(context, "Input does not match your current password", Toast.LENGTH_LONG).show()
                        }
                    }
                }
                .setNegativeButton("CANCEL") { dialog, which ->
                    Log.i("PASSWORD", "negative button")
                    dialog.dismiss()
                    settingsActivity(context)
                }
        val dialog: AlertDialog = dialogBuilder.create()
        dialog.show()
    }

    override fun getItemCount() = myDataset.size
}
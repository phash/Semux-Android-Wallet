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

import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import de.phash.manuel.asw.semux.SemuxAddress
import de.phash.manuel.asw.semux.key.Hex
import de.phash.manuel.asw.util.DeCryptor
import java.text.DecimalFormat

class ManageAdapter(private val myDataset: ArrayList<SemuxAddress>) :
        RecyclerView.Adapter<ManageAdapter.MyViewHolder>() {

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val address = itemView.findViewById<TextView>(R.id.manageAddress)
        val pkey = itemView.findViewById<TextView>(R.id.managePkey)
        val removeButton = itemView.findViewById<Button>(R.id.removeButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): ManageAdapter.MyViewHolder {

        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.custom_row_view, parent, false)

        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        val df = DecimalFormat("0.#########")
        Log.i("MGMT", "DatasetSize: ${myDataset.size}")
        val account = myDataset[position]
        holder.address?.text = account.address

        val decryptedKey = DeCryptor().decryptData(account.address + "s", Hex.decode0x(account.privateKey), Hex.decode0x(account.ivs))
        holder.pkey?.text = decryptedKey
        holder.removeButton.setOnClickListener(View.OnClickListener {
            removeClick(it)
            Log.i("MGMT", "removeClick for: ${account.address}")
        })
        /* holder.itemView.setOnLongClickListener(View.OnLongClickListener {
             Log.i("COPY", "LONGCLICK - ${myDataset[position].address}")
             copyToClipboard(it.context, myDataset[position].address)
             Toast.makeText(it.context, "address copied", Toast.LENGTH_SHORT).show()
             true
         })
         holder.itemView.setOnClickListener(View.OnClickListener {
             val intent = Intent(holder.itemView.context, SingleBalanceActivity::class.java)

             intent.putExtra("address", myDataset[position].address)
             intent.putExtra("available", myDataset[position].available)
             intent.putExtra("locked", myDataset[position].locked)

             holder.itemView.context.startActivity(intent)
             Log.i("CLICK", "clicked: ${myDataset[position].address}")
         })
         */
    }

    fun removeClick(view: View) {
        Log.i("MGMT", "remove button clicked for address ")
    }

    override fun getItemCount() = myDataset.size
}
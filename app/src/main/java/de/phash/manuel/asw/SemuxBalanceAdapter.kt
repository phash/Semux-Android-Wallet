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

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import de.phash.manuel.asw.semux.APIService
import de.phash.manuel.asw.semux.json.Result
import de.phash.manuel.asw.util.copyToClipboard
import java.math.BigDecimal
import java.text.DecimalFormat

class SemuxBalanceAdapter(private val myDataset: ArrayList<Result>) :
        RecyclerView.Adapter<SemuxBalanceAdapter.MyViewHolder>() {

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val address = itemView.findViewById<TextView>(R.id.listbalanceaddress)
        val available = itemView.findViewById<TextView>(R.id.listbalanceavailable)
        val locked = itemView.findViewById<TextView>(R.id.listbalancelocked)
    }

    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): SemuxBalanceAdapter.MyViewHolder {

        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.custom_row_view, parent, false)

        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        val df = DecimalFormat("0.#########")
        Log.i("TRX", "DatasetSize: ${myDataset.size}")
        holder.address?.text = myDataset[position].address
        holder.available?.text = df.format(BigDecimal(myDataset[position].available).divide(APIService.SEMUXMULTIPLICATOR)) + " SEM"
        holder.locked?.text = df.format(BigDecimal(myDataset[position].locked).divide(APIService.SEMUXMULTIPLICATOR)) + " SEM"
        holder.itemView.setOnLongClickListener {
            Log.i("COPY", "LONGCLICK - ${myDataset[position].address}")
            copyToClipboard(it.context, myDataset[position].address)
            Toast.makeText(it.context, "address copied", Toast.LENGTH_SHORT).show()
            true
        }

        holder.itemView.setOnClickListener {
            Log.i("CLICK", "clicked: ${myDataset[position].address}")
            singleAccountActivity(holder.itemView.context, myDataset[position].address)
        }
    }

    override fun getItemCount() = myDataset.size
}
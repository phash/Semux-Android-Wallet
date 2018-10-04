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

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import de.phash.manuel.asw.semux.APIService
import de.phash.manuel.asw.semux.json.delegates.Result
import java.math.BigDecimal

class DelegatesAdapter(
        private val delegates: ArrayList<Result>,
        private val ownVotes: ArrayList<de.phash.manuel.asw.semux.json.accountvotes.Result>,
        private val lookupAddress: String)
    : RecyclerView.Adapter<DelegatesAdapter.MyViewHolder>() {

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val address = itemView.findViewById<TextView>(R.id.delegatesListAddress)
        val votesByMe = itemView.findViewById<TextView>(R.id.delegatesListVotesByMe)
        val name = itemView.findViewById<TextView>(R.id.delegatesListName)
        val votes = itemView.findViewById<TextView>(R.id.delegatesListVotes)
        val rank = itemView.findViewById<TextView>(R.id.delegatesListRank)
        val byMe = itemView.findViewById<TextView>(R.id.byMeTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): DelegatesAdapter.MyViewHolder {

        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.delegates_row_view, parent, false)

        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        if (ownVotes.isNotEmpty()) {
            val myVotes = ownVotes.filter { it.delegate.address.equals(delegates.get(position).address) }.firstOrNull()?.votes
            if (myVotes != null) {
                holder.votesByMe.text = "${APIService.SEMUXFORMAT.format(BigDecimal(myVotes).divide(APIService.SEMUXMULTIPLICATOR))}"
                holder.votesByMe.visibility = VISIBLE
                holder.byMe.visibility = VISIBLE
            } else {
                holder.votesByMe.visibility = GONE
                holder.byMe.visibility = GONE
            }


        }
        Log.i("", "DatasetSize: ${delegates.size}")
        holder.address.text = delegates.get(position).address
        holder.name.text = delegates.get(position).name
        holder.votes.text = "${APIService.SEMUXFORMAT.format(BigDecimal.ZERO.add(BigDecimal(delegates.get(position).votes).divide(APIService.SEMUXMULTIPLICATOR)))} SEM"
        holder.rank.text = "# ${(position + 1)}"

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, VoteActivity::class.java)

            intent.putExtra("delegatesAddress", delegates.get(position).address)
            intent.putExtra("address", lookupAddress)
            holder.itemView.context.startActivity(intent)

        }
    }

    override fun getItemCount() = delegates.size
}
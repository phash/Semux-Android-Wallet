package de.phash.manuel.asw

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import de.phash.manuel.asw.semux.APIService
import de.phash.manuel.asw.semux.json.transactions.Result
import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

class SemuxTransactionAdapter(private val myDataset: ArrayList<Result>) :
        RecyclerView.Adapter<SemuxTransactionAdapter.MyViewHolder>() {

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val from = itemView.findViewById<TextView>(R.id.listtransactionsfrom)
        val amount = itemView.findViewById<TextView>(R.id.listtransactionsamount)
        val txdate = itemView.findViewById<TextView>(R.id.listtransactionsdate)
        val to = itemView.findViewById<TextView>(R.id.listtransactionsto)
    }

    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): SemuxTransactionAdapter.MyViewHolder {

        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.custom_row_view, parent, false)

        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        val df = DecimalFormat("0.#########")

        holder.from.text = myDataset[position].from
        holder.amount.text = df.format(BigDecimal(myDataset[position].value).divide(APIService.SEMUXMULTIPLICATOR))
        holder.to.text = myDataset[position].to
        holder.txdate.text = getDateTime(myDataset[position].to)

    }

    override fun getItemCount() = myDataset.size
}

private fun getDateTime(date: String): String? {
    try {
        val sdf = SimpleDateFormat("yyyy.MM.dd HH:mm:ss X z")
        val netDate = Date(date.toLong())
        return sdf.format(netDate)
    } catch (e: Exception) {
        return e.toString()
    }
}
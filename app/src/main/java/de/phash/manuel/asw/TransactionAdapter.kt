package de.phash.manuel.asw

import android.support.v7.widget.RecyclerView
import android.util.Log
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

class TransactionAdapter(private val myDataset: ArrayList<Result>) :
        RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val from = itemView.findViewById<TextView>(R.id.listtransactionsfrom) as TextView
        val amount = itemView.findViewById<TextView>(R.id.listtransactionsamount)
        val txdate = itemView.findViewById<TextView>(R.id.listtransactionsdate)
        val to = itemView.findViewById<TextView>(R.id.listtransactionsto)
        val type = itemView.findViewById<TextView>(R.id.listtransactionstype)
    }

    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): TransactionAdapter.TransactionViewHolder {

        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.transaction_row_view, parent, false)

        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        Log.i("TRX", "DatasetSize: ${myDataset.size}")
        val df = DecimalFormat("0.#########")

        holder.from.text = myDataset[position].from
        holder.amount?.text = df.format(BigDecimal(myDataset[position].value).divide(APIService.SEMUXMULTIPLICATOR)) + "SEM"
        holder.to?.text = myDataset[position].to
        holder.type?.text = myDataset[position].type
        holder.txdate?.text = getDateTime(myDataset[position].timestamp)


    }

    override fun getItemCount() = myDataset.size
    private fun getDateTime(date: String): String? {
        try {
            val sdf = SimpleDateFormat("yyyy.MM.dd HH:mm:ss z")
            val netDate = Date(date.toLong())
            return sdf.format(netDate)
        } catch (e: Exception) {
            return e.toString()
        }
    }
}

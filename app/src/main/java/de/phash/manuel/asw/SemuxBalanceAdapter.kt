package de.phash.manuel.asw

import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import de.phash.manuel.asw.semux.APIService
import de.phash.manuel.asw.semux.json.CheckBalance
import java.math.BigDecimal
import java.text.DecimalFormat

class SemuxBalanceAdapter(private val myDataset: ArrayList<CheckBalance>) :
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

        holder.address?.text = myDataset[position].result.address
        holder.available?.text = df.format(BigDecimal(myDataset[position].result.available).divide(APIService.SEMUXMULTIPLICATOR))
        holder.locked?.text = df.format(BigDecimal(myDataset[position].result.locked).divide(APIService.SEMUXMULTIPLICATOR))
        holder.itemView.setOnClickListener(View.OnClickListener {
            val intent = Intent(holder.itemView.context, SingleBalanceActivity::class.java)

            intent.putExtra("address", myDataset[position].result.address)
            intent.putExtra("available", myDataset[position].result.available)
            intent.putExtra("locked", myDataset[position].result.locked)

            holder.itemView.context.startActivity(intent)
            Log.i("CLICK", "clicked: ${myDataset[position].result.address}")
        })
    }
    override fun getItemCount() = myDataset.size
}
package de.phash.manuel.asw

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import de.phash.manuel.asw.semux.json.CheckBalance

class MyAdapter(private val myDataset: ArrayList<CheckBalance>) :
        RecyclerView.Adapter<MyAdapter.MyViewHolder>() {

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder.
    // Each data item is just a string in this case that is shown in a TextView.
    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val address = itemView.findViewById<TextView>(R.id.listbalanceaddress)
        val available = itemView.findViewById<TextView>(R.id.listbalanceavailable)
        val locked = itemView.findViewById<TextView>(R.id.listbalancelocked)
    }


    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): MyAdapter.MyViewHolder {


        // create a new view
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.custom_row_view, parent, false)
        // set the view's size, margins, paddings and layout parameters

        return MyViewHolder(view)
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element

        holder.address?.text = myDataset[position].result.address
        holder.available?.text = myDataset[position].result.available
        holder.locked?.text = myDataset[position].result.locked
    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = myDataset.size
}
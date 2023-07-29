package main.java.com.alpdroid.huGen10.ui

import android.content.ClipData
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.View.DragShadowBuilder
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.alpdroid.huGen10.R


class ParameterAdapter(val parameters: MutableList<Gauge>,    private val context: Context) :
    RecyclerView.Adapter<ParameterAdapter.ParameterViewHolder>() {


    class ParameterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val paramNameTextView: TextView = itemView.findViewById(R.id.list_item)

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParameterViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.telemetrics_parameter, parent, false)
        return ParameterViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ParameterViewHolder, position: Int) {
        val parameter = parameters[position]
        holder.paramNameTextView.text = parameter.id
        holder.itemView.setOnLongClickListener {
            val dragData = ClipData.newPlainText("id", parameter.id)
            val shadowBuilder = DragShadowBuilder(holder.itemView)
            holder.itemView.startDragAndDrop(dragData, shadowBuilder, null, 0)
            return@setOnLongClickListener true
        }
        holder.itemView.setOnClickListener {
            val gauge = parameters.find { it.id == parameter.id }
            gauge?.let {
                val alertDialog = AlertDialog.Builder(holder.itemView.context)
                    .setTitle("Supprimer la jauge")
                    .setMessage("Voulez-vous supprimer cette jauge ?")
                    .setPositiveButton("Oui") { _, _ ->
                        parameters.remove(gauge)
                        notifyDataSetChanged()
                    }
                    .setNegativeButton("Non", null)
                    .create()

                alertDialog.show()
            }
        }
    }

    override fun getItemCount(): Int {
        return parameters.size
    }

}

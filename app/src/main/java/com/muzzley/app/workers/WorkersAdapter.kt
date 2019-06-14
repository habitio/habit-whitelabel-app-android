package com.muzzley.app.workers

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import com.muzzley.R
import com.muzzley.model.workers.Worker
import com.muzzley.util.ui.inflate


class WorkersAdapter(val context: Context, val af: WorkersFragment)
     : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var workers  = mutableListOf<Worker>()

    override
    fun getItemCount(): Int =
        workers.size

    fun getItem(position: Int) =
        workers.get(position)

    override
    fun getItemId(position: Int): Long =
        workers[position].id?.hashCode()?.toLong() ?: 0

    fun setItems(workers: MutableList<Worker>){
        this.workers = workers
        notifyDataSetChanged()
    }

    override
    fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder  {
        return WorkerViewHolder(parent.inflate(R.layout.agent_item),af)
    }

    override
    fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val workerView: Worker = getItem(position)
        (holder as Composer).compose(context, workerView, position)
    }

    interface Composer {
        fun compose(context: Context, worker: Worker, position: Int)
    }



}

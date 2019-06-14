package com.muzzley.util.ui

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.muzzley.app.cards.Container
import java.util.*

class ViewModelAdapter<T : ViewModel>
    @JvmOverloads constructor(
            var context: Context?,
            private val nonDefaultRoots: Map<Int, Int> = LinkedHashMap()
    ) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var data: List<T>? = ArrayList()

    override fun getItemViewType(position: Int): Int {
        return data!![position].layout
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        val view = parent.inflate(viewType)
        return ViewModelHolder(view, nonDefaultRoots[viewType] ?: 0)

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ViewModelHolder).container.setContainerData(data!![position])
    }

    fun setData(data: List<T>) {
        this.data = data
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return if (data == null) 0 else data!!.size
    }

    class ViewModelHolder(itemView: View, rootContainer: Int) : RecyclerView.ViewHolder(itemView) {

        var container: Container<ViewModel> = (if (rootContainer > 0) itemView.findViewById(rootContainer) else itemView) as Container<ViewModel>

//        init {
//            container = (if (rootContainer > 0) itemView.findViewById(rootContainer) else itemView) as Container<ViewModel>
//        }
    }
}

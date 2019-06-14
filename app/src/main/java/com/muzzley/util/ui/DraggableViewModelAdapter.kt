package com.muzzley.util.ui

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import com.h6ah4i.android.widget.advrecyclerview.draggable.DraggableItemAdapter
import com.h6ah4i.android.widget.advrecyclerview.draggable.ItemDraggableRange
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractDraggableItemViewHolder
import com.muzzley.R
import com.muzzley.app.cards.Container
import io.reactivex.subjects.PublishSubject
import timber.log.Timber


class DraggableViewModelAdapter<T : ViewModel>(val context: Context , val nonDefaultRoots:Map<Int,Int>  = mutableMapOf())
        : RecyclerView.Adapter<RecyclerView.ViewHolder>()
        , DraggableItemAdapter<DraggableViewModelAdapter.MyViewHolder> {

//    var context: Context? = null
    private var data = listOf<T>()
//    private var nonDefaultRoots: Map<Int, Int>
    var fromTo: PublishSubject<Pair<Int,Int>>  = PublishSubject.create()

    init {
        setHasStableIds(true)
    }

    override
    fun getItemViewType(position: Int): Int {
        return data[position].layout
    }

    override
    fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        MyViewHolder(parent.inflate(viewType), nonDefaultRoots[viewType] ?: 0)

    override
    fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as MyViewHolder).container.setContainerData(data[position])
    }

    override
    fun getItemId(position: Int): Long =
//        if (data.size <= position) -1 else
            data[position].hashCode().toLong()


//    private fun onBindSectionItemViewHolder(holder: MyViewHolder, position: Int) {
//
//
//        // set background resource (target view ID: container)
//        Int dragState = holder.getDragStateFlags()
//
//        if (((dragState & DraggableItemConstants.STATE_FLAG_IS_UPDATED) != 0)) {
//            Int bgResId
//
//            if ((dragState & DraggableItemConstants.STATE_FLAG_IS_ACTIVE) != 0) {
//                bgResId = R.drawable.bg_item_dragging_active_state
//
//                // need to clear drawable state here to get correct appearance of the dragging item.
//                DrawableUtils.clearState(holder.mContainer.getForeground())
//            } else if (
//            ((dragState & DraggableItemConstants.STATE_FLAG_DRAGGING) != 0) &&
//                    ((dragState & DraggableItemConstants.STATE_FLAG_IS_IN_RANGE) != 0)) {
//                bgResId = R.drawable.bg_item_dragging_state
//            } else {
//                bgResId = R.drawable.bg_item_normal_state
//            }
//
//            holder.mContainer.setBackgroundResource(bgResId)
//        }
//    }


    fun setData(data: List<T>){
        this.data = data
        notifyDataSetChanged()
    }


    override
    fun getItemCount(): Int {
        return data.size
    }

    fun isHeader(position: Int): Boolean {
        val b = getItemViewType(position) == R.layout.section_view //FIXME: use this as parameter to the class
        Timber.d("isHeader: $b")
        return b
    }


    // draggable
    override
    fun onCheckCanStartDrag(holder: MyViewHolder , position: Int , x: Int , y: Int ) : Boolean {
        // x, y --- relative from the itemView"s top-left


//        if (isHeader(position)) { //FIXME: maybe the test should be if != Container ?
//            return false
//        }
        return !isHeader(position)

//        View containerView = holder.mContainer
//        View dragHandleView = holder.mDragHandle
//
//        Int offsetX = containerView.getLeft() + (int) (containerView.getTranslationX() + 0.5f)
//        Int offsetY = containerView.getTop() + (int) (containerView.getTranslationY() + 0.5f)
//
//        return ViewUtils.hitTest(dragHandleView, x - offsetX, y - offsetY)
    }

    override
    fun onGetItemDraggableRange(holder: MyViewHolder, position: Int): ItemDraggableRange {
        val start = findFirstSectionItem(position)
        val end = findLastSectionItem(position)

        return ItemDraggableRange(start, end)
    }

    override
    fun onMoveItem(fromPosition: Int, toPosition: Int) {
        Timber.d("onMoveItem(fromPosition = $fromPosition, toPosition = $toPosition)")
        fromTo.onNext(Pair(fromPosition,toPosition))

//        data.add(toPosition,data.remove(fromPosition))

    }

    override
    fun onCheckCanDrop(draggingPosition: Int, dropPosition: Int): Boolean =
        true

    override
    fun onItemDragStarted(position: Int) {
        notifyDataSetChanged()
    }

    override
    fun onItemDragFinished(fromPosition: Int , toPosition: Int , result: Boolean ) {
        notifyDataSetChanged()
    }

    private fun findFirstSectionItem(position: Int): Int  {
//        AbstractDataProvider.Data item = mProvider.getItem(position)

        var p = position
        if (isHeader(p)) {
            throw IllegalStateException("section item is expected")
        }

        while (p > 0) {
            if (isHeader(p -1)) {
                break
            }
            p -= 1
        }
        return p
    }

    private fun findLastSectionItem(position: Int): Int {
        var p = position
        if (isHeader(p)) {
            throw IllegalStateException("section item is expected")
        }

        val lastIndex = getItemCount() - 1

        while (p < lastIndex) {

            if (isHeader(p + 1)) {
                break
            }

            p += 1
        }

        return p
    }



    // \draggable

    class MyViewHolder(itemView: View, rootContainer: Int) : AbstractDraggableItemViewHolder(itemView) {
//        FrameLayout mContainer
//        View mDragHandle
//        TextView mTextView
        val container = (if(rootContainer > 0) itemView.findViewById(rootContainer) else itemView) as Container<ViewModel>

//        MyViewHolder(itemView: View, rootContainer: Int) {
//            super(itemView)
//            container = (Container<ViewModel>) (rootContainer > 0 ? itemView.findViewById(rootContainer) : itemView)
//
////            mContainer = v.findViewById(R.id.container)
////            mDragHandle = v.findViewById(R.id.drag_handle)
////            mTextView = v.findViewById(android.R.id.text1)
//        }
    }

}

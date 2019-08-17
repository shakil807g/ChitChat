package com.shakil.chitchat.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.shakil.chitchat.R


typealias SearchItemClass = Class<out Any>

typealias SearchItemBinder = SearchItemViewBinder<Any, RecyclerView.ViewHolder>

/** Encapsulates logic to create and bind a ViewHolder for a type of item in the Feed. */
abstract class SearchItemViewBinder<M, in VH : RecyclerView.ViewHolder>(
    val modelClass: Class<out M>
) : DiffUtil.ItemCallback<M>() {

    abstract fun createViewHolder(parent: ViewGroup): RecyclerView.ViewHolder
    abstract fun bindViewHolder(model: M, viewHolder: VH)
    abstract fun getFeedItemType(): Int

    // Having these as non abstract because not all the viewBinders are required to implement them.
    open fun onViewRecycled(viewHolder: VH) = Unit
    open fun onViewDetachedFromWindow(viewHolder: VH) = Unit
}

internal class SearchDiffCallback(
    private val viewBinders: Map<SearchItemClass, SearchItemBinder>
) : DiffUtil.ItemCallback<Any>() {

    override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
        if (oldItem::class != newItem::class) {
            return false
        }
        return viewBinders[oldItem::class.java]?.areItemsTheSame(oldItem, newItem) ?: false
    }

    override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
        // We know the items are the same class because [areItemsTheSame] returned true
        return viewBinders[oldItem::class.java]?.areContentsTheSame(oldItem, newItem) ?: false
    }
}



class SearchAdapter(
    private val viewBinders: Map<SearchItemClass, SearchItemBinder>
): ListAdapter<Any, RecyclerView.ViewHolder>(SearchDiffCallback(viewBinders)) {

    private val viewTypeToBinders = viewBinders.mapKeys { it.value.getFeedItemType() }

    private fun getViewBinder(viewType: Int): SearchItemBinder = viewTypeToBinders.getValue(viewType)

    override fun getItemViewType(position: Int): Int =
        viewBinders.getValue(super.getItem(position).javaClass).getFeedItemType()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return getViewBinder(viewType).createViewHolder(parent)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        return getViewBinder(getItemViewType(position)).bindViewHolder(getItem(position), holder)
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        getViewBinder(holder.itemViewType).onViewRecycled(holder)
        super.onViewRecycled(holder)
    }

    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
        getViewBinder(holder.itemViewType).onViewDetachedFromWindow(holder)
        super.onViewDetachedFromWindow(holder)
    }


}






object LoadingIndicator

class LoadingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

class LoadingViewBinder : SearchItemViewBinder<LoadingIndicator, LoadingViewHolder>(
    LoadingIndicator::class.java
) {

    override fun createViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        return LoadingViewHolder(LayoutInflater.from(parent.context)
            .inflate(getFeedItemType(), parent, false))
    }

    override fun bindViewHolder(model: LoadingIndicator, viewHolder: LoadingViewHolder) {}

    override fun getFeedItemType() = R.layout.item_loading

    override fun areItemsTheSame(oldItem: LoadingIndicator, newItem: LoadingIndicator) = true

    override fun areContentsTheSame(oldItem: LoadingIndicator, newItem: LoadingIndicator) = true
}


object Body

class BodyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

class BodyViewBinder : SearchItemViewBinder<Body, BodyViewHolder>(Body::class.java) {
    override fun createViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        return BodyViewHolder(LayoutInflater.from(parent.context)
            .inflate(getFeedItemType(), parent, false))
    }

    override fun bindViewHolder(model: Body, viewHolder: BodyViewHolder) {}

    override fun getFeedItemType() = R.layout.item_body

    override fun areItemsTheSame(oldItem: Body, newItem: Body) = true

    override fun areContentsTheSame(oldItem: Body, newItem: Body) = true
}





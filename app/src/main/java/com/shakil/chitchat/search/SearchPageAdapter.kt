package com.shakil.chitchat.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.NO_ID
import com.shakil.chitchat.R
import com.shakil.chitchat.extension.ItemBinder
import com.shakil.chitchat.extension.ItemClass
import com.shakil.chitchat.extension.ItemDiffCallback
import com.shakil.chitchat.extension.ItemViewBinder


class SearchAdapter(
    private val viewBinders: Map<ItemClass, ItemBinder>
): ListAdapter<Any, RecyclerView.ViewHolder>(ItemDiffCallback(viewBinders)) {

    init {
        setHasStableIds(true)
    }


    private val viewTypeToBinders = viewBinders.mapKeys { it.value.getFeedItemType() }

    private fun getViewBinder(viewType: Int): ItemBinder = viewTypeToBinders.getValue(viewType)


    override fun getItemId(position: Int): Long {
        return viewBinders.getValue(super.getItem(position).javaClass).getItemId(getItem(position))
    }

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

class LoadingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
    fun bind(loadingind: LoadingIndicator) {
     //   binding.executePendingBindings()
    }
}

class LoadingViewBinder : ItemViewBinder<LoadingIndicator, LoadingViewHolder>(
    LoadingIndicator::class.java
) {

    override fun getItemId(model: LoadingIndicator) = NO_ID


    override fun createViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        return LoadingViewHolder(LayoutInflater.from(parent.context)
            .inflate(getFeedItemType(), parent, false))
    }

    override fun bindViewHolder(model: LoadingIndicator, viewHolder: LoadingViewHolder) {
        viewHolder.bind(model)
    }

    override fun getFeedItemType() = R.layout.item_loading

    override fun areItemsTheSame(oldItem: LoadingIndicator, newItem: LoadingIndicator) = true

    override fun areContentsTheSame(oldItem: LoadingIndicator, newItem: LoadingIndicator) = true
}


data class Body(val data: String)

class BodyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

class BodyViewBinder : ItemViewBinder<Body, BodyViewHolder>(Body::class.java) {

    override fun getItemId(model: Body): Long = model.hashCode().toLong()

    override fun createViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        return BodyViewHolder(LayoutInflater.from(parent.context)
            .inflate(getFeedItemType(), parent, false))
    }

    override fun bindViewHolder(model: Body, viewHolder: BodyViewHolder) {

    }

    override fun getFeedItemType() = R.layout.item_body

    override fun areItemsTheSame(oldItem: Body, newItem: Body) = true

    override fun areContentsTheSame(oldItem: Body, newItem: Body) = true
}





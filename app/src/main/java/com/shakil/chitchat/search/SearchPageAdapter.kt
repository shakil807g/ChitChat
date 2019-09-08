package com.shakil.chitchat.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.NO_ID
import com.shakil.chitchat.R
import com.shakil.chitchat.databinding.ItemHeaderBinding
import com.shakil.chitchat.databinding.ItemMessageWithChainBinding
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




object NoContentItem

class NoContentItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

class NoContentItemViewBinder : ItemViewBinder<NoContentItem, NoContentItemViewHolder>(
    NoContentItem::class.java
) {

    override fun getItemId(model: NoContentItem) = NO_ID

    override fun createViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        return NoContentItemViewHolder(LayoutInflater.from(parent.context)
            .inflate(getFeedItemType(), parent, false))
    }

    override fun bindViewHolder(model: NoContentItem, viewHolder: NoContentItemViewHolder) {
    }

    override fun getFeedItemType() = R.layout.item_no_content

    override fun areItemsTheSame(oldItem: NoContentItem, newItem: NoContentItem) = true

    override fun areContentsTheSame(oldItem: NoContentItem, newItem: NoContentItem) = true
}




object LoadingItem

class LoadingItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

class LoadingItemViewBinder : ItemViewBinder<LoadingItem, LoadingItemViewHolder>(
    LoadingItem::class.java
) {

    override fun getItemId(model: LoadingItem) = NO_ID


    override fun createViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        return LoadingItemViewHolder(LayoutInflater.from(parent.context)
            .inflate(getFeedItemType(), parent, false))
    }

    override fun bindViewHolder(model: LoadingItem, viewHolder: LoadingItemViewHolder) {
    }

    override fun getFeedItemType() = R.layout.item_loading

    override fun areItemsTheSame(oldItem: LoadingItem, newItem: LoadingItem) = true

    override fun areContentsTheSame(oldItem: LoadingItem, newItem: LoadingItem) = true
}



data class HeaderItem(val txt: String)

class HeaderItemViewHolder(private val binding: ItemHeaderBinding) : RecyclerView.ViewHolder(binding.root){
    fun bind(headerItem : HeaderItem){
        binding.headingTxt = headerItem.txt
        binding.executePendingBindings()
    }
}

class HeaderItemViewBinder: ItemViewBinder<HeaderItem, HeaderItemViewHolder>(
    HeaderItem::class.java
) {

    override fun getItemId(model: HeaderItem) = "Header${model.txt}".hashCode().toLong()

    override fun createViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        return HeaderItemViewHolder(ItemHeaderBinding.inflate(
            LayoutInflater.from(parent.context),parent, false))
    }

    override fun bindViewHolder(model: HeaderItem, viewHolder: HeaderItemViewHolder) {
        viewHolder.bind(model)
    }
    override fun getFeedItemType() = R.layout.item_header

    override fun areItemsTheSame(oldItem: HeaderItem, newItem: HeaderItem) = oldItem.txt == newItem.txt

    override fun areContentsTheSame(oldItem: HeaderItem, newItem: HeaderItem) = oldItem.txt == newItem.txt
}







data class MessageItem(val id: String,
                       val name: String,
                       val message: String,
                       val profilePic: String?,
                       val query: String = "")

class MessageItemViewHolder(val binding: ItemMessageWithChainBinding,
                            val mQueryHighlighter: QueryHighlighter) : RecyclerView.ViewHolder(binding.root){

    fun bind(messageItem: MessageItem){
        binding.messageItem = messageItem
        binding.queryHighlighter = mQueryHighlighter
        binding.executePendingBindings()
    }

}





class MessageItemViewBinder(
    private val mQueryHighlighter: QueryHighlighter
                            ) : ItemViewBinder<MessageItem, MessageItemViewHolder>(MessageItem::class.java) {

    override fun getItemId(model: MessageItem): Long = model.id.toLong()

    override fun createViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        return MessageItemViewHolder(ItemMessageWithChainBinding.inflate(
            LayoutInflater.from(parent.context),parent, false),
            mQueryHighlighter
        )
    }

    override fun bindViewHolder(model: MessageItem,
                                viewHolder: MessageItemViewHolder) {
        viewHolder.bind(model)
    }

    override fun getFeedItemType() = R.layout.item_message_with_chain

    override fun areItemsTheSame(oldItem: MessageItem, newItem: MessageItem) = oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: MessageItem, newItem: MessageItem) = oldItem == newItem
}





package com.shakil.chitchat.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.shakil.chitchat.R

class SearchPageAdapter: RecyclerView.Adapter<SearchPageAdapter.SearchPageViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchPageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.view_search_page,parent,false)
        return SearchPageViewHolder(view)
    }

    override fun getItemCount() = 3

    override fun onBindViewHolder(holder: SearchPageViewHolder, position: Int) {

    }

    class SearchPageViewHolder(view: View): RecyclerView.ViewHolder(view) {


    }


}
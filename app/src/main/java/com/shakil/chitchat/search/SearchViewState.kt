package com.shakil.chitchat.search

data class SearchViewState(
    val itemBinders: List<Any> = emptyList(),
    val query: String = "")


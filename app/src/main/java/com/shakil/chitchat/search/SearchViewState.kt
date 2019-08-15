package com.shakil.chitchat.search

import com.shakil.chitchat.extension.SafeMediatorLiveData

data class SearchViewState(val message: List<String> = emptyList(),
                 val contacts: List<String> = emptyList(),
                 val query: String = "")



//fun SafeMediatorLiveData<SearchViewState>.update(
//    message: List<String> = value.message,
//    contacts: List<String> = value.contacts,
//    query: String = value.query
//) {
//    value = value.copy(message = message, contacts = contacts, query = query)
//}
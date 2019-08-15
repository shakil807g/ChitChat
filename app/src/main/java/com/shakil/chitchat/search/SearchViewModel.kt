package com.shakil.chitchat.search

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.shakil.chitchat.Normalizer
import com.shakil.chitchat.extension.SafeMediatorLiveData
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*

class SearchViewModel : ViewModel(){

    private val queryChannel = ConflatedBroadcastChannel<String>()

    val state = SafeMediatorLiveData(initialValue = SearchViewState())


    val messages = listOf(
        "hello how are you",
        "hi what are you doing",
        "hi what are you doing",
        "hi what are you doing",
        "hi what are you doing",
        "hi what are you doing",
        "hi what are you doing",
        "hi what are you doing",
        "hi what are you doing",
        "hi what are you doing",
        "hi what are you doing",
        "hi what are you doing",
        "ok bye!!  off",
        "ak bye!!  off",
        "bk bye!! off",
        "ck bye!! f",
        "dk !!  off",
        "ekk",
        "fk by")


    val contacts = listOf(
        "Ali",
        "Brown",
        "John Doe")


    init {

        queryChannel.asFlow().flatMapLatest { _query ->

            if(_query.isBlank()){
               return@flatMapLatest  flow { emit(state.value.copy(message = emptyList(),contacts = emptyList(),query = _query)) }
            }

            val normalLizeQuery = Normalizer.forSearch(_query)

            Log.d("flatMapLatest:","${kotlin.coroutines.coroutineContext}")
            channelFlow {
                Log.d("channelFlow:","${kotlin.coroutines.coroutineContext}")
                val queryMessages = async {
                    Log.d("queryMessages async:","${kotlin.coroutines.coroutineContext}")
                    //delay(5000)
                    messages.asSequence().filter { s ->
                        val normalizedItem = Normalizer.forSearch(s)
                        normalizedItem.startsWith(normalLizeQuery) ||
                            normalizedItem.contains(" $normalLizeQuery")

                    }.toList()
                }
                val queryContacts = async {
                    Log.d("queryContacts async:","${kotlin.coroutines.coroutineContext}")
                   // delay(2000)
                    contacts.asSequence().filter { s ->
                        val normalizedItem = Normalizer.forSearch(s)
                        normalizedItem.startsWith(normalLizeQuery) ||
                                normalizedItem.contains(" $normalLizeQuery")

                    }.toList()
                }

                send(state.value.copy(message = queryMessages.await(),
                    contacts = queryContacts.await(), query = _query))
//
            }
        }.onEach {
            Log.d("onEach: ${it.query}","${kotlin.coroutines.coroutineContext}")
            state.value = it
        }.launchIn(viewModelScope)


    }


    fun searchQuery(query: String){
        queryChannel.offer(query)
    }



}
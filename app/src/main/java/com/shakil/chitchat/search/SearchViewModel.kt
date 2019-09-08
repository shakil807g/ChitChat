package com.shakil.chitchat.search

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.shakil.chitchat.Database
import com.shakil.chitchat.extension.SafeMediatorLiveData
import com.squareup.sqldelight.android.AndroidSqliteDriver
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*

class SearchViewModel(app: Application) : AndroidViewModel(app) {

    private val queryChannel = ConflatedBroadcastChannel<String>()

    val state = SafeMediatorLiveData(initialValue = SearchViewState())

    val queries by lazy {
        val androidSqlDriver = AndroidSqliteDriver(
            schema = Database.Schema,
            context = app,
            name = "items.db"
        )
        Database(androidSqlDriver).usersEntityQueries
    }


    init {
        //  initDatabase()


//        val sideEffect1: SideEffect<String, Int> = { actions, stateAccessor ->
//            actions.flatMapConcat { action ->
//                println("-- SF1: Got action $action . current state ${stateAccessor()}")
//                if (action < 3)
//                    flowOf(3)
//                else
//                    emptyFlow()
//            }
//        }
//        val sideEffect2: SideEffect<String, Int> = { actions, stateAccessor ->
//            actions.flatMapConcat { action ->
//                println("-- SF2: Got action $action . current state ${stateAccessor()}")
//                if (action < 3)
//                    flowOf(4)
//                else if (action < 4)
//                    flowOf(5)
//                else
//                    emptyFlow()
//            }
//        }
//
//
//            flow {
//                delay(1000)
//                emit(1)
//                delay(4000)
//                emit(2)
//            }
//            .reduxStore(
//                initialStateSupplier = { "Start" },
//                sideEffects = listOf(sideEffect1, sideEffect2)
//            ) { state, action ->
//                state + action
//            }
//            .onEach {
//                println("STATE: $it")
//            }.launchIn(viewModelScope)


        queryChannel.asFlow().flatMapLatest { _query ->

            Log.d("flatMapLatest:", "${kotlin.coroutines.coroutineContext}")

            //val binder = mutableListOf<Any>()

            if (_query.isBlank()) {
                return@flatMapLatest flow {
                    Log.d("_query.isBlank():", "${kotlin.coroutines.coroutineContext}")

                    emit(
                        SearchViewState(
                            itemBinders = listOf(),
                            query = ""
                        )
                    )
                }
            }

            channelFlow {
                Log.d("flow:", "${kotlin.coroutines.coroutineContext}")

                val normalLizeQuery = Normalizer.forSearch(_query)

                val getMessages = async {
                    Log.d("flow:", "getMessages")

                    var listofUsers =
                        queries.selectAll(mapper = { id, _, _, _, message, _, name, profile_pic ->
                            MessageItem(
                                id.toString(),
                                name,
                                message ?: "",
                                profile_pic,
                                normalLizeQuery
                            )
                        }).executeAsList()


                    listofUsers = listofUsers.asSequence().filter { s ->
                        val normalizedItem = Normalizer.forSearch(s.message)
                        normalizedItem.startsWith(normalLizeQuery) ||
                                normalizedItem.contains(" $normalLizeQuery")

                    }.toList()
                    listofUsers
                }

                val allItems = prepateAllData(getMessages.await())


                send(
                    SearchViewState(
                        itemBinders = allItems,

                        query = _query
                    )
                )


            }
        }.onEach {
            Log.d("onEach : ${it.query}", "${kotlin.coroutines.coroutineContext}")
            setState(it)
        }.launchIn(viewModelScope)


        /*queryChannel.asFlow().flatMapLatest { _query ->

            if(_query.isBlank()){
               return@flatMapLatest  flow {
                   emit(state.value.copy(message = emptyList(),contacts = emptyList(),query = _query)) }
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
            Log.d("onEach : ${it.query}","${kotlin.coroutines.coroutineContext}")
            state.value = it
        }.launchIn(viewModelScope)*/


    }

    private fun prepateAllData(messages: List<MessageItem>): MutableList<Any> {
        val viewBinders = mutableListOf<Any>()
        if (messages.isNotEmpty()) {
            viewBinders.add(HeaderItem("Messages"))
            viewBinders.addAll(messages)
        } else {
            viewBinders + NoContentItem
        }
        return viewBinders
    }


    private fun initDatabase() {

        for (i in 1..100) {
            queries.insertUser("shakil$i", "https://picsum.photos/id/$i/1000/1000")
            val lastUserID = queries.findInsertRowid().executeAsOne().toString()
            Log.d("lastUserID", "$lastUserID")
            queries.insertMessage("Hello shakil for the $i time")
            val lastMessageId = queries.findInsertRowid().executeAsOne().toString()
            Log.d("lastMessageId", "$lastMessageId")
            queries.insertConversation(lastMessageId, lastUserID)
        }

        Log.d("ItemDatabase", "${queries.selectAll().executeAsList()}")


    }


    fun searchQuery(query: String) {
        queryChannel.offer(query)
    }


    fun setState(searchState: SearchViewState) {
        state.value = searchState
    }


}
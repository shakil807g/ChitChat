package com.shakil.chitchat.search

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.shakil.chitchat.*
import com.shakil.chitchat.extension.textChanges
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.view_search_page.*
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class MainActivity : AppCompatActivity() {

    val viewModel by lazy { ViewModelProvider(this).get(SearchViewModel::class.java) }


    override fun onStart() {
        super.onStart()
        Log.d("data:","onStart")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        Log.d("data:","onCreate")


        searh_txt.textChanges().debounce(300).onEach {
            Log.d("textChanges:","$it")
            viewModel.searchQuery(it.toString())
        }.launchIn(lifecycleScope)





        viewpager.adapter = object : FragmentStateAdapter(this) {
            override fun createFragment(position: Int): Fragment {
                return CardFragment()
            }

            override fun getItemCount(): Int {
                return 5
            }
        }

        TabLayoutMediator(tabs, viewpager) { tab, position ->
             tab.text = "TAB ${position}"
        }.attach()



      /*  list.withModels {

//            Constants.IMAGES.forEach {
//                body {
//                    id("body$it")
//                    name("$it")
//                    query(currentState.query)
//                    queryHighlighter(mQueryHighlighter)
//                    onClick(View.OnClickListener {
//                        startActivity(Intent(this@MainActivity,SecoundActivity::class.java))
//                        finish()
//
//                    })
//                    pic(it)
//
//                }
//            }

            currentState.message.forEach {
                header {
                    id("$it")
                    name("$it")
                    query(currentState.query)
                    queryHighlighter(mQueryHighlighter)
                }
            }


            currentState.contacts.forEach {
                body {
                    id("body$it")
                    name("$it")
                    query(currentState.query)
                    queryHighlighter(mQueryHighlighter)
                    onClick(View.OnClickListener {
                        startActivity(Intent(this@MainActivity, SecoundActivity::class.java))

                    })
                    pic("https://image.shutterstock.com/image-photo/modern-businessman-business-woman-sitting-600w-210260359.jpg")

                }
            }


        }*/

    }



}


class CardFragment : Fragment() {

    val viewModel by lazy { ViewModelProvider(requireActivity()).get(SearchViewModel::class.java) }
    var currentState = SearchViewState()
    lateinit var mQueryHighlighter: QueryHighlighter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return layoutInflater.inflate(R.layout.view_search_page,container,false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        mQueryHighlighter = QueryHighlighter().
            setQueryNormalizer(QueryHighlighter.QueryNormalizer.FOR_SEARCH)

        val loadingViewBinder = LoadingViewBinder()
        val bodyViewBinder = BodyViewBinder()

        val viewBinders = mutableMapOf<SearchItemClass, SearchItemBinder>().apply {
            put(loadingViewBinder.modelClass,loadingViewBinder as SearchItemBinder)
            put(bodyViewBinder.modelClass,bodyViewBinder as SearchItemBinder)
        }





        list.adapter = SearchAdapter(viewBinders)


        button.setOnClickListener {

            val payloadlist = mutableListOf<Any>()
            payloadlist.add(Body)

            (list.adapter as SearchAdapter).submitList(payloadlist)
        }

        viewModel.state.observe(this, Observer {
            currentState = it

            val payloadlist = mutableListOf<Any>()
            payloadlist.add(LoadingIndicator)
            payloadlist.add(Body)

            (list.adapter as SearchAdapter).submitList(payloadlist)

           // list.requestModelBuild()
        })


//          list.withModels {
//
//
//              currentState.message.forEach {
//                  header {
//                      id("$it")
//                      name("$it")
//                      query(currentState.query)
//                      queryHighlighter(mQueryHighlighter)
//                  }
//              }
//
//
//              currentState.contacts.forEach {
//                  body {
//                      id("body$it")
//                      name("$it")
//                      query(currentState.query)
//                      queryHighlighter(mQueryHighlighter)
//                      onClick(View.OnClickListener {
//                          //startActivity(Intent(this@MainActivity, SecoundActivity::class.java))
//
//                      })
//                      pic("https://image.shutterstock.com/image-photo/modern-businessman-business-woman-sitting-600w-210260359.jpg")
//
//                  }
//              }
//
//
//          }

    }



}




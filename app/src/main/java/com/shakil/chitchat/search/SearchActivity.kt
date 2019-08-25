package com.shakil.chitchat.search

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
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayoutMediator
import com.shakil.chitchat.R
import com.shakil.chitchat.extension.ItemBinder
import com.shakil.chitchat.extension.ItemClass
import com.shakil.chitchat.extension.textChanges
import kotlinx.android.synthetic.main.activity_search.*
import kotlinx.android.synthetic.main.view_search_page.*
import kotlinx.coroutines.flow.*

class SearchActivity : AppCompatActivity() {

    val viewModel by lazy { ViewModelProvider(this).get(SearchViewModel::class.java) }




    override fun onStart() {
        super.onStart()
        Log.d("data:","onStart")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)


        Log.d("data:","onCreate")

      /*  val itemsBefore = queries.selectAll().executeAsList()
        Log.d("ItemDatabase", "Items Before: $itemsBefore")

        for(i in 1..10){
            queries.insertOrReplace(
                id = i.toString(),
                name = "shakil${i}",
                profile_pic = "https://picsum.photos/id/$i/1000/1000"
            )

        }

        val itemsAfter = queries.selectAll().executeAsList()
        Log.d("ItemDatabase", "Items After: $itemsAfter")*/




        searh_txt.textChanges().debounce(200).drop(1).onEach {
            Log.d("textChanges:","$it")
            viewModel.searchQuery(it.toString())
        }.launchIn(lifecycleScope)





        viewpager.adapter = object : FragmentStateAdapter(this) {
            override fun createFragment(position: Int): Fragment {
                return CardFragment().apply {

                }
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

        val loadingViewBinder = LoadingItemViewBinder()
        val messageViewBinder = MessageItemViewBinder(mQueryHighlighter)
        val noContentItemViewBinder = NoContentItemViewBinder()
        val headerItemViewBinder = HeaderItemViewBinder()

        val viewBinders = mutableMapOf<ItemClass, ItemBinder>().apply {
            put(loadingViewBinder.modelClass,loadingViewBinder as ItemBinder)
            put(messageViewBinder.modelClass,messageViewBinder as ItemBinder)
            put(noContentItemViewBinder.modelClass,noContentItemViewBinder as ItemBinder)
            put(headerItemViewBinder.modelClass,headerItemViewBinder as ItemBinder)
        }


        list.adapter = SearchAdapter(viewBinders).apply { setHasStableIds(true) }
        (list.itemAnimator as DefaultItemAnimator).supportsChangeAnimations = false

        viewModel.state.observe(this, Observer { state ->
            currentState = state
            (list.adapter as SearchAdapter).submitList(currentState.itemBinders)
        })


    }



}




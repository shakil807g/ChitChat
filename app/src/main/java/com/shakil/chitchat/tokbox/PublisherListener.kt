package com.shakil.chitchat.tokbox


import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.opentok.android.OpentokError
import com.opentok.android.PublisherKit
import com.opentok.android.Stream
import com.shakil.chitchat.util.Event

class PublisherListener : PublisherKit.PublisherListener, PublisherKit.VideoStatsListener {

    val publisherLiveData = MutableLiveData<Event<PublisherState>>()

    sealed class PublisherState {
        data class StreamCreated(val publisherKit: PublisherKit?, val stream: Stream?) :
            PublisherState()

        data class StreamDestroyed(val publisherKit: PublisherKit?, val stream: Stream?) :
            PublisherState()

        data class onError(val error: String) : PublisherState()
    }


    //PublisherKit.PublisherListener
    override fun onStreamCreated(publisherKit: PublisherKit?, stream: Stream?) {
        Log.d(TAG, "onStreamCreated")
        publisherLiveData.value = Event(PublisherState.StreamCreated(publisherKit, stream))
    }

    override fun onStreamDestroyed(publisherKit: PublisherKit?, stream: Stream?) {
        Log.d(TAG, "onStreamDestroyed")
        publisherLiveData.value = Event(PublisherState.StreamDestroyed(publisherKit, stream))
    }

    override fun onError(publisherKit: PublisherKit?, opentokError: OpentokError?) {
        Log.d(TAG, "onError")
        publisherLiveData.value =
            Event(PublisherState.onError(opentokError?.message ?: "Publisher Error"))
    }
    //PublisherKit.PublisherListener


    companion object {
        private const val TAG = "PublisherListener"

    }

    override fun onVideoStats(
        publisherKit: PublisherKit?,
        publishersVideoStats: Array<out PublisherKit.PublisherVideoStats>?
    ) {

    }
}
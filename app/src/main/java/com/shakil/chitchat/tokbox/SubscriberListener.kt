package com.shakil.chitchat.tokbox

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.opentok.android.OpentokError
import com.opentok.android.SubscriberKit
import com.shakil.chitchat.util.Event


class SubscriberListener : SubscriberKit.VideoListener,SubscriberKit.StreamListener,SubscriberKit.SubscriberListener{

    val sublisherLiveData = MutableLiveData<Event<SubscriberState>>()


    sealed class SubscriberState{
        object  VideoDataReceived: SubscriberState()
        object  VideoEnabled: SubscriberState()
        object  VideoDisabled: SubscriberState()
        object Reconnected: SubscriberState()
        object Disconnected: SubscriberState()
        object Connected: SubscriberState()
        object Error: SubscriberState()
    }

    //SubscriberKit.VideoListener
    override fun onVideoDataReceived(subscriberKit: SubscriberKit?) {
        Log.d(TAG, "onVideoDataReceived")
        sublisherLiveData.value = Event(SubscriberState.VideoDataReceived)
    }

    override fun onVideoEnabled(subscriberKit: SubscriberKit?, p1: String?) {
        Log.d(TAG, "onVideoEnabled")
        sublisherLiveData.value = Event(SubscriberState.VideoEnabled)
    }

    override fun onVideoDisableWarning(subscriberKit: SubscriberKit?) {
        Log.d(TAG, "onVideoDisableWarning")

    }

    override fun onVideoDisableWarningLifted(subscriberKit: SubscriberKit?) {
        Log.d(TAG, "onVideoDisableWarningLifted")
    }

    override fun onVideoDisabled(subscriberKit: SubscriberKit?, p1: String?) {
        Log.d(TAG, "onVideoDisabled")
        sublisherLiveData.value = Event(SubscriberState.VideoDisabled)
    }

    //SubscriberKit.StreamListener
    override fun onReconnected(subscriberKit: SubscriberKit?) {
        sublisherLiveData.value = Event(SubscriberState.Reconnected)
    }

    override fun onDisconnected(subscriberKit: SubscriberKit?) {
        sublisherLiveData.value = Event(SubscriberState.Disconnected)
    }

    //SubscriberKit.SubscriberListener
    override fun onError(subscriberKit: SubscriberKit?, error: OpentokError?) {
        sublisherLiveData.value = Event(SubscriberState.Error)
    }

    override fun onConnected(subscriberKit: SubscriberKit?) {
        sublisherLiveData.value = Event(SubscriberState.Connected)
    }

    companion object {
        private const val TAG = "SubscriberListener"

    }

}
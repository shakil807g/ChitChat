package com.shakil.chitchat.tokbox

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.opentok.android.Connection
import com.opentok.android.OpentokError
import com.opentok.android.Session
import com.opentok.android.Stream
import com.shakil.chitchat.util.Event


class SessionListener : Session.SessionListener,Session.ConnectionListener,Session.ReconnectionListener{


    val sessionLiveData = MutableLiveData<Event<SessionState>>()

    sealed class SessionState{
        object  Connected: SessionState()
        object  Disconnected: SessionState()
        data class onError(val error: String): SessionState()
        object ConnectionDestroyed: SessionState()
        object ConnectionCreated: SessionState()
        data class StreamReceived(val stream: Stream): SessionState()
        data class StreamDropped(val stream: Stream): SessionState()
        object Reconnecting: SessionState()
        object Reconnected: SessionState()

    }


    //Session.ConnectionListener
    override fun onConnectionDestroyed(session: Session?, connection: Connection?) {
        Log.d(TAG, "onConnectionDestroyed")
        sessionLiveData.value = Event(SessionState.ConnectionDestroyed)
    }

    override fun onConnectionCreated(session: Session?, connection: Connection?) {
        Log.d(TAG, "onConnectionCreated")
        sessionLiveData.value = Event(SessionState.ConnectionCreated)
    }
    //Session.ConnectionListener


    //Session.SessionListener
    override fun onConnected(session: Session) {
        Log.d(TAG, "Connected to the session.")
        sessionLiveData.value = Event(SessionState.Connected)
    }

    override fun onDisconnected(session: Session) {
        Log.d(TAG, "Disconnected from the session.")
        sessionLiveData.value = Event(SessionState.Disconnected)
    }

    override fun onError(session: Session, exception: OpentokError) {
        Log.d(TAG, "Session exception: " + exception.message)
        sessionLiveData.value = Event(SessionState.onError(exception.message))
    }

    override fun onStreamReceived(session: Session, stream: Stream) {
        Log.d(TAG, "Stream Received: ")
        sessionLiveData.value = Event(SessionState.StreamReceived(stream))
    }

    override fun onStreamDropped(session: Session, stream: Stream) {
        Log.d(TAG, "Stream Drop: ")
        sessionLiveData.value = Event(SessionState.StreamDropped(stream))
    }
    //Session.SessionListener



    //Session.ReconnectionListener
    override fun onReconnecting(session: Session) {
        Log.d(TAG, "Reconnecting the session " + session.getSessionId())
        sessionLiveData.value = Event(SessionState.Reconnecting)
    }

    override fun onReconnected(session: Session) {
        Log.d(TAG, "Session has been reconnected")
        sessionLiveData.value = Event(SessionState.Reconnected)
    }
    //Session.ReconnectionListener

    companion object {

        private const val TAG = "SessionListener"


    }
}
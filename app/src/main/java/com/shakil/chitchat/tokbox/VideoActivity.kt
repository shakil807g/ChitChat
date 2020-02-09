package com.shakil.chitchat.tokbox

import android.opengl.GLSurfaceView
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.opentok.android.*
import com.shakil.chitchat.R
import com.shakil.chitchat.databinding.ActivityVideoBinding
import com.shakil.chitchat.tokbox.CustomVideoCapturerV2.HD
import com.shakil.chitchat.tokbox.OpenTokConfig.SESSION_ID
import com.shakil.chitchat.tokbox.OpenTokConfig.TOKEN
import com.shakil.chitchat.util.EventObserver
import java.lang.Math.*

class VideoActivity : AppCompatActivity() {
    lateinit var binding: ActivityVideoBinding

    var mSession: Session? = null
    var mPublisher: Publisher? = null
    var mSubscriber: Subscriber? = null
    var mQualityCheckSubscriber: Subscriber? = null

    //Quality variables;
    private var mVideoPLRatio: Double = 0.0;
    private var mVideoBw: Long = 0;


    private var mPrevVideoPacketsLost: Long = 0;
    private var mPrevVideoPacketsRcvd: Long = 0;
    private var mPrevVideoTimestamp: Double = 0.0;
    private var mPrevVideoBytes: Long = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_video)
        createSessions()
    }

    fun createSessions() {
        if (mSession != null) return
        val mSessionListener = SessionListener()
        mSession = Session.Builder(
            applicationContext,
            OpenTokConfig.API_KEY, SESSION_ID
        ).build().apply {
            setSessionListener(mSessionListener)
            setConnectionListener(mSessionListener)
            setReconnectionListener(mSessionListener)
            connect(TOKEN)
        }
        mSessionListener.sessionLiveData.observe(this, EventObserver {
            when (it) {
                SessionListener.SessionState.Connected -> {
                    Toast.makeText(this, "Session Connected", Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "Session Connected")
                    createPublisher()
                }
                SessionListener.SessionState.Disconnected -> {
                    Toast.makeText(this, "Session Disconnected", Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "Session Disconnected")
                    // mSession = null
                }
                is SessionListener.SessionState.onError -> {
                    Toast.makeText(this, "Session onError: ${it.error}", Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "Session onError")
                }
                SessionListener.SessionState.ConnectionDestroyed -> {
                    Toast.makeText(this, "Session ConnectionDestroyed", Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "Session ConnectionDestroyed")
                }
                SessionListener.SessionState.ConnectionCreated -> {
                    Toast.makeText(this, "Session ConnectionCreated", Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "Session ConnectionCreated")
                }
                is SessionListener.SessionState.StreamReceived -> {
                    Toast.makeText(this, "Session StreamReceived", Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "Session StreamReceived")
                    createSubscriber(it.stream)

                }
                is SessionListener.SessionState.StreamDropped -> {
                    Toast.makeText(this, "Session StreamDropped", Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "Session StreamDropped")

                    if (mSubscriber != null) {
                        if (mSubscriber!!.stream == it.stream) {
                            binding.subscriberContainer.removeAllViews()
                            mSubscriber!!.destroy()
                            mSubscriber = null
                        }
                    }
                }
                SessionListener.SessionState.Reconnecting -> {
                    Toast.makeText(this, "Session Reconnecting", Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "Session Reconnecting")
                }
                SessionListener.SessionState.Reconnected -> {
                    Toast.makeText(this, "Session Reconnected", Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "Session Reconnected")
                }
            }
        })
    }


    fun createPublisher() {
        if (mPublisher != null) return
        val mPublisherListener = PublisherListener()
        mPublisher = Publisher.Builder(this@VideoActivity)
            .name("publisher")
            .capturer( CustomVideoCapturer(this@VideoActivity, Publisher.CameraCaptureResolution.MEDIUM, Publisher.CameraCaptureFrameRate.FPS_30))
            //.resolution(Publisher.CameraCaptureResolution.HIGH)
            //.frameRate(Publisher.CameraCaptureFrameRate.FPS_30)
            .build().apply {
                setPublisherListener(mPublisherListener)
                 setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE, BaseVideoRenderer.STYLE_VIDEO_FILL)
                //cycleCamera()
            }

        mPublisher?.let { publisher ->
            binding.publisherContainer.addView(publisher.view)
            if (publisher.view is GLSurfaceView) {
                (publisher.view as GLSurfaceView).setZOrderOnTop(true);
            }
            mSession!!.publish(publisher)
            //mPublisher?.setVideoStatsListener { p0, p1 -> Log.d(TAG, "mPublisher setVideoStatsListener "+p1.toString()) }
        }

        mPublisherListener.publisherLiveData.observe(this, EventObserver {
            when (it) {
                is PublisherListener.PublisherState.StreamCreated -> {
                    Toast.makeText(this, "Publisher StreamCreated", Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "Publisher StreamCreated")
                    it.stream?.let { createVideoAudioQualityCheckSubscriber(it) }

                }
                is PublisherListener.PublisherState.StreamDestroyed -> {
                    Toast.makeText(this, "Publisher StreamDestroyed", Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "Publisher StreamDestroyed")
                }
                is PublisherListener.PublisherState.onError -> {
                    Toast.makeText(this, "Publisher onError ${it.error}", Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "Publisher onError ${it.error}")
                }
            }
        })
    }

    fun createVideoAudioQualityCheckSubscriber(stream: Stream) {
        if (mQualityCheckSubscriber != null) return

        mQualityCheckSubscriber = Subscriber.Builder(this@VideoActivity, stream).build().apply {
            setVideoStatsListener { subscriber: SubscriberKit, stats: SubscriberKit.SubscriberVideoStats ->
                checkVideoStats(subscriber, stats);
            }
        }
        mSession?.subscribe(mQualityCheckSubscriber)
        binding.subscriberContainer.addView(mQualityCheckSubscriber!!.view)
    }

    private fun checkVideoStats(
        subscriber: SubscriberKit,
        stats: SubscriberKit.SubscriberVideoStats
    ) {
        val videoTimestamp = stats.timeStamp / 1000
        if (mPrevVideoTimestamp.compareTo(0) == 0) {
            mPrevVideoTimestamp = videoTimestamp;
            mPrevVideoBytes = stats.videoBytesReceived.toLong();
        }

        if (videoTimestamp - mPrevVideoTimestamp >= TIME_WINDOW) {
            //calculate video packets lost ratio
            if (mPrevVideoPacketsRcvd.compareTo(0) != 0) {
                val pl = stats.videoPacketsLost - mPrevVideoPacketsLost
                val pr = stats.videoPacketsReceived - mPrevVideoPacketsRcvd
                val pt = pl + pr;

                if (pt > 0) {
                    mVideoPLRatio = (pl.toDouble()) / (pt.toDouble())
                }
            }

            mPrevVideoPacketsLost = stats.videoPacketsLost.toLong()
            mPrevVideoPacketsRcvd = stats.videoPacketsReceived.toLong()

            //calculate video bandwidth
            mVideoBw =
                (((8 * (stats.videoBytesReceived - mPrevVideoBytes)) / (videoTimestamp - mPrevVideoTimestamp)).toLong());

            mPrevVideoTimestamp = videoTimestamp;
            mPrevVideoBytes = stats.videoBytesReceived.toLong();

            val targetBitrate = targetBitrateForPixelCount(
                (subscriber.stream.videoHeight * subscriber.stream.videoWidth).toDouble())


            binding?.info.setText("Video bandwidth (bps): $mVideoBw" +
                    "\nVideo Bytes received: ${stats.videoBytesReceived}" +
                    "\nVideo packet lost: ${stats.videoPacketsLost}" +
                    "\nVideo packet loss ratio: $mVideoPLRatio" +
                    "\nVideo Height: ${subscriber.stream.videoHeight}" +
                    "\nVideo Width: ${subscriber.stream.videoWidth}" +
                    "\npreferredFrameRate: ${subscriber.preferredFrameRate}" +
                    "\ntargetBitrate: ${targetBitrate}" +
                    "\nscore: ${(log((mVideoBw / 150000).toDouble()) /
                            log(targetBitrate / 150000)) * 4 + 1}")


            Log.i(TAG, "Video bandwidth (bps): " + mVideoBw + " Video Bytes received: " + stats.videoBytesReceived + " Video packet lost: " + stats.videoPacketsLost + " Video packet loss ratio: " + mVideoPLRatio
            );

        }
    }

    fun targetBitrateForPixelCount(numPixels: Double): Double {
        val y = 2.069924867 * pow(log10(numPixels), 0.6250223771);
        return pow(10.0, y);
    }


    fun createSubscriber(stream: Stream) {
        if (mSubscriber != null) return

        val mSubscriberListener = SubscriberListener()
        mSubscriber = Subscriber.Builder(this@VideoActivity, stream)
            .build().apply {
                setVideoListener(mSubscriberListener)
                setStreamListener(mSubscriberListener)
                setSubscriberListener(mSubscriberListener)
            }
        binding.subscriberContainer.addView(mSubscriber!!.view)
        mSession?.subscribe(mSubscriber)


        mSubscriberListener.sublisherLiveData.observe(this, EventObserver {
            when (it) {
                SubscriberListener.SubscriberState.VideoDataReceived -> {
                    Toast.makeText(this, "Subscriber VideoDataReceived", Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "Subscriber VideoDataReceived")
                }
                SubscriberListener.SubscriberState.VideoEnabled -> {
                    Toast.makeText(this, "Subscriber VideoEnabled", Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "Subscriber VideoEnabled")
                }
                SubscriberListener.SubscriberState.VideoDisabled -> {
                    Toast.makeText(this, "Subscriber VideoDisabled", Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "Subscriber VideoDisabled")
                }
                SubscriberListener.SubscriberState.Reconnected -> {
                    Toast.makeText(this, "Subscriber Reconnected", Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "Subscriber Reconnected")
                }
                SubscriberListener.SubscriberState.Disconnected -> {
                    Toast.makeText(this, "Subscriber Disconnected", Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "Subscriber Disconnected")
                }
                SubscriberListener.SubscriberState.Connected -> {
                    Toast.makeText(this, "Subscriber Connected", Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "Subscriber Connected")
                }
                SubscriberListener.SubscriberState.Error -> {
                    Toast.makeText(this, "Subscriber Error", Toast.LENGTH_SHORT).show()
                    Log.d(TAG, "Subscriber Error")
                }

            }

        })
    }


    override fun onPause() {
        mSession?.onPause()
        super.onPause()
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onResume() {
        super.onResume()
        mSession?.onResume()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    companion object {
        private const val TAG = "VideoActivity"
        private const val TIME_WINDOW = 3; //3 seconds
    }

}

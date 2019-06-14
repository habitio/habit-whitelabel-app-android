package com.muzzley.util.ui.ijk

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.util.AttributeSet
import android.view.WindowManager
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.widget.FrameLayout
import com.muzzley.App
import com.muzzley.R
import com.muzzley.util.FeedbackMessages
import com.muzzley.util.ScreenInspector
import com.muzzley.util.ui.hide
import com.muzzley.util.ui.show
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.activity_video_player.view.*
import timber.log.Timber
import tv.danmaku.ijk.media.player.IMediaPlayer
import tv.danmaku.ijk.media.player.misc.ITrackInfo
import javax.inject.Inject
import javax.inject.Named

class VideoFrame : FrameLayout {

    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes)

    //    @Inject FoscamApi foscamApi
    @Inject @field:Named("io") lateinit var ioScheduler: Scheduler
    @Inject @field:Named("main") lateinit var mainScheduler: Scheduler

//    @InjectView(R.id.videoSurfaceContainer) FrameLayout videoSurfaceContainer
//    @InjectView(R.id.videoLayer) lateinit var videoLayer: RelativeLayout
//    @InjectView(R.id.videoLoading) lateinit var videoLoading: ProgressBar
//    @InjectView(R.id.videoBattery) lateinit var videoBattery: ImageView
//    @InjectView(R.id.videoLive) lateinit var videoLive: View
//    @InjectView(R.id.videoRecording) lateinit var videoRecording: ImageView
//    @InjectView(R.id.videoCounter) lateinit var videoCounter: TextView
//    @InjectView(R.id.videoText) lateinit var videoText: TextView
//    @InjectView(R.id.videoIcon) lateinit var videoIcon: ImageView
//    @InjectView(R.id.videoView) lateinit var videoView: IjkVideoView
//    @InjectView(R.id.fullscreen) lateinit var fullScreenBtn: ImageButton
//    String url
    var playing: Boolean? = null
    val typedUrls: MutableMap<UrlType,String> = mutableMapOf() 
    var cachedHeight: Int = 0
    var cachedWidth: Int = 0
    var cachedHeight2: Int = 0
    var cachedWidth2: Int = 0

//    FoscamApi.FoscamMicThread micThread
    var disposable: Disposable? = null


    var audioInBackground : Boolean= false

    enum class TrackType(val type: Int) {
        audio(ITrackInfo.MEDIA_TRACK_TYPE_AUDIO) , video(ITrackInfo.MEDIA_TRACK_TYPE_VIDEO);

        var enabled: Boolean = false

    }

    enum class PlayerState { idle, preparing, playing}

    var currentState : PlayerState = PlayerState.idle

    enum class UrlType { live, recorded, audio}

    override
    fun  onFinishInflate() {
        super.onFinishInflate()
        App.appComponent.inject(this)
        fullScreenBtn.setOnClickListener{
            toggleFullScreen()
        }
    }

    fun toggleFullScreen() {
        val activity = context as AppCompatActivity
        Timber.d("orientantion: ${getResources().getConfiguration().orientation}")

        val goFullScreen = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT
        val actionBar = activity.getSupportActionBar()
        activity.requestedOrientation =
            if (goFullScreen) {
                actionBar?.hide()

                activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);

                val params = videoView.getLayoutParams()
                cachedHeight = params.height
                cachedWidth = params.width
                Timber.d("1, cachedheight: $cachedHeight, cachedWidth: $cachedWidth, width: ${ScreenInspector.getScreenWidth(getContext())}, height: ${ScreenInspector.getScreenHeight(getContext())}")
                params.height = -1
                params.width = -1
                videoView.setLayoutParams(params)

                val params2 = getLayoutParams()
                cachedHeight2 = params2.height
                cachedWidth2 = params2.width
                params2.height = -1
                params2.width = -1
                setLayoutParams(params2)

                fullScreenBtn.setImageResource(R.drawable.icon_minimize)
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

            } else {
                actionBar?.show()

                activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
                activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
    //            Timber.d("2, cachedheight: $cachedHeight, cachedWidth: $cachedWidth, width: ${ScreenInspector.getScreenWidth(getContext())}, height: ${ScreenInspector.getScreenHeight(getContext())}")

    //            videoView.setMinimumHeight(cachedHeight)
    //            videoView.setMinimumWidth(cachedWidth)

                val params = videoView.getLayoutParams()
                params.height = cachedHeight
                params.width = cachedWidth
                videoView.setLayoutParams(params)

                val params2 = getLayoutParams()
                params2.height = cachedHeight2
                params2.width = cachedWidth2
                setLayoutParams(params2)

                fullScreenBtn.setImageResource(R.drawable.icon_fullscreen)
                ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
//        Timber.d("Orientation: $orientation, i:$i")
        videoView.requestLayout()
        requestLayout()
    }

    private fun setTrack(enable: Boolean, type: Int) {

        val tracks = videoView.getTrackInfo();
        for (n in tracks.indices) {
            val track : ITrackInfo= tracks[n];
            if (track.getTrackType() == type) {
                if (enable) {
                    videoView.selectTrack(n);
                } else {
                    videoView.deselectTrack(n);
                }
            }
        }
    }

    fun  setEnabled(enabled: Boolean, type: TrackType) {

        if (currentState == PlayerState.playing && type.enabled != enabled) {
            type.enabled = enabled
            setTrack(enabled, type.type)
        }
    }

//    fun  audioInBackground(Boolean enabled) {
//        if (audioInBackground != enabled) {
//            audioInBackground = enabled
//            setEnabled(!audioInBackground, VideoFrame.TrackType.video);
//        }
//    }

    fun  showError(text: String) {
        showInfo(text)
    }

    fun  showLoading(text: String? = null) {
        arrayOf(videoLayer, videoLoading, videoText).forEach { it.setVisibility(VISIBLE) }
        videoIcon.setVisibility(GONE)
        videoText.setText(text);
    }
    fun  showInfo(text: String?=null) {
        arrayOf(videoLayer, videoIcon, videoText).forEach { it.setVisibility(VISIBLE) }
        videoLoading.setVisibility(GONE)
        videoText.setText(text);
    }
    fun  showNoLayer(){
        videoLayer.setVisibility(GONE)
    }

    fun release() {
        try {
            TrackType.audio.enabled = false;
            TrackType.video.enabled = false;
            currentState = PlayerState.idle
            videoView.stopBackgroundPlay()
            videoView.stopPlayback()
        } catch (throwable: Throwable) {
            Timber.e(throwable,"Error releasing")
        }

    }

    fun  setUrl(type: UrlType, url: String) {
        typedUrls[type] = url
    }

    fun  play(type: UrlType) {
        typedUrls[type]?.let {
            when (type) {
                UrlType.live -> { // show live icon
                    showLoading(context.getString(R.string.mobile_buffering_live))
                    videoLive.show()
                }
                else -> { //remove icons
                    showLoading(context.getString(R.string.mobile_buffering))
                    videoLive.hide()
                }
            }
            setVideoPath(it)
        } ?: Timber.e("Could not find $type url")
    }

    fun  setAndPlay(type: UrlType, url: String){
        setUrl(type,url)
        play(type)
    }

    fun  setVideoPath(url: String, audioInBackground : Boolean= false) {
        this.audioInBackground = audioInBackground
        Timber.d("audioInBackground: $audioInBackground, this.audioInBackground: ${this.audioInBackground}")
        Timber.d("isPlaying: ${videoView.isPlaying()}")
        if (videoView.isPlaying()) {
            release()
        }
        currentState = PlayerState.preparing
        videoView.setOnErrorListener { _: IMediaPlayer, i: Int, i1: Int ->
                Timber.d("onError i:$i, i1:$i1");
                showInfo("Error: i:$i, i1:$i1")
                release()
                true;
        };
        videoView.setOnPreparedListener{
                Timber.d("onPrepared");
                Timber.d("audioInBackground: $audioInBackground, this.audioInBackground: $audioInBackground")
                if (audioInBackground) {
                    showInfo("Audio only")
                    Timber.d("showed audio only")
                } else {
                    showNoLayer()
                    Timber.d("showed no layer")
                }
                TrackType.audio.enabled = true
                TrackType.video.enabled = true
                videoView.start()
                currentState = PlayerState.playing
        }
        videoView.setOnCompletionListener{
                Timber.d("onCompletion");
                release()
                if (typedUrls[UrlType.live] != null) {
                    play(UrlType.live)
                } else {
                    showInfo("Completed")
                }
            }
        videoView.setOnInfoListener{_: IMediaPlayer, i: Int, i1: Int ->
                Timber.d("onInfo i:$i, i1:$i1");
                if (i == 701) {
                    showInfo("Info: i:$i, i1:$i1")
//                    release()
                }
                true
            }

        videoView.setVideoPath(url)

    }

    override
    fun onWindowFocusChanged(hasWindowFocus: Boolean) {
        Timber.d("hasFocus: "+hasWindowFocus)
        super.onWindowFocusChanged(hasWindowFocus)
        if (!hasWindowFocus) {
            if (!audioInBackground) {
                release()
            } else {
                videoView.enterBackground()
            }
            setMicrophone(false)
//            micThread?.interrupt()
//            micThread = null
        }
    }

    private fun  setFullScreen(fullScreen: Boolean) {
        Timber.d("FullScreen: $fullScreen");

        ( getContext() as Activity).requestedOrientation =
                if (fullScreen)
                    ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                else
                    ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

    }

    private fun  startPulseAnimation() {
        videoBattery.startAnimation(
                AlphaAnimation(1F, 0.4F).apply {
                    duration = 500;
                    interpolator = LinearInterpolator();
                    repeatCount = Animation.INFINITE;
                    repeatMode = Animation.REVERSE;
                }
        )
    }

    private fun  stopPulseAnimation() {
        videoBattery.clearAnimation()
    }


    fun  setComponentSize(componentSize: String) {
        val  videoComponentPreferredHeight = ScreenInspector.getScreenWidth(context) / componentSize.toFloat()
        Timber.d("videoComponentPreferredHeight = $videoComponentPreferredHeight")
        val layoutParams = videoView.layoutParams;
//        Timber.d("layoutParams.class: " + layoutParams.getClass().getCanonicalName());
        layoutParams.height = videoComponentPreferredHeight.toInt();
        videoView.layoutParams = layoutParams;
    }

    fun  setMicrophone(active: Boolean) {
        if (typedUrls[UrlType.live] != null && active) {
//            val uri = Uri.parse(typedUrls[UrlType.live])
//            val userInfo = uri.userInfo.split(':')
//            val user = userInfo[0]
//            val pass = userInfo[1]

            disposable = RxPermissions(context as Activity)
                    .request(Manifest.permission.RECORD_AUDIO)
                    .observeOn(ioScheduler)
                    .flatMap {
//                        foscamApi.aaa(user, pass, uri.host, uri.port)
                        Observable.error<Boolean>(RuntimeException("Not implemented yet. Must call camera SDK"))
                    }
                    .observeOn(mainScheduler)
                    .subscribe(
                            {
                                Timber.i("foscam mic read: $it")
                            },
                            {
                                Timber.e(it, "Error setting microphone")
//                                FeedbackMessages.showMessage(videoLayer, it instanceof FoscamApi.TooManyUsersException ?
//                                        R.string.mobile_error_max_users : R.string.mobile_error_text)
                                FeedbackMessages.showMessage(videoLayer, R.string.mobile_error_text)
                            }
                    )

        } else {
            disposable?.dispose()
            disposable = null
        }
    }
}
package com.chunter.simplevideoplayer

import android.content.ComponentName
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.pip.PictureInPictureDelegate
import androidx.core.pip.VideoPlaybackPictureInPicture
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.cast.MediaRouteButtonFactory
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.MediaMetadata.MEDIA_TYPE_VIDEO
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.media3.ui.PlayerView
import androidx.mediarouter.app.MediaRouteButton
import com.google.android.material.snackbar.Snackbar
import com.google.common.util.concurrent.FutureCallback
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import java.util.concurrent.Executor

@OptIn(UnstableApi::class)
class VideoPlayerActivity : AppCompatActivity() {

    private val root: ConstraintLayout
        get() = findViewById(R.id.main)

    private val playerView: PlayerView
        get() = findViewById(R.id.player)

    private val castButton: MediaRouteButton
        get() = findViewById(R.id.castButton)

    private val executor: Executor
        get() = ContextCompat.getMainExecutor(this)

    private val pictureInPictureListener =
        object : PictureInPictureDelegate.OnPictureInPictureEventListener {
            override fun onPictureInPictureEvent(
                event: PictureInPictureDelegate.Event,
                config: Configuration?
            ) {
                if (event == PictureInPictureDelegate.Event.ENTER_ANIMATION_START) {
                    playerView.hideController()
                }

                if (event == PictureInPictureDelegate.Event.EXITED) {
                    playerView.showController()
                }
            }
        }
    private val controllerVisibilityListener =
        PlayerView.ControllerVisibilityListener { visibility ->
            castButton.visibility = visibility
        }

    private lateinit var videoPlaybackPip: VideoPlaybackPictureInPicture

    private var mediaControllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_video_player)
    }

    override fun onStart() {
        super.onStart()

        mediaControllerFuture = MediaController.Builder(
            this,
            SessionToken(this, ComponentName(this, MediaService::class.java))
        ).buildAsync()

        Futures.addCallback<MediaController>(
            mediaControllerFuture!!,
            object : FutureCallback<MediaController> {
                override fun onSuccess(result: MediaController?) {
                    if (result == null) {
                        displayErrorSnackbar()
                        return
                    }

                    mediaController = result
                    playerView.player = result

                    if (result.currentMediaItem != null) return

                    val metadata = MediaMetadata.Builder()
                        .setTitle(TITLE)
                        .setSubtitle(SUBTITLE)
                        .setMediaType(MEDIA_TYPE_VIDEO)
                        .setArtworkUri(THUMBNAIL.toUri())
                        .setDurationMs(635000L)
                        .build()
                    val mediaItem = MediaItem.Builder()
                        .setUri(VIDEO_URL)
                        .setMediaMetadata(metadata)
                        .build()

                    result.setMediaItem(mediaItem)
                    result.prepare()
                    result.play()
                }

                override fun onFailure(t: Throwable) {
                    displayErrorSnackbar()
                }

                private fun displayErrorSnackbar() {
                    Snackbar.make(
                        this@VideoPlayerActivity,
                        root,
                        "Failed to connect to media controller",
                        Snackbar.LENGTH_LONG
                    ).show()
                }
            },
            executor,
        )

        MediaRouteButtonFactory.setUpMediaRouteButton(this, castButton)

        playerView.setControllerVisibilityListener(controllerVisibilityListener)

        videoPlaybackPip = VideoPlaybackPictureInPicture(this)
        videoPlaybackPip.setPlayerView(playerView)
        videoPlaybackPip.addOnPictureInPictureEventListener(executor, pictureInPictureListener)
    }

    override fun onStop() {
        super.onStop()
        // On entering Picture-in-Picture mode, onPause is called, but not onStop.
        // For this reason, this is the place where we should pause the video playback.
        mediaControllerFuture?.let {
            MediaController.releaseFuture(it)
            mediaController = null
        }
        mediaControllerFuture = null
        videoPlaybackPip.removeOnPictureInPictureEventListener(pictureInPictureListener)
    }

    override fun onRestart() {
        super.onRestart()
        if (!isInPictureInPictureMode) {
            // Show the video controls so the video can be easily resumed.
            playerView.showController()
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        adjustFullScreen(newConfig)

        val constraintSet = ConstraintSet()
        constraintSet.clone(root)
        constraintSet.connect(
            R.id.player,
            ConstraintSet.BOTTOM,
            if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) PARENT_ID else R.id.videoHeightGuide,
            ConstraintSet.BOTTOM
        )
        constraintSet.applyTo(root)
    }


    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            adjustFullScreen(resources.configuration)
        }
    }

    /**
     * Adjusts immersive full-screen flags depending on the screen orientation.
     * @param config The current [Configuration].
     */
    private fun adjustFullScreen(config: Configuration) {
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            insetsController.hide(WindowInsetsCompat.Type.systemBars())
        } else {
            insetsController.show(WindowInsetsCompat.Type.systemBars())
        }
    }

    companion object {

        private const val VIDEO_URL = "https://dash.akamaized.net/akamai/bbb_30fps/bbb_30fps.mpd"
        private const val TITLE = "Big Buck Bunny"
        private const val SUBTITLE = "A video about a big bunny called Buck?"
        private const val THUMBNAIL =
            "https://onlinechannel.s3.us-central-1.wasabisys.com/wasabithird/images/stream/poster/BigBuckBunny_2024-04-30_03-19-59.webp"
    }
}
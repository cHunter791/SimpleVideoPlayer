package com.chunter.simplevideoplayer

import android.content.ComponentName
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.core.pip.VideoPlaybackPictureInPicture
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.cast.MediaRouteButtonViewProvider
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.MediaMetadata.MEDIA_TYPE_VIDEO
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.media3.ui.PlayerView
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors

@OptIn(UnstableApi::class)
class VideoPlayerActivity : AppCompatActivity() {

    private val playerView: PlayerView
        get() = findViewById(R.id.player)

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

        mediaControllerFuture?.addListener({
            mediaController = mediaControllerFuture?.get()
            playerView.player = mediaController

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

            mediaController?.setMediaItem(mediaItem)
            mediaController?.prepare()
            mediaController?.play()
        }, MoreExecutors.directExecutor())

        playerView.setMediaRouteButtonViewProvider(MediaRouteButtonViewProvider())

        videoPlaybackPip = VideoPlaybackPictureInPicture(this)
        videoPlaybackPip.setPlayerView(playerView)
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
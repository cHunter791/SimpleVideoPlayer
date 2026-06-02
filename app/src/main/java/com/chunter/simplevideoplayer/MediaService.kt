package com.chunter.simplevideoplayer

import android.content.Intent
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.cast.CastPlayer
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.google.android.gms.cast.SessionState
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.cast.framework.CastSession
import com.google.android.gms.cast.framework.SessionManagerListener
import com.google.android.gms.cast.framework.SessionTransferCallback

@OptIn(UnstableApi::class)
class MediaService : MediaSessionService(), SessionManagerListener<CastSession> {

    private var mediaSession: MediaSession? = null
    private var castContext: CastContext? = null

    private lateinit var exoPlayer: Player
    private lateinit var castPlayer: Player

    private val sessionTransferCallback = object : SessionTransferCallback() {
        override fun onTransferring(transferType: Int) {
            super.onTransferring(transferType)
            Log.d(TAG, "onTransferring: $transferType")
        }

        override fun onTransferred(transferType: Int, sessionState: SessionState) {
            super.onTransferred(transferType, sessionState)
            Log.d(TAG, "onTransferred: $transferType")
        }

        override fun onTransferFailed(transferType: Int, reason: Int) {
            super.onTransferFailed(transferType, reason)
            Log.d(TAG, "onTransferFailed: $transferType $reason")
        }
    }

    /**
     * This method is called when the service is being created.
     * It initializes the ExoPlayer and MediaSession instances.
     */
    override fun onCreate() {
        super.onCreate()

        setMediaNotificationProvider(CustomNotificationProvider(this))

        exoPlayer = ExoPlayer.Builder(this).build()
        castPlayer = CastPlayer.Builder(this).setLocalPlayer(exoPlayer).build()
        mediaSession = MediaSession.Builder(this, castPlayer).build()

        castContext = CastContext.getSharedInstance(this)
        castContext?.sessionManager
            ?.addSessionManagerListener(this, CastSession::class.java)
        castContext?.addSessionTransferCallback(sessionTransferCallback)
    }

    /**
     * This method is called when the system determines that the service is no longer used and is being removed.
     * It checks the player's state and if the player is not ready to play or there are no items in the media queue, it stops the service.
     *
     * @param rootIntent The original root Intent that was used to launch the task that is being removed.
     */
    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = mediaSession?.player ?: return

        // Check if the player is not ready to play or there are no items in the media queue
        if (!player.playWhenReady || player.mediaItemCount == 0) {
            // Stop the service
            stopSelf()
        }
    }

    /**
     * This method is called when a MediaSession.ControllerInfo requests the MediaSession.
     * It returns the current MediaSession instance.
     *
     * @param controllerInfo The MediaSession.ControllerInfo that is requesting the MediaSession.
     * @return The current MediaSession instance.
     */
    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onUpdateNotification(session: MediaSession, startInForegroundRequired: Boolean) {
        super.onUpdateNotification(session, startInForegroundRequired)
        Log.d(TAG, "onUpdateNotification")
    }

    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }

        castContext?.run {
            sessionManager.removeSessionManagerListener(this@MediaService, CastSession::class.java)
            removeSessionTransferCallback(sessionTransferCallback)
            castContext = null
        }

        super.onDestroy()
    }

    override fun onSessionEnded(
        session: CastSession,
        error: Int
    ) {
        Log.d(TAG, "onSessionEnded: $error")
    }

    override fun onSessionEnding(session: CastSession) {
        Log.d(TAG, "onSessionEnding")
    }

    override fun onSessionResumeFailed(
        session: CastSession,
        error: Int
    ) {
        Log.d(TAG, "onSessionResumeFailed: $error")
    }

    override fun onSessionResumed(
        session: CastSession,
        wasSuspended: Boolean
    ) {
        Log.d(TAG, "onSessionResumed: $wasSuspended")
    }

    override fun onSessionResuming(
        session: CastSession,
        sessionId: String
    ) {
        Log.d(TAG, "onSessionResuming: $sessionId")
    }

    override fun onSessionStartFailed(
        session: CastSession,
        error: Int
    ) {
        Log.d(TAG, "onSessionStartFailed: $error")
    }

    override fun onSessionStarted(
        session: CastSession,
        sessionId: String
    ) {
        Log.d(TAG, "onSessionStarted: $sessionId")
    }

    override fun onSessionStarting(session: CastSession) {
        Log.d(TAG, "onSessionStarting")
    }

    override fun onSessionSuspended(
        session: CastSession,
        reason: Int
    ) {
        Log.d(TAG, "onSessionSuspended: $reason")
    }

    companion object {

        private const val TAG = "MediaService"
    }
}
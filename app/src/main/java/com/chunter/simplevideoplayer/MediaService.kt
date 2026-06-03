package com.chunter.simplevideoplayer

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.cast.CastPlayer
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.google.android.gms.cast.SessionState
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.cast.framework.CastSession
import com.google.android.gms.cast.framework.SessionManagerListener
import com.google.android.gms.cast.framework.SessionTransferCallback
import com.google.common.util.concurrent.ListenableFuture

@OptIn(UnstableApi::class)
class MediaService : MediaSessionService(), SessionManagerListener<CastSession>,
    MediaSession.Callback {

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
        mediaSession = MediaSession.Builder(this, castPlayer)
            .setCallback(this)
            .build()

        castContext = CastContext.getSharedInstance(this)
        castContext?.sessionManager
            ?.addSessionManagerListener(this, CastSession::class.java)
        castContext?.addSessionTransferCallback(sessionTransferCallback)
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

    override fun onConnect(
        session: MediaSession,
        controller: MediaSession.ControllerInfo
    ): MediaSession.ConnectionResult {
        Log.d(TAG, "onConnect")
        return super.onConnect(session, controller)
    }

    override fun onPostConnect(
        session: MediaSession,
        controller: MediaSession.ControllerInfo
    ) {
        super.onPostConnect(session, controller)
        Log.d(TAG, "onPostConnect")
    }

    override fun onDisconnected(
        session: MediaSession,
        controller: MediaSession.ControllerInfo
    ) {
        super.onDisconnected(session, controller)
        Log.d(TAG, "onDisconnected")
    }

    override fun onCustomCommand(
        session: MediaSession,
        controller: MediaSession.ControllerInfo,
        customCommand: SessionCommand,
        args: Bundle
    ): ListenableFuture<SessionResult> {
        Log.d(TAG, "onCustomCommand")
        return super.onCustomCommand(session, controller, customCommand, args)
    }

    override fun onCustomCommand(
        session: MediaSession,
        controller: MediaSession.ControllerInfo,
        customCommand: SessionCommand,
        args: Bundle,
        progressReporter: MediaSession.ProgressReporter?
    ): ListenableFuture<SessionResult> {
        Log.d(TAG, "onCustomCommand")
        return super.onCustomCommand(session, controller, customCommand, args, progressReporter)
    }

    override fun onAddMediaItems(
        mediaSession: MediaSession,
        controller: MediaSession.ControllerInfo,
        mediaItems: List<MediaItem>
    ): ListenableFuture<List<MediaItem>> {
        Log.d(TAG, "onAddMediaItems")
        return super.onAddMediaItems(mediaSession, controller, mediaItems)
    }

    override fun onSetMediaItems(
        mediaSession: MediaSession,
        controller: MediaSession.ControllerInfo,
        mediaItems: List<MediaItem>,
        startIndex: Int,
        startPositionMs: Long
    ): ListenableFuture<MediaSession.MediaItemsWithStartPosition> {
        Log.d(TAG, "onSetMediaItems")
        return super.onSetMediaItems(
            mediaSession,
            controller,
            mediaItems,
            startIndex,
            startPositionMs
        )
    }

    override fun onPlaybackResumption(
        mediaSession: MediaSession,
        controller: MediaSession.ControllerInfo,
        isForPlayback: Boolean
    ): ListenableFuture<MediaSession.MediaItemsWithStartPosition> {
        Log.d(TAG, "onPlaybackResumption")
        return super.onPlaybackResumption(mediaSession, controller, isForPlayback)
    }

    override fun onMediaButtonEvent(
        session: MediaSession,
        controllerInfo: MediaSession.ControllerInfo,
        intent: Intent
    ): Boolean {
        Log.d(TAG, "onMediaButtonEvent")
        return super.onMediaButtonEvent(session, controllerInfo, intent)
    }

    override fun onPlayerInteractionFinished(
        session: MediaSession,
        controllerInfo: MediaSession.ControllerInfo,
        playerCommands: Player.Commands
    ) {
        super.onPlayerInteractionFinished(session, controllerInfo, playerCommands)
        Log.d(TAG, "onPlayerInteractionFinished")
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
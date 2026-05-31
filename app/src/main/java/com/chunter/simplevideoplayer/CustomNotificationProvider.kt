package com.chunter.simplevideoplayer

import android.app.Notification
import android.content.Context
import android.os.Bundle
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.CommandButton
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaNotification
import androidx.media3.session.MediaSession
import com.google.common.collect.ImmutableList

@UnstableApi
class CustomNotificationProvider(private val context: Context) : MediaNotification.Provider {

    private val defaultProvider = DefaultMediaNotificationProvider(context)

    init {
        defaultProvider.setSmallIcon(R.drawable.ic_launcher_foreground)
    }

    override fun getNotificationChannelInfo(): MediaNotification.Provider.NotificationChannelInfo {
        return defaultProvider.notificationChannelInfo
    }

    override fun createNotification(
        mediaSession: MediaSession,
        customLayout: ImmutableList<CommandButton>,
        actionFactory: MediaNotification.ActionFactory,
        onNotificationChangedCallback: MediaNotification.Provider.Callback
    ): MediaNotification {
        val mediaNotification = defaultProvider.createNotification(
            mediaSession, customLayout, actionFactory, onNotificationChangedCallback
        )

        val player = mediaSession.player

        // Rebuild using NotificationCompat.Builder, copying channel from default
        val notification = Notification.Builder(
            context,
            mediaNotification.notification.channelId
        )
            .setStyle(
                Notification.MediaStyle()
                    .setMediaSession(mediaSession.platformToken)
                    .setShowActionsInCompactView(0, 1, 2)
            )
            .setSmallIcon(androidx.media3.session.R.drawable.media3_icon_play)
            .setContentTitle(player.mediaMetadata.title)
            .setContentText(player.mediaMetadata.subtitle)
            .setSubText(context.getString(R.string.app_name))
            .setOngoing(player.isPlaying)
            .apply {
                mediaNotification.notification.actions?.forEach { action -> addAction(action) }
            }
            .build()

        return MediaNotification(mediaNotification.notificationId, notification)
    }

    override fun handleCustomCommand(
        session: MediaSession,
        action: String,
        extras: Bundle
    ): Boolean = false
}
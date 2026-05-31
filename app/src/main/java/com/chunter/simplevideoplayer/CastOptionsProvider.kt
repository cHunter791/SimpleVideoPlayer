package com.chunter.simplevideoplayer

import android.content.Context
import com.google.android.gms.cast.CastMediaControlIntent
import com.google.android.gms.cast.LaunchOptions
import com.google.android.gms.cast.framework.CastOptions
import com.google.android.gms.cast.framework.OptionsProvider
import com.google.android.gms.cast.framework.SessionProvider
import com.google.android.gms.cast.framework.media.CastMediaOptions
import com.google.android.gms.cast.framework.media.NotificationOptions

class CastOptionsProvider : OptionsProvider {

    override fun getCastOptions(context: Context): CastOptions {
        val notificationOptions = NotificationOptions.Builder()
            .build()
        val castMediaOptions = CastMediaOptions.Builder()
            .setNotificationOptions(notificationOptions)
            .build()
        val launcherOptions = LaunchOptions.Builder()
            .build()
        return CastOptions.Builder()
            .setReceiverApplicationId(CastMediaControlIntent.DEFAULT_MEDIA_RECEIVER_APPLICATION_ID)
            .setRemoteToLocalEnabled(true)
            .setCastMediaOptions(castMediaOptions)
            .setLaunchOptions(launcherOptions)
            .setShowSystemOutputSwitcherOnCastIconClick(true)
            .build()
    }

    override fun getAdditionalSessionProviders(context: Context) = emptyList<SessionProvider>()
}
# Introduction

A simple, 2 screen app to mess about with media3 and video playback.

# Features

- Video playback
- Casting
- Picture in picture

# Android 17 Support

In Android 17 the permission `ACCESS_LOCAL_NETWORK` that was added as part of Android 16 is now [mandatory and enforced in apps that target Android 17](https://developer.android.com/about/versions/17/behavior-changes-17#local-network-protection-permission). This new permission will now prevent casting from finding devices to cast to unless this permission is granted. [Google guidelines](https://developer.android.com/privacy-and-security/local-network-permission#path-a) recommend using the Output Switcher instead so that you do not need to explicitly request permission from the user.

The [Output Switcher](https://developers.google.cn/cast/docs/android_sender/output_switcher) is a Cast SDK feature that works with the media notification. After *a lot* of searching and trial and error we found how to activate the Output Switcher feature with the media3 `MediaRouteButton`. 

You will need a custom `OptionsProvider` and need to set the `setShowSystemOutputSwitcherOnCastIconClick` option to `true`.

```
CastOptions.Builder()
            .setReceiverApplicationId(CastMediaControlIntent.DEFAULT_MEDIA_RECEIVER_APPLICATION_ID)
            .setRemoteToLocalEnabled(true)
            .setCastMediaOptions(castMediaOptions)
            .setLaunchOptions(launcherOptions)
            .setShowSystemOutputSwitcherOnCastIconClick(true)
            .build()
```

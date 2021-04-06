package tv.superawesome.sdk.publisher.common.repositories

import android.webkit.WebView
import android.widget.VideoView
import tv.superawesome.sdk.publisher.common.models.AdResponse

interface MoatRepositoryType {
    fun startMoatTrackingForDisplay(webView: WebView, adResponse: AdResponse): String = ""

    fun stopMoatTrackingForDisplay(): Boolean = false

    fun startMoatTrackingForVideoPlayer(
        videoView: VideoView?,
        duration: Int,
        adResponse: AdResponse
    ): Boolean = false

    fun sendPlayingEvent(position: Int): Boolean = false

    fun sendStartEvent(position: Int): Boolean = false

    fun sendFirstQuartileEvent(position: Int): Boolean = false

    fun sendMidpointEvent(position: Int): Boolean = false

    fun sendThirdQuartileEvent(position: Int): Boolean = false

    fun sendCompleteEvent(position: Int): Boolean = false

    fun stopMoatTrackingForVideoPlayer(): Boolean = false
}
package tv.superawesome.sdk.publisher.ui.banner

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.util.Log
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import org.koin.core.inject
import tv.superawesome.sdk.publisher.common.components.ImageProviderType
import tv.superawesome.sdk.publisher.common.di.Injectable
import tv.superawesome.sdk.publisher.common.extensions.toPx
import tv.superawesome.sdk.publisher.common.models.AdRequest
import tv.superawesome.sdk.publisher.common.models.SAInterface
import tv.superawesome.sdk.publisher.ui.common.AdControllerType

private val BANNER_BACKGROUND = Color.rgb(224, 224, 224)

class BannerView : FrameLayout, Injectable {
    private val controller: AdControllerType by inject()
    private val imageProvider: ImageProviderType by inject()

    private var webView: WebView? = null
    private var padlockButton: ImageButton? = null

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        Log.i("gunhan", "BannerView.constructor()")
    }

    /**
     * One of the main public methods of the SABannerAd class. This will load a new SAAd object
     * corresponding to a given placement Id.
     *
     * @param placementId Awesome Ads ID for ad data to be loaded
     */
    fun load(placementId: Int) {
        Log.i("gunhan", "BannerView.load(${placementId})")
        controller.load(placementId, makeAdRequest())
    }

    /**
     * One of the main public methods of the SABannerAd class. This will play an already existing
     * loaded ad, or fail.
     *
     * @param context current context (activity or fragment)
     */
    fun play() {
        Log.i("gunhan", "BannerView.play() thread:${Thread.currentThread()}")
        val dataPair = controller.getDataPair()

        if (dataPair == null) {
            controller.adFailedToShow()
            return
        }

        controller.triggerImpressionEvent()

        addWebView()
        showPadlockIfNeeded()
        webView?.loadHTML(dataPair.first, dataPair.second)

    }

    fun setListener(delegate: SAInterface) {
        Log.i("gunhan", "setListener")
        controller.delegate = delegate
    }

    /**
     * Method that gets called in order to close the banner ad, remove any fragments, etc
     */
    fun close() {
        removeWebView()
        controller.close()
    }

    /**
     * Method that determines if an ad is available
     *
     * @return true or false
     */
    fun hasAdAvailable(): Boolean = controller.adAvailable

    fun isClosed(): Boolean = controller.closed

    fun enableParentalGate() {
        setParentalGate(true)
    }

    fun disableParentalGate() {
        setParentalGate(false)
    }

    fun enableBumperPage() {
        setBumperPage(true)
    }

    fun disableBumperPage() {
        setBumperPage(false)
    }

    fun enableTestMode() {
        setTestMode(true)
    }

    fun disableTestMode() {
        setTestMode(false)
    }

    fun setColorTransparent() {
        setColor(true)
    }

    fun setColorGray() {
        setColor(false)
    }

    fun setParentalGate(value: Boolean) {
        controller.parentalGateEnabled = value
    }

    fun setBumperPage(value: Boolean) {
        controller.bumperPageEnabled = value
    }

    fun setTestMode(value: Boolean) {
        controller.testEnabled = value
    }

    fun setColor(value: Boolean) {
        if (value) {
            setBackgroundColor(Color.TRANSPARENT)
        } else {
            setBackgroundColor(BANNER_BACKGROUND)
        }
    }

    fun disableMoatLimiting() {
        controller.moatLimiting = false
    }

    private fun showPadlockIfNeeded() {
        if (!controller.showPadlock || webView == null) return

        val padlockButton = ImageButton(context)
        padlockButton.setImageBitmap(imageProvider.padlockImage())
        padlockButton.setBackgroundColor(Color.TRANSPARENT)
        padlockButton.scaleType = ImageView.ScaleType.FIT_XY
        padlockButton.setPadding(0, 2.toPx, 0, 0)
        padlockButton.layoutParams = ViewGroup.LayoutParams(77.toPx, 31.toPx)

        padlockButton.setOnClickListener { controller.handleSafeAdTap() }

        webView?.addView(padlockButton)

        this.padlockButton = padlockButton
    }

    private fun addWebView() {
        removeWebView()

        val webView = WebView(context)
        webView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT)
        webView.listener = object : WebView.Listener {
            override fun webViewOnStart() = controller.adShown()
            override fun webViewOnError() = controller.adFailedToShow()
            override fun webViewOnClick(url: String) = controller.handleAdTap(url, context)
        }

        addView(webView)

        this.webView = webView
    }

    private fun removeWebView() {
        if (webView != null) {
            webView?.destroy()
            removeView(webView)
            webView = null
        }
    }

    private fun makeAdRequest(): AdRequest = AdRequest(test = controller.testEnabled,
            pos = AdRequest.Position.AboveTheFold.value,
            skip = AdRequest.Skip.No.value,
            playbackmethod = AdRequest.PlaybackSoundOnScreen,
            startdelay = AdRequest.StartDelay.PreRoll.value,
            instl = AdRequest.FullScreen.Off.value,
            w = width,
            h = height)
}
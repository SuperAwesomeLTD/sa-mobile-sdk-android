package tv.superawesome.sdk.publisher.ui.banner

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.Parcelable
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import org.koin.core.get
import org.koin.core.inject
import tv.superawesome.sdk.publisher.common.components.ImageProviderType
import tv.superawesome.sdk.publisher.common.components.Logger
import tv.superawesome.sdk.publisher.common.di.Injectable
import tv.superawesome.sdk.publisher.common.extensions.toPx
import tv.superawesome.sdk.publisher.common.models.AdRequest
import tv.superawesome.sdk.publisher.common.models.Constants
import tv.superawesome.sdk.publisher.common.models.SAInterface
import tv.superawesome.sdk.publisher.common.models.VoidBlock
import tv.superawesome.sdk.publisher.ui.common.AdControllerType
import tv.superawesome.sdk.publisher.ui.common.ViewableDetectorType


class BannerView : FrameLayout, Injectable {
    val controller: AdControllerType by inject()
    private val imageProvider: ImageProviderType by inject()
    private val logger: Logger by inject()

    private var placementId: Int = 0
    private var webView: WebView? = null
    private var padlockButton: ImageButton? = null
    private var viewableDetector: ViewableDetectorType? = null
    private var hasBeenVisible: VoidBlock? = null

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        setColor(Constants.defaultBackgroundColorEnabled)
        isSaveEnabled = true
    }

    override fun onSaveInstanceState(): Parcelable? = Bundle().apply {
        putParcelable("superState", super.onSaveInstanceState())
        putInt("placementId", placementId)
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        var restoreState = state
        if (restoreState is Bundle) {
            val bundle = restoreState
            this.placementId = bundle.getInt("placementId")
            restoreState = bundle.getParcelable("superState")
        }
        super.onRestoreInstanceState(restoreState)
    }

    /**
     * One of the main public methods of the SABannerAd class. This will load a new SAAd object
     * corresponding to a given placement Id.
     *
     * @param placementId Awesome Ads ID for ad data to be loaded
     */
    fun load(placementId: Int) {
        logger.info("load(${placementId})")
        this.placementId = placementId
        controller.load(placementId, makeAdRequest())
    }

    /**
     * One of the main public methods of the SABannerAd class. This will play an already existing
     * loaded ad, or fail.
     *
     * @param context current context (activity or fragment)
     */
    fun play() {
        logger.info("play($placementId)")
        val adResponse = controller.play(placementId)
        val data = adResponse?.getDataPair()

        if (data == null) {
            controller.adFailedToShow()
            return
        }

        addWebView()
        showPadlockIfNeeded()
        webView?.loadHTML(data.first, data.second)
    }

    fun setListener(delegate: SAInterface) {
        controller.delegate = delegate
    }

    /**
     * Method that gets called in order to close the banner ad, remove any fragments, etc
     */
    fun close() {
        hasBeenVisible = null
        viewableDetector?.cancel()
        viewableDetector = null
        removeWebView()
        controller.close()
    }

    /**
     * Method that determines if an ad is available
     *
     * @return true or false
     */
    fun hasAdAvailable(): Boolean = controller.hasAdAvailable(placementId)

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
        controller.config.isParentalGateEnabled = value
    }

    fun setBumperPage(value: Boolean) {
        controller.config.isBumperPageEnabled = value
    }

    fun setTestMode(value: Boolean) {
        controller.config.testEnabled = value
    }

    fun setColor(value: Boolean) {
        if (value) {
            setBackgroundColor(Color.TRANSPARENT)
        } else {
            setBackgroundColor(Constants.backgroundColorGray)
        }
    }

    fun disableMoatLimiting() {
        controller.moatLimiting = false
    }

    private fun showPadlockIfNeeded() {
        if (!controller.config.shouldShowPadlock || webView == null) return

        val padlockButton = ImageButton(context)
        padlockButton.setImageBitmap(imageProvider.padlockImage())
        padlockButton.setBackgroundColor(Color.TRANSPARENT)
        padlockButton.scaleType = ImageView.ScaleType.FIT_XY
        padlockButton.setPadding(0, 2.toPx, 0, 0)
        padlockButton.layoutParams = ViewGroup.LayoutParams(77.toPx, 31.toPx)

        padlockButton.setOnClickListener { controller.handleSafeAdTap(context) }

        webView?.addView(padlockButton)

        this.padlockButton = padlockButton
    }

    private fun addWebView() {
        removeWebView()

        val webView = WebView(context)
        webView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT)
        webView.listener = object : WebView.Listener {
            override fun webViewOnStart() {
                controller.adShown()
                viewableDetector?.cancel()
                viewableDetector = get()
                controller.triggerImpressionEvent(placementId)
                viewableDetector?.start(this@BannerView) {
                    controller.triggerViewableImpression(placementId)
                    hasBeenVisible?.let { it() }
                }
            }

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

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        logger.info("BannerView.onDetachedFromWindow")
    }

    private fun makeAdRequest(): AdRequest = AdRequest(test = controller.config.testEnabled,
            pos = AdRequest.Position.AboveTheFold.value,
            skip = AdRequest.Skip.No.value,
            playbackmethod = AdRequest.PlaybackSoundOnScreen,
            startdelay = AdRequest.StartDelay.PreRoll.value,
            instl = AdRequest.FullScreen.Off.value,
            w = width,
            h = height)

    internal fun configure(placementId: Int, delegate: SAInterface?, hasBeenVisible: VoidBlock) {
        this.placementId = placementId
        delegate?.let { setListener(it) }
        this.hasBeenVisible = hasBeenVisible
    }
}
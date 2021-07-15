package tv.superawesome.plugins.publisher.mopub

import android.content.Context
import com.mopub.common.BaseAdapterConfiguration
import com.mopub.common.OnNetworkInitializationFinishedListener
import com.mopub.common.Preconditions
import com.mopub.common.logging.MoPubLog
import com.mopub.common.logging.MoPubLog.AdapterLogEvent.CUSTOM_WITH_THROWABLE
import com.mopub.mobileads.MoPubErrorCode
import tv.superawesome.sdk.publisher.AwesomeAds

class AwesomeAdsMoPubAdapterConfiguration : BaseAdapterConfiguration() {
    override fun initializeNetwork(context: Context, configuration: MutableMap<String, String>?, listener: OnNetworkInitializationFinishedListener) {
        Preconditions.checkNotNull(context)
        Preconditions.checkNotNull(listener)

        var networkInitializationSucceeded = false

        synchronized(AwesomeAdsMoPubAdapterConfiguration::class.java) {
            try {
                AwesomeAds.init(context, false)
                networkInitializationSucceeded = true
            } catch (e: Exception) {
                MoPubLog.log(CUSTOM_WITH_THROWABLE, "Initializing AwesomeAds has encountered an exception.", e)
            }
        }

        if (networkInitializationSucceeded) {
            listener.onNetworkInitializationFinished(AwesomeAdsMoPubAdapterConfiguration::class.java,
                    MoPubErrorCode.ADAPTER_INITIALIZATION_SUCCESS)
        } else {
            listener.onNetworkInitializationFinished(AwesomeAdsMoPubAdapterConfiguration::class.java,
                    MoPubErrorCode.ADAPTER_CONFIGURATION_ERROR)
        }
    }

    override fun getAdapterVersion(): String = "8.0.4.0"

    override fun getMoPubNetworkName(): String = "awesomeads"

    override fun getBiddingToken(context: Context): String? = null

    override fun getNetworkSdkVersion(): String = "8.1.1-kt1372"

}
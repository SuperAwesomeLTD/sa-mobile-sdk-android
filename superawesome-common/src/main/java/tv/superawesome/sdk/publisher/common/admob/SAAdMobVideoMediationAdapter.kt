package tv.superawesome.sdk.publisher.common.admob

import android.content.Context
import com.google.android.gms.ads.mediation.Adapter
import com.google.android.gms.ads.mediation.InitializationCompleteCallback
import com.google.android.gms.ads.mediation.MediationAdLoadCallback
import com.google.android.gms.ads.mediation.MediationConfiguration
import com.google.android.gms.ads.mediation.MediationRewardedAd
import com.google.android.gms.ads.mediation.MediationRewardedAdCallback
import com.google.android.gms.ads.mediation.MediationRewardedAdConfiguration
import com.google.android.gms.ads.mediation.VersionInfo
import tv.superawesome.sdk.publisher.common.BuildConfig
import tv.superawesome.sdk.publisher.common.awesomeAds.AwesomeAds
import tv.superawesome.sdk.publisher.common.models.Configuration
import tv.superawesome.sdk.publisher.common.network.Environment

class SAAdMobVideoMediationAdapter : Adapter() {

    private var rewardedAd: SAAdMobRewardedAd? = null

    override fun initialize(
        context: Context?,
        initializationCompleteCallback: InitializationCompleteCallback?,
        mediationConfigurations: MutableList<MediationConfiguration>?,
    ) {
        if (context == null) {
            initializationCompleteCallback?.onInitializationFailed("Initialization Failed: Context is null.")
            return
        }

        AwesomeAds.init(context, Configuration(Environment.Production, false))
        initializationCompleteCallback?.onInitializationSucceeded()
    }

    override fun getSDKVersionInfo(): VersionInfo {
        val splits = BuildConfig.SDK_VERSION.split(".")
        return if (splits.size >= 3) {
            VersionInfo(splits[0].toInt(), splits[1].toInt(), splits[2].toInt())
        } else {
            VersionInfo(0, 0, 0)
        }
    }

    override fun getVersionInfo(): VersionInfo {
        val splits = BuildConfig.SDK_VERSION.split(".")
        return if (splits.size >= 3) {
            VersionInfo(splits[0].toInt(), splits[1].toInt(), splits[2].toInt() * 100)
        } else {
            VersionInfo(0, 0, 0)
        }
    }

    override fun loadRewardedAd(
        adConfiguration: MediationRewardedAdConfiguration,
        callback: MediationAdLoadCallback<MediationRewardedAd, MediationRewardedAdCallback>,
    ) {
        rewardedAd = SAAdMobRewardedAd(adConfiguration, callback)
        rewardedAd?.load()
    }
}
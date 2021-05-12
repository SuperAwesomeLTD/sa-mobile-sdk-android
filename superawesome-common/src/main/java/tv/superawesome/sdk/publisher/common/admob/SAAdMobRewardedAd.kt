package tv.superawesome.sdk.publisher.common.admob

import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.mediation.MediationAdLoadCallback
import com.google.android.gms.ads.mediation.MediationRewardedAd
import com.google.android.gms.ads.mediation.MediationRewardedAdCallback
import com.google.android.gms.ads.mediation.MediationRewardedAdConfiguration
import com.google.android.gms.ads.rewarded.RewardItem
import tv.superawesome.sdk.publisher.common.models.AdRequest
import tv.superawesome.sdk.publisher.common.models.Orientation
import tv.superawesome.sdk.publisher.common.models.SAEvent
import tv.superawesome.sdk.publisher.common.models.SAInterface
import tv.superawesome.sdk.publisher.common.ui.video.SAVideoAd

class SAAdMobRewardedAd(
    private val adConfiguration: MediationRewardedAdConfiguration,
    private var mediationAdLoadCallback: MediationAdLoadCallback<MediationRewardedAd, MediationRewardedAdCallback>
) : MediationRewardedAd, SAInterface {
    private val paramKey = "parameter"

    private var rewardedAdCallback: MediationRewardedAdCallback? = null
    private var loadedPlacementId = 0

    fun load() {
        val context = adConfiguration.context

        val mediationExtras = adConfiguration.mediationExtras
        SAVideoAd.setTestMode(mediationExtras.getBoolean(SAAdMobExtras.kKEY_TEST))
        Orientation.fromValue(mediationExtras.getInt(SAAdMobExtras.kKEY_ORIENTATION))
            ?.let { SAVideoAd.setOrientation(it) }
        SAVideoAd.setParentalGate(mediationExtras.getBoolean(SAAdMobExtras.kKEY_PARENTAL_GATE))
        SAVideoAd.setBumperPage(mediationExtras.getBoolean(SAAdMobExtras.kKEY_BUMPER_PAGE))
        SAVideoAd.setSmallClick(mediationExtras.getBoolean(SAAdMobExtras.kKEY_SMALL_CLICK))
        SAVideoAd.setCloseButton(mediationExtras.getBoolean(SAAdMobExtras.kKEY_CLOSE_BUTTON))
        SAVideoAd.setCloseAtEnd(mediationExtras.getBoolean(SAAdMobExtras.kKEY_CLOSE_AT_END))
        SAVideoAd.setBackButton(mediationExtras.getBoolean(SAAdMobExtras.kKEY_BACK_BUTTON))
        AdRequest.StartDelay.fromValue(mediationExtras.getInt(SAAdMobExtras.kKEY_PLAYBACK_MODE))
            ?.let { SAVideoAd.setPlaybackMode(it) }

        loadedPlacementId = adConfiguration.serverParameters.getString(paramKey)?.toIntOrNull() ?: 0

        if (loadedPlacementId == 0) {
            mediationAdLoadCallback.onFailure(
                AdError(
                    0,
                    "Failed to request ad, placementID is null or empty.",
                    ""
                )
            )
            return
        }

        SAVideoAd.setListener(this)
        SAVideoAd.load(loadedPlacementId, context)
    }

    override fun showAd(context: Context) {
        if (SAVideoAd.hasAdAvailable(loadedPlacementId)) {
            SAVideoAd.play(loadedPlacementId, context)

            rewardedAdCallback?.onVideoStart()
        } else {
            val error = AdError(-1, "Ad is not ready", "Ad is not ready")
            mediationAdLoadCallback.onFailure(error)
            rewardedAdCallback?.onAdFailedToShow(error)
        }
    }

    // SAVideoAd Listener Event
    override fun onEvent(placementId: Int, event: SAEvent) {
        when (event) {
            SAEvent.AdLoaded -> adLoaded()
            SAEvent.AdEmpty, SAEvent.AdFailedToLoad -> adFailedToLoad()
            SAEvent.AdShown -> rewardedAdCallback?.onAdOpened()
            SAEvent.AdFailedToShow -> rewardedAdCallback?.onAdFailedToShow(AdError(-1, "Ad failed to load for $loadedPlacementId", "Ad failed to load for $loadedPlacementId"))
            SAEvent.AdClicked -> rewardedAdCallback?.reportAdClicked()
            SAEvent.AdEnded -> rewardedAdCallback?.onUserEarnedReward(RewardItem.DEFAULT_REWARD)
            SAEvent.AdClosed -> rewardedAdCallback?.onAdClosed()
            SAEvent.AdAlreadyLoaded -> {
                // do nothing
            }
        }
    }

    private fun adLoaded() {
        rewardedAdCallback = mediationAdLoadCallback.onSuccess(this)
    }

    private fun adFailedToLoad() {
        mediationAdLoadCallback.onFailure(AdError(-1, "Ad failed to load for $loadedPlacementId", "Ad failed to load for $loadedPlacementId"))
    }
}

/**
 * @Copyright:   SADefaults Trading Limited 2017
 * @Author:      Gabriel Coman (gabriel.coman@superawesome.tv)
 */
package tv.superawesome.plugins.publisher.unity

import android.content.Context
import tv.superawesome.sdk.publisher.common.models.AdRequest
import tv.superawesome.sdk.publisher.common.models.Orientation
import tv.superawesome.sdk.publisher.common.models.SAEvent
import tv.superawesome.sdk.publisher.common.models.SAInterface
import tv.superawesome.sdk.publisher.common.ui.video.SAVideoAd

/**
 * Class that holds a number of static methods used to communicate with Unity
 */
object SAUnityVideoAd {
    private const val unityName = "SAVideoAd"

    /**
     * Method that creates a new Video Ad (from Unity)
     */
    fun SuperAwesomeUnitySAVideoAdCreate(context: Context?) {
        SAVideoAd.setListener { placementId, event ->
            when (event) {
                SAEvent.AdLoaded -> SAUnityCallback.sendAdCallback(
                    unityName,
                    placementId,
                    SAEvent.AdLoaded.toString()
                )
                SAEvent.AdEmpty -> SAUnityCallback.sendAdCallback(
                    unityName,
                    placementId,
                    SAEvent.AdEmpty.toString()
                )
                SAEvent.AdFailedToLoad -> SAUnityCallback.sendAdCallback(
                    unityName,
                    placementId,
                    SAEvent.AdFailedToLoad.toString()
                )
                SAEvent.AdAlreadyLoaded -> SAUnityCallback.sendAdCallback(
                    unityName,
                    placementId,
                    SAEvent.AdAlreadyLoaded.toString()
                )
                SAEvent.AdShown -> SAUnityCallback.sendAdCallback(
                    unityName,
                    placementId,
                    SAEvent.AdShown.toString()
                )
                SAEvent.AdFailedToShow -> SAUnityCallback.sendAdCallback(
                    unityName,
                    placementId,
                    SAEvent.AdFailedToShow.toString()
                )
                SAEvent.AdClicked -> SAUnityCallback.sendAdCallback(
                    unityName,
                    placementId,
                    SAEvent.AdClicked.toString()
                )
                SAEvent.AdEnded -> SAUnityCallback.sendAdCallback(
                    unityName,
                    placementId,
                    SAEvent.AdEnded.toString()
                )
                SAEvent.AdClosed -> SAUnityCallback.sendAdCallback(
                    unityName,
                    placementId,
                    SAEvent.AdClosed.toString()
                )
            }
        }
    }

    /**
     * Method that loads a new Video Ad (from Unity)
     */
    fun SuperAwesomeUnitySAVideoAdLoad(
            context: Context,
            placementId: Int,
            configuration: Int,
            test: Boolean,
            playback: Int,
    ) {
        SAVideoAd.setTestMode(test)
        AdRequest.StartDelay.fromValue(playback)?.let { SAVideoAd.setPlaybackMode(it) }
        SAVideoAd.load(placementId, context)
    }

    /**
     * Method that checks to see if an ad is available for a video ad (from Unity)
     */
    fun SuperAwesomeUnitySAVideoAdHasAdAvailable(context: Context?, placementId: Int): Boolean =
            SAVideoAd.hasAdAvailable(placementId)

    /**
     * Method that plays a new video ad (from Unity)
     */
    fun SuperAwesomeUnitySAVideoAdPlay(
            context: Context,
            placementId: Int,
            isParentalGateEnabled: Boolean,
            isBumperPageEnabled: Boolean,
            shouldShowCloseButton: Boolean,
            shouldShowSmallClickButton: Boolean,
            shouldAutomaticallyCloseAtEnd: Boolean,
            orientation: Int,
            isBackButtonEnabled: Boolean,
            shouldShowCloseWarning: Boolean,
    ) {
        SAVideoAd.setParentalGate(isParentalGateEnabled)
        SAVideoAd.setBumperPage(isBumperPageEnabled)
        SAVideoAd.setCloseAtEnd(shouldAutomaticallyCloseAtEnd)
        SAVideoAd.setCloseButton(shouldShowCloseButton)
        SAVideoAd.setSmallClick(shouldShowSmallClickButton)
        SAVideoAd.setBackButton(isBackButtonEnabled)
        Orientation.fromValue(orientation)?.let { SAVideoAd.setOrientation(it) }
        // TODO: update close warning after merge with master branch
        //SAVideoAd.setCloseButtonWarning(shouldShowCloseWarning)
        SAVideoAd.play(placementId, context)
    }
}
/**
 * @Copyright:   SuperAwesome Trading Limited 2017
 * @Author:      Gabriel Coman (gabriel.coman@superawesome.tv)
 */
package tv.superawesome.plugins.unity;

import android.content.Context;

import tv.superawesome.lib.sasession.SAConfiguration;
import tv.superawesome.sdk.views.SAEvent;
import tv.superawesome.sdk.views.SAInterface;
import tv.superawesome.sdk.views.SAOrientation;
import tv.superawesome.sdk.views.SAVideoAd;

/**
 * Class that holds a number of static methods used to communicate with Unity
 */
public class SAUnityVideoAd {

    private static final String unityName = "SAVideoAd";

    /**
     * Method that creates a new Video Ad (from Unity)
     */
    public static void SuperAwesomeUnitySAVideoAdCreate (Context context) {
        SAVideoAd.setListener(new SAInterface() {
            @Override
            public void onEvent(int placementId, SAEvent event) {
                switch (event) {
                    case adLoaded: SAUnityCallback.sendAdCallback(unityName, placementId, "adLoaded"); break;
                    case adFailedToLoad: SAUnityCallback.sendAdCallback(unityName, placementId, "adFailedToShow"); break;
                    case adShown: SAUnityCallback.sendAdCallback(unityName, placementId, "adShown"); break;
                    case adFailedToShow: SAUnityCallback.sendAdCallback(unityName, placementId, "adFailedToShow"); break;
                    case adClicked: SAUnityCallback.sendAdCallback(unityName, placementId, "adClicked"); break;
                    case adClosed: SAUnityCallback.sendAdCallback(unityName, placementId, "adClosed"); break;
                }
            }
        });
    }

    /**
     * Method that loads a new Video Ad (from Unity)
     */
    public static void SuperAwesomeUnitySAVideoAdLoad(Context context, int placementId, int configuration, boolean test) {
        SAVideoAd.setTestMode(test);
        SAVideoAd.setConfiguration(SAConfiguration.fromValue(configuration));
        SAVideoAd.load(placementId, context);
    }

    /**
     * Method that checks to see if an ad is available for a video ad (from Unity)
     */
    public static boolean SuperAwesomeUnitySAVideoAdHasAdAvailable (Context context, int placementId) {
        return SAVideoAd.hasAdAvailable(placementId);
    }

    /**
     * Method that plays a new video ad (from Unity)
     */
    public static void SuperAwesomeUnitySAVideoAdPlay (Context context, int placementId, boolean isParentalGateEnabled, boolean shouldShowCloseButton, boolean shouldShowSmallClickButton, boolean shouldAutomaticallyCloseAtEnd, int orientation, boolean isBackButtonEnabled) {
        SAVideoAd.setParentalGate(isParentalGateEnabled);
        SAVideoAd.setCloseAtEnd(shouldAutomaticallyCloseAtEnd);
        SAVideoAd.setCloseButton(shouldShowCloseButton);
        SAVideoAd.setSmallClick(shouldShowSmallClickButton);
        SAVideoAd.setBackButton(isBackButtonEnabled);
        SAVideoAd.setOrientation(SAOrientation.fromValue(orientation));
        SAVideoAd.play(placementId, context);
    }

}

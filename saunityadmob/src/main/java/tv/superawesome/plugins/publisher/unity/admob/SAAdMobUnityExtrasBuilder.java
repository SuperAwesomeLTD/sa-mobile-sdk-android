package tv.superawesome.plugins.publisher.unity.admob;

import android.os.Bundle;

import com.google.unity.ads.AdNetworkExtras;

import java.util.HashMap;

import tv.superawesome.lib.sasession.defines.SAConfiguration;
import tv.superawesome.lib.sasession.defines.SARTBStartDelay;
import tv.superawesome.plugins.publisher.admob.SAAdMobExtras;
import tv.superawesome.sdk.publisher.SAOrientation;

class SAAdMobUnityExtrasBuilder implements AdNetworkExtras {

    private Boolean getBoolean(HashMap<String, String> map, String key) {
        String orientation = map.get(key);
        if (orientation != null) {
            return Boolean.parseBoolean(orientation);
        }
        return null;
    }

    private Integer getInt(HashMap<String, String> map, String key) {
        String orientation = map.get(key);
        if (orientation != null) {
            return Integer.parseInt(orientation);
        }
        return null;
    }

    @Override
    public Bundle buildExtras(HashMap<String, String> map) {
        SAAdMobExtras extras = SAAdMobExtras.extras();

        Boolean test = getBoolean(map, SAAdMobExtras.kKEY_TEST);
        Boolean transparent = getBoolean(map, SAAdMobExtras.kKEY_TRANSPARENT);
        Integer orientation = getInt(map, SAAdMobExtras.kKEY_ORIENTATION);
        Integer configuration = getInt(map, SAAdMobExtras.kKEY_CONFIGURATION);
        Integer playback = getInt(map, SAAdMobExtras.kKEY_PLAYBACK_MODE);
        Boolean parental = getBoolean(map, SAAdMobExtras.kKEY_PARENTAL_GATE);
        Boolean bumper = getBoolean(map, SAAdMobExtras.kKEY_BUMPER_PAGE);
        Boolean backButton = getBoolean(map, SAAdMobExtras.kKEY_BACK_BUTTON);
        Boolean closeButton = getBoolean(map, SAAdMobExtras.kKEY_CLOSE_BUTTON);
        Boolean closeAtEnd = getBoolean(map, SAAdMobExtras.kKEY_CLOSE_AT_END);
        Boolean smallClick = getBoolean(map, SAAdMobExtras.kKEY_SMALL_CLICK);
        Boolean closeWithWarning = getBoolean(map, SAAdMobExtras.kKEY_CLOSE_WARNING);

        if (test != null) extras.setTestMode(test);
        if (transparent != null) extras.setTransparent(transparent);
        if (orientation != null) extras.setOrientation(SAOrientation.fromValue(orientation));
        if (configuration != null)
            extras.setConfiguration(SAConfiguration.fromValue(configuration));
        if (playback != null) extras.setPlaybackMode(SARTBStartDelay.fromValue(playback));
        if (parental != null) extras.setParentalGate(parental);
        if (bumper != null) extras.setBumperPage(bumper);
        if (backButton != null) extras.setBackButton(backButton);
        if (closeButton != null) extras.setCloseButton(closeButton);
        if (closeAtEnd != null) extras.setCloseAtEnd(closeAtEnd);
        if (smallClick != null) extras.setSmallClick(smallClick);
        if (closeWithWarning != null) extras.setCloseWithWarning(closeWithWarning);

        return extras.build();
    }

    @Override
    public Class getAdapterClass() {
        return SAAdMobExtras.class;
    }
}

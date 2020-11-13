package tv.superawesome.plugins.publisher.admob;

import android.os.Bundle;

import tv.superawesome.lib.sasession.defines.SAConfiguration;
import tv.superawesome.lib.sasession.defines.SARTBStartDelay;
import tv.superawesome.sdk.publisher.SADefaults;
import tv.superawesome.sdk.publisher.SAOrientation;

public class SAAdMobExtras {

    public static final String kKEY_TEST = "SA_TEST_MODE";
    public static final String kKEY_TRANSPARENT = "SA_TRANSPARENT";
    public static final String kKEY_ORIENTATION = "SA_ORIENTATION";
    public static final String kKEY_CONFIGURATION = "SA_CONFIGURATION";
    public static final String kKEY_PARENTAL_GATE = "SA_PG";
    public static final String kKEY_BUMPER_PAGE = "SA_BUMPER";
    public static final String kKEY_BACK_BUTTON = "SA_BACK_BUTTON";
    public static final String kKEY_CLOSE_BUTTON = "SA_CLOSE_BUTTON";
    public static final String kKEY_CLOSE_AT_END = "SA_CLOSE_AT_END";
    public static final String kKEY_SMALL_CLICK = "SA_SMALL_CLICK";
    public static final String kKEY_CLOSE_WARNING = "SA_CLOSE_WARNING";
    public static final String kKEY_PLAYBACK_MODE = "SA_PLAYBACK_MODE";

    private boolean transparent = SADefaults.defaultBgColor();
    private boolean testMode = SADefaults.defaultTestMode();
    private SAOrientation orientation = SADefaults.defaultOrientation();
    private SAConfiguration configuration = SADefaults.defaultConfiguration();
    private SARTBStartDelay playback = SADefaults.defaultPlaybackMode();
    private boolean parentalGate = SADefaults.defaultParentalGate();
    private boolean bumperPage = SADefaults.defaultBumperPage();
    private boolean backButton = SADefaults.defaultBackButton();
    private boolean closeButton = SADefaults.defaultCloseButton();
    private boolean closeAtEnd = SADefaults.defaultCloseAtEnd();
    private boolean smallClick = SADefaults.defaultSmallClick();
    private boolean closeWithWarning = SADefaults.defaultCloseWarning();

    private SAAdMobExtras() {
        //
    }

    public static SAAdMobExtras extras() {
        return new SAAdMobExtras();
    }

    public SAAdMobExtras setTransparent(boolean value) {
        transparent = value;
        return this;
    }

    public SAAdMobExtras setTestMode(boolean value) {
        testMode = value;
        return this;
    }

    public SAAdMobExtras setOrientation(SAOrientation value) {
        orientation = value;
        return this;
    }

    public SAAdMobExtras setConfiguration(SAConfiguration value) {
        configuration = value;
        return this;
    }

    public SAAdMobExtras setParentalGate(boolean value) {
        parentalGate = value;
        return this;
    }

    public SAAdMobExtras setBumperPage(boolean value) {
        bumperPage = value;
        return this;
    }

    public SAAdMobExtras setBackButton(boolean value) {
        backButton = value;
        return this;
    }

    public SAAdMobExtras setCloseButton(boolean value) {
        closeButton = value;
        return this;
    }

    public SAAdMobExtras setCloseAtEnd(boolean value) {
        closeAtEnd = value;
        return this;
    }

    public SAAdMobExtras setSmallClick(boolean value) {
        smallClick = value;
        return this;
    }

    public SAAdMobExtras setPlaybackMode(SARTBStartDelay mode) {
        playback = mode;
        return this;
    }

    public SAAdMobExtras setCloseWithWarning(boolean value) {
        closeWithWarning = value;
        return this;
    }

    public Bundle build() {
        Bundle bundle = new Bundle();
        bundle.putBoolean(kKEY_TEST, testMode);
        bundle.putBoolean(kKEY_TRANSPARENT, transparent);
        bundle.putInt(kKEY_ORIENTATION, orientation.ordinal());
        bundle.putInt(kKEY_CONFIGURATION, configuration.ordinal());
        bundle.putInt(kKEY_PLAYBACK_MODE, playback.ordinal());
        bundle.putBoolean(kKEY_PARENTAL_GATE, parentalGate);
        bundle.putBoolean(kKEY_BUMPER_PAGE, bumperPage);
        bundle.putBoolean(kKEY_BACK_BUTTON, backButton);
        bundle.putBoolean(kKEY_CLOSE_BUTTON, closeButton);
        bundle.putBoolean(kKEY_CLOSE_AT_END, closeAtEnd);
        bundle.putBoolean(kKEY_SMALL_CLICK, smallClick);
        bundle.putBoolean(kKEY_CLOSE_WARNING, closeWithWarning);
        return bundle;
    }

}

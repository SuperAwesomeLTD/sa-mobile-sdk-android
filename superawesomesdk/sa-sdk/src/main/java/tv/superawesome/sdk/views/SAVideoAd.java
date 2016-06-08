package tv.superawesome.sdk.views;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.VideoView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Handler;

import tv.superawesome.lib.saevents.SAEvents;
import tv.superawesome.lib.sautils.SAUtils;
import tv.superawesome.lib.savastparser.SAVASTManager;
import tv.superawesome.lib.savastparser.SAVASTManagerInterface;
import tv.superawesome.lib.savideoplayer.SAVideoPlayer;
import tv.superawesome.sdk.SuperAwesome;
import tv.superawesome.lib.saadloader.models.SAAd;
import tv.superawesome.lib.saadloader.models.SACreativeFormat;

/**
 * Created by gabriel.coman on 05/04/16.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class SAVideoAd extends FrameLayout implements SAViewInterface, SAVASTManagerInterface {

    /** the Ad */
    private boolean isParentalGateEnabled = true;
    private boolean shouldShowCloseButton = false;
    private boolean shouldShowSmallClickButton = false;
    private SAAd ad;

    /** display stuff */
    private SAVASTManager manager;
    private SAVideoPlayer videoPlayer;
    private SAParentalGate gate;

    /** listeners */
    private SAAdInterface adListener;
    private SAParentalGateInterface parentalGateListener;
    private SAVideoAdInterface videoAdListener;
    private SAAdInterface internalAdListener;
    private SAVideoAdInterface internalVideoAdListener;

    /** helper vars */
    private boolean layoutOK = false;
    private String destinationURL = null;
    private List<String> clickTracking = new ArrayList<>();

    private int ticks = 0;
    private int viewabilityCount = 0;
    private Runnable viewabilityRunnable = null;
    private Rect thisRect;
    private Rect superRect;
    private Rect screenRect;

    /**
     * Constructors with different types of parameters; All should default to the three param
     * constructor, with context, attributes and default style
     */
    public SAVideoAd(Context context) { this(context, null, 0); }
    public SAVideoAd(Context context, AttributeSet attrs) { this(context, attrs, 0); }
    public SAVideoAd(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        String packageName = context.getPackageName();
        int view_sa_videoadId = getResources().getIdentifier("view_sa_video", "layout", packageName);
        int video_playerId = getResources().getIdentifier("sa_videoplayer_id", "id", packageName);

        LayoutInflater inflater = LayoutInflater.from(context);
        inflater.inflate(view_sa_videoadId, this);
        videoPlayer = (SAVideoPlayer)((Activity) context).getFragmentManager().findFragmentById(video_playerId);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        if (getWidth() != 0 && getHeight() != 0 && !layoutOK){

            int[] array = new int[2];
            getLocationInWindow(array);
            thisRect = new Rect(array[0], array[1], getWidth(), getHeight());

            // super view
            int[] sarray = new int[2];
            View parent = (View)getParent();
            parent.getLocationInWindow(sarray);
            superRect = new Rect(sarray[0], sarray[1], parent.getWidth(), parent.getHeight());

            // window
            SAUtils.SASize screenSize = SAUtils.getRealScreenSize((Activity)getContext(), false);
            screenRect = new Rect(0, 0, screenSize.width, screenSize.height);

            layoutOK = true;

        }
    }

    /**
     * *********************************************************************************************
     * <SAViewInterface> Implementation
     * *********************************************************************************************
     */

    @Override
    public void setAd(SAAd ad) {
        this.ad = ad;
    }

    @Override
    public SAAd getAd() {
        return ad;
    }

//    private android.os.Handler handy = new android.os.Handler();
//    private Runnable testR = null;

    @Override
    public void play() {
        /** check to see for the correct placement type */
        if (ad.creative.creativeFormat != SACreativeFormat.video) {
            if (adListener != null) {
                adListener.adHasIncorrectPlacement(ad.placementId);
            }

            if (internalAdListener != null) {
                internalAdListener.adHasIncorrectPlacement(ad.placementId);
            }

            return;
        }

        /**
         * start the vast manager - that in turn will start the video player;
         * this function should be called just once in an activity - not on every configuration change
         */
        videoPlayer.shouldShowPadlock = !(ad.isFallback || ad.isHouse);
        videoPlayer.shouldShowCloseButton = shouldShowCloseButton;
        videoPlayer.shouldShowSmallClickButton = shouldShowSmallClickButton;
        manager = new SAVASTManager(videoPlayer, this);

        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                manager.manageWithAd(ad.creative.details.data.vastAd);
            }
        };

        if (ad == null || !layoutOK) {
            this.postDelayed(runnable, 250);
        } else {
            post(runnable);
        }

//        testR = new Runnable() {
//            @Override
//            public void run() {
//                Log.d("SuperAwesome", "POST Runnable " + layoutOK);
//                postDelayed(this, 1000);
//            }
//        };
//        handy.postDelayed(testR, 1000);
    }

    @Override
    public void close() {
        videoPlayer.destroy();
    }

    public void pause () {
        videoPlayer.pausePlayer();
    }

    public void resume () {
        videoPlayer.resumePlayer();
    }

    @Override
    public void advanceToClick() {
        /** call listener */
        if (adListener != null) {
            adListener.adWasClicked(ad.placementId);
        }

        /** send click tracking events */
        for (String click : clickTracking) {
            SAEvents.sendEventToURL(click);
        }

        System.out.println("Going to URL: " + destinationURL);

        /** go-to-url */
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(destinationURL));
        getContext().startActivity(browserIntent);
    }

    @Override
    public void resizeToSize(int width, int height) {

    }

    /**
     * These functions sets the ad listener
     */
    public void setAdListener(SAAdInterface adListener) {
        this.adListener = adListener;
    }
    public void setInternalAdListener(SAAdInterface adListener) { this.internalAdListener = adListener; }
    public void setParentalGateListener(SAParentalGateInterface parentalGateListener){ this.parentalGateListener = parentalGateListener; }
    public void setVideoAdListener(SAVideoAdInterface videoAdListener){ this.videoAdListener = videoAdListener; }
    public void setInternalVideoAdListener(SAVideoAdInterface videoAdListener) { this.internalVideoAdListener = videoAdListener; }
    public void setIsParentalGateEnabled (boolean isParentalGateEnabled) { this.isParentalGateEnabled = isParentalGateEnabled; }
    public void setShouldShowCloseButton(boolean shouldShowCloseButton) { this.shouldShowCloseButton = shouldShowCloseButton; }
    public void setShouldShowSmallClickButton(boolean shouldShowSmallClickButton) { this.shouldShowSmallClickButton = shouldShowSmallClickButton; }

    /**
     * *********************************************************************************************
     * <SAVASTManager.SAVASTManagerInterface> Implementation
     * *********************************************************************************************
     */

    @Override
    public void didNotFindAds() {
        if (adListener != null) {
            adListener.adFailedToShow(ad.placementId);
        }
    }

    @Override
    public void didStartAd() {
        /** send this event just to be sure */
        if (ad != null && ad.creative != null ) {

            SAEvents.sendEventToURL(ad.creative.viewableImpressionUrl);

//            viewabilityRunnable = new Runnable() {
//                @Override
//                public void run() {
//
//                    if (ticks >= 5) {
//
//                        if (viewabilityCount == 5){
//                            SAEvents.sendEventToURL(ad.creative.viewableImpressionUrl);
//                        } else {
//                            Log.d("SuperAwesome", "[AA :: Error] Video is not in viewable rectangle");
//                        }
//
//                    } else {
//                        ticks++;
//                        Log.d("SuperAwesome", "Tick " + ticks + "/5");
//
//                        // child
//                        int[] array = new int[2];
//                        getLocationInWindow(array);
//                        Rect banner = new Rect(array[0], array[1], getWidth(), getHeight());
//
//                        // super view
//                        int[] sarray = new int[2];
//                        View parent = (View)getParent();
//                        parent.getLocationInWindow(sarray);
//                        Rect sbanner = new Rect(sarray[0], sarray[1], parent.getWidth(), parent.getHeight());
//
//                        // window
//                        SAUtils.SASize screenSize = SAUtils.getRealScreenSize((Activity)getContext(), false);
//                        Rect screen = new Rect(0, 0, screenSize.width, screenSize.height);
//
//                        if (SAUtils.isTargetRectInFrameRect(banner, sbanner) && SAUtils.isTargetRectInFrameRect(banner, screen)){
//                            viewabilityCount++;
//                        }
//
//                        Log.d("SuperAwesome", banner.toString());
//                        Log.d("SuperAwesome", sbanner.toString());
//                        Log.d("SuperAwesome", screen.toString());
//
//                        postDelayed(this, 1000);
//                    }
//
//                }
//            };
//            postDelayed(viewabilityRunnable, 1000);

            /** send impression URL, if exits, to 3rd party only */
            if (ad.creative.impressionUrl != null && !ad.creative.impressionUrl.contains(SuperAwesome.getInstance().getBaseURL())) {
                SAEvents.sendEventToURL(ad.creative.impressionUrl);
            }

            /** send moat */
            HashMap<String, String> adData = new HashMap<>();
            adData.put("campaignId", "" + ad.campaignId);
            adData.put("lineItemId", "" + ad.lineItemId);
            adData.put("creativeId", "" + ad.creative.id);
            adData.put("app", "" + ad.app);
            adData.put("placementId", "" + ad.placementId);
            SAEvents.sendVideoMoatEvent((Activity) getContext(), videoPlayer.getVideoPlayer(), videoPlayer.getMediaPlayer(), adData);
        }

        /** call listener */
        if (adListener != null){
            adListener.adWasShown(ad.placementId);
        }

        if (internalAdListener != null){
            internalVideoAdListener.adStarted(ad.placementId);
        }
    }

    @Override
    public void didStartCreative() {
        if (videoAdListener != null){
            videoAdListener.videoStarted(ad.placementId);
        }
    }

    @Override
    public void didReachFirstQuartileOfCreative() {
        if (videoAdListener != null){
            videoAdListener.videoReachedFirstQuartile(ad.placementId);
        }
    }

    @Override
    public void didReachMidpointOfCreative() {
        if (videoAdListener != null){
            videoAdListener.videoReachedMidpoint(ad.placementId);
        }
    }

    @Override
    public void didReachThirdQuartileOfCreative() {
        if (videoAdListener != null){
            videoAdListener.videoReachedThirdQuartile(ad.placementId);
        }
    }

    @Override
    public void didEndOfCreative() {
        if (videoAdListener != null){
            videoAdListener.videoEnded(ad.placementId);
        }
    }

    @Override
    public void didHaveErrorForCreative() {
        if (internalAdListener != null) {
            internalAdListener.adWasClosed(ad.placementId);
        }
    }

    @Override
    public void didEndAd() {
        if (videoAdListener != null){
            videoAdListener.adEnded(ad.placementId);
        }
    }

    @Override
    public void didEndAllAds() {
        /** close the gate */
        if (gate != null) {
            gate.close();
        }

        if (videoAdListener != null){
            videoAdListener.allAdsEnded(ad.placementId);
        }

        if (internalVideoAdListener != null){
            internalVideoAdListener.allAdsEnded(ad.placementId);
        }

        /** send the end event */
        SAEvents.sendVideoMoatComplete(ad.placementId);
    }

    @Override
    public void didGoToURL(String url, List<String> _clickTracking) {
        /** update the destination URL */
        destinationURL = url;
        clickTracking = _clickTracking;

        /** check for PG */
        if (isParentalGateEnabled) {
            /** send event */
            SAEvents.sendEventToURL(ad.creative.parentalGateClickUrl);
            /** create pg - make sure to access SAVideo.this, not this */
            gate = new SAParentalGate(getContext(), SAVideoAd.this, ad);
            gate.show();
            gate.setListener(parentalGateListener);
        } else {
            advanceToClick();
        }
    }

    @Override
    public void didClickOnClose() {
        if (internalAdListener != null) {
            internalAdListener.adWasClosed(ad.placementId);
        }
    }
}

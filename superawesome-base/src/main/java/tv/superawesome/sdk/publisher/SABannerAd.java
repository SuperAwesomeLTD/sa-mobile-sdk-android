package tv.superawesome.sdk.publisher;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;

import tv.superawesome.lib.saadloader.SALoader;
import tv.superawesome.lib.saadloader.SALoaderInterface;
import tv.superawesome.lib.sabumperpage.SABumperPage;
import tv.superawesome.lib.saevents.SAEvents;
import tv.superawesome.lib.saevents.SAViewTimeModule;
import tv.superawesome.lib.saevents.SAViewableModule;
import tv.superawesome.lib.samodelspace.saad.SAAd;
import tv.superawesome.lib.samodelspace.saad.SACampaignType;
import tv.superawesome.lib.samodelspace.saad.SACreativeFormat;
import tv.superawesome.lib.samodelspace.saad.SAResponse;
import tv.superawesome.lib.saparentalgate.SAParentalGate;
import tv.superawesome.lib.sasession.defines.SAConfiguration;
import tv.superawesome.lib.sasession.defines.SARTBInstl;
import tv.superawesome.lib.sasession.defines.SARTBPlaybackMethod;
import tv.superawesome.lib.sasession.defines.SARTBPosition;
import tv.superawesome.lib.sasession.defines.SARTBSkip;
import tv.superawesome.lib.sasession.defines.SARTBStartDelay;
import tv.superawesome.lib.sasession.session.SASession;
import tv.superawesome.lib.sasession.session.SASessionInterface;
import tv.superawesome.lib.sautils.SAImageUtils;
import tv.superawesome.lib.sautils.SAUtils;
import tv.superawesome.lib.sawebplayer.SAWebPlayer;

public class SABannerAd extends FrameLayout {

    interface VisibilityListener {
        void hasBeenVisible();
    }

    // constants
    private final int       BANNER_BACKGROUND = Color.rgb(224, 224, 224);

    // private vars w/ exposed setters & getters
    private boolean         isParentalGateEnabled = false;
    private boolean         isBumperPageEnabled = false;
    private SAAd            ad;
    private SAInterface     listener = new SAInterface() { @Override public void onEvent(int placementId, SAEvent event) {} };

    // the internal loader
    private SASession       session;
    private SAEvents        events;
    private SALoader        loader;

    // private subviews
    private SAWebPlayer     webPlayer;
    private ImageButton     padlock;

    // bool
    private boolean         canPlay              = true;
    private boolean         firstPlay            = true;
    private boolean         isClosed             = false;
    private boolean         moatLimiting;

    private Long            currentClickThreshold = 0L;

    private VisibilityListener visibilityListener = null;


    /**
     * Constructor with context
     *
     * @param context current context (activity or fragment)
     */
    public SABannerAd(Context context) {
        this(context, null, 0);
    }

    /**
     * Constructor with context and attribute set
     *
     * @param context current context (activity or fragment)
     * @param attrs   new attribute set
     */
    public SABannerAd(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    /**
     * Constructor with context, attribute set and default style attribute
     *
     * @param context       current context (activity or fragment)
     * @param attrs         new attribute set
     * @param defStyleAttr  default style attribute
     */
    public SABannerAd(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        if (isInEditMode()) return;

        // create the loader
        session = new SASession (context);
        loader = new SALoader(context);
        events = new SAEvents();

        // set default values
        setColor(SADefaults.defaultBgColor());
        setParentalGate(SADefaults.defaultParentalGate());
        setBumperPage(SADefaults.defaultBumperPage());
        setConfiguration(SADefaults.defaultConfiguration());
        setTestMode(SADefaults.defaultTestMode());
        moatLimiting = SADefaults.defaultMoatLimitingState();
    }

    /**
     * One of the main public methods of the SABannerAd class. This will load a new SAAd object
     * corresponding to a given placement Id.
     *
     * @param placementId Awesome Ads ID for ad data to be loaded
     */
    public void load (final int placementId) {

        // very late init of the AwesomeAds SDK
        try {
            AwesomeAds.init(((Activity)this.getContext()).getApplication(), false);
        } catch (Exception e) {
            Log.d("SuperAwesome", "Error initing AwesomeAds in SABannerAd " + e.getMessage());
        }

        // from this moment on the ad can't be played, to avoid wierd things
        canPlay = false;

        // close
        if (!firstPlay) {
            close();
        }

        // set this to false
        isClosed = false;

        // next init a new session & prepare it
        session.setPos(SARTBPosition.ABOVE_THE_FOLD);
        session.setPlaybackMethod(SARTBPlaybackMethod.WITH_SOUND_ON_SCREEN);
        session.setInstl(SARTBInstl.NOT_FULLSCREEN);
        session.setSkip(SARTBSkip.NO_SKIP);
        session.setStartDelay(SARTBStartDelay.PRE_ROLL);
        try {
            session.setWidth(getWidth());
            session.setHeight(getHeight());
        } catch (Exception e) {
            // do nothing
        }

        session.prepareSession(new SASessionInterface() {
            @Override
            public void didFindSessionReady() {

                // after session is OK, prepare
                loader.loadAd(placementId, session, new SALoaderInterface() {
                    @Override
                    public void saDidLoadAd(SAResponse response) {

                        if (response.status != 200) {
                            if (listener != null) {
                                listener.onEvent(placementId, SAEvent.adFailedToLoad);
                            } else {
                                Log.w("AwesomeAds", "Banner Ad listener not implemented. Event would have been: adFailedToLoad");
                            }
                        }
                        else {
                            canPlay = response.isValid();
                            setAd(response.isValid() ? response.ads.get(0) : null);
                            if (listener != null) {
                                listener.onEvent(placementId, response.isValid() ? SAEvent.adLoaded : SAEvent.adEmpty);
                            } else {
                                Log.w("AwesomeAds", "Banner Ad listener not implemented. Event would have been either adLoaded or adEmpty");
                            }
                        }
                    }
                });
            }
        });
    }

    /**
     * One of the main public methods of the SABannerAd class. This will play an already existing
     * loaded ad, or fail.
     *
     * @param context current context (activity or fragment)
     */
    public void play (final Context context) {

        if (!moatLimiting && events != null) {
            events.disableMoatLimiting();
        }

        // if the banner ad has a valid ad loaded then play it
        if (ad != null && ad.creative.format != SACreativeFormat.video && canPlay && !isClosed) {

            // canPlay becomes "false" again so no other playing can happen until a new load
            canPlay = false;
            firstPlay = false;

            // create a new web player fragment object
            webPlayer = new SAWebPlayer(context);
            webPlayer.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            // and set it's event listener
            webPlayer.setEventListener(new SAWebPlayer.Listener() {
                @Override
                public void saWebPlayerDidReceiveEvent(final SAWebPlayer.Event event, final String destination) {

                    switch (event) {
                        // this is called when the web player is on screen and prepared to load
                        // HTML data
                        case Web_Prepared: {

                            // trigger impression
                            events.triggerImpressionEvent();

                            // prepare moat tracking
                            String moatString = events.startMoatTrackingForDisplay(webPlayer.getWebView());
                            String fullHTML = ad.creative.details.media.html.replace("_MOAT_", moatString);

                            // load the HTML
                            Log.d("SADefaults", "Full HTML is " + fullHTML);
                            webPlayer.loadHTML(ad.creative.details.base, fullHTML);

                            break;
                        }
                        // this is called after the HTML data is loaded and is where all
                        // events are fired
                        case Web_Loaded: {

                            events.observeViewTime(SABannerAd.this, new SAViewTimeModule.Listener() {
                                @Override
                                public void onViewTimeObserved(int seconds) {
                                    Log.d("SuperAwesome", "onViewTimeObserved:" + seconds);
                                }
                            });

                            // send viewable impression
                            events.checkViewableStatusForDisplay(SABannerAd.this, new SAViewableModule.Listener() {
                                @Override
                                public void saDidFindViewOnScreen(boolean success) {
                                    if (success) {
                                        events.triggerViewableImpressionEvent();
                                        if (visibilityListener != null) {
                                            visibilityListener.hasBeenVisible();
                                        }
                                    }
                                }
                            });
                            // call listener
                            if (listener != null) {
                                listener.onEvent(ad.placementId, SAEvent.adShown);
                            } else {
                                Log.w("AwesomeAds", "Banner Ad listener not implemented. Event would have been adShown");
                            }

                            break;
                        }
                        // this is actually a fragment event notifying the banner class that
                        // the fragment has started
                        case Web_Started:{

                            float sf = SAUtils.getScaleFactor((Activity)context);
                            padlock = new ImageButton(context);
                            padlock.setImageBitmap(SAImageUtils.createPadlockBitmap());
                            padlock.setBackgroundColor(Color.TRANSPARENT);
                            padlock.setScaleType(ImageView.ScaleType.FIT_XY);
                            int topPadding = (int)(2 * sf);
                            padlock.setPadding(0, topPadding, 0, 0);
                            padlock.setLayoutParams(new ViewGroup.LayoutParams((int) (77 * sf), (int) (31 * sf)));

                            //
                            // weird condition: if banner is closed (and ad is nulled) in the time
                            // it was "play()-ed" and the time "Web_Started" gets called,
                            // then ad is null-ed and we get an exception.
                            try {
                                padlock.setVisibility(ad.isPadlockVisible ? VISIBLE : GONE);
                            } catch (Exception e) {
                                padlock.setVisibility(GONE);
                            }

                            padlock.setOnClickListener(new OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                Runnable runner = new Runnable() {
                                    @Override
                                    public void run() {
                                        showSuperAwesomeWebViewInExternalBrowser(context);
                                    }
                                };
                                showParentalGateIfNeededWithCompletion(context, runner);
                                }
                            });
                            webPlayer.getHolder().addView(padlock);
                            padlock.setTranslationX(webPlayer.getWebView().getTranslationX());
                            padlock.setTranslationY(webPlayer.getWebView().getTranslationY());

                            break;
                        }
                        // this is called when the fragment & web view have all been laid out
                        case Web_Layout:{

                            if (webPlayer.getWebView() != null && padlock != null) {
                                padlock.setTranslationX(webPlayer.getWebView().getTranslationX());
                                padlock.setTranslationY(webPlayer.getWebView().getTranslationY());
                            }
                            break;
                        }
                        // this is in case of error
                        case Web_Error: {
                            if (listener != null) {
                                listener.onEvent(ad.placementId, SAEvent.adFailedToShow);
                            } else {
                                Log.w("AwesomeAds", "Banner Ad listener not implemented. Event would have been adFailedToShow");
                            }
                            break;
                        }
                        // this is most likely from MoPub and other types of fallbacks indicating
                        // a failure in loading a proper ad (through mopub://failLoad)
                        case Web_Empty:{
                            if (listener != null) {
                                listener.onEvent(ad.placementId, SAEvent.adFailedToLoad);
                            } else {
                                Log.w("AwesomeAds", "Banner Ad listener not implemented. Event would have been adFailedToLoad");
                            }
                            break;
                        }
                        // and this is in case of click
                        case Web_Click: {

                            if (destination != null) {

                                Runnable runner = new Runnable() {
                                    @Override
                                    public void run() {
                                        click(destination);
                                    }
                                };
                                runner.run();
                                // showParentalGateIfNeededWithCompletion(context, runner);
                            }

                            break;
                        }
                    }
                }
            });

            this.addView(webPlayer);
            webPlayer.setup();
        }
        // if no ad has been loaded, send an ad failure event
        else {
            if (listener != null) {
                listener.onEvent(0, SAEvent.adFailedToShow);
            } else {
                Log.w("AwesomeAds", "Banner Ad listener not implemented. Event would have been adFailedToShow");
            }
        }
    }

    /**
     * One of the main public methods of the SABannerAd class that gets called when the web view
     * surface detects a click of some sort.
     *
     * @param destination the destination url
     */
    public void click (final String destination) {

        if ((ad != null && ad.creative != null && ad.creative.bumper) || isBumperPageEnabled) {
            SABumperPage.setListener(new SABumperPage.Interface() {
                @Override
                public void didEndBumper() {
                    handleUrl(destination);
                }
            });
            SABumperPage.play((Activity)getContext());
        } else {
            handleUrl(destination);
        }
    }

    private void handleUrl (String destination) {

        // if someone's closed this thing
        if (ad == null || ad.creative == null) {
            return;
        }

        Log.d("AwesomeAds-2", "Got here!");

        Long currentTime = System.currentTimeMillis()/1000;
        Long diff = Math.abs(currentTime - currentClickThreshold);

        if (diff < SADefaults.defaultClickThreshold()) {
            Log.d("AwesomeAds-2", "Current diff is " + diff);
            return;
        }

        currentClickThreshold = currentTime;

        Log.d("AwesomeAds-2", "Going to " + destination);

        // callback
        if (listener != null) {
            listener.onEvent(ad.placementId, SAEvent.adClicked);
        } else {
            Log.w("AwesomeAds", "Banner Ad listener not implemented. Event would have been adClicked");
        }

        // send tracking events, if needed
        if (ad != null &&
                ad.creative != null &&
                ad.creative.format != SACreativeFormat.rich &&
                session != null &&
                !destination.contains(session.getBaseUrl())) {
            events.triggerClickEvent();
        }

        // append CPI data to it
        destination += ad.campaignType == SACampaignType.CPI ? ("&referrer=" + ad.creative.referral.writeToReferralQuery()) : "";

        // start browser
        try {
            getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(destination)));
        } catch (Exception e) {
            // do nothing
        }
    }

    /**
     * Method that gets called in order to close the banner ad, remove any fragments, etc
     */
    public void close () {
        // de-set visibility listener
        if (visibilityListener != null) {
            visibilityListener = null;
        }

        // de-set public listener
        if (listener != null) {
            listener.onEvent(ad != null ? ad.placementId : 0, SAEvent.adClosed);
        } else {
            Log.w("AwesomeAds", "Banner Ad listener not implemented. Event would have been adClosed");
        }

        events.stopViewTime();
        
        // unregister moat events
        events.stopMoatTrackingForDisplay();

        // reset any ad that might be in here
        setAd(null);

        // remove the web player
        if (webPlayer != null) {
            this.removeView(webPlayer);
            webPlayer.destroy();
            webPlayer = null;
        }

        // make padlock invisible
        if (padlock != null) {
            padlock.setVisibility(GONE);
        }

        // close ad
        isClosed = true;
    }

    /**
     * Method that set an ad for the banner - and also sets an ad for the events object
     *
     * @param ad new ad
     */
    public void setAd(SAAd ad) {
        this.ad = ad;
        events.setAd(session, this.ad);
    }

    /**
     * Method that determines if an ad is available
     *
     * @return true or false
     */
    public boolean hasAdAvailable () {
        return ad != null;
    }

    public SAAd getAd () {
        return ad;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Log.d("SuperAwesome", "SABannerAd.onDetachedFromWindow: " +  this.hashCode());
        events.stopViewTime();
    }

    private void showParentalGateIfNeededWithCompletion(final Context context,
                                                        final Runnable completion) {

        if (isParentalGateEnabled) {
            SAParentalGate.setListener(new SAParentalGate.Interface() {
                @Override
                public void parentalGateOpen() {
                    events.triggerPgOpenEvent();
                }

                @Override
                public void parentalGateSuccess() {
                    events.triggerPgSuccessEvent();
                    completion.run();
                }

                @Override
                public void parentalGateFailure() {
                    events.triggerPgFailEvent();
                }

                @Override
                public void parentalGateCancel() {
                    events.triggerPgCloseEvent();
                }
            });

            SAParentalGate.show(context);
        } else {
            completion.run();
        }
    }

    private void showSuperAwesomeWebViewInExternalBrowser(final Context context) {

        SABumperPage.Interface bumperCallback = new SABumperPage.Interface() {
            @Override
            public void didEndBumper() {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://ads.superawesome.tv/v2/safead"));
                context.startActivity(browserIntent);
            }
        };

        if (isBumperPageEnabled) {
            SABumperPage.setListener(bumperCallback);
            SABumperPage.play((Activity)getContext());
        } else {
            bumperCallback.didEndBumper();
        }
    }

    /**********************************************************************************************
     * Setters & Getters
     **********************************************************************************************/

    public boolean isClosed () {
        return isClosed;
    }

    public void setListener(SAInterface value) {
        listener = value != null ? value : listener;
    }

    public void enableParentalGate () {
        setParentalGate(true);
    }

    public void disableParentalGate () {
        setParentalGate(false);
    }

    public void enableBumperPage () {
        setBumperPage(true);
    }

    public void disableBumperPage () {
        setBumperPage(false);
    }

    public void enableTestMode () {
        setTestMode(true);
    }

    public void disableTestMode () {
        setTestMode(false);
    }

    public void setConfigurationProduction () {
        setConfiguration(SAConfiguration.PRODUCTION);
    }

    public void setConfigurationStaging () {
        setConfiguration(SAConfiguration.STAGING);
    }

    public void setColorTransparent () {
        setColor(true);
    }

    public void setColorGray () {
        setColor(false);
    }

    public void setParentalGate (boolean value) {
        isParentalGateEnabled = value;
    }

    public void setBumperPage (boolean value) {
        isBumperPageEnabled = value;
    }

    public void setTestMode (boolean value) {
        session.setTestMode(value);
    }

    public void setConfiguration (SAConfiguration value) {
        session.setConfiguration(value);
    }

    public void setVisibilityListener(VisibilityListener listener) {
        visibilityListener = listener;
    }

    public void setColor (boolean value) {
        if (value) {
            setBackgroundColor(Color.TRANSPARENT);
        } else  {
            setBackgroundColor(BANNER_BACKGROUND);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // SABannerAd.VisibilityListener
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public void disableMoatLimiting () {
        moatLimiting = false;
    }
}

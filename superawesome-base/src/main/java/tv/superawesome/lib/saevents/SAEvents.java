package tv.superawesome.lib.saevents;

import android.app.Application;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.VideoView;

import tv.superawesome.lib.samodelspace.saad.SAAd;
import tv.superawesome.lib.sasession.session.ISASession;

public class SAEvents {

    private SAServerModule              serverModule;
    private SAVASTModule                vastModule;
    private SAMoatModule                moatModule;
    private SAViewableModule            viewableModule;
    private SAViewTimeModule            viewTimeModule;

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Set & unset the ad needed for triggering events
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public void setAd (ISASession session, SAAd ad) {
        serverModule = new SAServerModule(ad, session);
        vastModule = new SAVASTModule(ad);
        moatModule = new SAMoatModule(ad, true);
        viewableModule = new SAViewableModule();
        viewTimeModule = new SAViewTimeModule();
    }

    public void unsetAd () {
        serverModule = null;
        vastModule = null;
        moatModule = null;
        viewableModule = null;
        viewTimeModule = null;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Normal ad events
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public void triggerClickEvent () {
        if (serverModule != null) {
            serverModule.triggerClickEvent(null);
        }
    }

    public void triggerImpressionEvent () {
        if (serverModule != null) {
            serverModule.triggerImpressionEvent(null);
        }
    }

    public void triggerViewableImpressionEvent () {
        if (serverModule != null) {
            serverModule.triggerViewableImpressionEvent(null);
        }
    }

    public void triggerViewTimeEvent(int seconds) {
        if(serverModule != null) {
            serverModule.triggerViewableImpressionEvent(null);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Parental Gate Events
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public void triggerPgOpenEvent () {
        if (serverModule != null) {
            serverModule.triggerPgOpenEvent(null);
        }
    }

    public void triggerPgCloseEvent () {
        if (serverModule != null) {
            serverModule.triggerPgCloseEvent(null);
        }
    }

    public void triggerPgFailEvent () {
        if (serverModule != null) {
            serverModule.triggerPgFailEvent(null);
        }
    }

    public void triggerPgSuccessEvent () {
        if (serverModule != null) {
            serverModule.triggerPgSuccessEvent(null);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // VAST Events
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public String getVASTClickThroughEvent () {
        return vastModule != null ? vastModule.getVASTClickThroughEvent() : "";
    }

    public void triggerVASTClickThroughEvent () {
        if (vastModule != null) {
            vastModule.triggerVastClickThroughEvent(null);
        }
    }

    public void triggerVASTErrorEvent () {
        if (vastModule != null) {
            vastModule.triggerVASTErrorEvent(null);
        }
    }

    public void triggerVASTImpressionEvent () {
        if (vastModule != null) {
            vastModule.triggerVASTImpressionEvent(null);
        }
    }

    public void triggerVASTCreativeViewEvent () {
        if (vastModule != null) {
            vastModule.triggerVASTCreativeViewEvent(null);
        }
    }

    public void triggerVASTStartEvent () {
        if (vastModule != null) {
            vastModule.triggerVASTStartEvent(null);
        }
    }

    public void triggerVASTFirstQuartileEvent () {
        if (vastModule != null) {
            vastModule.triggerVASTFirstQuartileEvent(null);
        }
    }

    public void triggerVASTMidpointEvent () {
        if (vastModule != null) {
            vastModule.triggerVASTMidpointEvent(null);
        }
    }

    public void triggerVASTThirdQuartileEvent () {
        if (vastModule != null) {
            vastModule.triggerVASTThirdQuartileEvent(null);
        }
    }

    public void triggerVASTCompleteEvent () {
        if (vastModule != null) {
            vastModule.triggerVASTCompleteEvent(null);
        }
    }

    public void triggerVASTClickTrackingEvent () {
        if (vastModule != null) {
            vastModule.triggerVASTClickTrackingEvent(null);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // View Time
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public void observeViewTime(ViewGroup view, SAViewTimeModule.Listener listener) {
        if (viewTimeModule != null) {
            viewTimeModule.observeViewTime(view, listener);
        }
    }

    public void stopViewTime() {
        if(viewTimeModule != null) {
            viewTimeModule.stop();
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Viewable impression
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public boolean isChildInRect (ViewGroup layout) {
        return viewableModule != null && viewableModule.isChildInRect(layout);
    }

    public void checkViewableStatusForDisplay (ViewGroup layout, SAViewableModule.Listener listener) {
        if (viewableModule != null) {
            viewableModule.checkViewableStatusForDisplay (layout, listener);
        }
    }

    public void checkViewableStatusForVideo (ViewGroup layout, SAViewableModule.Listener listener) {
        if (viewableModule != null) {
            viewableModule.checkViewableStatusForVideo (layout, listener);
        }

    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // MOAT
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public static void initMoat (Application application, boolean loggingEnabled) {
        SAMoatModule.initMoat(application, loggingEnabled);
    }

    public String startMoatTrackingForDisplay(WebView view) {
        return moatModule != null ? moatModule.startMoatTrackingForDisplay(view) : "";
    }

    public boolean stopMoatTrackingForDisplay() {
        return moatModule == null || moatModule.stopMoatTrackingForDisplay();
    }

    public boolean startMoatTrackingForVideoPlayer(VideoView videoView, int duration){
        return moatModule == null || moatModule.startMoatTrackingForVideoPlayer(videoView, duration);
    }

    public boolean sendMoatPlayingEvent (int position) {
        return moatModule == null || moatModule.sendPlayingEvent(position);
    }

    public boolean sendMoatStartEvent (int position) {
        return moatModule == null || moatModule.sendStartEvent(position);
    }

    public boolean sendMoatFirstQuartileEvent (int position) {
        return moatModule == null || moatModule.sendFirstQuartileEvent(position);
    }

    public boolean sendMoatMidpointEvent (int position) {
        return moatModule == null || moatModule.sendMidpointEvent(position);
    }

    public boolean sendMoatThirdQuartileEvent (int position) {
        return moatModule == null || moatModule.sendThirdQuartileEvent(position);
    }

    public boolean sendMoatCompleteEvent (int position) {
        return moatModule == null || moatModule.sendCompleteEvent(position);
    }

    public boolean stopMoatTrackingForVideoPlayer() {
        return moatModule == null || moatModule.stopMoatTrackingForVideoPlayer();
    }

    public void disableMoatLimiting () {
        if (moatModule != null) {
            moatModule.disableMoatLimiting();
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Getters
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public SAServerModule getServerModule() {
        return serverModule;
    }

    public SAVASTModule getVastModule() {
        return vastModule;
    }

    public SAMoatModule getMoatModule() {
        return moatModule;
    }

    public SAViewableModule getViewableModule() {
        return viewableModule;
    }
}

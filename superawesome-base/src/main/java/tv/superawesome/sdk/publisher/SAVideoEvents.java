package tv.superawesome.sdk.publisher;

import android.util.Log;
import android.view.ViewGroup;

import tv.superawesome.lib.saevents.SAEvents;
import tv.superawesome.lib.saevents.SAViewableModule;
import tv.superawesome.lib.savideoplayer.IVideoPlayer;

public class SAVideoEvents {

    interface  Listener {
        void hasBeenVisible();
    }

    private SAEvents events;

    public Listener listener;

    private boolean isStartHandled = false;
    private boolean is2SHandled = false;
    private boolean isFirstQuartileHandled = false;
    private boolean isMidpointHandled = false;
    private boolean isThirdQuartileHandled = false;

    public SAVideoEvents(SAEvents events, Listener listener) {
        this.events = events;
        this.listener = listener;
    }

    public void prepare(IVideoPlayer videoPlayer, int time, int duration) {
        if (videoPlayer != null && videoPlayer.getSurface() != null) {
            events.startMoatTrackingForVideoPlayer(videoPlayer.getSurface(), duration);
        }
    }

    public void complete(IVideoPlayer videoPlayer, int time, int duration) {
        Log.d("SuperAwesome", " SAVideoEvents.complete: time: " + time + " duration: " + duration);
        events.sendMoatCompleteEvent(duration);
        events.triggerVASTCompleteEvent();
        events.stopMoatTrackingForVideoPlayer();
    }

    public void error(IVideoPlayer videoPlayer, int time, int duration) {
        Log.d("SuperAwesome", " SAVideoEvents.error: time: " + time + " duration: " + duration);

        events.stopMoatTrackingForVideoPlayer();
        events.triggerVASTErrorEvent();
    }

    public void time(IVideoPlayer videoPlayer, int time, int duration) {
        Log.d("SuperAwesome", "SAVideoEvents.time: time: " + time + " duration: " + duration);

        // Start
        if (time >= 1 && !isStartHandled) {
            isStartHandled = true;

            // send vast events - including impression
            events.triggerVASTImpressionEvent();
            events.triggerVASTStartEvent();
            events.triggerVASTCreativeViewEvent();

            // moat
            events.sendMoatPlayingEvent(time);
            events.sendMoatStartEvent(time);
        }
        // 2 second (viewability)
        if (time >= 2000 && !is2SHandled) {
            is2SHandled = true;

            if (videoPlayer instanceof ViewGroup) {
                ViewGroup player = (ViewGroup) videoPlayer;
                events.checkViewableStatusForVideo(player, new SAViewableModule.Listener() {
                    @Override
                    public void saDidFindViewOnScreen(boolean isViewable) {
                        if (isViewable) {
                            events.triggerViewableImpressionEvent();
                            if (listener != null) {
                                listener.hasBeenVisible();
                            }
                        }
                    }
                });
            }
        }
        // 1/4
        if (time >= duration / 4 && !isFirstQuartileHandled) {
            isFirstQuartileHandled = true;

            // send events
            events.sendMoatFirstQuartileEvent(time);
            events.triggerVASTFirstQuartileEvent();
        }
        // 1/2
        if (time >= duration / 2 && !isMidpointHandled) {
            isMidpointHandled = true;

            // send events
            events.sendMoatMidpointEvent(time);
            events.triggerVASTMidpointEvent();
        }
        // 3/4
        if (time >= 3 * duration / 4 && !isThirdQuartileHandled) {
            isThirdQuartileHandled = true;

            // send events
            events.sendMoatThirdQuartileEvent(time);
            events.triggerVASTThirdQuartileEvent();
        }
    }
}

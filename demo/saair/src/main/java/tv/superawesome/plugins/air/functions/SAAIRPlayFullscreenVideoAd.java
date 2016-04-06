package tv.superawesome.plugins.air.functions;

import android.content.Context;
import android.util.Log;

import com.adobe.fre.FREContext;
import com.adobe.fre.FREFunction;
import com.adobe.fre.FREInvalidObjectException;
import com.adobe.fre.FREObject;
import com.adobe.fre.FRETypeMismatchException;
import com.adobe.fre.FREWrongThreadException;

import org.json.JSONException;
import org.json.JSONObject;

import tv.superawesome.sdk.models.SAAd;
import tv.superawesome.sdk.parser.SAParser;
import tv.superawesome.sdk.listeners.SAAdListener;
import tv.superawesome.sdk.listeners.SAParentalGateListener;
import tv.superawesome.sdk.listeners.SAVideoAdListener;
import tv.superawesome.sdk.views.SAVideoActivity;

/**
 * Created by gabriel.coman on 17/03/16.
 */
public class SAAIRPlayFullscreenVideoAd implements FREFunction {
    @Override
    public FREObject call(final FREContext freContext, FREObject[] freObjects) {
        /** setup vars with default values */
        Log.d("AIREXT", "playFullscreenVideoAd");

        if (freObjects.length == 6){
            try {
                /** get variables */
                final String name = freObjects[0].getAsString();
                final int placementId = freObjects[1].getAsInt();
                final String adJson = freObjects[2].getAsString();
                final boolean isParentalGateEnabled = freObjects[3].getAsBool();
                final boolean shouldShowCloseButton = freObjects[4].getAsBool();
                final boolean shouldAutomaticallyCloseAtEnd = freObjects[5].getAsBool();
                final Context context = freContext.getActivity().getApplicationContext();

                Log.d("AIREXT", "Meta: " + name + "/" + placementId + "/" + isParentalGateEnabled + "/" + shouldShowCloseButton + "/" + shouldAutomaticallyCloseAtEnd);
                Log.d("AIREXT", "adJson: " + adJson);

                try {
                    JSONObject dataJson = new JSONObject(adJson);
                    SAAd ad = SAParser.parseDictionaryIntoAd(dataJson, placementId);

                    if (ad != null) {
                        Log.d("AIREXT", "ad data valid");
                        /** create the video */
                        SAVideoActivity video = new SAVideoActivity(freContext.getActivity());
                        video.setAd(ad);
                        video.setIsParentalGateEnabled(isParentalGateEnabled);
                        video.setShouldShowCloseButton(shouldShowCloseButton);
                        video.setShouldAutomaticallyCloseAtEnd(shouldAutomaticallyCloseAtEnd);
                        video.setAdListener(new SAAdListener() {
                            @Override
                            public void adWasShown(int placementId) {
                                String meta = "{\"name\":\"" + name + "\", \"func\":\"adWasShown\"}";
                                freContext.dispatchStatusEventAsync(meta, "");
                            }

                            @Override
                            public void adFailedToShow(int placementId) {
                                String meta = "{\"name\":\"" + name + "\", \"func\":\"adFailedToShow\"}";
                                freContext.dispatchStatusEventAsync(meta, "");
                            }

                            @Override
                            public void adWasClosed(int placementId) {
                                String meta = "{\"name\":\"" + name + "\", \"func\":\"adWasClosed\"}";
                                freContext.dispatchStatusEventAsync(meta, "");
                            }

                            @Override
                            public void adWasClicked(int placementId) {
                                String meta = "{\"name\":\"" + name + "\", \"func\":\"adWasClicked\"}";
                                freContext.dispatchStatusEventAsync(meta, "");
                            }

                            @Override
                            public void adHasIncorrectPlacement(int placementId) {
                                String meta = "{\"name\":\"" + name + "\", \"func\":\"adHasIncorrectPlacement\"}";
                                freContext.dispatchStatusEventAsync(meta, "");
                            }
                        });
                        video.setVideoAdListener(new SAVideoAdListener() {
                            @Override
                            public void adStarted(int placementId) {
                                String meta = "{\"name\":\"" + name + "\", \"func\":\"adStarted\"}";
                                freContext.dispatchStatusEventAsync(meta, "");
                            }

                            @Override
                            public void videoStarted(int placementId) {
                                String meta = "{\"name\":\"" + name + "\", \"func\":\"videoStarted\"}";
                                freContext.dispatchStatusEventAsync(meta, "");
                            }

                            @Override
                            public void videoReachedFirstQuartile(int placementId) {
                                String meta = "{\"name\":\"" + name + "\", \"func\":\"videoReachedFirstQuartile\"}";
                                freContext.dispatchStatusEventAsync(meta, "");
                            }

                            @Override
                            public void videoReachedMidpoint(int placementId) {
                                String meta = "{\"name\":\"" + name + "\", \"func\":\"videoReachedMidpoint\"}";
                                freContext.dispatchStatusEventAsync(meta, "");
                            }

                            @Override
                            public void videoReachedThirdQuartile(int placementId) {
                                String meta = "{\"name\":\"" + name + "\", \"func\":\"videoReachedThirdQuartile\"}";
                                freContext.dispatchStatusEventAsync(meta, "");
                            }

                            @Override
                            public void videoEnded(int placementId) {
                                String meta = "{\"name\":\"" + name + "\", \"func\":\"videoEnded\"}";
                                freContext.dispatchStatusEventAsync(meta, "");
                            }

                            @Override
                            public void adEnded(int placementId) {
                                String meta = "{\"name\":\"" + name + "\", \"func\":\"adEnded\"}";
                                freContext.dispatchStatusEventAsync(meta, "");
                            }

                            @Override
                            public void allAdsEnded(int placementId) {
                                String meta = "{\"name\":\"" + name + "\", \"func\":\"allAdsEnded\"}";
                                freContext.dispatchStatusEventAsync(meta, "");
                            }
                        });
                        video.setParentalGateListener(new SAParentalGateListener() {
                            @Override
                            public void parentalGateWasCanceled(int placementId) {
                                String meta = "{\"name\":\"" + name + "\", \"func\":\"parentalGateWasCanceled\"}";
                                freContext.dispatchStatusEventAsync(meta, "");
                            }

                            @Override
                            public void parentalGateWasFailed(int placementId) {
                                String meta = "{\"name\":\"" + name + "\", \"func\":\"parentalGateWasFailed\"}";
                                freContext.dispatchStatusEventAsync(meta, "");
                            }

                            @Override
                            public void parentalGateWasSucceded(int placementId) {
                                String meta = "{\"name\":\"" + name + "\", \"func\":\"parentalGateWasSucceded\"}";
                                freContext.dispatchStatusEventAsync(meta, "");
                            }
                        });
                        video.play();

                    } else {
                        String meta = "{\"name\":\"" + name + "\", \"func\":\"adFailedToShow\"}";
                        freContext.dispatchStatusEventAsync(meta, "");
                    }
                } catch (JSONException e) {
                    String meta = "{\"name\":\"" + name + "\", \"func\":\"adFailedToShow\"}";
                    freContext.dispatchStatusEventAsync(meta, "");
                }

            } catch (FRETypeMismatchException | FREInvalidObjectException | FREWrongThreadException  e) {
                e.printStackTrace();
            }
        }

        return null;
    }
}

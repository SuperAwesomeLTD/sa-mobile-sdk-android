/**
 * @Copyright:   SuperAwesome Trading Limited 2017
 * @Author:      Gabriel Coman (gabriel.coman@superawesome.tv)
 */
package tv.superawesome.sdk.cpi;

import android.content.Context;
import android.content.Intent;

import org.json.JSONObject;

import tv.superawesome.lib.saevents.SAEvents;
import tv.superawesome.lib.sajsonparser.SAJsonParser;
import tv.superawesome.lib.samodelspace.SAReferralData;
import tv.superawesome.lib.sasession.SAConfiguration;
import tv.superawesome.lib.sasession.SASession;
import tv.superawesome.lib.sautils.SAUtils;
import tv.superawesome.sdk.SuperAwesome;

/**
 * Class that abstracts away dealing with referral data coming from the Google Play Store and
 * sending an event to the ad server.
 */
public class SAReferralEvent {

    // current context and intent private members
    private Context context;
    private Intent intent;

    /**
     * Constructor taking the current context and an intent as params
     *
     * @param context current context (activity or fragment)
     * @param intent  an intent
     */
    public SAReferralEvent(Context context, Intent intent) {
        this.context = context;
        this.intent = intent;
    }

    /**
     * Main method of the class that checks for referral data and, if it's valid, sends a
     * custom event to the ad server
     */
    public void sendEvent () {

        // now get the referral data
        String referrer = intent.getStringExtra("referrer");
        referrer = referrer != null ? referrer : "";
        referrer = referrer.replace("=", " : ");
        referrer = referrer.replace("%3D", " : ");
        referrer = referrer.replace("\\&", ", ");
        referrer = referrer.replace("&", ", ");
        referrer = referrer.replace("%26", ", ");
        referrer = "{ " + referrer + " }";
        referrer = referrer.replace("utm_source", "\"utm_source\"");
        referrer = referrer.replace("utm_campaign", "\"utm_campaign\"");
        referrer = referrer.replace("utm_term", "\"utm_term\"");
        referrer = referrer.replace("utm_content", "\"utm_content\"");
        referrer = referrer.replace("utm_medium", "\"utm_medium\"");

        SAReferralData cpiData = new SAReferralData(referrer);

        if (cpiData.isValid()) {

            SAUtils.SAConnectionType connectionType = SAUtils.getNetworkConnectivity(context);

            JSONObject cpiDict = SAJsonParser.newObject(new Object[]{
                    "sdkVersion", SuperAwesome.getInstance().getSDKVersion(),
                    "rnd", SAUtils.getCacheBuster(),
                    "ct", connectionType.ordinal(),
                    "data", SAUtils.encodeDictAsJsonDict(SAJsonParser.newObject(new Object[]{
                        "placement", cpiData.placementId,
                        "line_item", cpiData.lineItemId,
                        "creative", cpiData.creativeId,
                        "type", "custom.referred_install"
                    }))
            });

            // setup a configuration
            SASession session = new SASession(this.context);
            SAConfiguration configuration = SAConfiguration.fromValue(cpiData.configuration);
            session.setConfiguration(configuration);

            // form the cpi URL
            String cpiEventURL = session.getBaseUrl() + "/event?" + SAUtils.formGetQueryFromDict(cpiDict);

            // send the event
            SAEvents events = new SAEvents(context);
            events.sendEventToURL(cpiEventURL);
        }
    }

}

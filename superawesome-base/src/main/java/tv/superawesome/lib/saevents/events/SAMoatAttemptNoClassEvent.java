package tv.superawesome.lib.saevents.events;

import org.json.JSONObject;

import java.util.concurrent.Executor;

import tv.superawesome.lib.sajsonparser.SAJsonParser;
import tv.superawesome.lib.samodelspace.saad.SAAd;
import tv.superawesome.lib.sasession.session.ISASession;

public class SAMoatAttemptNoClassEvent extends SAServerEvent {

    public SAMoatAttemptNoClassEvent(SAAd ad, ISASession session) {
        super(ad, session);
    }

    public SAMoatAttemptNoClassEvent(SAAd ad, ISASession session, Executor executor, int timeout, boolean isDebug) {
        super(ad, session, executor, timeout, isDebug);
    }

    @Override
    public String getEndpoint() {
        return "/moat";
    }


    @Override
    public JSONObject getQuery() {
        try {
            return SAJsonParser.newObject(
                    "placement", ad.placementId,
                    "creative", ad.creative.id,
                    "line_item", ad.lineItemId,
                    "sdkVersion", session.getVersion(),
                    "bundle", session.getPackageName(),
                    "ct", session.getConnectionType().ordinal(),
                    "no_image", true,
                    "rnd", session.getCachebuster(),
                    "type", "attempt_no_class"
            );
        } catch (Exception e) {
            return new JSONObject();
        }
    }
}

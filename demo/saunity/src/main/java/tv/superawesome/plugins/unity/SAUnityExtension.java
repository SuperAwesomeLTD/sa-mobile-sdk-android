package tv.superawesome.plugins.unity;

import com.unity3d.player.*;

/**
 * Created by gabriel.coman on 21/01/16.
 */
public class SAUnityExtension {

    /**
     * Function that sends a message back to Unity
     * @param unityAd the unique unity Ad name that send a request to SAUnityExtension.java plugin in the first place
     * @param callback the actual callback that's supposed to get called on the Unity side
     * @param payloadName used only when sending Ad json data back to Unity really
     * @param payloadData the payload contents, again, used when sending Ad json data back to Unity
     */
    public static void SendUnityMsgPayload(String unityAd, String callback, int placementId, String payloadName, String payloadData) {
        String payload = "{ \"placementId\":\"" + placementId + "\", \"type\":\""+callback+"\",\""+payloadName+"\":" + payloadData + "}";
        UnityPlayer.UnitySendMessage(unityAd, "nativeCallback", payload);
    }

    /**
     * Simplified helper function of SendUnityMsgPayload() that just sends out a callback to Unity
     * @param unityAd the unique unity object that generated the request in the first place
     * @param callback the callback to be sent
     */
    public static void SendUnityMsg(String unityAd, int placementId, String callback) {
        SendUnityMsgPayload(unityAd, callback, placementId, "na", "\"na\"");
    }
}
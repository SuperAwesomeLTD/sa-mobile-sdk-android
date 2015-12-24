/**
 * @class: SAAd.java
 * @copyright: (c) 2015 SuperAwesome Ltd. All rights reserved.
 * @author: Gabriel Coman
 * @date: 28/09/2015
 *
 */
package tv.superawesome.sdk.data.Models;

/**
 * Imports needed for this class
 */
import tv.superawesome.lib.sautils.*;

/**
 * @brief:
 * This model class contains all information that is received from the server
 * when an Ad is requested, as well as some aux fields that will be generated
 * by the SDK
 */
public class SAAd {

    /** the SA server can send an error; if that's the case, this field will not be nill */
    public int error;

    /** the ID of the placement that the ad was sent for */
    public int placementId;

    /** line item */
    public int lineItemId;

    /** the ID of the campaign that the ad is a part of */
    public int campaignId;

    /** is true when the ad is a test ad */
    public boolean isTest;

    /**
     * is true when ad is fallback (fallback ads are sent when there are no
     * real ads to display for a certain placement)
     */
    public boolean isFallback;
    public boolean isFill;

    /**
     * the HTML of the ad - this is generated by the SDK based on the type of ad
     * that is presented (see SAFormatter.h)
     */
    public String adHTML;

    /** pointer to the creative data associated with the ad */
    public SACreative creative;

    /** aux print function */
    public void print(){
        String printout = "";
        printout += "error: " + error;
        printout += "placementId: " + placementId;
        printout += "lineItemId: " + lineItemId;
        printout += "campaignId: " + campaignId;
        printout += "isTest: " + isTest;
        printout += "isFallback: " + isFallback;
        printout += "isFill: " + isFill;
        printout += "adHTML: " + adHTML;
        SALog.Log(printout);
        creative.print();
    }
}

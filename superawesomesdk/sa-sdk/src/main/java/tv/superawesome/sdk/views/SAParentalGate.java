package tv.superawesome.sdk.views;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;
import android.widget.EditText;

import java.lang.ref.WeakReference;
import java.util.Objects;
import java.util.Random;

import tv.superawesome.lib.sautils.SALog;
import tv.superawesome.sdk.data.Models.SAAd;
import tv.superawesome.sdk.listeners.SAParentalGateListener;

/**
 * Created by connor.leigh-smith on 28/08/15.
 *
 * The SAParentalGate class. It's main goal is to show an AlertDialog
 * that challenges the user to respond to a simple math riddle
 *
 */
public class SAParentalGate {

    /** constants for the rand nr. generator */
    private static final int RAND_MIN = 50;
    private static final int RAND_MAX = 99;

    /** JAVA constants for text based stuff */
    private static final String SA_CHALLANGE_ALERTVIEW_TITLE = "Parental Gate";
    private static final String SA_CHALLANGE_ALERTVIEW_MESSAGE = "Please solve the following problem to continue: ";
    private static final String SA_CHALLANGE_ALERTVIEW_CANCELBUTTON_TITLE = "Cancel";
    private static final String SA_CHALLANGE_ALERTVIEW_CONTINUEBUTTON_TITLE = "Continue";

    private static final String SA_ERROR_ALERTVIEW_TITLE = "Oops! That was the wrong answer.";
    private static final String SA_ERROR_ALERTVIEW_MESSAGE = "Please seek guidance from a responsible adult to help you continue.";
    private static final String SA_ERROR_ALERTVIEW_CANCELBUTTON_TITLE = "Ok";

    /** variables private */
    private int startNum;
    private int endNum;
    private Context c = null;
    private WeakReference<Object> parentRef = null;
    private SAParentalGateListener listener;
    private SAAd refAd;

    /** the alert dialog */
    private AlertDialog dialog;

    public SAParentalGate(Context c, Object parent, SAAd _refAd){
        super();
        this.c = c;
        this.parentRef = new WeakReference<Object> (parent);
        SALog.Log("Object is " + parent.getClass().getName());
        SALog.Log("Ref is " + parentRef.getClass().getName());
        SALog.Log("Ref get is " + parentRef.get().getClass().getName());
        this.refAd = _refAd;

        if (this.refAd == null){
            this.refAd = new SAAd();
        }
    }

    /** show function */
    public void show() {
        startNum = randInt(RAND_MIN, RAND_MAX);
        endNum = randInt(RAND_MIN, RAND_MAX);

        /* we have an alert dialog builder */
        final AlertDialog.Builder alert = new AlertDialog.Builder(c);
        // set title and message
        alert.setTitle(SA_CHALLANGE_ALERTVIEW_TITLE);
        alert.setCancelable(false);
        alert.setMessage(SA_CHALLANGE_ALERTVIEW_MESSAGE + startNum + " + " + endNum + " = ? ");

        // Set an EditText view to get user input
        final EditText input = new EditText(c);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        alert.setView(input);

        final AlertDialog.Builder aContinue = alert.setPositiveButton(SA_CHALLANGE_ALERTVIEW_CONTINUEBUTTON_TITLE, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                if (!input.getText().toString().equals("")) {
                    int userValue = Integer.parseInt(input.getText().toString());

                    // dismiss
                    dialog.dismiss();

                    if (userValue == (startNum + endNum)) {
                        // go on success way
                        if (listener != null) {
                            listener.parentalGateWasSucceded(refAd.placementId);
                        }

                        String refClassName = parentRef.get().getClass().getName();
                        SALog.Log("ref class name " + refClassName);
                        String bannerName = SABannerAd.class.getCanonicalName();
                        String videoName = SAVideoAd.class.getCanonicalName();

                        if ((parentRef.get() instanceof SAViewProtocol) == true) {
                            SALog.Log("implenents");
                        }

                        if (refClassName.contains(bannerName)) {
                            ((SABannerAd) parentRef.get()).advanceToClick();
                        } else if (refClassName.contains(videoName)) {
                            ((SAVideoAd) parentRef.get()).advanceToClick();
                        }
                    } else {

                        // go on error way
                        AlertDialog.Builder erroralert = new android.app.AlertDialog.Builder(c);
                        erroralert.setTitle(SA_ERROR_ALERTVIEW_TITLE);
                        erroralert.setMessage(SA_ERROR_ALERTVIEW_MESSAGE);

                        // set button action
                        erroralert.setPositiveButton(SA_ERROR_ALERTVIEW_CANCELBUTTON_TITLE, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {

                                // dismiss this
                                dialog.dismiss();

                                // do nothing
                                if (listener != null) {
                                    listener.parentalGateWasFailed(refAd.placementId);
                                }
                                return;
                            }
                        });
                        erroralert.show();

                    }
                }

                return;
            }
        });

        alert.setNegativeButton(SA_CHALLANGE_ALERTVIEW_CANCELBUTTON_TITLE, new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {

                // dismiss
                dialog.dismiss();

                // go on cancel way
                if (listener != null) {
                    listener.parentalGateWasCanceled(refAd.placementId);
                }
                return;
            }
        });

        dialog = alert.create();
        dialog.show();
    }

    /**
     * Function that closes this
     */
    public void close() {
        dialog.cancel();
    }

    // aux function for random number generation
    private static int randInt(int min, int max) {
        Random rand = new Random();
        return rand.nextInt((max - min) + 1) + min;
    }

    // setters and getters (it's 2015, why is not this automated by now?)
    public void setListener(SAParentalGateListener list){
        this.listener = list;
    }

    public  SAParentalGateListener getListener(){
        return this.listener;
    }
}
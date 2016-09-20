package tv.superawesome.sdk.capper;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;

import java.io.IOException;
import tv.superawesome.lib.sautils.SAUtils;

/**
 * Created by gabriel.coman on 16/02/16.
 */
public class SACapper {

    // constant that specifies the local-pref dict key to use
    private static final String SUPER_AWESOME_FIRST_PART_DAU = "SUPER_AWESOME_FIRST_PART_DAU";

    /**
     * static method to enable Capping
     * Through it's listener interface it returns a dauID
     * The dauID can be non-0 -> in which case it's valid
     * or it can be 0 -> in which case it's not valid (user does not have tracking enabled or
     * gms enabled)
     **/
    public void enableCapping(final Context context, final SACapperInterface listener) {

        // guard against horrible errors!
        if (!SAUtils.isClassAvailable("com.google.android.gms.ads.identifier.AdvertisingIdClient")) {
            listener.didFindDAUId(0);
            return;
        }

        AsyncTask<Void, Void, String> task = new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {

                // get the ad data from Google Play Services
                String adString = "";
                try {
                    AdvertisingIdClient.Info adInfo = AdvertisingIdClient.getAdvertisingIdInfo(context);
                    if (!adInfo.isLimitAdTrackingEnabled()) {
                        adString = adInfo.getId();
                    }
                } catch (VerifyError | IOException | NullPointerException | GooglePlayServicesNotAvailableException | GooglePlayServicesRepairableException e) {
                    // do nothing
                }

                return adString;
            }

            @Override
            protected void onPostExecute(String firstPartOfDAU) {
                super.onPostExecute(firstPartOfDAU);

                if (firstPartOfDAU != null && !firstPartOfDAU.isEmpty()){

                    // continue as if  user has Ad Tracking enabled and all
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

                    // create the dauID
                    String secondPartOfDAU = preferences.getString(SUPER_AWESOME_FIRST_PART_DAU, null);

                    if (secondPartOfDAU == null || secondPartOfDAU.isEmpty()) {
                        SharedPreferences.Editor editor = preferences.edit();
                        secondPartOfDAU = SAUtils.generateUniqueKey();
                        editor.putString(SUPER_AWESOME_FIRST_PART_DAU, secondPartOfDAU);
                        editor.apply();
                    }

                    int hash1 = Math.abs(firstPartOfDAU.hashCode());
                    int hash2 = Math.abs(secondPartOfDAU.hashCode());
                    int dauHash = Math.abs(hash1 ^ hash2);
                    Log.d("SuperAwesome", "hashes " + hash1 + "-" + hash2 + "-" + dauHash);

                    if (listener != null){
                        listener.didFindDAUId(dauHash);
                    }
                }
                // either the service is not available or the user does not have Google Play Services
                else {
                    if (listener != null) {
                        listener.didFindDAUId(0);
                    }
                }
            }
        };
        task.execute();
    }
}

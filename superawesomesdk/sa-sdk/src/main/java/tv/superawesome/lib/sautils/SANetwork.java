package tv.superawesome.lib.sautils;

import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by gabriel.coman on 06/04/16.
 */
public class SANetwork {

    /**
     * More manageable asyncGet function, based on the generic SAASyncTask class
     * @param urlString - the URL to make the request to
     * @param listener - the listener
     */
    public void asyncGet(String urlString, JSONObject querydict, final SANetworkInterface listener) {

        /** form the actual final endpoint */
        final String endpoint = urlString + (!SAUtils.isJSONEmpty(querydict) ? "?" + SAUtils.formGetQueryFromDict(querydict) : "");

        /** create a new SAAsync Task */
        SAAsyncTask task = new SAAsyncTask(SAApplication.getSAApplicationContext(), new SAAsyncTaskInterface() {
            @Override
            public Object taskToExecute() throws Exception {

                URL url;
                String response = null;
                HttpsURLConnection conn = null;
                InputStreamReader in = null;
                try {
                    url = new URL(endpoint);
                    conn = (HttpsURLConnection) url.openConnection();
                    conn.setReadTimeout(15000);
                    conn.setConnectTimeout(15000);
                    conn.setRequestMethod("GET");
                    conn.setDoInput(true);
                    conn.setRequestProperty("Content-Type", "application/json");
                    conn.setRequestProperty("User-Agent", SAUtils.getUserAgent());

                    int responseCode = conn.getResponseCode();

                    if (responseCode == HttpsURLConnection.HTTP_OK) {
                        String line;
                        response = "";
                        in = new InputStreamReader(conn.getInputStream());
                        BufferedReader reader = new BufferedReader(in);
                        while ((line = reader.readLine()) != null) {
                            response += line;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    in.close();
                    conn.disconnect();
                }

                return response;
            }

            @Override
            public void onFinish(Object result) {
                if (result != null) {
                    String strRes = result.toString();
                    if (strRes.length() > 10) {
                        strRes = strRes.substring(0, 9);
                    }
                    Log.d("SuperAwesome", "[OK] " + endpoint + " ==> " + strRes);
                    listener.success(result);
                } else {
                    Log.d("SuperAwesome", "[NOK] " + endpoint);
                    listener.failure();
                }
            }

            @Override
            public void onError() {
                Log.d("SuperAwesome", "[NOK] " + endpoint);
                listener.failure();
            }
        });
    }
}

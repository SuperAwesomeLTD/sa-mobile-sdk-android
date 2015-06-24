package tv.superawesome.superawesomesdk;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import tv.superawesome.superawesomesdk.view.AdView;

/**
 * Created by connor.leigh-smith on 24/06/15.
 */
public class AdLoader extends AsyncTask<String, Integer, JSONObject> {

    private AdView delegate;

    public AdLoader(AdView delegate) {
        this.delegate = delegate;
    }

    @Override
    protected JSONObject doInBackground(String[] params) {
        try {
            return getJson(new URL(params[0]));
        } catch (Exception ex) {
            System.out.println("Could not fetch JSON from server");
        }
        return null;
    }

    @Override
    protected void onPostExecute(JSONObject response) {
        this.delegate.onAdLoaded(response);
    }


    private JSONObject getJson(URL url) throws IOException, JSONException {
        this.delegate.onAdBeginLoad();
        try {
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            try {
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                String inputStr;
                BufferedReader streamReader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
                StringBuilder responseStrBuilder = new StringBuilder();

                while ((inputStr = streamReader.readLine()) != null) responseStrBuilder.append(inputStr);

                JSONObject response = new JSONObject(responseStrBuilder.toString());

                return response;
            } catch (Exception e) {
                throw e;
            } finally {
                urlConnection.disconnect();
            }
        } catch (Exception e) {
            throw e;
        }

    }
}
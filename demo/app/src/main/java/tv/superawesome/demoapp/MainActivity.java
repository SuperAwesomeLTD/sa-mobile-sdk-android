package tv.superawesome.demoapp;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import tv.superawesome.lib.savast.models.SAVASTAd;
import tv.superawesome.sdk.SuperAwesome;
import tv.superawesome.sdk.loader.SALoader;
import tv.superawesome.sdk.loader.SALoaderInterface;
import tv.superawesome.sdk.models.SAAd;
import tv.superawesome.sdk.views.SABannerAd;
import tv.superawesome.sdk.views.SAInterstitialActivity;
import tv.superawesome.sdk.views.SAVideoActivity;

public class MainActivity extends Activity implements SALoaderInterface {

    private SALoader loader = null;
    private SAAd bannerData = null;
    private SAAd interstitial1Data = null;
    private SAAd interstitial2Data = null;
    private SAAd interstitial3Data = null;
    private SAAd video1Data = null;
    private SAAd video2Data = null;
    private SABannerAd bannerAd = null;

    /** the options list */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SuperAwesome.getInstance().setConfigurationStaging();
        SuperAwesome.getInstance().setApplicationContext(getApplicationContext());
        SuperAwesome.getInstance().disableTestMode();

        if (savedInstanceState == null) {
            loader = new SALoader();
            loader.loadAd(113, this);
            loader.loadAd(114, this);
            loader.loadAd(115, this);
            loader.loadAd(116, this);
            loader.loadAd(117, this);
            loader.loadAd(118, this);
        } else {
            bannerData = (SAAd) savedInstanceState.get("bannerData");
            interstitial1Data = (SAAd) savedInstanceState.get("interstitial1Data");
            interstitial2Data = (SAAd) savedInstanceState.get("interstitial2Data");
            interstitial3Data = (SAAd) savedInstanceState.get("interstitial3Data");
            video1Data = (SAAd) savedInstanceState.get("video1Data");
            video2Data = (SAAd) savedInstanceState.get("video2Data");
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable("bannerData", bannerData);
        outState.putParcelable("interstitial1Data", interstitial1Data);
        outState.putParcelable("interstitial2Data", interstitial2Data);
        outState.putParcelable("interstitial3Data", interstitial3Data);
        outState.putParcelable("video1Data", video1Data);
        outState.putParcelable("video2Data", video2Data);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void didLoadAd(SAAd ad) {
        switch (ad.placementId) {
            case 113: bannerData = ad; break;
            case 114: interstitial1Data = ad; break;
            case 115: interstitial2Data = ad; break;
            case 116: video1Data = ad; break;
            case 117: video2Data = ad; break;
            case 118: interstitial3Data = ad; break;
        }
    }

    @Override
    public void didFailToLoadAdForPlacementId(int placementId) {
        Log.d("SuperAwesome", "Failed for " + placementId);
    }

    /** <Button Actions> */
    public void playBanner(View v){
        if (bannerData != null) {
            bannerAd = (SABannerAd) findViewById(R.id.mybanner);
            bannerAd.setAd(bannerData);
            bannerAd.play();
        }
    }

    public void playInterstitial1(View v){
        if (interstitial1Data != null){
            SAInterstitialActivity iad = new SAInterstitialActivity(MainActivity.this);
            iad.setAd(interstitial1Data);
            iad.play();
        }
    }

    public void playInterstitial2(View v){
        if (interstitial2Data != null) {
            SAInterstitialActivity iad = new SAInterstitialActivity(MainActivity.this);
            iad.setAd(interstitial2Data);
            iad.setShouldLockOrientation(true);
            iad.setLockOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            iad.play();
        }
    }

    public void playInterstitial3(View v){
        if (interstitial3Data != null){
            SAInterstitialActivity iad = new SAInterstitialActivity(MainActivity.this);
            iad.setAd(interstitial3Data);
            iad.play();
        }
    }

    public void playVideo1(View v){
        if (video1Data != null){

            JSONObject videoJson = video1Data.writeToJson();
            String videoString = videoJson.toString();
            TextView t = (TextView)findViewById(R.id.mytextbox1);
            t.setText(videoString);
            t.setMovementMethod(new ScrollingMovementMethod());
            try {
                SAAd ad2 = new SAAd(videoJson);
                String test = ad2.writeToJson().toString();
                TextView t2 = (TextView)findViewById(R.id.mytextbox2);
                t2.setText(test);
                t2.setMovementMethod(new ScrollingMovementMethod());
                if (test.equals(videoString)){
                    Log.d("SuperAwesome", "Is all OK");
                    Log.d("SuperWAesome", test);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

//            SAVideoActivity fvad = new SAVideoActivity(MainActivity.this);
//            fvad.setAd(video1Data);
//            fvad.setShouldAutomaticallyCloseAtEnd(false);
//            fvad.play();
        }
    }

    public void playVideo2(View v){
        if (video2Data != null){
            SAVideoActivity fvad = new SAVideoActivity(MainActivity.this);
            fvad.setAd(video2Data);
            fvad.setShouldLockOrientation(true);
            fvad.setLockOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            fvad.setShouldShowCloseButton(false);
            fvad.play();
        }
    }
}

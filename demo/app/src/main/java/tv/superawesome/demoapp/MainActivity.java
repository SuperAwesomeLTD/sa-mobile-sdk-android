package tv.superawesome.demoapp;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.VideoView;

import tv.superawesome.lib.sautils.SAFileDownloader;
import tv.superawesome.sdk.SuperAwesome;
import tv.superawesome.sdk.loader.SALoader;
import tv.superawesome.sdk.loader.SALoaderListener;
import tv.superawesome.sdk.models.SAAd;
import tv.superawesome.sdk.views.SAInterstitialActivity;
import tv.superawesome.sdk.views.SAVideoAd;

public class MainActivity extends Activity implements SALoaderListener {

    private SALoader loader = null;
    private SAAd savedAd = null;
    private SAVideoAd videoAd = null;

    /** the options list */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SuperAwesome.getInstance().setConfigurationProduction();
        SuperAwesome.getInstance().setApplicationContext(getApplicationContext());
        SuperAwesome.getInstance().enableTestMode();

        if (savedInstanceState == null) {
            loader = new SALoader();
            loader.loadAd(28000, this);
        } else {
            savedAd = (SAAd) savedInstanceState.get("savedAd");
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable("savedAd", savedAd);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void didLoadAd(SAAd ad) {
        savedAd = ad;
        videoAd = (SAVideoAd) findViewById(R.id.SAVideoAd2Id);
        videoAd.setAd(savedAd);
        videoAd.play();
    }

    @Override
    public void didFailToLoadAdForPlacementId(int placementId) {
        Log.d("SuperAwesome", "Failed for " + placementId);
    }
}

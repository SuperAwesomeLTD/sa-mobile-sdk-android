package tv.superawesome.demoapp;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import tv.superawesome.sdk.SuperAwesome;
import tv.superawesome.lib.saadloader.SALoader;
import tv.superawesome.lib.saadloader.SALoaderInterface;
import tv.superawesome.lib.samodelspace.SAAd;
import tv.superawesome.sdk.views.SABannerAd;

public class MainActivity extends Activity {

//    private SALoader loader = null;
//    private SAAd bannerData = null;
//    private SAAd interstitial1Data = null;
//    private SAAd interstitial2Data = null;
//    private SAAd interstitial3Data = null;
//    private SAAd video1Data = null;
//    private SAAd video2Data = null;
    private SABannerAd bannerAd = null;
//    private SAVideoAd videoAd = null;

    /** the options list */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SuperAwesome.getInstance().setConfigurationStaging();
        SuperAwesome.getInstance().setApplicationContext(getApplicationContext());
        SuperAwesome.getInstance().disableTestMode();

        bannerAd = (SABannerAd) findViewById(R.id.mybanner);
        bannerAd.load(250);

//        if (savedInstanceState == null) {
//            loader = new SALoader();
////            loader.loadAd(236, this);
////            loader.loadAd(31378, this);
////            loader.loadAd(222, this);
////            loader.loadAd(223, this);
//            loader.loadAd(247, this);
////            loader.loadAd(113, this);
////            loader.loadAd(114, this);
////            loader.loadAd(115, this);
////            loader.loadAd(116, this);
////            loader.loadAd(117, this);
////            loader.loadAd(118, this);
////            loader.loadAd(230, this);
////            loader.loadAd(200, this);
//        } else {
//            bannerData = (SAAd) savedInstanceState.get("bannerData");
//            interstitial1Data = (SAAd) savedInstanceState.get("interstitial1Data");
//            interstitial2Data = (SAAd) savedInstanceState.get("interstitial2Data");
//            interstitial3Data = (SAAd) savedInstanceState.get("interstitial3Data");
//            video1Data = (SAAd) savedInstanceState.get("video1Data");
//            video2Data = (SAAd) savedInstanceState.get("video2Data");
//        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
//        outState.putParcelable("bannerData", bannerData);
//        outState.putParcelable("interstitial1Data", interstitial1Data);
//        outState.putParcelable("interstitial2Data", interstitial2Data);
//        outState.putParcelable("interstitial3Data", interstitial3Data);
//        outState.putParcelable("video1Data", video1Data);
//        outState.putParcelable("video2Data", video2Data);
        super.onSaveInstanceState(outState);
    }

//    @Override
//    public void didLoadAd(SAAd ad) {
//
//        Log.d("SuperAwesome-ADData", ad.writeToJson().toString());
//        Log.d("SuperAwesome-ADData", ad.placementId + " " + ad.saCampaignType.toString());
//
//        switch (ad.placementId) {
////            case 236: interstitial1Data = ad; break;
////            case 31378: interstitial1Data = ad; break;
////            case 222: bannerData = ad; break;
////            case 223: video1Data = ad; break;
////            case 230: video1Data = ad; break;
////            case 30260: video1Data = ad; break;
//            case 247: interstitial1Data = ad; break;
////            case 113: bannerData = ad; break;
////            case 114: interstitial1Data = ad; break;
////            case 115: interstitial2Data = ad; break;
////            case 116: video1Data = ad; break;
////            case 117: video2Data = ad; break;
////            case 118: interstitial3Data = ad; break;
////            case 200: bannerData = ad; break;
//        }
//    }
//
//    @Override
//    public void didFailToLoadAdForPlacementId(int placementId) {
//        Log.d("SuperAwesome", "Failed for " + placementId);
//    }

    /** <Button Actions> */
    public void playBanner(View v){
        bannerAd.play();
//        if (bannerData != null) {
//            bannerAd = (SABannerAd) findViewById(R.id.mybanner);
//            bannerAd.setAd(bannerData);
//            bannerAd.setIsParentalGateEnabled(false);
//            bannerAd.play();
//        }
    }

    public void playInterstitial1(View v){
//        if (interstitial1Data != null){
//            SAInterstitialAd iad = new SAInterstitialAd(MainActivity.this);
//            iad.setAd(interstitial1Data);
//            iad.play();
//        }
    }

    public void playInterstitial2(View v){
//        if (interstitial2Data != null) {
//            SAInterstitialAd iad = new SAInterstitialAd(MainActivity.this);
//            iad.setAd(interstitial2Data);
//            iad.setShouldLockOrientation(true);
//            iad.setLockOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
//            iad.play();
//        }
    }

    public void playInterstitial3(View v){
//        if (interstitial3Data != null){
//            SAInterstitialAd iad = new SAInterstitialAd(MainActivity.this);
//            iad.setAd(interstitial3Data);
//            iad.play();
//        }
    }

    public void playVideo1(View v){
//        if (video1Data != null){
//            SAFullscreenVideoAd fvad = new SAFullscreenVideoAd(MainActivity.this);
//            fvad.setAd(video1Data);
//            fvad.setShouldAutomaticallyCloseAtEnd(true);
//            fvad.setShouldLockOrientation(true);
//            fvad.setIsParentalGateEnabled(true);
//            fvad.setLockOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
//            fvad.setShouldShowSmallClickButton(true);
//            fvad.play();
//        }
    }

    public void playVideo2(View v){
//        if (video2Data != null){
//            videoAd = (SAVideoAd)findViewById(R.id.myVideoAd);
//            videoAd.setAd(video2Data);
//            videoAd.play();
//        }
    }
}

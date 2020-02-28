package com.darkweb.pluginManager;

import androidx.appcompat.app.AppCompatActivity;

import com.darkweb.constants.status;
import com.darkweb.constants.constants;
import com.darkweb.helperManager.eventObserver;
import com.google.android.gms.ads.*;

class adManager
{

    /*Private Variables*/

    private AppCompatActivity mAppContext;
    private eventObserver.eventListener mEvent;
    private AdView mBannerAds;
    private boolean bannerAdsLoading = false;
    private boolean bannerAdsLoaded = false;

    /*Initializations*/

    adManager(AppCompatActivity app_context, eventObserver.eventListener event, AdView banner_ads) {
        this.mAppContext = app_context;
        this.mEvent = event;
        mBannerAds = banner_ads;
    }

    void loadAds(){
        if(!status.paid_status)
        {
            if (!bannerAdsLoading)
            {
                bannerAdsLoading = true;
                MobileAds.initialize(mAppContext, constants.ADMOB_KEY);
                mBannerAds.setAlpha(0f);
                initializeBannerAds();
            }
        }
    }

    boolean isAdvertLoaded(){
        return bannerAdsLoaded;
    }

    /*Local Helper Methods*/

    private void admobListeners(){
        if(!status.paid_status){
            mBannerAds.setAdListener(new AdListener() {
                @Override
                public void onAdLoaded() {
                    bannerAdsLoaded = true;
                    mEvent.invokeObserver(null,null);
                }

                @Override
                public void onAdFailedToLoad(int errorCode) {
                    //Log.i("Failure___",""+errorCode);
                }

                @Override
                public void onAdOpened() {
                }

                @Override
                public void onAdClicked() {
                }

                @Override
                public void onAdLeftApplication() {
                }

                @Override
                public void onAdClosed() {
                }
            });
        }
    }

    /*External Helper Methods*/

    private void initializeBannerAds(){
        if(!status.paid_status){
            AdRequest request = new AdRequest.Builder().addTestDevice("E731DE5933CDC0E42B335787CE3E23EF").build();
            mBannerAds.loadAd(request);
            admobListeners();
        }
    }

}

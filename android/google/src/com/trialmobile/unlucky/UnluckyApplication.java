package com.trialmobile.unlucky;

import android.app.Application;

import com.google.android.gms.ads.MobileAds;

public class UnluckyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        MobileAds.initialize(this, initializationStatus -> {
        });
    }
}

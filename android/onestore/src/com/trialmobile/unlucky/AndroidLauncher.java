package com.trialmobile.unlucky;

import android.annotation.TargetApi;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.resyp.offerwall.Resyp;
import com.unity3d.ads.IUnityAdsInitializationListener;
import com.unity3d.ads.IUnityAdsLoadListener;
import com.unity3d.ads.IUnityAdsShowListener;
import com.unity3d.ads.UnityAds;
import com.unity3d.ads.UnityAdsShowOptions;
import com.unity3d.services.banners.BannerView;
import com.unity3d.services.banners.UnityBannerSize;

public class OneAndroidLauncher extends AndroidApplication implements AppInterface, IUnityAdsInitializationListener {

	private BannerView bottomBanner;

	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		UnityAds.initialize(getApplicationContext(), Constants.UNITY_GAME_ID, false, this);

		RelativeLayout layout = new RelativeLayout(this);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);

		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		config.useAccelerometer = false;
		config.useCompass = false;
		config.useImmersiveMode = true;
		View gameView = initializeForView(new UnluckyMain(this), config);

		createAndLoadRewardedAd();

		bottomBanner = new BannerView(this, Constants.UNITY_BANNER_PLACEMENT_ID, new UnityBannerSize(320, 50));
		bottomBanner.setVisibility(View.GONE);
		bottomBanner.load();

		layout.addView(gameView);

		RelativeLayout.LayoutParams adParams =
				new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
						RelativeLayout.LayoutParams.WRAP_CONTENT);
		adParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

		layout.addView(bottomBanner, adParams);

		setContentView(layout);
	}

	@TargetApi(19)
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus) {
			getWindow().getDecorView().setSystemUiVisibility(
					View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
							View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
							View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
							View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
		}
	}

	@Override
	public void onInitializationComplete() {

	}

	@Override
	public void onInitializationFailed(UnityAds.UnityAdsInitializationError error, String message) {

	}

	@Override
	public void openOfferwall(String userId) {
		runOnUiThread(() -> Resyp.openOfferwall(this, Constants.RESYP_MEDIA_CODE, userId));
	}

	@Override
	public void showBanner(boolean show) {
		runOnUiThread(() -> bottomBanner.setVisibility(show ? View.VISIBLE : View.GONE));
	}

	private void createAndLoadRewardedAd() {
		UnityAds.load(Constants.UNITY_REWARDED_PLACEMENT_ID, new IUnityAdsLoadListener() {
			@Override
			public void onUnityAdsAdLoaded(String placementId) {
			}

			@Override
			public void onUnityAdsFailedToLoad(String placementId, UnityAds.UnityAdsLoadError error, String message) {
			}
		});
	}

	@Override
	public void showVideo(VideoCallback callback) {
		runOnUiThread(() -> UnityAds.show(this, Constants.UNITY_REWARDED_PLACEMENT_ID, new UnityAdsShowOptions(), new IUnityAdsShowListener() {
			@Override
			public void onUnityAdsShowFailure(String placementId, UnityAds.UnityAdsShowError error, String message) {
				callback.noAd();
			}

			@Override
			public void onUnityAdsShowStart(String placementId) {
			}

			@Override
			public void onUnityAdsShowClick(String placementId) {
			}

			@Override
			public void onUnityAdsShowComplete(String placementId, UnityAds.UnityAdsShowCompletionState state) {
				if (state.equals(UnityAds.UnityAdsShowCompletionState.COMPLETED)) {
					callback.success(placementId, 100);
				}
			}
		}));
	}

}

package com.trialmobile.unlucky;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdCallback;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.resyp.offerwall.Resyp;

public class AndroidLauncher extends AndroidApplication implements AppInterface {
	protected AdView adView;
	private RewardedAd rewardedAd;

	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		RelativeLayout layout = new RelativeLayout(this);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);

		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		config.useAccelerometer = false;
		config.useCompass = false;
		View gameView = initializeForView(new UnluckyMain(this), config);

		adView = new AdView(this);
		adView.setVisibility(View.GONE);
		adView.setAdSize(AdSize.BANNER);
		adView.setAdUnitId("ca-app-pub-3940256099942544/6300978111");

		createAndLoadRewardedAd();

		layout.addView(gameView);

		RelativeLayout.LayoutParams adParams =
				new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
						RelativeLayout.LayoutParams.WRAP_CONTENT);
		adParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

		layout.addView(adView, adParams);

		setContentView(layout);

		AdRequest adRequest = new AdRequest.Builder().build();
		adView.loadAd(adRequest);
	}

	@Override
	public void openOfferwall(String userId) {
		runOnUiThread(() -> {
			Resyp.openOfferwall(this, "55a94c2c-82da-4716-abf6-01fa66db2a01", userId);
		});
	}

	@Override
	public void showBanner(boolean show) {
		runOnUiThread(() -> adView.setVisibility(show ? View.VISIBLE : View.GONE));
	}

	private void createAndLoadRewardedAd() {
		rewardedAd = new RewardedAd(this, "ca-app-pub-3940256099942544/5224354917");
		RewardedAdLoadCallback adLoadCallback = new RewardedAdLoadCallback() {
			@Override
			public void onRewardedAdLoaded() {
				// Ad successfully loaded.
			}
			@Override
			public void onRewardedAdFailedToLoad(LoadAdError adError) {
				// Ad failed to load.
			}
		};
		rewardedAd.loadAd(new AdRequest.Builder().build(), adLoadCallback);
	}

	@Override
	public void showVideo(VideoCallback callback) {
		runOnUiThread(() -> {
			if (rewardedAd.isLoaded()) {
				Activity activityContext = AndroidLauncher.this;
				RewardedAdCallback adCallback = new RewardedAdCallback() {
					@Override
					public void onRewardedAdOpened() {
						// Ad opened.
					}

					@Override
					public void onRewardedAdClosed() {
						createAndLoadRewardedAd();
					}

					@Override
					public void onUserEarnedReward(@NonNull RewardItem reward) {
						// User earned reward.
						Log.d("AndroidLauncher", reward.getType() + "=" + reward.getAmount());
						callback.success(reward.getType(), reward.getAmount());
					}

					@Override
					public void onRewardedAdFailedToShow(AdError adError) {
						// Ad failed to display.
					}
				};
				rewardedAd.show(activityContext, adCallback);
			}
		});
	}

}

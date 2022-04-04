package com.trialmobile.unlucky;

import android.annotation.TargetApi;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.google.ads.mediation.unity.UnityAdapter;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.rewarded.RewardedAd;
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
		config.useImmersiveMode = true;
		View gameView = initializeForView(new UnluckyMain(this), config);

		adView = new AdView(this);
		adView.setVisibility(View.GONE);
		adView.setAdSize(AdSize.BANNER);
		adView.setAdUnitId(Constants.AD_UNIT_ID);

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
	public void openOfferwall(String userId) {
		runOnUiThread(() -> Resyp.openOfferwall(this, Constants.RESYP_MEDIA_CODE, userId));
	}

	@Override
	public void showBanner(boolean show) {
		runOnUiThread(() -> adView.setVisibility(show ? View.VISIBLE : View.GONE));
	}

	private void createAndLoadRewardedAd() {
		Bundle extra = new Bundle();
		AdRequest adRequest = new AdRequest.Builder()
				.addNetworkExtrasBundle(UnityAdapter.class, extra)
				.build();

		RewardedAd.load(this, Constants.REWARDED_UNIT_ID, adRequest, new RewardedAdLoadCallback() {
			@Override
			public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
				super.onAdFailedToLoad(loadAdError);
				rewardedAd = null;
			}

			@Override
			public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
				super.onAdLoaded(rewardedAd);
				AndroidLauncher.this.rewardedAd = rewardedAd;
				AndroidLauncher.this.rewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
					@Override
					public void onAdDismissedFullScreenContent() {
						super.onAdDismissedFullScreenContent();
						createAndLoadRewardedAd();
					}

					@Override
					public void onAdFailedToShowFullScreenContent(@NonNull AdError adError) {
						super.onAdFailedToShowFullScreenContent(adError);
					}

					@Override
					public void onAdShowedFullScreenContent() {
						super.onAdShowedFullScreenContent();
						AndroidLauncher.this.rewardedAd = null;
					}
				});
			}
		});
	}

	@Override
	public void showVideo(VideoCallback callback) {
		runOnUiThread(() -> {
			if (rewardedAd != null) {
				rewardedAd.show(this, rewardItem -> callback.success(rewardItem.getType(), rewardItem.getAmount()));
			} else {
				callback.noAd();
			}
		});
	}

}

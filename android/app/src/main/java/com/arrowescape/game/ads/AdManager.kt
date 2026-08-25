package com.arrowescape.game.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import java.util.Date

object AdManager {
    private const val TAG = "AdManager"

    // Real AdMob Unit IDs configured for Arrow Escape
    const val APP_ID = "ca-app-pub-6146868530948467~3047670393"
    const val TOP_BANNER_ID = "ca-app-pub-6146868530948467/6074173465"
    const val BOTTOM_BANNER_ID = "ca-app-pub-6146868530948467/4135617284"
    const val INTERSTITIAL_ID = "ca-app-pub-6146868530948467/9603566382"
    const val REWARDED_ID = "ca-app-pub-6146868530948467/5664321378"
    const val APP_OPEN_ID = "ca-app-pub-6146868530948467/2935989754"

    private var interstitialAd: InterstitialAd? = null
    private var isInterstitialLoading = false

    private var rewardedAd: RewardedAd? = null
    private var isRewardedLoading = false

    private var appOpenAd: AppOpenAd? = null
    private var isAppOpenLoading = false
    private var loadTime: Long = 0
    private var isShowingAppOpenAd = false

    fun initialize(context: Context) {
        MobileAds.initialize(context) {
            Log.d(TAG, "AdMob MobileAds initialized successfully.")
            loadInterstitial(context)
            loadRewarded(context)
            loadAppOpenAd(context)
        }
    }

    // ==========================================
    // INTERSTITIAL ADS
    // ==========================================

    fun loadInterstitial(context: Context) {
        if (interstitialAd != null || isInterstitialLoading) return

        isInterstitialLoading = true
        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(
            context,
            INTERSTITIAL_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    isInterstitialLoading = false
                    Log.d(TAG, "Interstitial ad loaded.")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                    isInterstitialLoading = false
                    Log.w(TAG, "Interstitial ad failed to load: ${error.message}")
                }
            }
        )
    }

    fun showInterstitial(activity: Activity, onAdClosed: () -> Unit) {
        val currentAd = interstitialAd
        if (currentAd != null) {
            currentAd.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitialAd = null
                    loadInterstitial(activity)
                    onAdClosed()
                }

                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    interstitialAd = null
                    loadInterstitial(activity)
                    onAdClosed()
                }
            }
            currentAd.show(activity)
        } else {
            loadInterstitial(activity)
            onAdClosed()
        }
    }

    // ==========================================
    // REWARDED ADS (HINTS)
    // ==========================================

    fun loadRewarded(context: Context) {
        if (rewardedAd != null || isRewardedLoading) return

        isRewardedLoading = true
        val adRequest = AdRequest.Builder().build()

        RewardedAd.load(
            context,
            REWARDED_ID,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    isRewardedLoading = false
                    Log.d(TAG, "Rewarded ad loaded.")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewardedAd = null
                    isRewardedLoading = false
                    Log.w(TAG, "Rewarded ad failed to load: ${error.message}")
                }
            }
        )
    }

    fun showRewarded(
        activity: Activity,
        onUserEarnedReward: () -> Unit,
        onAdDismissed: () -> Unit
    ) {
        val currentAd = rewardedAd
        if (currentAd != null) {
            var rewardEarned = false

            currentAd.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    rewardedAd = null
                    loadRewarded(activity)
                    if (rewardEarned) {
                        onUserEarnedReward()
                    }
                    onAdDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    rewardedAd = null
                    loadRewarded(activity)
                    onAdDismissed()
                }
            }

            currentAd.show(activity) { _ ->
                rewardEarned = true
            }
        } else {
            loadRewarded(activity)
            onAdDismissed()
        }
    }

    // ==========================================
    // APP OPEN AD
    // ==========================================

    fun loadAppOpenAd(context: Context) {
        if (isAppOpenAdAvailable() || isAppOpenLoading) return

        isAppOpenLoading = true
        val request = AdRequest.Builder().build()
        AppOpenAd.load(
            context,
            APP_OPEN_ID,
            request,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    appOpenAd = ad
                    isAppOpenLoading = false
                    loadTime = Date().time
                    Log.d(TAG, "App Open Ad loaded.")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    appOpenAd = null
                    isAppOpenLoading = false
                    Log.w(TAG, "App Open Ad failed to load: ${error.message}")
                }
            }
        )
    }

    private fun isAppOpenAdAvailable(): Boolean {
        val wasLoadedRecently = (Date().time - loadTime) < 4 * 3600000 // 4 hours
        return appOpenAd != null && wasLoadedRecently
    }

    fun showAppOpenAdIfAvailable(activity: Activity, onComplete: () -> Unit = {}) {
        if (isShowingAppOpenAd) {
            onComplete()
            return
        }

        if (!isAppOpenAdAvailable()) {
            loadAppOpenAd(activity)
            onComplete()
            return
        }

        val ad = appOpenAd ?: run {
            onComplete()
            return
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                appOpenAd = null
                isShowingAppOpenAd = false
                loadAppOpenAd(activity)
                onComplete()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                appOpenAd = null
                isShowingAppOpenAd = false
                loadAppOpenAd(activity)
                onComplete()
            }

            override fun onAdShowedFullScreenContent() {
                isShowingAppOpenAd = true
            }
        }

        isShowingAppOpenAd = true
        ad.show(activity)
    }
}

/**
 * Reusable Jetpack Compose AdMob Banner View.
 * Dynamically loads and binds to real AdMob Banner instance without crashing or leaving blank blockers.
 */
@Composable
fun AdMobBannerView(
    adUnitId: String,
    modifier: Modifier = Modifier
) {
    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp),
        factory = { context ->
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                this.adUnitId = adUnitId
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                adListener = object : AdListener() {
                    override fun onAdFailedToLoad(error: LoadAdError) {
                        Log.w("AdMobBanner", "Banner ($adUnitId) failed: ${error.message}")
                    }
                }
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}

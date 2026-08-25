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
import java.util.concurrent.atomic.AtomicBoolean

object AdManager {
    private const val TAG = "AdManager"

    // Real AdMob Unit IDs configured for Arrow Escape
    const val APP_ID = "ca-app-pub-6146868530948467~3047670393"
    const val TOP_BANNER_ID = "ca-app-pub-6146868530948467/6074173465"
    const val BOTTOM_BANNER_ID = "ca-app-pub-6146868530948467/4135617284"
    const val INTERSTITIAL_ID = "ca-app-pub-6146868530948467/9603566382"
    const val REWARDED_ID = "ca-app-pub-6146868530948467/5664321378"
    const val APP_OPEN_ID = "ca-app-pub-6146868530948467/2935989754"

    @Volatile
    private var interstitialAd: InterstitialAd? = null
    private var isInterstitialLoading = false

    @Volatile
    private var rewardedAd: RewardedAd? = null
    private var isRewardedLoading = false

    @Volatile
    private var appOpenAd: AppOpenAd? = null
    private var isAppOpenLoading = false
    private var appOpenLoadTime: Long = 0
    private var isShowingAppOpenAd = false

    fun initialize(context: Context) {
        try {
            MobileAds.initialize(context) {
                Log.d(TAG, "AdMob MobileAds initialized.")
                loadInterstitial(context.applicationContext)
                loadRewarded(context.applicationContext)
                loadAppOpenAd(context.applicationContext)
            }
        } catch (e: Exception) {
            Log.e(TAG, "AdMob initialization error: ${e.message}")
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
                    Log.d(TAG, "Interstitial ad loaded successfully.")
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
            val hasInvoked = AtomicBoolean(false)
            fun safeClose() {
                if (hasInvoked.compareAndSet(false, true)) {
                    activity.runOnUiThread { onAdClosed() }
                }
            }

            currentAd.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitialAd = null
                    loadInterstitial(activity.applicationContext)
                    safeClose()
                }

                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    interstitialAd = null
                    loadInterstitial(activity.applicationContext)
                    safeClose()
                }
            }

            activity.runOnUiThread {
                try {
                    currentAd.show(activity)
                } catch (e: Exception) {
                    Log.e(TAG, "Error showing interstitial: ${e.message}")
                    interstitialAd = null
                    loadInterstitial(activity.applicationContext)
                    safeClose()
                }
            }
        } else {
            // Ad not ready: preload for next time and continue immediately
            loadInterstitial(activity.applicationContext)
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
                    Log.d(TAG, "Rewarded ad loaded successfully.")
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
            val rewardGranted = AtomicBoolean(false)
            val dismissedInvoked = AtomicBoolean(false)

            currentAd.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    rewardedAd = null
                    loadRewarded(activity.applicationContext)
                    if (dismissedInvoked.compareAndSet(false, true)) {
                        activity.runOnUiThread { onAdDismissed() }
                    }
                }

                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    rewardedAd = null
                    loadRewarded(activity.applicationContext)
                    if (dismissedInvoked.compareAndSet(false, true)) {
                        activity.runOnUiThread { onAdDismissed() }
                    }
                }
            }

            activity.runOnUiThread {
                try {
                    currentAd.show(activity) { _ ->
                        // STRICT REWARD VERIFICATION: only triggered by AdMob onUserEarnedReward
                        if (rewardGranted.compareAndSet(false, true)) {
                            activity.runOnUiThread { onUserEarnedReward() }
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error showing rewarded ad: ${e.message}")
                    rewardedAd = null
                    loadRewarded(activity.applicationContext)
                    if (dismissedInvoked.compareAndSet(false, true)) {
                        activity.runOnUiThread { onAdDismissed() }
                    }
                }
            }
        } else {
            // Not ready yet, start loading
            loadRewarded(activity.applicationContext)
            onAdDismissed()
        }
    }

    // ==========================================
    // APP OPEN ADS
    // ==========================================

    fun loadAppOpenAd(context: Context) {
        if (appOpenAd != null || isAppOpenLoading) return

        isAppOpenLoading = true
        val adRequest = AdRequest.Builder().build()

        AppOpenAd.load(
            context,
            APP_OPEN_ID,
            adRequest,
            AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    appOpenAd = ad
                    isAppOpenLoading = false
                    appOpenLoadTime = Date().time
                    Log.d(TAG, "App Open ad loaded successfully.")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    appOpenAd = null
                    isAppOpenLoading = false
                    Log.w(TAG, "App Open ad failed to load: ${error.message}")
                }
            }
        )
    }

    private fun isAppOpenAdAvailable(): Boolean {
        return appOpenAd != null && wasLoadTimeLessThanNHoursAgo(4)
    }

    private fun wasLoadTimeLessThanNHoursAgo(numHours: Long): Boolean {
        val dateDifference = Date().time - appOpenLoadTime
        val numMilliSecondsPerHour: Long = 3600000
        return dateDifference < numMilliSecondsPerHour * numHours
    }

    fun showAppOpenAdIfAvailable(activity: Activity, onAdDismissed: (() -> Unit)? = null) {
        if (isShowingAppOpenAd) return

        if (!isAppOpenAdAvailable()) {
            loadAppOpenAd(activity.applicationContext)
            onAdDismissed?.invoke()
            return
        }

        val ad = appOpenAd ?: return
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                appOpenAd = null
                isShowingAppOpenAd = false
                loadAppOpenAd(activity.applicationContext)
                onAdDismissed?.invoke()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                appOpenAd = null
                isShowingAppOpenAd = false
                loadAppOpenAd(activity.applicationContext)
                onAdDismissed?.invoke()
            }

            override fun onAdShowedFullScreenContent() {
                isShowingAppOpenAd = true
            }
        }

        activity.runOnUiThread {
            try {
                ad.show(activity)
            } catch (e: Exception) {
                isShowingAppOpenAd = false
                appOpenAd = null
                loadAppOpenAd(activity.applicationContext)
                onAdDismissed?.invoke()
            }
        }
    }

    // ==========================================
    // BANNER COMPOSABLES
    // ==========================================

    @Composable
    fun TopBannerView(modifier: Modifier = Modifier) {
        BannerAdComposable(adUnitId = TOP_BANNER_ID, modifier = modifier)
    }

    @Composable
    fun BottomBannerView(modifier: Modifier = Modifier) {
        BannerAdComposable(adUnitId = BOTTOM_BANNER_ID, modifier = modifier)
    }

    @Composable
    private fun BannerAdComposable(
        adUnitId: String,
        modifier: Modifier = Modifier
    ) {
        AndroidView(
            modifier = modifier
                .fillMaxWidth()
                .height(50.dp),
            factory = { ctx ->
                AdView(ctx).apply {
                    setAdSize(AdSize.BANNER)
                    this.adUnitId = adUnitId
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    adListener = object : AdListener() {
                        override fun onAdLoaded() {
                            Log.d(TAG, "Banner loaded for $adUnitId")
                        }

                        override fun onAdFailedToLoad(error: LoadAdError) {
                            Log.w(TAG, "Banner failed to load for $adUnitId: ${error.message}")
                        }
                    }
                    loadAd(AdRequest.Builder().build())
                }
            }
        )
    }
}

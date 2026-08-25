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
    private const val TAG = "ArrowEscapeAds"

    // Real AdMob Unit IDs configured for Arrow Escape
    const val APP_ID = "ca-app-pub-6146868530948467~3047670393"
    const val TOP_BANNER_ID = "ca-app-pub-6146868530948467/6074173465"
    const val BOTTOM_BANNER_ID = "ca-app-pub-6146868530948467/4135617284"
    const val INTERSTITIAL_ID = "ca-app-pub-6146868530948467/9603566382"
    const val REWARDED_ID = "ca-app-pub-6146868530948467/5664321378"
    const val APP_OPEN_ID = "ca-app-pub-6146868530948467/2935989754"

    private val isInitialized = AtomicBoolean(false)

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
        if (isInitialized.compareAndSet(false, true)) {
            try {
                Log.d(TAG, "Initializing MobileAds SDK with App ID: $APP_ID...")
                MobileAds.initialize(context) { status ->
                    Log.d(TAG, "MobileAds initialization complete: $status")
                    loadInterstitial(context.applicationContext)
                    loadRewarded(context.applicationContext)
                    loadAppOpenAd(context.applicationContext)
                }
            } catch (e: Exception) {
                Log.e(TAG, "AdMob initialization error: ${e.message}", e)
            }
        }
    }

    // ==========================================
    // 1. INTERSTITIAL ADS
    // ==========================================

    fun loadInterstitial(context: Context) {
        if (interstitialAd != null) {
            Log.d(TAG, "INTERSTITIAL: already loaded and ready.")
            return
        }
        if (isInterstitialLoading) {
            Log.d(TAG, "INTERSTITIAL: already loading...")
            return
        }

        isInterstitialLoading = true
        Log.d(TAG, "INTERSTITIAL LOADING: Unit ID: $INTERSTITIAL_ID")
        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(
            context,
            INTERSTITIAL_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    isInterstitialLoading = false
                    Log.d(
                        TAG,
                        "INTERSTITIAL LOADED successfully. AdUnit: ${ad.adUnitId}, responseInfo=${ad.responseInfo}"
                    )
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                    isInterstitialLoading = false
                    Log.e(
                        TAG,
                        "INTERSTITIAL LOAD FAILED: code=${error.code}, message=${error.message}, domain=${error.domain}, responseInfo=${error.responseInfo}"
                    )
                }
            }
        )
    }

    fun showInterstitial(activity: Activity, onAdClosed: () -> Unit) {
        if (activity.isFinishing || activity.isDestroyed) {
            Log.w(TAG, "INTERSTITIAL SHOW FAILED: Activity is finishing or destroyed.")
            onAdClosed()
            return
        }

        val currentAd = interstitialAd
        if (currentAd != null) {
            Log.d(TAG, "INTERSTITIAL SHOWING on ${activity.localClassName}")
            val hasInvoked = AtomicBoolean(false)

            fun safeClose() {
                if (hasInvoked.compareAndSet(false, true)) {
                    activity.runOnUiThread { onAdClosed() }
                }
            }

            currentAd.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "INTERSTITIAL SHOWED FULL SCREEN")
                }

                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "INTERSTITIAL DISMISSED")
                    interstitialAd = null
                    loadInterstitial(activity.applicationContext)
                    safeClose()
                }

                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    Log.e(
                        TAG,
                        "INTERSTITIAL SHOW FAILED: code=${error.code}, message=${error.message}, domain=${error.domain}"
                    )
                    interstitialAd = null
                    loadInterstitial(activity.applicationContext)
                    safeClose()
                }
            }

            activity.runOnUiThread {
                try {
                    currentAd.show(activity)
                } catch (e: Exception) {
                    Log.e(TAG, "INTERSTITIAL SHOW EXCEPTION: ${e.message}", e)
                    interstitialAd = null
                    loadInterstitial(activity.applicationContext)
                    safeClose()
                }
            }
        } else {
            // Ad not available: do not block player, continue and preload
            Log.w(TAG, "INTERSTITIAL NOT AVAILABLE: Proceeding without ad and preloading.")
            loadInterstitial(activity.applicationContext)
            onAdClosed()
        }
    }

    // ==========================================
    // 2. REWARDED ADS (HINTS)
    // ==========================================

    fun loadRewarded(context: Context) {
        if (rewardedAd != null) {
            Log.d(TAG, "REWARDED: already loaded and ready.")
            return
        }
        if (isRewardedLoading) {
            Log.d(TAG, "REWARDED: already loading...")
            return
        }

        isRewardedLoading = true
        Log.d(TAG, "REWARDED LOADING: Unit ID: $REWARDED_ID")
        val adRequest = AdRequest.Builder().build()

        RewardedAd.load(
            context,
            REWARDED_ID,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    isRewardedLoading = false
                    Log.d(
                        TAG,
                        "REWARDED LOADED successfully. AdUnit: ${ad.adUnitId}, responseInfo=${ad.responseInfo}"
                    )
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewardedAd = null
                    isRewardedLoading = false
                    Log.e(
                        TAG,
                        "REWARDED LOAD FAILED: code=${error.code}, message=${error.message}, domain=${error.domain}, responseInfo=${error.responseInfo}"
                    )
                }
            }
        )
    }

    fun showRewarded(
        activity: Activity,
        onUserEarnedReward: () -> Unit,
        onAdUnavailable: () -> Unit,
        onAdDismissed: () -> Unit = {}
    ) {
        if (activity.isFinishing || activity.isDestroyed) {
            Log.w(TAG, "REWARDED SHOW FAILED: Activity is finishing or destroyed.")
            onAdUnavailable()
            return
        }

        val currentAd = rewardedAd
        if (currentAd != null) {
            Log.d(TAG, "REWARDED SHOWING on ${activity.localClassName}")
            val rewardGranted = AtomicBoolean(false)
            val dismissedInvoked = AtomicBoolean(false)

            fun safeDismiss() {
                if (dismissedInvoked.compareAndSet(false, true)) {
                    activity.runOnUiThread { onAdDismissed() }
                }
            }

            currentAd.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "REWARDED SHOWED FULL SCREEN")
                }

                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "REWARDED DISMISSED")
                    rewardedAd = null
                    loadRewarded(activity.applicationContext)
                    safeDismiss()
                }

                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    Log.e(
                        TAG,
                        "REWARDED SHOW FAILED: code=${error.code}, message=${error.message}, domain=${error.domain}"
                    )
                    rewardedAd = null
                    loadRewarded(activity.applicationContext)
                    if (!rewardGranted.get()) {
                        activity.runOnUiThread { onAdUnavailable() }
                    }
                    safeDismiss()
                }
            }

            activity.runOnUiThread {
                try {
                    currentAd.show(activity) { rewardItem ->
                        Log.d(
                            TAG,
                            "REWARDED USER EARNED REWARD: type=${rewardItem.type}, amount=${rewardItem.amount}"
                        )
                        if (rewardGranted.compareAndSet(false, true)) {
                            activity.runOnUiThread { onUserEarnedReward() }
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "REWARDED SHOW EXCEPTION: ${e.message}", e)
                    rewardedAd = null
                    loadRewarded(activity.applicationContext)
                    if (!rewardGranted.get()) {
                        activity.runOnUiThread { onAdUnavailable() }
                    }
                    safeDismiss()
                }
            }
        } else {
            Log.w(TAG, "REWARDED NOT LOADED: attempting load and triggering onAdUnavailable callback.")
            loadRewarded(activity.applicationContext)
            onAdUnavailable()
        }
    }

    // ==========================================
    // 3. APP OPEN ADS
    // ==========================================

    fun loadAppOpenAd(context: Context) {
        if (appOpenAd != null || isAppOpenLoading) return

        isAppOpenLoading = true
        Log.d(TAG, "APP OPEN LOADING: Unit ID: $APP_OPEN_ID")
        val adRequest = AdRequest.Builder().build()

        AppOpenAd.load(
            context,
            APP_OPEN_ID,
            adRequest,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    appOpenAd = ad
                    isAppOpenLoading = false
                    appOpenLoadTime = Date().time
                    Log.d(TAG, "APP OPEN LOADED successfully.")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    appOpenAd = null
                    isAppOpenLoading = false
                    Log.e(
                        TAG,
                        "APP OPEN LOAD FAILED: code=${error.code}, message=${error.message}"
                    )
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
                Log.d(TAG, "APP OPEN DISMISSED")
                appOpenAd = null
                isShowingAppOpenAd = false
                loadAppOpenAd(activity.applicationContext)
                onAdDismissed?.invoke()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                Log.e(
                    TAG,
                    "APP OPEN SHOW FAILED: code=${error.code}, message=${error.message}"
                )
                appOpenAd = null
                isShowingAppOpenAd = false
                loadAppOpenAd(activity.applicationContext)
                onAdDismissed?.invoke()
            }

            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "APP OPEN SHOWED FULL SCREEN")
                isShowingAppOpenAd = true
            }
        }

        activity.runOnUiThread {
            try {
                ad.show(activity)
            } catch (e: Exception) {
                Log.e(TAG, "APP OPEN SHOW EXCEPTION: ${e.message}", e)
                isShowingAppOpenAd = false
                appOpenAd = null
                loadAppOpenAd(activity.applicationContext)
                onAdDismissed?.invoke()
            }
        }
    }

    // ==========================================
    // 4. BANNER COMPOSABLES (Top & Bottom)
    // ==========================================

    @Composable
    fun TopBannerView(modifier: Modifier = Modifier) {
        AndroidView(
            modifier = modifier
                .fillMaxWidth()
                .height(50.dp),
            factory = { ctx ->
                AdView(ctx).apply {
                    setAdSize(AdSize.BANNER)
                    this.adUnitId = TOP_BANNER_ID
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    adListener = object : AdListener() {
                        override fun onAdLoaded() {
                            Log.d(TAG, "TOP BANNER LOADED: $TOP_BANNER_ID")
                        }

                        override fun onAdFailedToLoad(error: LoadAdError) {
                            Log.e(
                                TAG,
                                "TOP BANNER FAILED: code=${error.code}, message=${error.message}"
                            )
                        }
                    }
                    loadAd(AdRequest.Builder().build())
                }
            }
        )
    }

    @Composable
    fun BottomBannerView(modifier: Modifier = Modifier) {
        AndroidView(
            modifier = modifier
                .fillMaxWidth()
                .height(50.dp),
            factory = { ctx ->
                AdView(ctx).apply {
                    setAdSize(AdSize.BANNER)
                    this.adUnitId = BOTTOM_BANNER_ID
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    adListener = object : AdListener() {
                        override fun onAdLoaded() {
                            Log.d(TAG, "BOTTOM BANNER LOADED: $BOTTOM_BANNER_ID")
                        }

                        override fun onAdFailedToLoad(error: LoadAdError) {
                            Log.e(
                                TAG,
                                "BOTTOM BANNER FAILED: code=${error.code}, message=${error.message}"
                            )
                        }
                    }
                    loadAd(AdRequest.Builder().build())
                }
            }
        )
    }
}


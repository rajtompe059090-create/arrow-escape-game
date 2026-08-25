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
                Log.d(TAG, "Initializing MobileAds SDK...")
                MobileAds.initialize(context) { status ->
                    Log.d(TAG, "MobileAds initialized: $status")
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
            Log.d(TAG, "[Interstitial] already loaded and ready.")
            return
        }
        if (isInterstitialLoading) {
            Log.d(TAG, "[Interstitial] already loading in background.")
            return
        }

        isInterstitialLoading = true
        Log.d(TAG, "[Interstitial] loading... Unit ID: $INTERSTITIAL_ID")
        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(
            context,
            INTERSTITIAL_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    isInterstitialLoading = false
                    Log.d(TAG, "[Interstitial] loaded successfully.")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                    isInterstitialLoading = false
                    Log.w(
                        TAG,
                        "[Interstitial] failed to load: code=${error.code}, message=${error.message}, domain=${error.domain}"
                    )
                }
            }
        )
    }

    fun showInterstitial(activity: Activity, onAdClosed: () -> Unit) {
        if (activity.isFinishing || activity.isDestroyed) {
            Log.w(TAG, "[Interstitial] cannot show: Activity is finishing or destroyed.")
            onAdClosed()
            return
        }

        val currentAd = interstitialAd
        if (currentAd != null) {
            Log.d(TAG, "[Interstitial] showing on activity: ${activity.localClassName}")
            val hasInvoked = AtomicBoolean(false)

            fun safeClose() {
                if (hasInvoked.compareAndSet(false, true)) {
                    activity.runOnUiThread { onAdClosed() }
                }
            }

            currentAd.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "[Interstitial] showed full screen.")
                }

                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "[Interstitial] dismissed.")
                    interstitialAd = null
                    loadInterstitial(activity.applicationContext)
                    safeClose()
                }

                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    Log.w(
                        TAG,
                        "[Interstitial] failed to show: code=${error.code}, message=${error.message}"
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
                    Log.e(TAG, "[Interstitial] exception during show: ${e.message}", e)
                    interstitialAd = null
                    loadInterstitial(activity.applicationContext)
                    safeClose()
                }
            }
        } else {
            // Ad not available: do not block player, continue and preload
            Log.w(TAG, "[Interstitial] ad not available. Proceeding without ad.")
            loadInterstitial(activity.applicationContext)
            onAdClosed()
        }
    }

    // ==========================================
    // 2. REWARDED ADS (HINTS)
    // ==========================================

    fun loadRewarded(context: Context) {
        if (rewardedAd != null) {
            Log.d(TAG, "[Rewarded] already loaded and ready.")
            return
        }
        if (isRewardedLoading) {
            Log.d(TAG, "[Rewarded] already loading in background.")
            return
        }

        isRewardedLoading = true
        Log.d(TAG, "[Rewarded] loading... Unit ID: $REWARDED_ID")
        val adRequest = AdRequest.Builder().build()

        RewardedAd.load(
            context,
            REWARDED_ID,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    isRewardedLoading = false
                    Log.d(TAG, "[Rewarded] loaded successfully.")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewardedAd = null
                    isRewardedLoading = false
                    Log.w(
                        TAG,
                        "[Rewarded] failed to load: code=${error.code}, message=${error.message}, domain=${error.domain}"
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
            Log.w(TAG, "[Rewarded] cannot show: Activity is finishing or destroyed.")
            onAdUnavailable()
            return
        }

        val currentAd = rewardedAd
        if (currentAd != null) {
            Log.d(TAG, "[Rewarded] showing on activity: ${activity.localClassName}")
            val rewardGranted = AtomicBoolean(false)
            val dismissedInvoked = AtomicBoolean(false)

            fun safeDismiss() {
                if (dismissedInvoked.compareAndSet(false, true)) {
                    activity.runOnUiThread { onAdDismissed() }
                }
            }

            currentAd.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdShowedFullScreenContent() {
                    Log.d(TAG, "[Rewarded] showed full screen.")
                }

                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "[Rewarded] dismissed.")
                    rewardedAd = null
                    loadRewarded(activity.applicationContext)
                    safeDismiss()
                }

                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    Log.w(
                        TAG,
                        "[Rewarded] failed to show: code=${error.code}, message=${error.message}"
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
                            "[Rewarded] reward earned: type=${rewardItem.type}, amount=${rewardItem.amount}"
                        )
                        if (rewardGranted.compareAndSet(false, true)) {
                            activity.runOnUiThread { onUserEarnedReward() }
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "[Rewarded] exception during show: ${e.message}", e)
                    rewardedAd = null
                    loadRewarded(activity.applicationContext)
                    if (!rewardGranted.get()) {
                        activity.runOnUiThread { onAdUnavailable() }
                    }
                    safeDismiss()
                }
            }
        } else {
            Log.w(TAG, "[Rewarded] ad not available / not loaded yet.")
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
        Log.d(TAG, "[AppOpen] loading... Unit ID: $APP_OPEN_ID")
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
                    Log.d(TAG, "[AppOpen] loaded successfully.")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    appOpenAd = null
                    isAppOpenLoading = false
                    Log.w(
                        TAG,
                        "[AppOpen] failed to load: code=${error.code}, message=${error.message}"
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
                Log.d(TAG, "[AppOpen] dismissed.")
                appOpenAd = null
                isShowingAppOpenAd = false
                loadAppOpenAd(activity.applicationContext)
                onAdDismissed?.invoke()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                Log.w(
                    TAG,
                    "[AppOpen] failed to show: code=${error.code}, message=${error.message}"
                )
                appOpenAd = null
                isShowingAppOpenAd = false
                loadAppOpenAd(activity.applicationContext)
                onAdDismissed?.invoke()
            }

            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "[AppOpen] showed full screen.")
                isShowingAppOpenAd = true
            }
        }

        activity.runOnUiThread {
            try {
                ad.show(activity)
            } catch (e: Exception) {
                Log.e(TAG, "[AppOpen] exception during show: ${e.message}", e)
                isShowingAppOpenAd = false
                appOpenAd = null
                loadAppOpenAd(activity.applicationContext)
                onAdDismissed?.invoke()
            }
        }
    }

    // ==========================================
    // 4. BANNER COMPOSABLES
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
                            Log.d(TAG, "[Banner] loaded for $adUnitId")
                        }

                        override fun onAdFailedToLoad(error: LoadAdError) {
                            Log.w(
                                TAG,
                                "[Banner] failed to load for $adUnitId: code=${error.code}, message=${error.message}"
                            )
                        }
                    }
                    loadAd(AdRequest.Builder().build())
                }
            }
        )
    }
}


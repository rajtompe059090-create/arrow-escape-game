package com.arrowescape.game.ads

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.arrowescape.game.BuildConfig
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import java.util.Date
import java.util.concurrent.atomic.AtomicBoolean

object AdManager {

    private const val TAG = "ArrowEscapeAds"
    private val mainHandler = Handler(Looper.getMainLooper())

    // =========================================================
    // ADMOB APPLICATION & AD UNIT IDS (PRESERVED EXACTLY)
    // =========================================================

    const val APP_ID = "ca-app-pub-6146868530948467~3047670393"
    const val BANNER_ID = "ca-app-pub-6146868530948467/4890772086"
    const val TOP_BANNER_ID = BANNER_ID
    const val BOTTOM_BANNER_ID = BANNER_ID
    const val INTERSTITIAL_ID = "ca-app-pub-6146868530948467/8819293283"
    const val REWARDED_ID = "ca-app-pub-6146868530948467/5664321378"
    const val APP_OPEN_ID = "ca-app-pub-6146868530948467/2935989754"

    // =========================================================
    // RUNTIME STATE
    // =========================================================

    private val isInitialized = AtomicBoolean(false)
    private val isShowingFullscreenAd = AtomicBoolean(false)

    @Volatile
    private var interstitialAd: InterstitialAd? = null
    private val isInterstitialLoading = AtomicBoolean(false)
    private var interstitialRetryAttempt = 0

    @Volatile
    private var rewardedAd: RewardedAd? = null
    private val isRewardedLoading = AtomicBoolean(false)
    private var rewardedRetryAttempt = 0

    @Volatile
    private var appOpenAd: AppOpenAd? = null
    private val isAppOpenLoading = AtomicBoolean(false)
    private var appOpenLoadTime: Long = 0L

    // =========================================================
    // 1. ADMOB INITIALIZATION
    // =========================================================

    fun initialize(context: Context) {
        if (!isInitialized.compareAndSet(false, true)) {
            return
        }

        try {
            // Configure Test Device Support for DEBUG builds
            if (BuildConfig.DEBUG) {
                val testDeviceIds = listOf(
                    AdRequest.DEVICE_ID_EMULATOR
                )
                val configuration = RequestConfiguration.Builder()
                    .setTestDeviceIds(testDeviceIds)
                    .build()
                MobileAds.setRequestConfiguration(configuration)
            }

            MobileAds.initialize(context) { status ->
                Log.d(TAG, "AdMob initialized")

                // Preload all ad formats safely
                loadInterstitial(context.applicationContext)
                loadRewarded(context.applicationContext)
                loadAppOpenAd(context.applicationContext)
            }
        } catch (e: Exception) {
            Log.e(TAG, "AdMob initialization error: ${e.message}", e)
        }
    }

    // =========================================================
    // 2. INTERSTITIAL (LEVEL COMPLETE FLOW)
    // =========================================================

    fun loadInterstitial(context: Context) {
        if (interstitialAd != null) {
            return
        }

        if (!isInterstitialLoading.compareAndSet(false, true)) {
            return
        }

        Log.d(TAG, "Interstitial load requested")

        InterstitialAd.load(
            context,
            INTERSTITIAL_ID,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    isInterstitialLoading.set(false)
                    interstitialRetryAttempt = 0
                    Log.d(TAG, "Interstitial loaded")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                    isInterstitialLoading.set(false)
                    Log.w(
                        TAG,
                        "Interstitial failed: code=${error.code}, message=${error.message}, domain=${error.domain}, responseInfo=${error.responseInfo}"
                    )

                    // Controlled exponential backoff retry (5s, 10s, 20s, max 30s) up to 5 attempts
                    if (interstitialRetryAttempt < 5) {
                        interstitialRetryAttempt++
                        val delayMs = (5000L * (1 shl (interstitialRetryAttempt - 1))).coerceAtMost(30000L)
                        mainHandler.postDelayed({
                            loadInterstitial(context.applicationContext)
                        }, delayMs)
                    }
                }
            }
        )
    }

    fun isInterstitialLoaded(): Boolean {
        return interstitialAd != null
    }

    fun showInterstitial(
        activity: Activity,
        onAdClosed: () -> Unit
    ) {
        if (activity.isFinishing || activity.isDestroyed) {
            onAdClosed()
            return
        }

        val ad = interstitialAd
        interstitialAd = null

        if (ad == null) {
            loadInterstitial(activity.applicationContext)
            onAdClosed()
            return
        }

        if (!isShowingFullscreenAd.compareAndSet(false, true)) {
            onAdClosed()
            return
        }

        val safeCloseOnce = AtomicBoolean(false)
        fun safeClose() {
            if (safeCloseOnce.compareAndSet(false, true)) {
                isShowingFullscreenAd.set(false)
                activity.runOnUiThread {
                    onAdClosed()
                }
            }
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "Interstitial showed")
            }

            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "Interstitial dismissed")
                loadInterstitial(activity.applicationContext)
                safeClose()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                Log.w(
                    TAG,
                    "Interstitial show failed: code=${error.code}, message=${error.message}, domain=${error.domain}"
                )
                loadInterstitial(activity.applicationContext)
                safeClose()
            }

            override fun onAdImpression() {
                Log.d(TAG, "Interstitial impression")
            }

            override fun onAdClicked() {
                Log.d(TAG, "Interstitial clicked")
            }
        }

        activity.runOnUiThread {
            try {
                ad.show(activity)
            } catch (e: Exception) {
                Log.e(TAG, "Interstitial show exception: ${e.message}", e)
                loadInterstitial(activity.applicationContext)
                safeClose()
            }
        }
    }

    // =========================================================
    // 3. REWARDED / HINT FLOW
    // =========================================================

    fun loadRewarded(context: Context) {
        if (rewardedAd != null) {
            return
        }

        if (!isRewardedLoading.compareAndSet(false, true)) {
            return
        }

        Log.d(TAG, "Rewarded load requested")

        RewardedAd.load(
            context,
            REWARDED_ID,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    isRewardedLoading.set(false)
                    rewardedRetryAttempt = 0
                    Log.d(TAG, "Rewarded loaded")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewardedAd = null
                    isRewardedLoading.set(false)
                    Log.w(
                        TAG,
                        "Rewarded failed: code=${error.code}, message=${error.message}, domain=${error.domain}, responseInfo=${error.responseInfo}"
                    )

                    if (rewardedRetryAttempt < 5) {
                        rewardedRetryAttempt++
                        val delayMs = (5000L * (1 shl (rewardedRetryAttempt - 1))).coerceAtMost(30000L)
                        mainHandler.postDelayed({
                            loadRewarded(context.applicationContext)
                        }, delayMs)
                    }
                }
            }
        )
    }

    fun isRewardedLoaded(): Boolean {
        return rewardedAd != null
    }

    fun showRewarded(
        activity: Activity,
        onUserEarnedReward: () -> Unit,
        onAdUnavailable: () -> Unit = {},
        onAdDismissed: (() -> Unit)? = null
    ) {
        showRewardedAd(
            activity = activity,
            onUserEarnedReward = onUserEarnedReward,
            onAdUnavailable = onAdUnavailable,
            onAdDismissed = onAdDismissed
        )
    }

    fun showRewardedAd(
        activity: Activity,
        onUserEarnedReward: () -> Unit,
        onAdUnavailable: () -> Unit = {},
        onAdDismissed: (() -> Unit)? = null
    ) {
        if (activity.isFinishing || activity.isDestroyed) {
            onAdUnavailable()
            onAdDismissed?.invoke()
            return
        }

        val ad = rewardedAd
        rewardedAd = null

        if (ad == null) {
            loadRewarded(activity.applicationContext)
            onAdUnavailable()
            onAdDismissed?.invoke()
            return
        }

        if (!isShowingFullscreenAd.compareAndSet(false, true)) {
            onAdUnavailable()
            onAdDismissed?.invoke()
            return
        }

        val rewardGranted = AtomicBoolean(false)
        val dismissedOnce = AtomicBoolean(false)

        fun safeDismiss() {
            if (dismissedOnce.compareAndSet(false, true)) {
                isShowingFullscreenAd.set(false)
                activity.runOnUiThread {
                    onAdDismissed?.invoke()
                }
            }
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "Rewarded showed")
            }

            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "Rewarded dismissed")
                loadRewarded(activity.applicationContext)
                safeDismiss()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                Log.w(
                    TAG,
                    "Rewarded show failed: code=${error.code}, message=${error.message}, domain=${error.domain}"
                )
                loadRewarded(activity.applicationContext)
                if (!rewardGranted.get()) {
                    activity.runOnUiThread {
                        onAdUnavailable()
                    }
                }
                safeDismiss()
            }

            override fun onAdImpression() {
                Log.d(TAG, "Rewarded impression")
            }

            override fun onAdClicked() {
                Log.d(TAG, "Rewarded clicked")
            }
        }

        activity.runOnUiThread {
            try {
                ad.show(activity) { rewardItem ->
                    Log.d(TAG, "Rewarded earned: amount=${rewardItem.amount}, type=${rewardItem.type}")
                    if (rewardGranted.compareAndSet(false, true)) {
                        activity.runOnUiThread {
                            onUserEarnedReward()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Rewarded show exception: ${e.message}", e)
                loadRewarded(activity.applicationContext)
                safeDismiss()
                onAdUnavailable()
            }
        }
    }

    // =========================================================
    // 4. APP OPEN AD
    // =========================================================

    fun loadAppOpenAd(context: Context) {
        if (isAppOpenAdAvailable() || !isAppOpenLoading.compareAndSet(false, true)) {
            return
        }

        AppOpenAd.load(
            context,
            APP_OPEN_ID,
            AdRequest.Builder().build(),
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    appOpenAd = ad
                    isAppOpenLoading.set(false)
                    appOpenLoadTime = Date().time
                    Log.d(TAG, "App Open loaded")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    appOpenAd = null
                    isAppOpenLoading.set(false)
                    Log.w(
                        TAG,
                        "App Open failed: code=${error.code}, message=${error.message}, domain=${error.domain}, responseInfo=${error.responseInfo}"
                    )
                }
            }
        )
    }

    private fun isAppOpenAdAvailable(): Boolean {
        val numHours = 4
        val dateDifference: Long = Date().time - appOpenLoadTime
        val numMilliSecondsPerHour: Long = 3600000
        return appOpenAd != null && dateDifference < numMilliSecondsPerHour * numHours
    }

    fun showAppOpenAdIfAvailable(
        activity: Activity,
        onAdDismissed: (() -> Unit)? = null
    ) {
        if (activity.isFinishing || activity.isDestroyed) {
            onAdDismissed?.invoke()
            return
        }

        if (!isAppOpenAdAvailable()) {
            loadAppOpenAd(activity.applicationContext)
            onAdDismissed?.invoke()
            return
        }

        val ad = appOpenAd
        appOpenAd = null

        if (ad == null) {
            onAdDismissed?.invoke()
            return
        }

        if (!isShowingFullscreenAd.compareAndSet(false, true)) {
            onAdDismissed?.invoke()
            return
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "App Open showed")
            }

            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "App Open dismissed")
                isShowingFullscreenAd.set(false)
                loadAppOpenAd(activity.applicationContext)
                onAdDismissed?.invoke()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                Log.w(
                    TAG,
                    "App Open show failed: code=${error.code}, message=${error.message}, domain=${error.domain}"
                )
                isShowingFullscreenAd.set(false)
                loadAppOpenAd(activity.applicationContext)
                onAdDismissed?.invoke()
            }
        }

        activity.runOnUiThread {
            try {
                ad.show(activity)
            } catch (e: Exception) {
                Log.e(TAG, "App Open show exception: ${e.message}", e)
                isShowingFullscreenAd.set(false)
                loadAppOpenAd(activity.applicationContext)
                onAdDismissed?.invoke()
            }
        }
    }

    // =========================================================
    // 5. BANNER AD VIEW (REAL ANDROID ADVIEW WITH AdSize.BANNER)
    // =========================================================

    @Composable
    fun BannerAdView(
        modifier: Modifier = Modifier,
        adUnitId: String = BANNER_ID
    ) {
        var isAdLoaded by remember { mutableStateOf(false) }

        Box(
            modifier = modifier
                .fillMaxWidth()
                .then(
                    if (isAdLoaded) {
                        Modifier.wrapContentHeight()
                    } else {
                        Modifier.height(0.dp)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                factory = { context ->
                    AdView(context).apply {
                        setAdSize(AdSize.BANNER)
                        this.adUnitId = adUnitId
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )

                        adListener = object : AdListener() {
                            override fun onAdLoaded() {
                                isAdLoaded = true
                                Log.d(TAG, "Banner loaded")
                            }

                            override fun onAdFailedToLoad(error: LoadAdError) {
                                isAdLoaded = false
                                Log.w(
                                    TAG,
                                    "Banner failed: code=${error.code}, message=${error.message}, domain=${error.domain}, responseInfo=${error.responseInfo}"
                                )

                                // Background safe retry after 30 seconds
                                mainHandler.postDelayed({
                                    try {
                                        loadAd(AdRequest.Builder().build())
                                    } catch (e: Exception) {
                                        Log.w(TAG, "Banner auto-retry exception: ${e.message}")
                                    }
                                }, 30000L)
                            }

                            override fun onAdOpened() {
                                Log.d(TAG, "Banner opened")
                            }

                            override fun onAdClosed() {
                                Log.d(TAG, "Banner closed")
                            }

                            override fun onAdImpression() {
                                Log.d(TAG, "Banner impression")
                            }

                            override fun onAdClicked() {
                                Log.d(TAG, "Banner clicked")
                            }
                        }

                        Log.d(TAG, "Banner load requested")
                        loadAd(AdRequest.Builder().build())
                    }
                },
                onRelease = { adView ->
                    try {
                        adView.destroy()
                    } catch (e: Exception) {
                        Log.w(TAG, "Banner destroy exception: ${e.message}")
                    }
                }
            )
        }
    }

    @Composable
    fun TopBannerView(modifier: Modifier = Modifier) {
        BannerAdView(modifier = modifier, adUnitId = TOP_BANNER_ID)
    }

    @Composable
    fun BottomBannerView(modifier: Modifier = Modifier) {
        BannerAdView(modifier = modifier, adUnitId = BOTTOM_BANNER_ID)
    }
}

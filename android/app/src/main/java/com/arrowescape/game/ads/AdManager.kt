package com.arrowescape.game.ads

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    private val mainHandler = Handler(Looper.getMainLooper())

    // =========================================================
    // ADMOB APP ID
    // =========================================================

    const val APP_ID =
        "ca-app-pub-6146868530948467~3047670393"

    // =========================================================
    // REAL AD UNIT IDS (PRESERVED EXACTLY)
    // =========================================================

    const val BANNER_ID =
        "ca-app-pub-6146868530948467/4890772086"

    const val TOP_BANNER_ID =
        BANNER_ID

    const val BOTTOM_BANNER_ID =
        BANNER_ID

    const val INTERSTITIAL_ID =
        "ca-app-pub-6146868530948467/8819293283"

    const val REWARDED_ID =
        "ca-app-pub-6146868530948467/5664321378"

    const val APP_OPEN_ID =
        "ca-app-pub-6146868530948467/2935989754"

    // =========================================================
    // STATE
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
    // INITIALIZE
    // =========================================================

    fun initialize(context: Context) {
        if (!isInitialized.compareAndSet(false, true)) {
            return
        }

        try {
            Log.d(TAG, "Initializing MobileAds SDK: $APP_ID")

            MobileAds.initialize(context) { status ->
                Log.d(TAG, "MOBILE_ADS_INITIALIZED: $status")

                loadInterstitial(context.applicationContext)
                loadRewarded(context.applicationContext)
                loadAppOpenAd(context.applicationContext)
            }
        } catch (e: Exception) {
            Log.e(TAG, "AdMob initialization error: ${e.message}", e)
        }
    }

    // =========================================================
    // INTERSTITIAL (LEVEL COMPLETE)
    // =========================================================

    fun loadInterstitial(context: Context) {
        if (interstitialAd != null) {
            Log.d(TAG, "INTERSTITIAL_ALREADY_LOADED")
            return
        }

        if (!isInterstitialLoading.compareAndSet(false, true)) {
            Log.d(TAG, "INTERSTITIAL_ALREADY_LOADING")
            return
        }

        Log.d(TAG, "INTERSTITIAL_LOAD_STARTED: $INTERSTITIAL_ID")

        InterstitialAd.load(
            context,
            INTERSTITIAL_ID,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    isInterstitialLoading.set(false)
                    interstitialRetryAttempt = 0
                    Log.d(TAG, "INTERSTITIAL_LOADED")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                    isInterstitialLoading.set(false)
                    Log.w(TAG, "INTERSTITIAL_LOAD_FAILED: code=${error.code}, message=${error.message}")

                    // Exponential backoff retry (5s, 10s, 20s, max 30s) up to 5 attempts
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

    fun showInterstitial(
        activity: Activity,
        onAdClosed: () -> Unit
    ) {
        if (activity.isFinishing || activity.isDestroyed) {
            Log.w(TAG, "INTERSTITIAL_SHOW_SKIPPED: Activity is finishing or destroyed.")
            onAdClosed()
            return
        }

        // Take current ad and immediately clear reference to prevent duplicate show
        val ad = interstitialAd
        interstitialAd = null

        if (ad == null) {
            Log.w(TAG, "INTERSTITIAL_UNAVAILABLE: Proceeding to level completion dialog immediately.")
            loadInterstitial(activity.applicationContext)
            onAdClosed()
            return
        }

        if (!isShowingFullscreenAd.compareAndSet(false, true)) {
            Log.w(TAG, "INTERSTITIAL_SHOW_SKIPPED: Fullscreen ad already showing.")
            onAdClosed()
            return
        }

        val callbackCalled = AtomicBoolean(false)

        fun safeClose() {
            isShowingFullscreenAd.set(false)
            if (callbackCalled.compareAndSet(false, true)) {
                activity.runOnUiThread {
                    onAdClosed()
                }
            }
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "INTERSTITIAL_SHOWING")
            }

            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "INTERSTITIAL_DISMISSED")
                loadInterstitial(activity.applicationContext)
                safeClose()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                Log.w(TAG, "INTERSTITIAL_SHOW_FAILED: code=${error.code}, message=${error.message}")
                loadInterstitial(activity.applicationContext)
                safeClose()
            }
        }

        activity.runOnUiThread {
            try {
                ad.show(activity)
            } catch (e: Exception) {
                Log.e(TAG, "INTERSTITIAL_SHOW_EXCEPTION: ${e.message}", e)
                loadInterstitial(activity.applicationContext)
                safeClose()
            }
        }
    }

    // =========================================================
    // REWARDED / HINT
    // =========================================================

    fun loadRewarded(context: Context) {
        if (rewardedAd != null) {
            return
        }

        if (!isRewardedLoading.compareAndSet(false, true)) {
            return
        }

        Log.d(TAG, "REWARDED_LOAD_STARTED: $REWARDED_ID")

        RewardedAd.load(
            context,
            REWARDED_ID,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    isRewardedLoading.set(false)
                    rewardedRetryAttempt = 0
                    Log.d(TAG, "REWARDED_LOADED")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewardedAd = null
                    isRewardedLoading.set(false)
                    Log.w(TAG, "REWARDED_LOAD_FAILED: code=${error.code}, message=${error.message}")

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

    fun showRewarded(
        activity: Activity,
        onUserEarnedReward: () -> Unit,
        onAdUnavailable: () -> Unit,
        onAdDismissed: () -> Unit = {}
    ) {
        if (activity.isFinishing || activity.isDestroyed) {
            onAdUnavailable()
            return
        }

        val ad = rewardedAd
        rewardedAd = null

        if (ad == null) {
            Log.w(TAG, "REWARDED_NOT_READY: Triggering load.")
            loadRewarded(activity.applicationContext)
            onAdUnavailable()
            return
        }

        if (!isShowingFullscreenAd.compareAndSet(false, true)) {
            Log.w(TAG, "REWARDED_SHOW_SKIPPED: Fullscreen ad already showing.")
            onAdUnavailable()
            return
        }

        val rewardGranted = AtomicBoolean(false)
        val dismissed = AtomicBoolean(false)

        fun safeDismiss() {
            isShowingFullscreenAd.set(false)
            if (dismissed.compareAndSet(false, true)) {
                activity.runOnUiThread {
                    onAdDismissed()
                }
            }
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "REWARDED_SHOWED")
            }

            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "REWARDED_DISMISSED")
                loadRewarded(activity.applicationContext)
                safeDismiss()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                Log.w(TAG, "REWARDED_SHOW_FAILED: ${error.message}")
                loadRewarded(activity.applicationContext)
                if (!rewardGranted.get()) {
                    activity.runOnUiThread {
                        onAdUnavailable()
                    }
                }
                safeDismiss()
            }
        }

        activity.runOnUiThread {
            try {
                ad.show(activity) {
                    Log.d(TAG, "REWARDED_USER_EARNED")
                    if (rewardGranted.compareAndSet(false, true)) {
                        activity.runOnUiThread {
                            onUserEarnedReward()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "REWARDED_SHOW_EXCEPTION: ${e.message}", e)
                loadRewarded(activity.applicationContext)
                onAdUnavailable()
                safeDismiss()
            }
        }
    }

    // =========================================================
    // APP OPEN
    // =========================================================

    fun loadAppOpenAd(context: Context) {
        if (appOpenAd != null || !isAppOpenLoading.compareAndSet(false, true)) {
            return
        }

        Log.d(TAG, "APP_OPEN_LOAD_STARTED: $APP_OPEN_ID")

        AppOpenAd.load(
            context,
            APP_OPEN_ID,
            AdRequest.Builder().build(),
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    appOpenAd = ad
                    isAppOpenLoading.set(false)
                    appOpenLoadTime = Date().time
                    Log.d(TAG, "APP_OPEN_LOADED")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    appOpenAd = null
                    isAppOpenLoading.set(false)
                    Log.w(TAG, "APP_OPEN_LOAD_FAILED: code=${error.code}, message=${error.message}")
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
            Log.d(TAG, "APP_OPEN_UNAVAILABLE: Skipping.")
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
                Log.d(TAG, "APP_OPEN_SHOWED")
            }

            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "APP_OPEN_DISMISSED")
                isShowingFullscreenAd.set(false)
                loadAppOpenAd(activity.applicationContext)
                onAdDismissed?.invoke()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                Log.w(TAG, "APP_OPEN_SHOW_FAILED: ${error.message}")
                isShowingFullscreenAd.set(false)
                loadAppOpenAd(activity.applicationContext)
                onAdDismissed?.invoke()
            }
        }

        activity.runOnUiThread {
            try {
                ad.show(activity)
            } catch (e: Exception) {
                Log.e(TAG, "APP_OPEN_SHOW_EXCEPTION: ${e.message}", e)
                isShowingFullscreenAd.set(false)
                loadAppOpenAd(activity.applicationContext)
                onAdDismissed?.invoke()
            }
        }
    }

    // =========================================================
    // ADAPTIVE BANNER SIZE HELPER
    // =========================================================

    private fun findActivity(context: Context): Activity? {
        var currentContext = context
        while (currentContext is android.content.ContextWrapper) {
            if (currentContext is Activity) {
                return currentContext
            }
            currentContext = currentContext.baseContext
        }
        return null
    }

    private fun getAdaptiveAdSize(context: Context): AdSize {
        return try {
            val act = findActivity(context)
            val displayMetrics = act?.resources?.displayMetrics ?: context.resources.displayMetrics
            val density = displayMetrics.density
            var widthPixels = displayMetrics.widthPixels.toFloat()
            if (widthPixels <= 0f) {
                widthPixels = 320f * density
            }
            val adWidthDp = (widthPixels / density).toInt().coerceAtLeast(320)

            AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(
                act ?: context,
                adWidthDp
            ) ?: AdSize.BANNER
        } catch (e: Exception) {
            Log.w(TAG, "Adaptive banner sizing fallback: ${e.message}")
            AdSize.BANNER
        }
    }

    // =========================================================
    // BANNER VIEW (Adaptive Banner)
    // =========================================================

    @Composable
    fun BannerAdView(
        modifier: Modifier = Modifier,
        adUnitId: String = BANNER_ID
    ) {
        var isLoaded by remember { mutableStateOf(false) }

        Box(
            modifier = modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            contentAlignment = Alignment.Center
        ) {
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                factory = { context ->
                    Log.d(TAG, "BANNER_CREATED")
                    AdView(context).apply {
                        val adSize = getAdaptiveAdSize(context)
                        setAdSize(adSize)
                        this.adUnitId = adUnitId
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )

                        adListener = object : AdListener() {
                            override fun onAdLoaded() {
                                isLoaded = true
                                Log.d(TAG, "BANNER_LOADED")
                            }

                            override fun onAdFailedToLoad(error: LoadAdError) {
                                isLoaded = false
                                Log.w(
                                    TAG,
                                    "BANNER_FAILED code=${error.code} message=${error.message}"
                                )
                                // Schedule a safe background retry after 30s without spamming
                                mainHandler.postDelayed({
                                    try {
                                        loadAd(AdRequest.Builder().build())
                                    } catch (e: Exception) {
                                        Log.w(TAG, "Banner retry load exception: ${e.message}")
                                    }
                                }, 30000L)
                            }
                        }

                        Log.d(TAG, "BANNER_LOAD_STARTED")
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

    // Backwards-compatible aliases for top / bottom instances
    @Composable
    fun TopBannerView(modifier: Modifier = Modifier) {
        BannerAdView(modifier = modifier, adUnitId = TOP_BANNER_ID)
    }

    @Composable
    fun BottomBannerView(modifier: Modifier = Modifier) {
        BannerAdView(modifier = modifier, adUnitId = BOTTOM_BANNER_ID)
    }
}

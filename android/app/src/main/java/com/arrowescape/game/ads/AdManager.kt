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
            Log.d(TAG, "AdManager already initialized. Skipping duplicate call.")
            return
        }

        try {
            Log.d(TAG, "==================================================")
            Log.d(TAG, "ADMOB_INITIALIZATION_STARTED: AppId=$APP_ID")

            // Configure Test Device Support for DEBUG builds
            if (BuildConfig.DEBUG) {
                val testDeviceIds = listOf(
                    AdRequest.DEVICE_ID_EMULATOR
                )
                val configuration = RequestConfiguration.Builder()
                    .setTestDeviceIds(testDeviceIds)
                    .build()
                MobileAds.setRequestConfiguration(configuration)
                Log.d(TAG, "DEBUG_BUILD: Configured AdMob RequestConfiguration with emulator test device support")
            }

            MobileAds.initialize(context) { status ->
                Log.d(TAG, "ADMOB_INITIALIZATION_COMPLETED")
                for ((adapterClass, adapterStatus) in status.adapterStatusMap) {
                    Log.d(
                        TAG,
                        "  Adapter: $adapterClass | State: ${adapterStatus.initializationState} | Description: ${adapterStatus.description} | Latency: ${adapterStatus.latency}ms"
                    )
                }
                Log.d(TAG, "==================================================")

                // Preload all ad formats safely
                loadInterstitial(context.applicationContext)
                loadRewarded(context.applicationContext)
                loadAppOpenAd(context.applicationContext)
            }
        } catch (e: Exception) {
            Log.e(TAG, "AdMob initialization encountered error: ${e.message}", e)
        }
    }

    // =========================================================
    // 2. INTERSTITIAL (LEVEL COMPLETE FLOW)
    // =========================================================

    fun loadInterstitial(context: Context) {
        if (interstitialAd != null) {
            Log.d(TAG, "INTERSTITIAL_ALREADY_AVAILABLE: Skipping request.")
            return
        }

        if (!isInterstitialLoading.compareAndSet(false, true)) {
            Log.d(TAG, "INTERSTITIAL_LOAD_IN_PROGRESS: Skipping duplicate request.")
            return
        }

        Log.d(TAG, "INTERSTITIAL_LOAD_STARTED: UnitId=$INTERSTITIAL_ID")

        InterstitialAd.load(
            context,
            INTERSTITIAL_ID,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    isInterstitialLoading.set(false)
                    interstitialRetryAttempt = 0
                    Log.d(TAG, "INTERSTITIAL_LOAD_SUCCESS: ResponseInfo=${ad.responseInfo}")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                    isInterstitialLoading.set(false)
                    Log.w(
                        TAG,
                        "INTERSTITIAL_LOAD_FAILED: " +
                                "errorCode=${error.code} (${getErrorCodeName(error.code)}), " +
                                "errorMessage=${error.message}, " +
                                "domain=${error.domain}, " +
                                "responseInfo=${error.responseInfo}"
                    )

                    // Exponential backoff retry (5s, 10s, 20s, max 30s) up to 5 attempts
                    if (interstitialRetryAttempt < 5) {
                        interstitialRetryAttempt++
                        val delayMs = (5000L * (1 shl (interstitialRetryAttempt - 1))).coerceAtMost(30000L)
                        Log.d(TAG, "INTERSTITIAL_RETRY_SCHEDULED in ${delayMs}ms (attempt $interstitialRetryAttempt)")
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

        // Take available ad and clear reference immediately to prevent duplicate shows
        val ad = interstitialAd
        interstitialAd = null

        if (ad == null) {
            Log.w(TAG, "INTERSTITIAL_NOT_LOADED: Proceeding immediately to level complete UI.")
            loadInterstitial(activity.applicationContext)
            onAdClosed()
            return
        }

        if (!isShowingFullscreenAd.compareAndSet(false, true)) {
            Log.w(TAG, "INTERSTITIAL_SHOW_SKIPPED: Another fullscreen ad is already active.")
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
                Log.d(TAG, "INTERSTITIAL_SHOW_SUCCESS: Ad is now showing on screen.")
            }

            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "INTERSTITIAL_DISMISSED: User dismissed the ad. Preloading next interstitial.")
                loadInterstitial(activity.applicationContext)
                safeClose()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                Log.w(
                    TAG,
                    "INTERSTITIAL_SHOW_FAILED: " +
                            "errorCode=${error.code}, " +
                            "errorMessage=${error.message}, " +
                            "domain=${error.domain}"
                )
                loadInterstitial(activity.applicationContext)
                safeClose()
            }

            override fun onAdImpression() {
                Log.d(TAG, "INTERSTITIAL_IMPRESSION_RECORDED")
            }

            override fun onAdClicked() {
                Log.d(TAG, "INTERSTITIAL_CLICKED")
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
    // 3. REWARDED / HINT FLOW
    // =========================================================

    fun loadRewarded(context: Context) {
        if (rewardedAd != null) {
            Log.d(TAG, "REWARDED_ALREADY_AVAILABLE: Skipping request.")
            return
        }

        if (!isRewardedLoading.compareAndSet(false, true)) {
            Log.d(TAG, "REWARDED_LOAD_IN_PROGRESS: Skipping duplicate request.")
            return
        }

        Log.d(TAG, "REWARDED_LOAD_STARTED: UnitId=$REWARDED_ID")

        RewardedAd.load(
            context,
            REWARDED_ID,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    isRewardedLoading.set(false)
                    rewardedRetryAttempt = 0
                    Log.d(TAG, "REWARDED_LOAD_SUCCESS: ResponseInfo=${ad.responseInfo}")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewardedAd = null
                    isRewardedLoading.set(false)
                    Log.w(
                        TAG,
                        "REWARDED_LOAD_FAILED: " +
                                "errorCode=${error.code} (${getErrorCodeName(error.code)}), " +
                                "errorMessage=${error.message}, " +
                                "domain=${error.domain}, " +
                                "responseInfo=${error.responseInfo}"
                    )

                    if (rewardedRetryAttempt < 5) {
                        rewardedRetryAttempt++
                        val delayMs = (5000L * (1 shl (rewardedRetryAttempt - 1))).coerceAtMost(30000L)
                        Log.d(TAG, "REWARDED_RETRY_SCHEDULED in ${delayMs}ms (attempt $rewardedRetryAttempt)")
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
            Log.w(TAG, "REWARDED_NOT_LOADED: Triggering background reload.")
            loadRewarded(activity.applicationContext)
            onAdUnavailable()
            return
        }

        if (!isShowingFullscreenAd.compareAndSet(false, true)) {
            Log.w(TAG, "REWARDED_SHOW_SKIPPED: Fullscreen ad already displaying.")
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
                Log.d(TAG, "REWARDED_SHOW_SUCCESS: Rewarded ad is now visible.")
            }

            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "REWARDED_DISMISSED: Preloading next rewarded ad.")
                loadRewarded(activity.applicationContext)
                safeDismiss()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                Log.w(
                    TAG,
                    "REWARDED_SHOW_FAILED: " +
                            "errorCode=${error.code}, " +
                            "errorMessage=${error.message}, " +
                            "domain=${error.domain}"
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
                Log.d(TAG, "REWARDED_IMPRESSION_RECORDED")
            }

            override fun onAdClicked() {
                Log.d(TAG, "REWARDED_CLICKED")
            }
        }

        activity.runOnUiThread {
            try {
                ad.show(activity) { rewardItem ->
                    Log.d(TAG, "REWARDED_USER_EARNED: amount=${rewardItem.amount}, type=${rewardItem.type}")
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
    // 4. APP OPEN AD
    // =========================================================

    fun loadAppOpenAd(context: Context) {
        if (appOpenAd != null || !isAppOpenLoading.compareAndSet(false, true)) {
            return
        }

        Log.d(TAG, "APP_OPEN_LOAD_STARTED: UnitId=$APP_OPEN_ID")

        AppOpenAd.load(
            context,
            APP_OPEN_ID,
            AdRequest.Builder().build(),
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    appOpenAd = ad
                    isAppOpenLoading.set(false)
                    appOpenLoadTime = Date().time
                    Log.d(TAG, "APP_OPEN_LOAD_SUCCESS: ResponseInfo=${ad.responseInfo}")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    appOpenAd = null
                    isAppOpenLoading.set(false)
                    Log.w(
                        TAG,
                        "APP_OPEN_LOAD_FAILED: " +
                                "errorCode=${error.code} (${getErrorCodeName(error.code)}), " +
                                "errorMessage=${error.message}, " +
                                "domain=${error.domain}, " +
                                "responseInfo=${error.responseInfo}"
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
                Log.d(TAG, "APP_OPEN_SHOW_SUCCESS")
            }

            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "APP_OPEN_DISMISSED: Preloading next App Open Ad.")
                isShowingFullscreenAd.set(false)
                loadAppOpenAd(activity.applicationContext)
                onAdDismissed?.invoke()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                Log.w(
                    TAG,
                    "APP_OPEN_SHOW_FAILED: " +
                            "errorCode=${error.code}, " +
                            "errorMessage=${error.message}, " +
                            "domain=${error.domain}"
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
                Log.e(TAG, "APP_OPEN_SHOW_EXCEPTION: ${e.message}", e)
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
                        // Keep subtle 50dp reserved container while loading, collapse if failed
                        Modifier.height(50.dp)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                factory = { context ->
                    Log.d(TAG, "BANNER_ADVIEW_CREATED: AdUnitId=$adUnitId")

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
                                Log.d(TAG, "BANNER_LOAD_SUCCESS: AdUnitId=$adUnitId | ResponseInfo=$responseInfo")
                            }

                            override fun onAdFailedToLoad(error: LoadAdError) {
                                isAdLoaded = false
                                Log.w(
                                    TAG,
                                    "BANNER_LOAD_FAILED: " +
                                            "adUnitId=$adUnitId, " +
                                            "errorCode=${error.code} (${getErrorCodeName(error.code)}), " +
                                            "errorMessage=${error.message}, " +
                                            "domain=${error.domain}, " +
                                            "responseInfo=${error.responseInfo}"
                                )

                                // Background safe retry after 30 seconds
                                mainHandler.postDelayed({
                                    try {
                                        Log.d(TAG, "BANNER_AUTO_RETRY: Attempting to reload banner.")
                                        loadAd(AdRequest.Builder().build())
                                    } catch (e: Exception) {
                                        Log.w(TAG, "Banner auto-retry exception: ${e.message}")
                                    }
                                }, 30000L)
                            }

                            override fun onAdOpened() {
                                Log.d(TAG, "BANNER_OPENED")
                            }

                            override fun onAdClosed() {
                                Log.d(TAG, "BANNER_CLOSED")
                            }

                            override fun onAdImpression() {
                                Log.d(TAG, "BANNER_IMPRESSION_RECORDED")
                            }

                            override fun onAdClicked() {
                                Log.d(TAG, "BANNER_CLICKED")
                            }
                        }

                        Log.d(TAG, "BANNER_LOAD_REQUEST_SENT: AdUnitId=$adUnitId")
                        loadAd(AdRequest.Builder().build())
                    }
                },
                onRelease = { adView ->
                    try {
                        Log.d(TAG, "BANNER_ADVIEW_DESTROYED")
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

    // =========================================================
    // HELPER FOR AD ERROR NAMES
    // =========================================================

    private fun getErrorCodeName(code: Int): String {
        return when (code) {
            AdRequest.ERROR_CODE_INTERNAL_ERROR -> "ERROR_CODE_INTERNAL_ERROR (0)"
            AdRequest.ERROR_CODE_INVALID_REQUEST -> "ERROR_CODE_INVALID_REQUEST (1)"
            AdRequest.ERROR_CODE_NETWORK_ERROR -> "ERROR_CODE_NETWORK_ERROR (2)"
            AdRequest.ERROR_CODE_NO_FILL -> "ERROR_CODE_NO_FILL (3)"
            else -> "CODE_$code"
        }
    }
}


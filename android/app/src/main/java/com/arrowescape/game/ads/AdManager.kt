package com.arrowescape.game.ads

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
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

    // =========================================================
    // ADMOB APP ID
    // =========================================================

    const val APP_ID =
        "ca-app-pub-6146868530948467~3047670393"

    // =========================================================
    // REAL ADMOB AD UNIT IDS
    // =========================================================

    const val TOP_BANNER_ID =
        "ca-app-pub-6146868530948467/6074173465"

    const val BOTTOM_BANNER_ID =
        "ca-app-pub-6146868530948467/4135617284"

    const val INTERSTITIAL_ID =
        "ca-app-pub-6146868530948467/9603566382"

    // HINT / REWARDED - WORKING ID
    const val REWARDED_ID =
        "ca-app-pub-6146868530948467/5664321378"

    // APP OPEN - WORKING ID
    const val APP_OPEN_ID =
        "ca-app-pub-6146868530948467/2935989754"

    // =========================================================
    // STATE
    // =========================================================

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

    private var appOpenLoadTime: Long = 0L

    private var isShowingAppOpenAd = false

    // =========================================================
    // INITIALIZE ADMOB
    // =========================================================

    fun initialize(context: Context) {

        if (!isInitialized.compareAndSet(false, true)) {
            return
        }

        try {

            Log.d(
                TAG,
                "Initializing MobileAds SDK"
            )

            MobileAds.initialize(context) { status ->

                Log.d(
                    TAG,
                    "MobileAds initialization complete: $status"
                )

                // Preload fullscreen ads
                loadInterstitial(context.applicationContext)
                loadRewarded(context.applicationContext)
                loadAppOpenAd(context.applicationContext)
            }

        } catch (e: Exception) {

            Log.e(
                TAG,
                "AdMob initialization error: ${e.message}",
                e
            )
        }
    }

    // =========================================================
    // 1. INTERSTITIAL
    // LEVEL COMPLETE AD
    // =========================================================

    fun loadInterstitial(context: Context) {

        if (interstitialAd != null) {
            Log.d(TAG, "INTERSTITIAL_ALREADY_LOADED")
            return
        }

        if (isInterstitialLoading) {
            Log.d(TAG, "INTERSTITIAL_ALREADY_LOADING")
            return
        }

        isInterstitialLoading = true

        Log.d(
            TAG,
            "INTERSTITIAL_LOADING: $INTERSTITIAL_ID"
        )

        val request =
            AdRequest.Builder().build()

        InterstitialAd.load(
            context,
            INTERSTITIAL_ID,
            request,
            object : InterstitialAdLoadCallback() {

                override fun onAdLoaded(
                    ad: InterstitialAd
                ) {

                    interstitialAd = ad
                    isInterstitialLoading = false

                    Log.d(
                        TAG,
                        "INTERSTITIAL_LOADED"
                    )
                }

                override fun onAdFailedToLoad(
                    error: LoadAdError
                ) {

                    interstitialAd = null
                    isInterstitialLoading = false

                    Log.e(
                        TAG,
                        "INTERSTITIAL_LOAD_FAILED: " +
                                "code=${error.code}, " +
                                "message=${error.message}, " +
                                "domain=${error.domain}"
                    )
                }
            }
        )
    }

    fun showInterstitial(
        activity: Activity,
        onAdClosed: () -> Unit
    ) {

        if (
            activity.isFinishing ||
            activity.isDestroyed
        ) {

            Log.w(
                TAG,
                "INTERSTITIAL_ACTIVITY_UNAVAILABLE"
            )

            onAdClosed()
            return
        }

        val ad = interstitialAd

        if (ad == null) {

            Log.w(
                TAG,
                "INTERSTITIAL_NOT_READY"
            )

            loadInterstitial(
                activity.applicationContext
            )

            onAdClosed()
            return
        }

        val callbackCalled =
            AtomicBoolean(false)

        fun safeClose() {

            if (
                callbackCalled.compareAndSet(
                    false,
                    true
                )
            ) {

                activity.runOnUiThread {
                    onAdClosed()
                }
            }
        }

        ad.fullScreenContentCallback =
            object : FullScreenContentCallback() {

                override fun onAdShowedFullScreenContent() {

                    Log.d(
                        TAG,
                        "INTERSTITIAL_SHOWED"
                    )
                }

                override fun onAdDismissedFullScreenContent() {

                    Log.d(
                        TAG,
                        "INTERSTITIAL_DISMISSED"
                    )

                    interstitialAd = null

                    loadInterstitial(
                        activity.applicationContext
                    )

                    safeClose()
                }

                override fun onAdFailedToShowFullScreenContent(
                    error: AdError
                ) {

                    Log.e(
                        TAG,
                        "INTERSTITIAL_SHOW_FAILED: " +
                                "${error.message}"
                    )

                    interstitialAd = null

                    loadInterstitial(
                        activity.applicationContext
                    )

                    safeClose()
                }
            }

        activity.runOnUiThread {

            try {

                Log.d(
                    TAG,
                    "INTERSTITIAL_SHOWING"
                )

                ad.show(activity)

            } catch (e: Exception) {

                Log.e(
                    TAG,
                    "INTERSTITIAL_SHOW_EXCEPTION: ${e.message}",
                    e
                )

                interstitialAd = null

                loadInterstitial(
                    activity.applicationContext
                )

                safeClose()
            }
        }
    }

    // =========================================================
    // 2. REWARDED
    // HINT AD - REAL ID
    // =========================================================

    fun loadRewarded(context: Context) {

        if (rewardedAd != null) {
            Log.d(TAG, "REWARDED_ALREADY_LOADED")
            return
        }

        if (isRewardedLoading) {
            return
        }

        isRewardedLoading = true

        Log.d(
            TAG,
            "REWARDED_LOADING: $REWARDED_ID"
        )

        val request =
            AdRequest.Builder().build()

        RewardedAd.load(
            context,
            REWARDED_ID,
            request,
            object : RewardedAdLoadCallback() {

                override fun onAdLoaded(
                    ad: RewardedAd
                ) {

                    rewardedAd = ad
                    isRewardedLoading = false

                    Log.d(
                        TAG,
                        "REWARDED_LOADED"
                    )
                }

                override fun onAdFailedToLoad(
                    error: LoadAdError
                ) {

                    rewardedAd = null
                    isRewardedLoading = false

                    Log.e(
                        TAG,
                        "REWARDED_LOAD_FAILED: " +
                                "code=${error.code}, " +
                                "message=${error.message}"
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

        if (
            activity.isFinishing ||
            activity.isDestroyed
        ) {

            onAdUnavailable()
            return
        }

        val ad = rewardedAd

        if (ad == null) {

            Log.w(
                TAG,
                "REWARDED_NOT_READY"
            )

            loadRewarded(
                activity.applicationContext
            )

            onAdUnavailable()
            return
        }

        val rewardGranted =
            AtomicBoolean(false)

        val dismissed =
            AtomicBoolean(false)

        fun safeDismiss() {

            if (
                dismissed.compareAndSet(
                    false,
                    true
                )
            ) {

                activity.runOnUiThread {
                    onAdDismissed()
                }
            }
        }

        ad.fullScreenContentCallback =
            object : FullScreenContentCallback() {

                override fun onAdShowedFullScreenContent() {

                    Log.d(
                        TAG,
                        "REWARDED_SHOWED"
                    )
                }

                override fun onAdDismissedFullScreenContent() {

                    Log.d(
                        TAG,
                        "REWARDED_DISMISSED"
                    )

                    rewardedAd = null

                    loadRewarded(
                        activity.applicationContext
                    )

                    safeDismiss()
                }

                override fun onAdFailedToShowFullScreenContent(
                    error: AdError
                ) {

                    Log.e(
                        TAG,
                        "REWARDED_SHOW_FAILED: ${error.message}"
                    )

                    rewardedAd = null

                    loadRewarded(
                        activity.applicationContext
                    )

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

                    Log.d(
                        TAG,
                        "REWARDED_USER_EARNED"
                    )

                    if (
                        rewardGranted.compareAndSet(
                            false,
                            true
                        )
                    ) {

                        activity.runOnUiThread {
                            onUserEarnedReward()
                        }
                    }
                }

            } catch (e: Exception) {

                Log.e(
                    TAG,
                    "REWARDED_SHOW_EXCEPTION: ${e.message}",
                    e
                )

                rewardedAd = null

                loadRewarded(
                    activity.applicationContext
                )

                onAdUnavailable()

                safeDismiss()
            }
        }
    }

    // =========================================================
    // 3. APP OPEN - REAL ID
    // =========================================================

    fun loadAppOpenAd(context: Context) {

        if (
            appOpenAd != null ||
            isAppOpenLoading
        ) {
            return
        }

        isAppOpenLoading = true

        Log.d(
            TAG,
            "APP_OPEN_LOADING: $APP_OPEN_ID"
        )

        val request =
            AdRequest.Builder().build()

        AppOpenAd.load(
            context,
            APP_OPEN_ID,
            request,
            object : AppOpenAd.AppOpenAdLoadCallback() {

                override fun onAdLoaded(
                    ad: AppOpenAd
                ) {

                    appOpenAd = ad
                    isAppOpenLoading = false
                    appOpenLoadTime = Date().time

                    Log.d(
                        TAG,
                        "APP_OPEN_LOADED"
                    )
                }

                override fun onAdFailedToLoad(
                    error: LoadAdError
                ) {

                    appOpenAd = null
                    isAppOpenLoading = false

                    Log.e(
                        TAG,
                        "APP_OPEN_LOAD_FAILED: " +
                                "code=${error.code}, " +
                                "message=${error.message}"
                    )
                }
            }
        )
    }

    private fun isAppOpenAdAvailable(): Boolean {

        return appOpenAd != null &&
                wasLoadTimeLessThanNHoursAgo(4)
    }

    private fun wasLoadTimeLessThanNHoursAgo(
        hours: Long
    ): Boolean {

        val difference =
            Date().time - appOpenLoadTime

        return difference < 3600000L * hours
    }

    fun showAppOpenAdIfAvailable(
        activity: Activity,
        onAdDismissed: (() -> Unit)? = null
    ) {

        if (isShowingAppOpenAd) {
            return
        }

        if (!isAppOpenAdAvailable()) {

            loadAppOpenAd(
                activity.applicationContext
            )

            onAdDismissed?.invoke()
            return
        }

        val ad = appOpenAd ?: return

        ad.fullScreenContentCallback =
            object : FullScreenContentCallback() {

                override fun onAdShowedFullScreenContent() {

                    isShowingAppOpenAd = true

                    Log.d(
                        TAG,
                        "APP_OPEN_SHOWED"
                    )
                }

                override fun onAdDismissedFullScreenContent() {

                    Log.d(
                        TAG,
                        "APP_OPEN_DISMISSED"
                    )

                    appOpenAd = null
                    isShowingAppOpenAd = false

                    loadAppOpenAd(
                        activity.applicationContext
                    )

                    onAdDismissed?.invoke()
                }

                override fun onAdFailedToShowFullScreenContent(
                    error: AdError
                ) {

                    Log.e(
                        TAG,
                        "APP_OPEN_SHOW_FAILED: ${error.message}"
                    )

                    appOpenAd = null
                    isShowingAppOpenAd = false

                    loadAppOpenAd(
                        activity.applicationContext
                    )

                    onAdDismissed?.invoke()
                }
            }

        activity.runOnUiThread {

            try {

                ad.show(activity)

            } catch (e: Exception) {

                Log.e(
                    TAG,
                    "APP_OPEN_SHOW_EXCEPTION: ${e.message}",
                    e
                )

                appOpenAd = null
                isShowingAppOpenAd = false

                loadAppOpenAd(
                    activity.applicationContext
                )

                onAdDismissed?.invoke()
            }
        }
    }

    // =========================================================
    // 4. ADAPTIVE BANNER SIZE
    // =========================================================

    private fun getAdaptiveAdSize(
        context: Context
    ): AdSize {

        return try {

            val metrics =
                context.resources.displayMetrics

            val widthPixels =
                metrics.widthPixels.toFloat()

            val density =
                metrics.density

            val widthDp =
                if (density > 0f) {
                    (widthPixels / density).toInt()
                } else {
                    320
                }

            AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(
                context,
                widthDp
            )

        } catch (e: Exception) {

            Log.w(
                TAG,
                "Adaptive banner failed: ${e.message}"
            )

            AdSize.BANNER
        }
    }

    // =========================================================
    // 5. TOP BANNER - REAL ID
    // =========================================================

    @Composable
    fun TopBannerView(
        modifier: Modifier = Modifier
    ) {

        AndroidView(
            modifier = modifier
                .fillMaxWidth()
                .wrapContentHeight(),

            factory = { context ->

                Log.d(
                    TAG,
                    "TOP_BANNER_CREATED"
                )

                AdView(context).apply {

                    setAdSize(
                        getAdaptiveAdSize(context)
                    )

                    adUnitId =
                        TOP_BANNER_ID

                    layoutParams =
                        ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )

                    adListener =
                        object : AdListener() {

                            override fun onAdLoaded() {

                                Log.d(
                                    TAG,
                                    "TOP_BANNER_LOADED"
                                )
                            }

                            override fun onAdFailedToLoad(
                                error: LoadAdError
                            ) {

                                Log.e(
                                    TAG,
                                    "TOP_BANNER_FAILED: " +
                                            "code=${error.code}, " +
                                            "message=${error.message}, " +
                                            "domain=${error.domain}"
                                )
                            }
                        }

                    Log.d(
                        TAG,
                        "TOP_BANNER_LOADING: $TOP_BANNER_ID"
                    )

                    loadAd(
                        AdRequest.Builder().build()
                    )
                }
            }
        )
    }

    // =========================================================
    // 6. BOTTOM BANNER - REAL ID
    // =========================================================

    @Composable
    fun BottomBannerView(
        modifier: Modifier = Modifier
    ) {

        AndroidView(
            modifier = modifier
                .fillMaxWidth()
                .wrapContentHeight(),

            factory = { context ->

                Log.d(
                    TAG,
                    "BOTTOM_BANNER_CREATED"
                )

                AdView(context).apply {

                    setAdSize(
                        getAdaptiveAdSize(context)
                    )

                    adUnitId =
                        BOTTOM_BANNER_ID

                    layoutParams =
                        ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )

                    adListener =
                        object : AdListener() {

                            override fun onAdLoaded() {

                                Log.d(
                                    TAG,
                                    "BOTTOM_BANNER_LOADED"
                                )
                            }

                            override fun onAdFailedToLoad(
                                error: LoadAdError
                            ) {

                                Log.e(
                                    TAG,
                                    "BOTTOM_BANNER_FAILED: " +
                                            "code=${error.code}, " +
                                            "message=${error.message}, " +
                                            "domain=${error.domain}"
                                )
                            }
                        }

                    Log.d(
                        TAG,
                        "BOTTOM_BANNER_LOADING: $BOTTOM_BANNER_ID"
                    )

                    loadAd(
                        AdRequest.Builder().build()
                    )
                }
            }
        )
    }
}

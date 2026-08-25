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
    // APP ID
    // =========================================================

    const val APP_ID =
        "ca-app-pub-6146868530948467~3047670393"

    // =========================================================
    // GOOGLE TEST AD UNIT IDs
    // =========================================================

    private const val TEST_BANNER_ID =
        "ca-app-pub-3940256099942544/6300978111"

    private const val TEST_INTERSTITIAL_ID =
        "ca-app-pub-3940256099942544/1033173712"

    private const val TEST_REWARDED_ID =
        "ca-app-pub-3940256099942544/5224354917"

    private const val TEST_APP_OPEN_ID =
        "ca-app-pub-3940256099942544/9257395921"

    // =========================================================
    // YOUR REAL AD UNIT IDs
    // =========================================================

    const val TOP_BANNER_ID =
        "ca-app-pub-6146868530948467/6074173465"

    const val BOTTOM_BANNER_ID =
        "ca-app-pub-6146868530948467/4135617284"

    const val INTERSTITIAL_ID =
        "ca-app-pub-6146868530948467/9603566382"

    const val REWARDED_ID =
        "ca-app-pub-6146868530948467/5664321378"

    const val APP_OPEN_ID =
        "ca-app-pub-6146868530948467/2935989754"

    // =========================================================
    // TEST SWITCHES
    // =========================================================
    //
    // CURRENT TEST:
    // TOP BANNER = REAL
    // BOTTOM BANNER = TEST
    // INTERSTITIAL = TEST
    // REWARDED = TEST
    // APP OPEN = TEST
    //
    // Ek-ek karke test karenge.
    // =========================================================

    private const val USE_TEST_TOP_BANNER = false
    private const val USE_TEST_BOTTOM_BANNER = true
    private const val USE_TEST_INTERSTITIAL = true
    private const val USE_TEST_REWARDED = true
    private const val USE_TEST_APP_OPEN = true

    // =========================================================
    // ACTIVE IDs
    // =========================================================

    private val activeTopBannerId: String
        get() = if (USE_TEST_TOP_BANNER) {
            TEST_BANNER_ID
        } else {
            TOP_BANNER_ID
        }

    private val activeBottomBannerId: String
        get() = if (USE_TEST_BOTTOM_BANNER) {
            TEST_BANNER_ID
        } else {
            BOTTOM_BANNER_ID
        }

    private val activeInterstitialId: String
        get() = if (USE_TEST_INTERSTITIAL) {
            TEST_INTERSTITIAL_ID
        } else {
            INTERSTITIAL_ID
        }

    private val activeRewardedId: String
        get() = if (USE_TEST_REWARDED) {
            TEST_REWARDED_ID
        } else {
            REWARDED_ID
        }

    private val activeAppOpenId: String
        get() = if (USE_TEST_APP_OPEN) {
            TEST_APP_OPEN_ID
        } else {
            APP_OPEN_ID
        }

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
                "Initializing MobileAds SDK. App ID=$APP_ID"
            )

            MobileAds.initialize(context) { status ->

                Log.d(
                    TAG,
                    "MobileAds initialized: $status"
                )

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
    // INTERSTITIAL
    // =========================================================

    fun loadInterstitial(context: Context) {

        if (interstitialAd != null || isInterstitialLoading) {
            return
        }

        isInterstitialLoading = true

        Log.d(
            TAG,
            "INTERSTITIAL_LOADING: $activeInterstitialId"
        )

        InterstitialAd.load(
            context,
            activeInterstitialId,
            AdRequest.Builder().build(),

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
                                error.message
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
    // REWARDED
    // =========================================================

    fun loadRewarded(context: Context) {

        if (rewardedAd != null || isRewardedLoading) {
            return
        }

        isRewardedLoading = true

        Log.d(
            TAG,
            "REWARDED_LOADING: $activeRewardedId"
        )

        RewardedAd.load(
            context,
            activeRewardedId,
            AdRequest.Builder().build(),

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
                        onAdUnavailable()
                    }

                    safeDismiss()
                }
            }

        activity.runOnUiThread {

            try {

                ad.show(activity) {

                    if (
                        rewardGranted.compareAndSet(
                            false,
                            true
                        )
                    ) {

                        Log.d(
                            TAG,
                            "REWARDED_USER_EARNED"
                        )

                        onUserEarnedReward()
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
    // APP OPEN
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
            "APP_OPEN_LOADING: $activeAppOpenId"
        )

        AppOpenAd.load(
            context,
            activeAppOpenId,
            AdRequest.Builder().build(),

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
        numHours: Long
    ): Boolean {

        return Date().time - appOpenLoadTime <
                3600000L * numHours
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
    // ADAPTIVE BANNER SIZE
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

            val adWidth =
                if (density > 0) {
                    (widthPixels / density).toInt()
                } else {
                    320
                }

            AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(
                context,
                adWidth
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
    // TOP BANNER
    // =========================================================

    @Composable
    fun TopBannerView(
        modifier: Modifier = Modifier
    ) {

        AndroidView(
            modifier = modifier
                .fillMaxWidth()
                .wrapContentHeight(),

            factory = { ctx ->

                AdView(ctx).apply {

                    setAdSize(
                        getAdaptiveAdSize(ctx)
                    )

                    adUnitId =
                        activeTopBannerId

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
                                    "TOP_BANNER_LOADED | REAL=${
                                        !USE_TEST_TOP_BANNER
                                    }"
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
                        "TOP_BANNER_LOADING: $activeTopBannerId"
                    )

                    loadAd(
                        AdRequest.Builder().build()
                    )
                }
            }
        )
    }

    // =========================================================
    // BOTTOM BANNER
    // =========================================================

    @Composable
    fun BottomBannerView(
        modifier: Modifier = Modifier
    ) {

        AndroidView(
            modifier = modifier
                .fillMaxWidth()
                .wrapContentHeight(),

            factory = { ctx ->

                AdView(ctx).apply {

                    setAdSize(
                        getAdaptiveAdSize(ctx)
                    )

                    adUnitId =
                        activeBottomBannerId

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
                                    "BOTTOM_BANNER_LOADED | TEST=$USE_TEST_BOTTOM_BANNER"
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
                        "BOTTOM_BANNER_LOADING: $activeBottomBannerId"
                    )

                    loadAd(
                        AdRequest.Builder().build()
                    )
                }
            }
        )
    }
}

package com.arrowescape.game

import android.app.Application
import com.arrowescape.game.ads.AdManager

class ArrowEscapeApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Initialize Google Mobile Ads SDK with provided AdMob configuration
        AdManager.initialize(this)
    }
}

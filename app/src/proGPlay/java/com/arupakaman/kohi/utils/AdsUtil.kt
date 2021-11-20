package com.arupakaman.kohi.utils

import android.app.Activity
import android.util.Log
import android.view.View

class AdsUtil(private val mActivity: Activity) {

    fun loadBannerAd(adView: View){
        Log.d("Nothing", "${mActivity.hashCode()} ${adView.hashCode()}")
    }

    fun showInterstitialAd(adUnitId: String, onAdLoaded : () -> Unit = {}){
        Log.d("Nothing", adUnitId)
        onAdLoaded()
    }

    fun destroyBannerAd(adView: View){
        Log.d("Nothing", "${adView.hashCode()}")
    }

}
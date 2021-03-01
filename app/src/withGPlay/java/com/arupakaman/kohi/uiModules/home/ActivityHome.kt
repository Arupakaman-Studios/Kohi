package com.arupakaman.kohi.uiModules.home

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.core.view.isVisible
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.arupakaman.kohi.R
import com.arupakaman.kohi.data.KohiSharedPrefs
import com.arupakaman.kohi.databinding.ActivityHomeBinding
import com.arupakaman.kohi.services.ForegroundService
import com.arupakaman.kohi.uiModules.base.BaseAppCompatActivity
import com.arupakaman.kohi.uiModules.common.ActivityCommon
import com.arupakaman.kohi.uiModules.gotoAboutScreen
import com.arupakaman.kohi.uiModules.gotoSettingsScreen
import com.arupakaman.kohi.uiModules.openDonationVersion
import com.arupakaman.kohi.utils.invoke
import com.arupakaman.kohi.utils.setSafeOnClickListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback


class ActivityHome : BaseAppCompatActivity() {

    companion object{
        private const val AD_MOB_TEST_DEVICE_ID = "FE0C59029CB20A5711C8C1E2BCBD264A"
    }

    private val mBinding by lazy { ActivityHomeBinding.inflate(layoutInflater) }

    private val mPrefs by lazy { KohiSharedPrefs.getInstance(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mBinding.root)

        MobileAds.initialize(applicationContext)

        /*val requestConfiguration = RequestConfiguration.Builder()
            .setTestDeviceIds(listOf(AD_MOB_TEST_DEVICE_ID))
            .build()
        MobileAds.setRequestConfiguration(requestConfiguration)*/

        mBinding{

            fun showInterstitialAd(){
                InterstitialAd.load(this@ActivityHome, getString(R.string.key_ad_mob_interstitial_home_unit_id),
                    AdRequest.Builder().build(), object : InterstitialAdLoadCallback(){
                        override fun onAdLoaded(p0: InterstitialAd) {
                            super.onAdLoaded(p0)
                            p0.show(this@ActivityHome)
                            mPrefs.toggleClickCount = 0
                        }
                    })
            }

            val toggleClickCount = mPrefs.toggleClickCount

            btnKohiToggle.setSafeOnClickListener {
                val isRunning = mPrefs.isForegroundServiceRunning && ForegroundService.isRunning
                mPrefs.wasForegroundServiceRunningOnLock = false
                ForegroundService.startOrStopService(applicationContext, !isRunning)
                if (toggleClickCount == 6)
                    showInterstitialAd()
            }

            btnSettings.setSafeOnClickListener {
                if (toggleClickCount == 6)
                    showInterstitialAd()
                else
                    gotoSettingsScreen()
            }

            btnAbout.setSafeOnClickListener {
                if (toggleClickCount == 6)
                    showInterstitialAd()
                else
                    gotoAboutScreen()
            }

            btnGetDonationVersion.setSafeOnClickListener {
                openDonationVersion()
            }
            btnGetDonationVersion.isVisible = true

            adView.loadAd(AdRequest.Builder().build())

        }

    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(this).registerReceiver(
            mReceiver,
            IntentFilter(ForegroundService.ACTION_FOREGROUND_SERVICE_TOGGLE)
        )
        mBinding.updateToggleBtn(mPrefs.isForegroundServiceRunning)
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver)
    }

    private fun ActivityHomeBinding.updateToggleBtn(isRunning: Boolean){
        btnKohiToggle.setBackgroundResource(if (isRunning) R.drawable.bg_circle_primary else R.color.colorTransparent)
    }

    /**
     * Broadcast receiver to receive the Foreground service state
     */
    private val mReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            val isRunning = intent.getBooleanExtra(
                ForegroundService.EXTRA_FOREGROUND_SERVICE_TOGGLE_RUNNING,
                false
            )
            mBinding.updateToggleBtn(isRunning)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ActivityCommon.REQUEST_CODE_SETTINGS && resultCode == Activity.RESULT_OK) {
            recreate()
        }
    }

}
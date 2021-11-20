package com.arupakaman.kohi.uiModules.home

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.arupakaman.kohi.BuildConfig
import com.arupakaman.kohi.KohiApp
import com.arupakaman.kohi.R
import com.arupakaman.kohi.data.KohiSharedPrefs
import com.arupakaman.kohi.databinding.ActivityHomeBinding
import com.arupakaman.kohi.services.ForegroundService
import com.arupakaman.kohi.uiModules.base.BaseAppCompatActivity
import com.arupakaman.kohi.uiModules.gotoAboutScreen
import com.arupakaman.kohi.uiModules.gotoSettingsScreen
import com.arupakaman.kohi.uiModules.openDonationVersion
import com.arupakaman.kohi.utils.AdsUtil
import com.arupakaman.kohi.utils.GooglePlayUtils
import com.arupakaman.kohi.utils.invoke
import com.arupakaman.kohi.utils.setFirebaseAnalyticsLogEvent
import com.arupakaman.kohi.utils.setSafeOnClickListener
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ActivityHome : BaseAppCompatActivity() {

    companion object{
        private const val EXTRA_KOHI_TRIGGER = "kohi_trigger"

        fun getIntent(mContext: Context, kohiTrigger: Boolean) = Intent(mContext, ActivityHome::class.java).apply {
            putExtra(EXTRA_KOHI_TRIGGER, kohiTrigger)
        }
    }

    private val mBinding by lazy { ActivityHomeBinding.inflate(layoutInflater) }

    private val mPrefs by lazy { KohiSharedPrefs.getInstance(applicationContext) }

    private lateinit var mAdsUtil: AdsUtil

    private var mDialog: AlertDialog? = null
    private var isPermissionRequested = false

    private var toggleClickCount: Int
        get() = mPrefs.toggleClickCount
        set(value) { mPrefs.toggleClickCount = value }


    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            recreate()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mBinding.root)

        mAdsUtil = AdsUtil(this)

        mBinding{

            fun showInterstitialAd(){
                mAdsUtil.showInterstitialAd(getString(R.string.key_ad_mob_interstitial_home_unit_id)){
                    mPrefs.toggleClickCount = 0
                }
            }

            btnKohiToggle.setSafeOnClickListener {
                toggleKohi()

                if (BuildConfig.isAdsOn) {
                    if (toggleClickCount == 6)
                        showInterstitialAd()
                    else toggleClickCount++
                }
            }

            btnSettings.setSafeOnClickListener {
                if (BuildConfig.isAdsOn) {
                    if (toggleClickCount == 6)
                        showInterstitialAd()
                    else {
                        gotoSettingsScreen(resultLauncher)
                        toggleClickCount++
                    }
                } else gotoSettingsScreen(resultLauncher)
                setFirebaseAnalyticsLogEvent(KohiApp.EVENT_KOHI, bundleOf("Go_To_Nav" to "Settings"))
            }

            btnAbout.setSafeOnClickListener {
                if (BuildConfig.isAdsOn) {
                    if (toggleClickCount == 6)
                        showInterstitialAd()
                    else {
                        gotoAboutScreen()
                        toggleClickCount++
                    }
                } else gotoAboutScreen()
                setFirebaseAnalyticsLogEvent(KohiApp.EVENT_KOHI, bundleOf("Go_To_Nav" to "About"))
            }

            btnGetDonationVersion.setSafeOnClickListener {
                openDonationVersion()
            }
            btnGetDonationVersion.isVisible = BuildConfig.isAdsOn

            mAdsUtil.loadBannerAd(includeAdView.adView)

            if (intent.getBooleanExtra(EXTRA_KOHI_TRIGGER, false))
                toggleKohi()

            GooglePlayUtils(this@ActivityHome).checkUpdate()

        }

    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(this).registerReceiver(
            mReceiver,
            IntentFilter(ForegroundService.ACTION_FOREGROUND_SERVICE_TOGGLE)
        )
        mBinding.updateToggleBtn(mPrefs.isForegroundServiceRunning)
        if (isPermissionRequested){
            toggleKohi()
        }
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver)
    }

    override fun onDestroy() {
        mDialog?.let {
            if (it.isShowing){
                it.dismiss()
            }
        }

        mDialog = null
        super.onDestroy()
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

    private fun toggleKohi(){
        isPermissionRequested = false
        fun startKohiService(){
            val isRunning = mPrefs.isForegroundServiceRunning && ForegroundService.isRunning
            mPrefs.wasForegroundServiceRunningOnLock = false
            ForegroundService.startOrStopService(applicationContext, !isRunning)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Settings.System.canWrite(applicationContext)) startKohiService()
            else {
                mDialog = MaterialAlertDialogBuilder(this)
                    .setTitle(R.string.title_permission_required)
                    .setMessage(R.string.desc_write_settings_permission)
                    .setCancelable(false)
                    .setPositiveButton(R.string.action_grant){ dialog, _ ->
                        val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
                            .setData(Uri.parse("package:$packageName"))
                            .addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                            .addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)

                        startActivity(intent)
                        dialog.dismiss()
                        mDialog = null
                        isPermissionRequested = true
                    }.create()

                mDialog?.show()
            }
        } else startKohiService()
    }

}
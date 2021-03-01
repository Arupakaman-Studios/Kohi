package com.arupakaman.kohi.uiModules.home

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.core.view.isVisible
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.arupakaman.kohi.BuildConfig
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


class ActivityHome : BaseAppCompatActivity() {

    private val mBinding by lazy { ActivityHomeBinding.inflate(layoutInflater) }

    private val mPrefs by lazy { KohiSharedPrefs.getInstance(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mBinding.root)

        mBinding{

            btnKohiToggle.setSafeOnClickListener {
                val isRunning = mPrefs.isForegroundServiceRunning && ForegroundService.isRunning
                mPrefs.wasForegroundServiceRunningOnLock = false
                ForegroundService.startOrStopService(applicationContext, !isRunning)
            }

            btnSettings.setSafeOnClickListener {
                gotoSettingsScreen()
            }

            btnAbout.setSafeOnClickListener {
                gotoAboutScreen()
            }

            btnGetDonationVersion.setSafeOnClickListener {
                openDonationVersion()
            }
            btnGetDonationVersion.isVisible = false

        }

    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver,
            IntentFilter(ForegroundService.ACTION_FOREGROUND_SERVICE_TOGGLE))
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
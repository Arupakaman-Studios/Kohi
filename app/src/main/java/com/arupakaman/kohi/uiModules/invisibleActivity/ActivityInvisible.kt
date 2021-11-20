package com.arupakaman.kohi.uiModules.invisibleActivity

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.core.os.bundleOf
import com.arupakaman.kohi.KohiApp
import com.arupakaman.kohi.data.KohiSharedPrefs
import com.arupakaman.kohi.services.ForegroundService
import com.arupakaman.kohi.uiModules.gotoHomeScreen
import com.arupakaman.kohi.utils.setFirebaseAnalyticsLogEvent
import com.google.firebase.analytics.FirebaseAnalytics
import kotlin.system.exitProcess

class ActivityInvisible : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent?.action == ACTION_TOGGLE) {
            val mPrefs = KohiSharedPrefs.getInstance(applicationContext)
            val isRunning = mPrefs.isForegroundServiceRunning
            Log.d(TAG, "Received ACTION_TOGGLE $isRunning")
            mPrefs.wasForegroundServiceRunningOnLock = false
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.System.canWrite(applicationContext)){
                gotoHomeScreen(true)
            }else {
                ForegroundService.startOrStopService(this, !isRunning)
                applicationContext.setFirebaseAnalyticsLogEvent(KohiApp.EVENT_KOHI, bundleOf("Kohi_Toggle_Notif" to "is_on_$isRunning"))
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            finishAndRemoveTask()
        }else {
            finishAffinity()
            exitProcess(2)
        }
    }

    companion object {
        private val TAG by lazy { "ActivityInvisible" }
        const val ACTION_TOGGLE = "toggle"
    }
}
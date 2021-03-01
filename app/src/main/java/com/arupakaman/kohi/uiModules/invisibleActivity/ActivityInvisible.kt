package com.arupakaman.kohi.uiModules.invisibleActivity

import android.app.Activity
import android.os.Bundle
import android.util.Log
import com.arupakaman.kohi.KohiApp
import com.arupakaman.kohi.data.KohiSharedPrefs
import com.arupakaman.kohi.services.ForegroundService
import kotlin.system.exitProcess

class ActivityInvisible : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent?.action == ACTION_TOGGLE) {
            val mPrefs = KohiSharedPrefs.getInstance(applicationContext)
            val isRunning = mPrefs.isForegroundServiceRunning
            Log.d(TAG, "Received ACTION_TOGGLE $isRunning")
            mPrefs.wasForegroundServiceRunningOnLock = false
            ForegroundService.startOrStopService(this, !isRunning)
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
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
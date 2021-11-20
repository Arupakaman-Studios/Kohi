package com.arupakaman.kohi.broadcastReceivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.arupakaman.kohi.R
import com.arupakaman.kohi.data.KohiSharedPrefs
import com.arupakaman.kohi.services.ForegroundService
import com.arupakaman.kohi.utils.KohiRes
import com.arupakaman.kohi.utils.toast

class BroadcastReceiverBootCompleted: BroadcastReceiver() {

    override fun onReceive(p0: Context?, p1: Intent?) {
        p0?.run {
            val mPrefs = KohiSharedPrefs.getInstance(applicationContext)
            ForegroundService.updateInitialTimeout(applicationContext, mPrefs)
            KohiRes.setContext(applicationContext)
            val action = p1?.action
            if (action == Intent.ACTION_BOOT_COMPLETED ||
                action == Intent.ACTION_LOCKED_BOOT_COMPLETED) {
                val wasRunning = mPrefs.isForegroundServiceRunning
                val restoreOnBoot = mPrefs.restoreOnBoot

                if (restoreOnBoot && wasRunning) {
                    toast(KohiRes.getString(R.string.msg_kohi_started))
                    mPrefs.wasForegroundServiceRunningOnLock = false
                    ForegroundService.startOrStopService(applicationContext, true)
                }
            }
        }
    }

}
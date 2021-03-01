package com.arupakaman.kohi.broadcastReceivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.arupakaman.kohi.R
import com.arupakaman.kohi.data.KohiSharedPrefs
import com.arupakaman.kohi.services.ForegroundService
import com.arupakaman.kohi.utils.KohiRes
import com.arupakaman.kohi.utils.toast

class BroadcastReceiverScreenUnlock: BroadcastReceiver() {

    override fun onReceive(p0: Context?, p1: Intent?) {
        p0?.run {
            KohiRes.setContext(applicationContext)
            val action = p1?.action
            if (action == Intent.ACTION_USER_PRESENT) {
                val mPrefs = KohiSharedPrefs.getInstance(applicationContext)
                val wasRunning = mPrefs.wasForegroundServiceRunningOnLock
                val restoreOnUnlock = mPrefs.restoreOnUnlock

                if (restoreOnUnlock && wasRunning) {
                    toast(KohiRes.getString(R.string.msg_kohi_started))
                    mPrefs.wasForegroundServiceRunningOnLock = false
                    ForegroundService.startOrStopService(applicationContext, true)
                }
            }
        }
    }

}
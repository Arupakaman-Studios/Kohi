package com.arupakaman.kohi.services

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.service.quicksettings.Tile.STATE_ACTIVE
import android.service.quicksettings.Tile.STATE_INACTIVE
import android.service.quicksettings.TileService
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.os.bundleOf
import com.arupakaman.kohi.KohiApp
import com.arupakaman.kohi.data.KohiSharedPrefs
import com.arupakaman.kohi.utils.invoke
import com.arupakaman.kohi.utils.setFirebaseAnalyticsLogEvent
import com.google.firebase.analytics.FirebaseAnalytics

@RequiresApi(Build.VERSION_CODES.N)
class KohiTileService : TileService() {

    override fun onClick() {
        val mPrefs = KohiSharedPrefs.getInstance(applicationContext)
        val isRunning = mPrefs.isForegroundServiceRunning
        Log.d(TAG, "onClick $isRunning")
        mPrefs.wasForegroundServiceRunningOnLock = false
        ForegroundService.startOrStopService(applicationContext, !isRunning)
        applicationContext.setFirebaseAnalyticsLogEvent(KohiApp.EVENT_KOHI, bundleOf("Kohi_Toggle_Tile" to "is_on_$isRunning"))
    }

    override fun onStartListening() {
        Log.d(TAG, "onStartListening")
        setTileState()
        super.onStartListening()
    }

    override fun onStopListening() {
        Log.d(TAG, "onStopListening")
        setTileState()
        super.onStopListening()
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        super.onDestroy()
    }

    override fun onLowMemory() {
        Log.d(TAG, "onLowMemory")
        super.onLowMemory()
    }

    override fun onTileAdded() {
        Log.d(TAG, "onTileAdded")
        setTileState()
        super.onTileAdded()
    }

    override fun onTileRemoved() {
        Log.d(TAG, "onTileRemoved")
        super.onTileRemoved()
    }

    override fun onTrimMemory(level: Int) {
        Log.d(TAG, "onTrimMemory")
        super.onTrimMemory(level)
    }

    override fun onRebind(intent: Intent?) {
        Log.d(TAG, "onRebind")
        super.onRebind(intent)
    }

    private fun setTileState() {
        val isRunning = KohiSharedPrefs.getInstance(applicationContext).isForegroundServiceRunning
        Log.d(TAG, "setTileState running -> $isRunning")
        val tile = qsTile ?: return
        tile {
            state = if (isRunning) STATE_ACTIVE else STATE_INACTIVE
            updateTile()
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    companion object {
        private val TAG by lazy { "KohiTileService" }

        fun requestTileStateUpdate(mContext: Context) {
            Log.d(TAG, "requestTileStateUpdate")
            requestListeningState(mContext, ComponentName(mContext, KohiTileService::class.java))
        }
    }
}
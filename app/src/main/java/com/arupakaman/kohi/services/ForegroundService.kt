package com.arupakaman.kohi.services

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.arupakaman.kohi.BuildConfig
import com.arupakaman.kohi.R
import com.arupakaman.kohi.data.KohiSharedPrefs
import com.arupakaman.kohi.utils.KohiRes

class ForegroundService : Service() {

    companion object {
        private val TAG by lazy { "ForegroundService" }
        private const val ACTION_STOP = "stop_action"
        const val ACTION_FOREGROUND_SERVICE_TOGGLE = "foreground_service_toggle"
        const val EXTRA_FOREGROUND_SERVICE_TOGGLE_RUNNING = "foreground_service_running"
        const val NOTIFICATION_ID = 101

        var isRunning = false

        fun startOrStopService(mContext: Context, start: Boolean) {
            Log.d(TAG, "startOrStop start -> $start")
            val mPrefs = KohiSharedPrefs.getInstance(mContext)
            mPrefs.isForegroundServiceRunning = start
            Intent(mContext, ForegroundService::class.java).let { intent->
                if (start) {
                    ContextCompat.startForegroundService(mContext, intent)
                    if (BuildConfig.isAdsOn && mPrefs.toggleClickCount < 6){
                        mPrefs.toggleClickCount = mPrefs.toggleClickCount+1
                    }
                } else {
                    mContext.stopService(intent)
                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(
                        getForegroundServiceToggleIntent(false))
                }
                isRunning = start
            }
        }

        fun getForegroundServiceToggleIntent(isRunning: Boolean) = Intent(ACTION_FOREGROUND_SERVICE_TOGGLE).apply {
            putExtra(EXTRA_FOREGROUND_SERVICE_TOGGLE_RUNNING, isRunning)
        }

    }

    private var mWakeLock: PowerManager.WakeLock? = null
    private val mPhoneStateReceiver by lazy { PhoneStateReceiver() }
    private var isPhoneStateListenerRegistered = false


    override fun onBind(p0: Intent?): IBinder? = null

    override fun onCreate() {
        Log.d(TAG, "onCreate")
        super.onCreate()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notifMan = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            with(NotificationChannel(
                KohiRes.getString(R.string.notification_channel_name),
                KohiRes.getString(R.string.app_name),
                NotificationManager.IMPORTANCE_MIN
            )) {
                setShowBadge(true)
                enableVibration(false)
                enableLights(false)
                setSound(null, null)
                notifMan.createNotificationChannel(this)
            }
        }

        val stopIntent = Intent(this, ForegroundService::class.java)
        stopIntent.action = ACTION_STOP
        val pendingStopIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            PendingIntent.getForegroundService(this, 0, stopIntent, 0)
        } else {
            PendingIntent.getService(this, 0, stopIntent, 0)
        }

        val notification = notifBuilder
            .setContentText(KohiRes.getString(R.string.msg_tap_to_turn_off))
            .setContentIntent(pendingStopIntent)
            .setPublicVersion(notifBuilder.build())
            .build()

        KohiSharedPrefs.getInstance(applicationContext).isForegroundServiceRunning = true

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            KohiTileService.requestTileStateUpdate(this)

        startForeground(NOTIFICATION_ID, notification)

        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(
            getForegroundServiceToggleIntent(true))

    }

    private val notifBuilder: NotificationCompat.Builder
        get() = NotificationCompat.Builder(this, KohiRes.getString(R.string.notification_channel_name))
            .setContentTitle(KohiRes.getString(R.string.msg_keeping_display_on))
            .setTicker(KohiRes.getString(R.string.msg_keeping_display_on))
            .setSmallIcon(R.drawable.ic_kohi_outline)
            .setOngoing(true)
            .setShowWhen(true)
            .setWhen(System.currentTimeMillis())
            .setColor(ContextCompat.getColor(applicationContext, R.color.colorCoffee))
            .setCategory(NotificationCompat.CATEGORY_SERVICE)


    @SuppressLint("WakelockTimeout")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand()")
        val mPrefs = KohiSharedPrefs.getInstance(applicationContext)
        if (intent?.action == ACTION_STOP) {
            Log.d(TAG, "Received ACTION_STOP")
            mPrefs.wasForegroundServiceRunningOnLock = false
            startOrStopService(this, false)
        }

        @Suppress("DEPRECATION")
        mWakeLock = (getSystemService(Context.POWER_SERVICE) as PowerManager)
            .newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "Kohi::ForegroundService")
        mWakeLock?.acquire()

        mPrefs.isForegroundServiceRunning = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            KohiTileService.requestTileStateUpdate(this)
        }
        if (!isPhoneStateListenerRegistered) {
            val intentFilter = IntentFilter().apply {
                addAction(Intent.ACTION_SCREEN_OFF)
                addAction(Intent.ACTION_BATTERY_LOW)
            }
            registerReceiver(mPhoneStateReceiver, intentFilter)
            isPhoneStateListenerRegistered = true
        }
        return START_STICKY
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy()")
        super.onDestroy()
        mWakeLock?.release()
        if (isPhoneStateListenerRegistered) {
            unregisterReceiver(mPhoneStateReceiver)
            isPhoneStateListenerRegistered = false
        }

        KohiSharedPrefs.getInstance(applicationContext).isForegroundServiceRunning = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            KohiTileService.requestTileStateUpdate(this)
        }
    }


    private class PhoneStateReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            context?.run {
                val action = intent?.action
                val mPrefs = KohiSharedPrefs.getInstance(applicationContext)
                if ((mPrefs.stopOnLock && action == Intent.ACTION_SCREEN_OFF) || (mPrefs.stopOnLowBattery && action == Intent.ACTION_BATTERY_LOW)) {
                    Log.d(TAG, "PhoneStateReceiver -> Stop service")
                    //if (action == Intent.ACTION_SCREEN_OFF) mPrefs.wasForegroundServiceRunningOnLock = true
                    startOrStopService(applicationContext, false)
                }
                /*if (action == Intent.ACTION_SCREEN_OFF || (mPrefs.stopOnLowBattery && action == Intent.ACTION_BATTERY_LOW)) {
                    Log.d(TAG, "PhoneStateReceiver -> Stop service")
                    if (action == Intent.ACTION_SCREEN_OFF) mPrefs.wasForegroundServiceRunningOnLock = true
                    startOrStopService(applicationContext, false)
                }*/
            }
        }
    }

}
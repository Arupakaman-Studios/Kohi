package com.arupakaman.kohi

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.multidex.MultiDexApplication
import com.arupakaman.kohi.data.KohiSharedPrefs
import com.arupakaman.kohi.uiModules.settings.FragmentSettings
import com.arupakaman.kohi.utils.DefaultExceptionHandler
import com.arupakaman.kohi.utils.KohiRes
import com.arupakaman.kohi.utils.LocaleHelper

class KohiApp : MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()

        KohiRes.setContext(applicationContext)

        if (BuildConfig.DEBUG || BuildConfig.isFDroid)
            Thread.setDefaultUncaughtExceptionHandler(DefaultExceptionHandler(this))

        //Init
        val mPrefs = KohiSharedPrefs.getInstance(this)

        AppCompatDelegate.setDefaultNightMode(mPrefs.selectedThemeMode)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            ShortcutManagerCompat.addDynamicShortcuts(this, listOf(FragmentSettings.getToggleShortCut(this)))
        }
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(LocaleHelper.onAttach(base))
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        LocaleHelper.onAttach(this)
    }

}
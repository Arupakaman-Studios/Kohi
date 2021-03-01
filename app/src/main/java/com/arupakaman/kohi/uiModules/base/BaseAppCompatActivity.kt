package com.arupakaman.kohi.uiModules.base

import android.content.Context
import android.os.Bundle
import android.os.PersistableBundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.arupakaman.kohi.data.KohiSharedPrefs
import com.arupakaman.kohi.utils.LocaleHelper

abstract class BaseAppCompatActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
        //AppCompatDelegate.setDefaultNightMode(KohiSharedPrefs.getInstance(applicationContext).selectedThemeMode)
        //delegate.applyDayNight()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) onBackPressed()
        return super.onOptionsItemSelected(item)
    }

}
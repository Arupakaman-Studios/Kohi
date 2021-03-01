package com.arupakaman.kohi.uiModules.settings

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.core.view.isVisible
import com.arupakaman.kohi.R
import com.arupakaman.kohi.data.KohiSharedPrefs
import com.arupakaman.kohi.databinding.FragmentSettingsBinding
import com.arupakaman.kohi.uiModules.base.BaseFragment
import com.arupakaman.kohi.uiModules.common.ActivityCommon
import com.arupakaman.kohi.uiModules.invisibleActivity.ActivityInvisible
import com.arupakaman.kohi.utils.KohiRes
import com.arupakaman.kohi.utils.LocaleHelper
import com.arupakaman.kohi.utils.invoke
import com.arupakaman.kohi.utils.setSafeOnClickListener
import com.arupakaman.kohi.utils.toast
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.util.*

class FragmentSettings : BaseFragment() {

    companion object{

        private val TAG by lazy { "FragmentSettings" }

        fun newInstance() = FragmentSettings()

        fun getToggleShortCut(mContext: Context) = ShortcutInfoCompat.Builder(mContext, "kohi_toggle")
            .setIntent(Intent(mContext, ActivityInvisible::class.java)
                .setAction(ActivityInvisible.ACTION_TOGGLE))
            .setShortLabel(KohiRes.getString(R.string.app_name))
            .setLongLabel(KohiRes.getString(R.string.title_toggle_kohi))
            .setIcon(IconCompat.createWithResource(mContext, R.mipmap.ic_shortcut))
            .setAlwaysBadged()
            .build()

    }

    private val mBinding by lazy { FragmentSettingsBinding.inflate(layoutInflater) }
    private val mPrefs by lazy { KohiSharedPrefs.getInstance(mActivity.applicationContext) }

    private var selLangPos = 0

    private var mDialog: AlertDialog? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding{
            setViewListeners()
            setSelectedTheme()

            switchRestoreOnBoot.isChecked = mPrefs.restoreOnBoot
            switchRestoreOnUnlock.isChecked = mPrefs.restoreOnUnlock
            switchDisableOnLowBattery.isChecked = mPrefs.stopOnLowBattery
            switchRestoreOnUnlock.isChecked = mPrefs.stopOnLock

            val isShortcutSupported = ShortcutManagerCompat.isRequestPinShortcutSupported(mActivity)
            clShortcut.isVisible = isShortcutSupported

        }
    }

    override fun onResume() {
        super.onResume()
        mBinding {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                setBatteryOptimizationView()
            } else clBatteryOptimization.isVisible = false
        }
    }

    override fun onDestroy() {
        if (mDialog?.isShowing == true){
            mDialog?.dismiss()
        }
        mDialog = null
        super.onDestroy()
    }

    @SuppressLint("SetTextI18n")
    private fun FragmentSettingsBinding.setViewListeners(){
        llSystemTheme.setSafeOnClickListener {
            updateTheme(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
        llLightTheme.setSafeOnClickListener {
            updateTheme(AppCompatDelegate.MODE_NIGHT_NO)
        }
        llDarkTheme.setSafeOnClickListener {
            updateTheme(AppCompatDelegate.MODE_NIGHT_YES)
        }

        switchRestoreOnBoot.setOnCheckedChangeListener { _, b ->
            mPrefs.restoreOnBoot = b
        }

        switchRestoreOnUnlock.setOnCheckedChangeListener { _, b ->
            mPrefs.restoreOnUnlock = b
        }

        switchStopOnLock.setOnCheckedChangeListener { _, b ->
            mPrefs.stopOnLock = b
        }

        switchDisableOnLowBattery.setOnCheckedChangeListener { _, b ->
            mPrefs.stopOnLowBattery = b
        }

        clBatteryOptimization.setSafeOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Intent().apply {
                    action = Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS
                    mActivity.startActivity(this)
                }
            }
        }

        clShortcut.setSafeOnClickListener {
            /*val isSuccess = */
            ShortcutManagerCompat.requestPinShortcut(mActivity,
                getToggleShortCut(mActivity), null)

            // mActivity.toast(if (isSuccess) R.string.msg_shortcut_add_success else R.string.msg_shortcut_add_failed)
        }

        selLangPos = LocaleHelper.getSelectedLanguageCodePosition(mActivity)

        tvLanguageDesc.text = KohiRes.getString(R.string.desc_current_language_colon) + " " + LocaleHelper.getLanguageByPosition(mActivity, selLangPos).second

        clLanguage.setSafeOnClickListener {
            mDialog = MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.title_select_language_colon)
                .setSingleChoiceItems(R.array.arr_languages, LocaleHelper.getSelectedLanguageCodePosition(mActivity)){ _, which ->
                    //mActivity.toast("$which  ${Locale.CHINESE.language}")
                    selLangPos = which
                }
                .setPositiveButton(R.string.action_select){ dialog, _ ->
                    val selLang = LocaleHelper.getLanguageByPosition(mActivity, selLangPos)
                    LocaleHelper.setLocale(mActivity, selLang.first, selLang.second)
                    dialog.dismiss()
                    mDialog = null
                    (mActivity as ActivityCommon).languageChanged = true
                    mActivity.onBackPressed()
                }
                .setNegativeButton(R.string.action_cancel){ dialog, _ ->
                    dialog.dismiss()
                    mDialog = null
                }.create()

            mDialog?.show()

        }

    }


    @RequiresApi(Build.VERSION_CODES.M)
    private fun FragmentSettingsBinding.setBatteryOptimizationView(){
        val pm = mActivity.getSystemService(Context.POWER_SERVICE) as PowerManager
        if (pm.isIgnoringBatteryOptimizations(mActivity.packageName)) {
            clBatteryOptimization.isEnabled = false
            clBatteryOptimization.alpha = 0.4f
            tvDisableBatteryOptimizationTitle.text = KohiRes.getString(R.string.title_disabled_battery_optimization)
            tvDisableBatteryOptimizationDesc.text = ""
        }else{
            clBatteryOptimization.isEnabled = true
            clBatteryOptimization.alpha = 1f
            tvDisableBatteryOptimizationTitle.text = KohiRes.getString(R.string.title_disable_battery_optimization)
            tvDisableBatteryOptimizationDesc.text = KohiRes.getString(R.string.desc_disable_battery_optimization)
        }
    }

    private fun FragmentSettingsBinding.setSelectedTheme(){
        when(mPrefs.selectedThemeMode){
            AppCompatDelegate.MODE_NIGHT_NO -> {
                llSystemTheme.setBackgroundResource(R.color.colorTransparent)
                llDarkTheme.setBackgroundResource(R.color.colorTransparent)
                llLightTheme.setBackgroundResource(R.drawable.bg_rounded_rectangle_primary)
            }
            AppCompatDelegate.MODE_NIGHT_YES -> {
                llSystemTheme.setBackgroundResource(R.color.colorTransparent)
                llLightTheme.setBackgroundResource(R.color.colorTransparent)
                llDarkTheme.setBackgroundResource(R.drawable.bg_rounded_rectangle_primary)
            }
            else -> {
                llLightTheme.setBackgroundResource(R.color.colorTransparent)
                llDarkTheme.setBackgroundResource(R.color.colorTransparent)
                llSystemTheme.setBackgroundResource(R.drawable.bg_rounded_rectangle_primary)
            }
        }
    }

    private fun updateTheme(themeMode: Int){
        mPrefs.selectedThemeMode = themeMode
        AppCompatDelegate.setDefaultNightMode(themeMode)
        (mActivity as AppCompatActivity).delegate.applyDayNight()
        mBinding.setSelectedTheme()
    }

}
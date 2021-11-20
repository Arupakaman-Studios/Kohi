package com.arupakaman.kohi.uiModules.common

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.arupakaman.kohi.R
import com.arupakaman.kohi.databinding.ActivityCommonBinding
import com.arupakaman.kohi.services.ForegroundService
import com.arupakaman.kohi.uiModules.about.FragmentAbout
import com.arupakaman.kohi.uiModules.base.BaseAppCompatActivity
import com.arupakaman.kohi.uiModules.settings.FragmentSettings
import com.arupakaman.kohi.utils.KohiRes
import com.arupakaman.kohi.utils.invoke

class ActivityCommon : BaseAppCompatActivity() {

    companion object{

        const val KEY_FRAG_NAME_SETTINGS = "settings"
        const val KEY_FRAG_NAME_ABOUT = "about"

        private const val EXTRA_KEY_FRAG_NAME = "frag_name"

        fun getActivityIntent(mContext: Context, fragName: String) =
            Intent(mContext, ActivityCommon::class.java).apply {
                putExtra(EXTRA_KEY_FRAG_NAME, fragName)
            }

    }

    private val mBinding by lazy { ActivityCommonBinding.inflate(layoutInflater) }

    private var fragName = ""

    var languageChanged = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(mBinding.root)

        fragName = intent.getStringExtra(EXTRA_KEY_FRAG_NAME)?:""

        val fragTransaction = supportFragmentManager.beginTransaction()

        mBinding{
            setSupportActionBar(toolbar)
            supportActionBar?.apply {
                setDisplayHomeAsUpEnabled(true)
                setDisplayShowHomeEnabled(true)
            }

            when(fragName){
                KEY_FRAG_NAME_SETTINGS -> {
                    supportActionBar?.title = KohiRes.getString(R.string.title_settings)
                    fragTransaction.replace(R.id.fragmentContainer, FragmentSettings.newInstance(), "FragmentSettings").commit()
                }
                KEY_FRAG_NAME_ABOUT -> {
                    supportActionBar?.title = KohiRes.getString(R.string.title_about_us)
                    fragTransaction.replace(R.id.fragmentContainer, FragmentAbout.newInstance(), "FragmentAbout").commit()
                }
                else -> finish()
            }
        }

    }

    override fun onBackPressed() {
        if (languageChanged){
            val intent = Intent()
            setResult(Activity.RESULT_OK, intent)
            finish()
        }else super.onBackPressed()
    }

}
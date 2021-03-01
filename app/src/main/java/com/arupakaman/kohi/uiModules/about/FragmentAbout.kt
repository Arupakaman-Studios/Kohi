package com.arupakaman.kohi.uiModules.about

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.arupakaman.kohi.BuildConfig
import com.arupakaman.kohi.R
import com.arupakaman.kohi.data.KohiSharedPrefs
import com.arupakaman.kohi.databinding.FragmentAboutBinding
import com.arupakaman.kohi.uiModules.base.BaseFragment
import com.arupakaman.kohi.uiModules.openArupakamanPlayStore
import com.arupakaman.kohi.uiModules.openContactMail
import com.arupakaman.kohi.uiModules.openDonationVersion
import com.arupakaman.kohi.utils.AppReviewUtil
import com.arupakaman.kohi.utils.KohiRes
import com.arupakaman.kohi.utils.invoke
import com.arupakaman.kohi.utils.isUnderlined
import com.arupakaman.kohi.utils.setSafeOnClickListener


class FragmentAbout : BaseFragment() {

    companion object{

        private val TAG by lazy { "FragmentAbout" }

        fun newInstance() = FragmentAbout()

    }

    private val mBinding by lazy { FragmentAboutBinding.inflate(layoutInflater) }
    //private val mPrefs by lazy { KohiSharedPrefs.getInstance(mActivity.applicationContext) }

    private lateinit var mAppReviewUtil: AppReviewUtil

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding {

            mAppReviewUtil = AppReviewUtil(mActivity)

            btnRateApp.setSafeOnClickListener {
               mAppReviewUtil.askForReview()
            }

            btnGetDonationVersion.setSafeOnClickListener {
                mActivity.openDonationVersion()
            }
            btnGetDonationVersion.isVisible = BuildConfig.isAdsOn || BuildConfig.isFDroid

            btnShare.setSafeOnClickListener {
                shareApp()
            }

            btnMoreApps.setSafeOnClickListener {
                mActivity.openArupakamanPlayStore()
            }

            tvMsgContact.isUnderlined = true
            tvMsgContact.setSafeOnClickListener {
                mActivity.openContactMail()
            }

        }
    }

    private fun shareApp(){
        kotlin.runCatching {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name))
            var shareMessage = "\n${KohiRes.getString(R.string.msg_share_kohi_app)}"
            shareMessage = "$shareMessage https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}"
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
            startActivity(Intent.createChooser(shareIntent, KohiRes.getString(R.string.title_share_via)))
        }.onFailure {
            Log.e(TAG, "shareApp Exc : $it")
        }
    }


}
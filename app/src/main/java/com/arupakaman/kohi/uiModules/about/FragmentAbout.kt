package com.arupakaman.kohi.uiModules.about

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import com.arupakaman.kohi.BuildConfig
import com.arupakaman.kohi.KohiApp
import com.arupakaman.kohi.R
import com.arupakaman.kohi.databinding.FragmentAboutBinding
import com.arupakaman.kohi.uiModules.base.BaseFragment
import com.arupakaman.kohi.uiModules.openArupakamanPlayStore
import com.arupakaman.kohi.uiModules.openContactMail
import com.arupakaman.kohi.uiModules.openDonationVersion
import com.arupakaman.kohi.utils.GooglePlayUtils
import com.arupakaman.kohi.utils.KohiRes
import com.arupakaman.kohi.utils.invoke
import com.arupakaman.kohi.utils.isUnderlined
import com.arupakaman.kohi.utils.setFirebaseAnalyticsLogEvent
import com.arupakaman.kohi.utils.setSafeOnClickListener


class FragmentAbout : BaseFragment() {

    companion object{

        private val TAG by lazy { "FragmentAbout" }

        fun newInstance() = FragmentAbout()

    }

    private val mBinding by lazy { FragmentAboutBinding.inflate(layoutInflater) }
    //private val mPrefs by lazy { KohiSharedPrefs.getInstance(mActivity.applicationContext) }

    private lateinit var mGooglePlayUtils: GooglePlayUtils

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

            mGooglePlayUtils = GooglePlayUtils(mActivity)

            btnRateApp.setSafeOnClickListener {
               mGooglePlayUtils.askForReview()
                mActivity.setFirebaseAnalyticsLogEvent(KohiApp.EVENT_KOHI, bundleOf("App_Review" to "Asked"))
            }

            btnGetDonationVersion.setSafeOnClickListener {
                mActivity.openDonationVersion()
                mActivity.setFirebaseAnalyticsLogEvent(KohiApp.EVENT_KOHI, bundleOf("Donation_Version" to "Clicked"))
            }
            btnGetDonationVersion.isVisible = BuildConfig.isAdsOn || BuildConfig.isFDroid

            btnShare.setSafeOnClickListener {
                shareApp()
                mActivity.setFirebaseAnalyticsLogEvent(KohiApp.EVENT_KOHI, bundleOf("App_Shared" to "Shared"))
            }

            btnMoreApps.setSafeOnClickListener {
                mActivity.openArupakamanPlayStore()
                mActivity.setFirebaseAnalyticsLogEvent(KohiApp.EVENT_KOHI, bundleOf("More_Apps" to "More"))
            }

            tvMsgContact.isUnderlined = true
            tvMsgContact.setSafeOnClickListener {
                mActivity.openContactMail()
                mActivity.setFirebaseAnalyticsLogEvent(KohiApp.EVENT_KOHI, bundleOf("Contact_Dev" to "Mail"))
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
package com.arupakaman.kohi.utils

import android.app.Activity
import android.util.Log
import com.arupakaman.kohi.BuildConfig
import com.arupakaman.kohi.R
import com.arupakaman.kohi.uiModules.openAppInPlayStore
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.android.play.core.tasks.Task

class AppReviewUtil(private val mActivity: Activity) {

    fun askForReview(){
        val reviewManager = ReviewManagerFactory.create(mActivity)
        reviewManager.requestReviewFlow().addOnCompleteListener { request ->
            if (request.isSuccessful) {
                //Received ReviewInfo object
                val reviewInfo = request.result
                Log.d("AppReviewUtil", "reviewInfo -> $reviewInfo")
                kotlin.runCatching {
                    val flow = reviewManager.launchReviewFlow(mActivity, reviewInfo)
                    flow.addOnCompleteListener {
                        Log.d("AppReviewUtil", "CompleteListener -> ${it.isSuccessful} ${it.exception}")
                        if (!it.isSuccessful)
                            mActivity.openAppInPlayStore(BuildConfig.APPLICATION_ID)
                    }
                }.onFailure {
                    mActivity.openAppInPlayStore(BuildConfig.APPLICATION_ID)
                }
            }
        }
    }

}
package com.arupakaman.kohi.utils

import android.app.Activity
import android.util.Log
import com.arupakaman.kohi.BuildConfig
import com.arupakaman.kohi.uiModules.openAppInPlayStore
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.review.ReviewManagerFactory

class GooglePlayUtils(private val mActivity: Activity) {

    private val TAG by lazy { "AppReviewUtil" }

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
            }else mActivity.openAppInPlayStore(BuildConfig.APPLICATION_ID)
        }
    }

    fun checkUpdate() {
        kotlin.runCatching {
            // Creates instance of the manager.
            val appUpdateManager: AppUpdateManager = AppUpdateManagerFactory.create(mActivity)
            // Returns an intent object that you use to check for an update.
            val appUpdateInfoTask = appUpdateManager.appUpdateInfo
            // Before starting an update, register a listener for updates.
            var listener: InstallStateUpdatedListener? = null
            listener = InstallStateUpdatedListener { installState ->
                if (installState.installStatus() == InstallStatus.DOWNLOADED) {
                    // After the update is downloaded, show a notification
                    // and request user confirmation to restart the app.
                    Log.d(TAG, "An update has been downloaded")
                    // When status updates are no longer needed, unregister the listener.
                    listener?.let {
                        appUpdateManager.unregisterListener(it)
                    }
                    appUpdateManager.completeUpdate()
                }
            }
            appUpdateManager.registerListener(listener)
            // Checks that the platform will allow the specified type of update.
            Log.d(TAG, "Checking for updates")
            appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
                if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                    // Request the update.
                    Log.d(TAG, "Update available")
                    if (appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)){
                        appUpdateManager.startUpdateFlowForResult(
                            // Pass the intent that is returned by 'getAppUpdateInfo()'.
                            appUpdateInfo,
                            // Or 'AppUpdateType.FLEXIBLE' for flexible updates.
                            AppUpdateType.FLEXIBLE,
                            // The current activity making the update request.
                            mActivity,
                            // Include a request code to later monitor this update request.
                            1011)
                    }else{
                        appUpdateManager.startUpdateFlowForResult(
                            // Pass the intent that is returned by 'getAppUpdateInfo()'.
                            appUpdateInfo,
                            // Or 'AppUpdateType.IMMEDIATE' for IMMEDIATE updates.
                            AppUpdateType.IMMEDIATE,
                            // The current activity making the update request.
                            mActivity,
                            // Include a request code to later monitor this update request.
                            1011)
                    }
                } else {
                    Log.d(TAG, "No Update available")
                }
            }
        }
    }

}
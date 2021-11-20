package com.arupakaman.kohi.utils

import android.content.Context
import android.os.Bundle
import com.arupakaman.kohi.BuildConfig
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.messaging.FirebaseMessaging

fun Throwable.reportExceptionToCrashlytics(msg: String){
    if (!BuildConfig.DEBUG) FirebaseCrashlytics.getInstance().log("MANUAL_LOGGING : $msg | Exc : $this")
}

fun Context.setFirebaseAnalyticsLogEvent(eventName: String, params: Bundle){
    if (!BuildConfig.DEBUG) FirebaseAnalytics.getInstance(this).logEvent(eventName, params)
}

fun subscribeToFCMTopicAll(){
    FirebaseMessaging.getInstance().subscribeToTopic("all")
}
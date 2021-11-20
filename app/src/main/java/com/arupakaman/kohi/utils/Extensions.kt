package com.arupakaman.kohi.utils

import android.content.Context
import android.graphics.Paint
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.StringRes
import com.arupakaman.kohi.BuildConfig
import com.google.firebase.analytics.FirebaseAnalytics

operator fun <T> T.invoke(block: T.() -> Unit) = block()

/**
 *   Toast
 */

fun Context.toast(mMsg: String) {
    Toast.makeText(this, mMsg, Toast.LENGTH_SHORT).show()
}

fun Context.toast(@StringRes mResId: Int) {
    toast(KohiRes.getString(mResId))
}

fun Context.toastLong(mMsg: String) {
    Toast.makeText(this, mMsg, Toast.LENGTH_LONG).show()
}

fun Context.toastLong(@StringRes mResId: Int) {
    toastLong(KohiRes.getString(mResId))
}

var TextView.isUnderlined: Boolean
    get() = ((paintFlags and Paint.UNDERLINE_TEXT_FLAG) == Paint.UNDERLINE_TEXT_FLAG)
    set(isUnderlined) {
        paintFlags = if(isUnderlined) (paintFlags or Paint.UNDERLINE_TEXT_FLAG) else (paintFlags xor Paint.UNDERLINE_TEXT_FLAG)
    }
package com.arupakaman.kohi.uiModules

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import com.arupakaman.kohi.R
import com.arupakaman.kohi.uiModules.common.ActivityCommon
import com.arupakaman.kohi.uiModules.home.ActivityHome
import com.arupakaman.kohi.utils.KohiRes

fun Context.gotoHomeScreen(triggerKohi: Boolean){
    startActivity(ActivityHome.getIntent(this, triggerKohi))
}

fun AppCompatActivity.gotoSettingsScreen(launcher: ActivityResultLauncher<Intent>){
    launcher.launch(ActivityCommon.getActivityIntent(this, ActivityCommon.KEY_FRAG_NAME_SETTINGS))
}

fun Context.gotoAboutScreen(){
    startActivity(ActivityCommon.getActivityIntent(this, ActivityCommon.KEY_FRAG_NAME_ABOUT))
}

fun Context.openDonationVersion(){
   openAppInPlayStore(getString(R.string.donate_version_pkg_name))
}

fun Context.openAppInPlayStore(id: String){
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$id"))
    val optionalIntent =  Intent(
        Intent.ACTION_VIEW,
        Uri.parse("https://play.google.com/store/apps/details?id=$id")
    )
    kotlin.runCatching {
        if (intent.resolveActivity(packageManager) != null) startActivity(intent)
        if (optionalIntent.resolveActivity(packageManager) != null) startActivity(optionalIntent)
    }.onFailure {
        Log.e("AppNavigation", "openAppInPlayStore Exc $it")
    }
}


fun Context.openContactMail(msg: String? = null){
    kotlin.runCatching {
        Intent(Intent.ACTION_SENDTO).let { emailIntent ->
            emailIntent.data = Uri.parse("mailto:")
            emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.email_publisher)))
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name))
            emailIntent.putExtra(Intent.EXTRA_TEXT, msg ?: KohiRes.getString(R.string.msg_enter_your_message))
            val packageManager = packageManager

            if (emailIntent.resolveActivity(packageManager) != null) {
                startActivity(Intent.createChooser(emailIntent, KohiRes.getString(R.string.title_send_via)))
            }
        }
    }
}

fun Context.openArupakamanPlayStore(){
    val intent =  Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/developer?id=Arupakaman+Studios"))
    kotlin.runCatching {
        if (intent.resolveActivity(packageManager) != null) startActivity(intent)
    }
}
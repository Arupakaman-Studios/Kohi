package com.arupakaman.kohi.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.arupakaman.kohi.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.util.concurrent.atomic.AtomicInteger

class MyFirebaseMessagingService: FirebaseMessagingService() {

    companion object {
        private val TAG by lazy { "MyFCMService" }
    }

    private val c: AtomicInteger = AtomicInteger(1000)
    private fun getNewNotificationId() = c.incrementAndGet()

    private fun getNotificationIcon(): Int {
        val useWhiteIcon = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
        return if (useWhiteIcon) R.drawable.ic_kohi_outline else R.mipmap.ic_launcher_foreground
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate")
    }

    override fun onMessageReceived(p0: RemoteMessage) {
        super.onMessageReceived(p0)

        Log.v(TAG, "onMessageReceived $p0")
        Log.v(TAG, "ContentNotif 1 " + p0.notification?.title + "\n" + p0.notification?.body + "")

        val notification: RemoteMessage.Notification? = p0.notification
        val data: Map<String, String> = p0.data

        Log.v(TAG, "ContentNotif 2 $notification \n $data")

        kotlin.runCatching {
            if (p0.data["body"] == null) {
                p0.notification?.let {data->
                    val title = data.title ?: getString(R.string.app_name)
                    var body = data.body ?: ""
                    var id = "Arupakaman+Studios"
                    kotlin.runCatching {
                        if (body.contains("::")) {
                            body.split("::").let {
                                body = it[0]
                                id = it[1]
                            }
                        }
                    }
                    sendNotification(title, body, id, null)
                }
            }else {
                p0.runCatching {
                    sendNotification(data["title"]?: getString(R.string.app_name),
                        data["body"]?:"", (data["id"]?:"Arupakaman+Studios"), data["url"])
                }
            }
        }.onFailure {
            Log.e(TAG, "Notification Send Exc $it")
        }

    }

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
        Log.v(TAG, "onNewToken FCM_token $p0")
    }

    override fun onDestroy() {
        Log.v(TAG, "onDestroy")
        super.onDestroy()
    }

    private fun sendNotification(title: String, body: String, id: String?, url: String?) {

        val channelId = getString(R.string.default_notification_channel_id)

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url?:("https://play.google.com/store/apps/details?id=$id")))
        val pendingIntent =  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_ONE_SHOT)
        } else {
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)
        }

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(getNotificationIcon())
            //.setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher_foreground))
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setChannelId(channelId)
            .setContentIntent(pendingIntent)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            notificationBuilder.color = ContextCompat.getColor(baseContext, R.color.colorCoffee)


        //initiate notification sending
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        try {

            fun fireNotification() {
                notificationManager.notify(getNewNotificationId(), notificationBuilder.build())
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val notificationChannel = NotificationChannel(channelId, title, NotificationManager.IMPORTANCE_DEFAULT)
                notificationManager.createNotificationChannel(notificationChannel)
            }

            val textStyle = NotificationCompat.BigTextStyle().bigText(body)
            textStyle.setBigContentTitle(title)
            notificationBuilder.setStyle(textStyle)

            fireNotification()
        } catch (e: Exception) {
            Log.e("NotifyingException", "" + e)
        }

    }

}

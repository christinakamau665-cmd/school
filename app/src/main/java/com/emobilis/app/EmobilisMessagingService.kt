package com.emobilis.app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * EmobilisMessagingService — handles all FCM push notifications.
 *
 * Notification channels:
 *  - emobilis_alerts  : Lab alerts (HIGH priority — red badge)
 *  - emobilis_fees    : Fees reminders (DEFAULT priority)
 *  - emobilis_general : Class notices & messages (DEFAULT priority)
 */
class EmobilisMessagingService : FirebaseMessagingService() {

    companion object {
        const val CHANNEL_ALERTS  = "emobilis_alerts"
        const val CHANNEL_FEES    = "emobilis_fees"
        const val CHANNEL_GENERAL = "emobilis_general"
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val type    = remoteMessage.data["type"] ?: "general"
        val title   = remoteMessage.notification?.title ?: remoteMessage.data["title"] ?: "Emobilis"
        val body    = remoteMessage.notification?.body  ?: remoteMessage.data["body"]  ?: ""
        val channel = when (type) {
            "lab_alert"     -> CHANNEL_ALERTS
            "fees_reminder" -> CHANNEL_FEES
            else            -> CHANNEL_GENERAL
        }
        showNotification(title, body, channel)
    }

    override fun onNewToken(token: String) {
        // TODO: upload token to Firestore so server can send targeted notifications
        // db.collection("fcm_tokens").document(uid).set(mapOf("token" to token))
    }

    private fun showNotification(title: String, body: String, channelId: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(
                if (channelId == CHANNEL_ALERTS) NotificationCompat.PRIORITY_HIGH
                else NotificationCompat.PRIORITY_DEFAULT
            )
            .build()

        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(System.currentTimeMillis().toInt(), notification)
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        listOf(
            NotificationChannel(CHANNEL_ALERTS,  "Lab Alerts",      NotificationManager.IMPORTANCE_HIGH).apply { description = "Computer issue alerts for lab technicians" },
            NotificationChannel(CHANNEL_FEES,    "Fees Reminders",  NotificationManager.IMPORTANCE_DEFAULT).apply { description = "Fee installment reminders" },
            NotificationChannel(CHANNEL_GENERAL, "General Notices", NotificationManager.IMPORTANCE_DEFAULT).apply { description = "Class notices and school messages" }
        ).forEach { manager.createNotificationChannel(it) }
    }
}

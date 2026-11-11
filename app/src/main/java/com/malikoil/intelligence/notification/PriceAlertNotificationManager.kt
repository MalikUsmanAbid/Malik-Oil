package com.malikoil.intelligence.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.malikoil.intelligence.MainActivity
import com.malikoil.intelligence.R
import com.malikoil.intelligence.model.PriceCondition
import com.malikoil.intelligence.model.RateObservation
import com.malikoil.intelligence.model.TargetPriceAlert

object PriceAlertNotificationManager {
    const val CHANNEL_ID = "malik_target_price_alerts"
    const val CHANNEL_NAME = "Target Price Alerts (ہدف ریٹ الرٹس)"
    const val CHANNEL_DESC = "Push notifications when Sarson or Canola hits your target purchase price"

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
                enableLights(true)
                enableVibration(true)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.createNotificationChannel(channel)
        }
    }

    fun sendTargetPriceNotification(
        context: Context,
        alert: TargetPriceAlert,
        currentRate: RateObservation
    ) {
        ensureChannel(context)

        // Android 13+ (API 33) runtime notification check
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!hasPermission) {
                return
            }
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("open_tab", "alerts")
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            alert.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val commLabel = if (alert.commodity.equals("canola", ignoreCase = true)) "Canola (کینولا)" else "Sarson (سرسوں)"
        val condText = if (alert.condition == PriceCondition.AT_OR_BELOW) {
            "Target Purchase Opportunity (خرید کا سنہری موقع)"
        } else {
            "Target Selling Price Reached (فروخت کا ریٹ)"
        }

        val title = "🎯 $condText: $commLabel"
        val body = "Market rate in %s reached PKR %.2f/kg (PKR %.0f/40kg Maund)! Your target: PKR %.2f/kg.".format(
            currentRate.market,
            currentRate.pricePkrKg,
            currentRate.pricePkrMaund,
            alert.targetPricePkrKg
        )

        val bigText = "%s\n\n🌾 Commodity: %s\n📍 Mandi: %s\n💰 Current Rate: PKR %.2f/kg (PKR %.0f/40kg)\n🎯 Target Threshold: PKR %.2f/kg (PKR %.0f/40kg)\n\nTap to inspect mandi intelligence and place bids.".format(
            body,
            commLabel,
            currentRate.market,
            currentRate.pricePkrKg,
            currentRate.pricePkrMaund,
            alert.targetPricePkrKg,
            alert.targetPricePkrMaund
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.brand_icon)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(bigText))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat.from(context).notify(alert.id.hashCode(), notification)
    }
}

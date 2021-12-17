package com.draco.smdesktopmode

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationManager.IMPORTANCE_HIGH
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.renderscript.RenderScript
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.PRIORITY_HIGH
import android.util.Log
import com.draco.smdesktopmode.R.mipmap.ic_launcher
import android.R.attr.name
import android.app.NotificationChannel
import android.app.NotificationManager







class BootReceiver : BroadcastReceiver() {

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    override fun onReceive(context: Context, intent: Intent) {
        sharedPrefs = context.getSharedPreferences("SMDesktopMode", Context.MODE_PRIVATE)
        editor = sharedPrefs.edit()

        val serviceIntent = Intent(context, DisplayConnectionService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }

        Log.d("STARTUP", "Startup intent received")

    }
}

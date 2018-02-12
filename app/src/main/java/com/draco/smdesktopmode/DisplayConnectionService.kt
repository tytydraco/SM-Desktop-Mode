package com.draco.smdesktopmode

/* Copyright 2015 Braden Farmer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.display.DisplayManager
import android.os.IBinder
import android.util.Log


class DisplayConnectionService : Service() {

    private var listener: DisplayManager.DisplayListener = object : DisplayManager.DisplayListener {
        override fun onDisplayAdded(displayId: Int) {
            sharedPrefs = getSharedPreferences("SMDesktopMode", Context.MODE_PRIVATE)
            editor = sharedPrefs.edit()
            Log.d("SERVICE_STARTED", "Display connected")
            setBrightnessMode = 0
            setBrightness = 0
            setRotationLock = 0
            setRotation = 1
            applyResolution(applicationContext, desktopDensity)
        }

        override fun onDisplayChanged(displayId: Int) {}

        override fun onDisplayRemoved(displayId: Int) {
            sharedPrefs = getSharedPreferences("SMDesktopMode", Context.MODE_PRIVATE)
            editor = sharedPrefs.edit()
            Log.d("SERVICE_STARTED", "Display removed")
            val d = sharedPrefs.getInt("defDensity", 480)
            setRotationLock = sharedPrefs.getInt("defRotationLock", 1)
            setRotation = 0
            setBrightnessMode = sharedPrefs.getInt("defBrightnessMode", 0)
            setBrightness = 128
            applyResolution(applicationContext, d.toString())
        }
    }

    override fun onCreate() {
        try {
            val manager = getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
            manager.registerDisplayListener(listener, null)
        } catch (e: Exception) {
            Log.d("SomethingBad", e.toString())
        }
        Log.d("SERVICE_STARTED", "Display service started")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return Service.START_STICKY
    }

    override fun onDestroy() {
        try {
            val manager = getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
            manager.unregisterDisplayListener(listener)
        } catch (e: Exception) {}
    }

    override fun onBind(arg0: Intent): IBinder? {
        return null
    }
}

package com.draco.smdesktopmode

import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_HIGH
import android.content.ComponentName
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.hardware.display.DisplayManager
import android.os.Bundle
import androidx.core.app.NotificationCompat
import android.view.Display
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

lateinit var sharedPrefs: SharedPreferences
lateinit var editor: SharedPreferences.Editor
lateinit var disableAppIcon: Button
lateinit var manualEnable: Button
lateinit var manualDisable: Button

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sharedPrefs = getSharedPreferences("SMDesktopMode", Context.MODE_PRIVATE)
        editor = sharedPrefs.edit()

        disableAppIcon = findViewById(R.id.disable_app_icon)
        manualEnable = findViewById(R.id.manual_enable)
        manualDisable = findViewById(R.id.manual_disable)
        disableAppIcon.setOnClickListener {
            val p = packageManager
            val componentName = ComponentName(this, MainActivity::class.java)
            p.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP)
        }
        manualEnable.setOnClickListener {
            setBrightnessMode = 0
            setBrightness = 0
            setRotationLock = 0
            setRotation = 1
            applyResolution(applicationContext, desktopDensity)
        }
        manualDisable.setOnClickListener {
            val d = sharedPrefs.getInt("defDensity", 480)
            setRotationLock = sharedPrefs.getInt("defRotationLock", 1)
            setRotation = 0
            setBrightnessMode = sharedPrefs.getInt("defBrightnessMode", 0)
            setBrightness = 128
            applyResolution(applicationContext, d.toString())
        }

        permissionCheck(this)
    }
}

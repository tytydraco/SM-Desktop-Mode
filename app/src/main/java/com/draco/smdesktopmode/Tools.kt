package com.draco.smdesktopmode

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.core.content.ContextCompat
import android.view.Display
import android.widget.Toast
import android.content.IntentFilter
import androidx.appcompat.app.AlertDialog


const val sizeName = "display_size_forced"
const val densityName = "display_density_forced"
const val rotationLockName = "accelerometer_rotation" // 0 off 1 on
const val brightnessModeName = "screen_brightness_mode" // 0 off 1 on
const val desktopDensity = "200" // 0 off 1 on

// defaults
var setRotationLock: Int = 1
var setRotation: Int = 0
var setBrightnessMode: Int = 0
var setBrightness: Int = 128

fun permissionCheck(context: Context): Boolean {
    val permissionCheck = ContextCompat.checkSelfPermission(context,
            Manifest.permission.WRITE_SECURE_SETTINGS)
    if (permissionCheck == PackageManager.PERMISSION_DENIED) {
        val error = AlertDialog.Builder(context)
        error.setTitle("Missing Permissions")
        error.setMessage("To allow this app to work, you must run an ADB command via your computer.\n\nadb shell pm grant " + context.packageName + " android.permission.WRITE_SECURE_SETTINGS")
        error.setPositiveButton("Ok") { _, _ -> permissionCheck(context) }
        error.setNegativeButton("Close") { _, _ -> System.exit(0) }
        error.setCancelable(false)
        error.show()
    } else {
        afterPermissionCheck(context)
        return true
    }
    return false
}

fun afterPermissionCheck(context: Context) {
    val firstLaunch = sharedPrefs.getBoolean("firstLaunch", true)
    if (firstLaunch) {
        defaultResolution(context)
    }
    resetDesktopSettings()
    val serviceIntent = Intent(context, DisplayConnectionService::class.java)
    context.startService(serviceIntent)
}

fun defaultResolution(context: Context) {

    val firstLaunch = sharedPrefs.getBoolean("firstLaunch", true)
    if (firstLaunch) {
        val sizeSetting = Settings.Global.getString(context.contentResolver, sizeName)
        val densitySetting = Settings.Secure.getString(context.contentResolver, densityName)
        val rotationLock = Settings.System.getInt(context.contentResolver, rotationLockName)
        val brightnessMode = Settings.System.getInt(context.contentResolver, brightnessModeName)
        if (sizeSetting == null || densitySetting == null) {
            editor.putInt("defWidth", 1080)
            editor.putInt("defHeight", 1920)
            editor.putInt("defDensity", 480)
            Toast.makeText(context, "Assuming standard 1080x1920.", Toast.LENGTH_SHORT).show()
        } else if (!sizeSetting.contains(",")) {
            editor.putInt("defWidth", 1080)
            editor.putInt("defHeight", 1920)
            editor.putInt("defDensity", 480)
            editor.apply()
            Toast.makeText(context, "Assuming standard 1080x1920.", Toast.LENGTH_SHORT).show()
        } else {
            val settingArr = sizeSetting.split(",")
            editor.putInt("defWidth", settingArr[0].toInt())
            editor.putInt("defHeight", settingArr[1].toInt())
            editor.putInt("defDensity", densitySetting.toInt())
        }
        editor.putBoolean("firstLaunch", false)
        editor.putInt("defRotationLock", rotationLock)
        editor.putInt("defBrightnessMode", brightnessMode)
        editor.apply()
    } else {
        setRotationLock = sharedPrefs.getInt("defRotationLock", 1)
        setRotation = 0
        setBrightnessMode = sharedPrefs.getInt("defBrightnessMode", 0)
        setBrightness = 128
        val densityInt = sharedPrefs.getInt("defDensity", 480)
        applyResolution(context, densityInt.toString())
    }
}

fun resetDesktopSettings() {
    setBrightnessMode = 0
    setBrightness = 0
    setRotationLock = 0
    setRotation = 1
}

fun applyResolution(context: Context, d: String) {
    editor.putString("setDensity", d)
    editor.putInt("setBrightnessMode", setBrightnessMode)
    editor.putInt("setBrightness", setBrightness)
    editor.putInt("setRotationLock", setRotationLock)
    editor.putInt("setRotation", setRotation)
    editor.apply()

    Settings.System.putInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE, setBrightnessMode)
    Settings.System.putInt(context.contentResolver, Settings.System.SCREEN_BRIGHTNESS, setBrightness)
    Settings.System.putInt(context.contentResolver, Settings.System.ACCELEROMETER_ROTATION, setRotationLock)
    Settings.System.putInt(context.contentResolver, Settings.System.USER_ROTATION, setRotation)

    if (Build.VERSION.SDK_INT > Build.VERSION_CODES.N) {
        wmDensityNew(d)
    } else {
        wmDensity(d)
    }
}

@SuppressLint("PrivateApi")
@Throws(Exception::class)
fun getWindowManagerService(): Any {
    return Class.forName("android.view.WindowManagerGlobal")
            .getMethod("getWindowManagerService")
            .invoke(null)
}

@SuppressLint("PrivateApi")
@Throws(Exception::class)
fun wmDensity(commandArg: String) {
    if (commandArg == "reset") {
        Class.forName("android.view.IWindowManager")
                .getMethod("clearForcedDisplayDensity", Int::class.javaPrimitiveType)
                .invoke(getWindowManagerService(), Display.DEFAULT_DISPLAY)
    } else {
        val density = Integer.parseInt(commandArg)

        Class.forName("android.view.IWindowManager")
                .getMethod("setForcedDisplayDensity", Int::class.javaPrimitiveType, Int::class.javaPrimitiveType)
                .invoke(getWindowManagerService(), Display.DEFAULT_DISPLAY, density)
    }
}

@SuppressLint("PrivateApi")
@Throws(Exception::class)
private fun wmDensityNew(commandArg: String) {
    // From android.os.UserHandle
    val USER_CURRENT_OR_SELF = -3

    if (commandArg == "reset") {
        Class.forName("android.view.IWindowManager")
                .getMethod("clearForcedDisplayDensityForUser", Int::class.javaPrimitiveType, Int::class.javaPrimitiveType)
                .invoke(getWindowManagerService(), Display.DEFAULT_DISPLAY, USER_CURRENT_OR_SELF)
    } else {
        val density = Integer.parseInt(commandArg)

        Class.forName("android.view.IWindowManager")
                .getMethod("setForcedDisplayDensityForUser", Int::class.javaPrimitiveType, Int::class.javaPrimitiveType, Int::class.javaPrimitiveType)
                .invoke(getWindowManagerService(), Display.DEFAULT_DISPLAY, density, USER_CURRENT_OR_SELF)
    }
}

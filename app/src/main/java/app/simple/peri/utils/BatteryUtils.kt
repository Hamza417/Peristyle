package app.simple.peri.utils

import android.content.Context
import android.os.BatteryManager

object BatteryUtils {

    private const val LOW_BATTERY_THRESHOLD = 20

    fun Context.getBatteryPercentage(): Int {
        val batteryManager = getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        return batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
    }

    fun Context.isLowBattery(): Boolean {
        return getBatteryPercentage() <= LOW_BATTERY_THRESHOLD
    }
}
package com.redbox.iot.app.android_wifi_flutter

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager

class WiFiStateReceiver : BroadcastReceiver() {
  private var wiFiStateListener: WiFiStateListener? = null

  interface WiFiStateListener {
    fun onWiFiStateChanged(isWifiEnabled: Boolean)
  }

  fun setWiFiStateListener(listener: WiFiStateListener?) {
    this.wiFiStateListener = listener
  }

  override fun onReceive(context: Context, intent: Intent) {
    if (WifiManager.WIFI_STATE_CHANGED_ACTION == intent.action) {
      val wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN)
      val isWifiEnabled = (wifiState == WifiManager.WIFI_STATE_ENABLED)
      wiFiStateListener?.onWiFiStateChanged(isWifiEnabled)
    }
  }
}
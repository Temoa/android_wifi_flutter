package com.redbox.iot.app.android_wifi_flutter

import android.content.Context
import android.content.IntentFilter
import android.net.wifi.WifiManager
import io.flutter.plugin.common.EventChannel

class WiFiEnabledStreamHandler(
  private val context: Context,
  val onChange: ((eventSink: EventChannel.EventSink, enable: Boolean) -> Unit)
) : BaseStreamHandler() {

  private var wiFiStateReceiver: WiFiStateReceiver? = null

  override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
    super.onListen(arguments, events)
    wiFiStateReceiver = WiFiStateReceiver()
    wiFiStateReceiver!!.setWiFiStateListener(object : WiFiStateReceiver.WiFiStateListener {
      override fun onWiFiStateChanged(isWifiEnabled: Boolean) {
        val eventSink = eventSink ?: return
        onChange.invoke(eventSink, isWifiEnabled)
      }
    })
    val intentFilter = IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION)
    context.registerReceiver(wiFiStateReceiver, intentFilter)
  }

  override fun onCancel(arguments: Any?) {
    super.onCancel(arguments)
    context.unregisterReceiver(wiFiStateReceiver)
  }
}
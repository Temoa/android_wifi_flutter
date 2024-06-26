package com.redbox.iot.app.android_wifi_flutter

import io.flutter.plugin.common.EventChannel

open class BaseStreamHandler : EventChannel.StreamHandler {

  var eventSink: EventChannel.EventSink? = null

  override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
    eventSink = events
  }

  override fun onCancel(arguments: Any?) {
    eventSink = null
  }
}
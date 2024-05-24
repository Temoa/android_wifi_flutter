@file:Suppress("DEPRECATION")

package com.redbox.iot.app.android_wifi_flutter

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result


/** AndroidWifiFlutterPlugin */
class AndroidWifiFlutterPlugin : FlutterPlugin, MethodCallHandler {
  private lateinit var channel: MethodChannel

  private lateinit var context: Context

  private lateinit var wifiManager: WifiManager
  private lateinit var connectivityManager: ConnectivityManager

  override fun onAttachedToEngine(flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "com.redbox.iot.app/android_wifi_flutter")
    channel.setMethodCallHandler(this)

    context = flutterPluginBinding.applicationContext;

    wifiManager = (flutterPluginBinding.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager)
    connectivityManager = (flutterPluginBinding.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager)
  }

  override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
  }

  @Suppress("DEPRECATION")
  override fun onMethodCall(call: MethodCall, result: Result) {
    when (call.method) {
      "getWifiInfo" -> result.success(getWifiInfo())
      "enableWifi" -> result.success(wifiManager.setWifiEnabled(true))
      "disableWifi" -> result.success(wifiManager.setWifiEnabled(false))
      "isWifiEnabled" -> result.success(wifiManager.isWifiEnabled)
      "connectWiFi" -> {
        val ssid = call.argument<String>("ssid")!!
        val password = call.argument<String>("password")!!
        connectWiFi(ssid, password, result)
      }

      "connectWiFi2" -> {
        val networkId = call.argument<String>("networkId")!!
        connectWiFi2(networkId, result)
      }

      "disconnectWiFi" -> {
        val ssid = call.argument<String>("ssid")!!
        disconnectWiFi(ssid, result)
      }

      "getConfiguredWiFis" -> result.success(getConfiguredWiFis())

      else -> {
        result.notImplemented()
      }
    }
  }

  private fun getWifiInfo(): Map<String, String?> {
    val wifiInfo = wifiManager.connectionInfo
    val map: MutableMap<String, String?> = HashMap()
    map["ssid"] = wifiInfo.ssid
    map["mac"] = wifiInfo.bssid
    map["ip"] = wifiInfo.ipAddress.toString()
    map["link_speed"] = wifiInfo.linkSpeed.toString()
    map["network_id"] = wifiInfo.networkId.toString()
    return map
  }

  @SuppressLint("MissingPermission")
  private fun getConfiguredWiFis(): List<Map<String, String?>> {
    val configured = wifiManager.configuredNetworks
    val list = mutableListOf<Map<String, String?>>()
    for (config in configured) {
      val map: MutableMap<String, String?> = HashMap()
      map["networkId"] = config.networkId.toString()
      map["ssid"] = config.SSID.replace("\"", "")
      list.add(map)
    }
    return list
  }

  @SuppressLint("MissingPermission")
  fun disconnectWiFi(ssid: String, result: Result) {
    val config = wifiManager.configuredNetworks.firstOrNull { it.SSID.replace("\"", "") == ssid }
    println(config)
    if (config == null) {
      result.success(false)
    } else {
      wifiManager.disconnect()
      wifiManager.disableNetwork(config.networkId)
      wifiManager.reconnect()
      result.success(true)
    }
  }

  @SuppressLint("MissingPermission")
  fun connectWiFi(ssid: String, password: String, result: Result) {
    val scanResults = wifiManager.scanResults
    scanResults.size
    val scanResult = wifiManager.scanResults.firstOrNull { it.SSID == ssid }
    if (scanResult == null) {
      result.success(false)
      return
    } else {
      // 如果找到了wifi了，从配置表中搜索该wifi的配置config，也就是以前有没有连接过
      // 注意configuredNetworks中的ssid，系统源码中加上了双引号，这里比对的时候要去掉
      wifiManager.disconnect()
      val config = wifiManager.configuredNetworks.firstOrNull { it.SSID.replace("\"", "") == ssid }
      if (config != null) {
        // 如果找到了，那么直接连接，不要调用wifiManager.addNetwork  这个方法会更改config的！
        wifiManager.enableNetwork(config.networkId, true)
      } else {
        // 没找到的话，就创建一个新的配置，然后正常的addNetWork、enableNetwork即可
        val padWifiNetwork = createWifiConfig(scanResult.SSID, password, getCipherType(scanResult.capabilities))
        val netId = wifiManager.addNetwork(padWifiNetwork)
        wifiManager.enableNetwork(netId, true)
      }
      result.success(wifiManager.reconnect())
    }
  }

  @SuppressLint("MissingPermission")
  fun connectWiFi2(networkId: String, result: Result) {
    val config = wifiManager.configuredNetworks.firstOrNull { it.networkId.toString() == networkId }
    if (config != null) {
      wifiManager.disconnect()
      wifiManager.enableNetwork(config.networkId, true)
      result.success(wifiManager.reconnect())
    } else {
      result.success(false)
    }
  }

  @SuppressLint("MissingPermission")
  private fun createWifiConfig(ssid: String, password: String, type: WifiCapability): WifiConfiguration {
    // 初始化WifiConfiguration
    val config = WifiConfiguration()
    config.allowedAuthAlgorithms.clear()
    config.allowedGroupCiphers.clear()
    config.allowedKeyManagement.clear()
    config.allowedPairwiseCiphers.clear()
    config.allowedProtocols.clear()

    // 指定对应的SSID
    config.SSID = "\"" + ssid + "\""

    // 如果之前有类似的配置
    val tempConfig = wifiManager.configuredNetworks.firstOrNull { it.SSID == "\"$ssid\"" }
    if (tempConfig != null) {
      // 则清除旧有配置  不是自己创建的 network 这里其实是删不掉的
      wifiManager.removeNetwork(tempConfig.networkId)
      wifiManager.saveConfiguration()
    }

    // 不需要密码的场景
    when (type) {
      WifiCapability.WIFI_CIPHER_NO_PASS -> {
        config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE)
        // 以WEP加密的场景
      }

      WifiCapability.WIFI_CIPHER_WEP -> {
        config.hiddenSSID = true
        config.wepKeys[0] = "\"" + password + "\""
        config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN)
        config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED)
        config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE)
        config.wepTxKeyIndex = 0
        // 以WPA加密的场景，自己测试时，发现热点以WPA2建立时，同样可以用这种配置连接
      }

      WifiCapability.WIFI_CIPHER_WPA -> {
        config.preSharedKey = "\"" + password + "\""
        config.hiddenSSID = true
        config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN)
        config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP)
        config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK)
        config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP)
        config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP)
        config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP)
        config.status = WifiConfiguration.Status.ENABLED
      }
    }
    return config
  }

  private fun getCipherType(capabilities: String): WifiCapability {
    return when {
      capabilities.contains("WEB") -> {
        WifiCapability.WIFI_CIPHER_WEP
      }

      capabilities.contains("PSK") -> {
        WifiCapability.WIFI_CIPHER_WPA
      }

      capabilities.contains("WPS") -> {
        WifiCapability.WIFI_CIPHER_NO_PASS
      }

      else -> {
        WifiCapability.WIFI_CIPHER_NO_PASS
      }
    }
  }
}

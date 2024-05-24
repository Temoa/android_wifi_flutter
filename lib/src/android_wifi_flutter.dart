part of '../android_wifi_flutter.dart';

class AndroidWifiFlutter {
  AndroidWifiFlutter._();

  static final AndroidWifiFlutter _instance = AndroidWifiFlutter._();

  factory AndroidWifiFlutter() => instance;

  static AndroidWifiFlutter get instance => _instance;

  static const pluginMethodChannelName = 'com.redbox.iot.app/android_wifi_flutter';
  static const pluginMethodChannel = MethodChannel(pluginMethodChannelName);

  Future<ActiveWifiNetwork> getActiveWifiInfo() async {
    Map<dynamic, dynamic> result = await pluginMethodChannel.invokeMethod("getWifiInfo");
    ActiveWifiNetwork activeWifiNetwork = ActiveWifiNetwork.fromMap(result);
    return activeWifiNetwork;
  }

  Future<bool> enableWifi() async {
    return await pluginMethodChannel.invokeMethod("enableWifi");
  }

  Future<bool> disableWifi() async {
    return await pluginMethodChannel.invokeMethod("disableWifi");
  }

  Future<bool> isWifiEnabled() async {
    return await pluginMethodChannel.invokeMethod("isWifiEnabled");
  }

  Future<bool> connectWiFi(String ssid, String password) async {
    return await pluginMethodChannel.invokeMethod("connectWiFi", {"ssid": ssid, "password": password});
  }

  Future<bool> connectWiFi2(String networkId) async {
    return await pluginMethodChannel.invokeMethod("connectWiFi2", {"networkId": networkId});
  }

  Future<bool> disconnectWiFi(String ssid) async {
    return await pluginMethodChannel.invokeMethod("disconnectWiFi", {"ssid": ssid});
  }

  Future<List<ConfiguredWiFi>> getConfiguredWiFis() async {
    List<dynamic> result = await pluginMethodChannel.invokeMethod("getConfiguredWiFis");
    List<ConfiguredWiFi> list = result.map((e) => ConfiguredWiFi.fromMap(e)).toList();
    return list;
  }
}

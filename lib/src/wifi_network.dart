part of '../android_wifi_flutter.dart';

class ActiveWifiNetwork {
  String? ip;
  String? bssid;
  String? frequency;
  String? linkSpeed;
  String? networkId;
  String? ssid;

  ActiveWifiNetwork({this.ip, this.bssid, this.frequency, this.linkSpeed, this.networkId, this.ssid});

  factory ActiveWifiNetwork.fromMap(Map<dynamic, dynamic> map) {
    return ActiveWifiNetwork(ssid: map['ssid'], frequency: map['frequency'], ip: map['ip'], linkSpeed: map['link_speed'], bssid: map['mac'], networkId: map['network_id']);
  }
}

class ConfiguredWiFi {
  String? networkId;
  String? ssid;

  ConfiguredWiFi({this.networkId, this.ssid});

  factory ConfiguredWiFi.fromMap(Map<dynamic, dynamic> map) {
    return ConfiguredWiFi(networkId: map["networkId"], ssid: map["ssid"]);
  }
}

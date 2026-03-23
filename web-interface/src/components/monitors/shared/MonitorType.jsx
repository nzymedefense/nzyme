import React from "react";

export default function MonitorType({type}) {

  switch (type) {
    case "DOT11_BSSID":
      return "802.11/WiFi BSSID"
    default:
      return "Unknown"
  }

}
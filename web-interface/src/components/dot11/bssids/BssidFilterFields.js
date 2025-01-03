import {FILTER_TYPE} from "../../shared/filtering/Filters";

const transformMode = (mode) => {
  switch (mode) {
    case "Infrastructure":
      return "accesspoint";
    case "Ad-Hoc":
      return "adhoc"
    case "Unknown/Invalid":
      return "invalid"
  }
}

const transformSecurity = (security) => {
  switch (security) {
    case "WEP":
      return "WEP";
    case "WPA1":
      return "WPA1";
    case "WPA2-Personal":
      return "WPA2Personal";
    case "WPA2-Enterprise":
      return "WPA2Enterprise";
    case "WPA3-Transition":
      return "WPA3Transition";
    case "WPA3-Personal":
      return "WPA3Personal";
    case "WPA3-Enterprise":
      return "WPA3Enterprise";
    case "WPA3-Enterprise (CNSA/192)":
      return "WPA3EnterpriseCNSA";
    default:
      return security;
  }
}

export const BSSID_FILTER_FIELDS = {
  bssid: { title: "BSSID", type: FILTER_TYPE.MAC_ADDRESS },
  signal_strength: { title: "Signal Strength", type: FILTER_TYPE.NUMERIC },
  mode: { title: "Mode", type: FILTER_TYPE.STRING_NO_REGEX, value_transform: transformMode },
  advertised_ssid: { title: "Advertised SSID", type: FILTER_TYPE.STRING },
  client_count: { title: "Client Count", type: FILTER_TYPE.NUMERIC },
  security: { title: "Security", type: FILTER_TYPE.STRING_NO_REGEX, value_transform: transformSecurity }
}
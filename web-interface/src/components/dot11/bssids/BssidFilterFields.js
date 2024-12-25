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

export const BSSID_FILTER_FIELDS = {
  bssid: { title: "BSSID", type: FILTER_TYPE.MAC_ADDRESS },
  signal_strength: { title: "Signal Strength", type: FILTER_TYPE.NUMERIC },
  mode: { title: "Mode", type: FILTER_TYPE.STRING, value_transform: transformMode },
  advertised_ssid: { title: "Advertised SSID", type: FILTER_TYPE.STRING },
  client_count: { title: "Client Count", type: FILTER_TYPE.NUMERIC },
  security: { title: "Security", type: FILTER_TYPE.STRING }
}
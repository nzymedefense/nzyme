import {FILTER_TYPE} from "../../shared/filtering/Filters";

export const BSSID_FILTER_FIELDS = {
  bssid: { title: "BSSID", type: FILTER_TYPE.MAC_ADDRESS },
  signal_strength: { title: "Signal Strength", type: FILTER_TYPE.NUMERIC },
  mode: { title: "Mode", type: FILTER_TYPE.STRING },
  advertised_ssid: { title: "Advertised SSID", type: FILTER_TYPE.STRING },
  client_count: { title: "Client Count", type: FILTER_TYPE.NUMERIC },
  security: { title: "Security", type: FILTER_TYPE.STRING }
}